package com.luguanyu.wifip2ptest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.opengl.Visibility
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.DataInputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity(), WifiP2pManager.ActionListener, WifiP2pManager.GroupInfoListener, WifiP2pManager.ConnectionInfoListener, WifiP2pAdapter.OnItemClickListener {

    private val TAG = "MainActivity"

    private lateinit var wifiP2pManager : WifiP2pManager
    private lateinit var channel : WifiP2pManager.Channel
    private lateinit var wifiReceive : BroadcastReceiver

    private lateinit var intentFilter : IntentFilter
    private lateinit var peers : MutableList<WifiP2pDevice>
    private lateinit var wifiAdapter : WifiP2pAdapter
    private var isConnect = false

    private lateinit var info : WifiP2pInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        peers = arrayListOf()
        wifiAdapter = WifiP2pAdapter(peers, this)
        wifiP2p_list.layoutManager = LinearLayoutManager(this)
        wifiP2p_list.adapter = wifiAdapter


        wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(this, Looper.getMainLooper(), null)
        wifiReceive = WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this)

        intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

    }


    private fun discoveryPeer() {
        //開啟 Wifi 直連
        wifiP2pManager.discoverPeers(channel, this)

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(wifiReceive, intentFilter)
        discoveryPeer()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiReceive)
        wifiP2pManager.stopPeerDiscovery(channel, this)
    }

    private fun connectWifi(wifiDevice : WifiP2pDevice) {
        rela_progress.visibility = View.VISIBLE

        val wifiConfig = WifiP2pConfig()
        wifiConfig.deviceAddress = wifiDevice.deviceAddress
        wifiConfig.wps.setup = WpsInfo.PBC

        wifiP2pManager.connect(channel, wifiConfig, null)

    }

    private fun disConnectWifi() {
        wifiP2pManager.requestGroupInfo(channel, this)
    }

    fun disConnect() {
        toast("DisConnect")
        isConnect = false
        discoveryPeer()
    }

    fun connectSuccess() {
        toast("Connect Success")
        isConnect = true
        rela_progress.visibility = View.GONE
        wifiP2pManager.requestConnectionInfo(channel, this)
    }

    fun updateP2pStatus(wifiDevice: WifiP2pDevice) {
        txt_name.text = wifiDevice.deviceName
        txt_connect_status.text = getDeviceStatus(wifiDevice.status)
    }

    private fun getDeviceStatus(deviceStatus: Int): String {
        return when (deviceStatus) {
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }


    //-------------------------- WifiP2pManager.ActionListener ------------------//
    override fun onSuccess() {
        Log.d(TAG, "WifiP2p discover Success")
    }

    override fun onFailure(reason: Int) {
        Log.d(TAG, reason.toString())
    }
    //-------------------------- WifiP2pManager.ActionListener ------------------//


    //配對成功
    //------------------------- PeerListListener --------------------//
    val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshPeers = peerList.deviceList
        if (refreshPeers != peers) {
            peers.clear()
            peers.addAll(refreshPeers)

            wifiAdapter.notifyDataSetChanged()
        }

        if (peers.size == 0) {
            Log.i(TAG, "No device found!!")
        }
    }
    //------------------------- PeerListListener --------------------//

    override fun onGroupInfoAvailable(group: WifiP2pGroup) {
        wifiP2pManager.removeGroup(channel, null)
    }

    override fun onConnectionInfoAvailable(info: WifiP2pInfo) {
        this.info = info
        if (info.groupOwnerAddress?.hostAddress != null) {

            SendMessageIntentService.startAction(this, "Hello World", info.groupOwnerAddress.hostAddress)

            val weakReference: WeakReference<MainActivity> = WeakReference(this)
            val receiveServerAsyncTask = ReceiveServerAsyncTask(weakReference)
            receiveServerAsyncTask.execute()
        }
    }


    private class ReceiveServerAsyncTask(private val mainActivity: WeakReference<MainActivity>) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void): String? {
            var serverSocket: ServerSocket? = null
            var client: Socket? = null
            var inputstream: DataInputStream? = null

            try {
                serverSocket = ServerSocket()
                serverSocket.reuseAddress = true
                serverSocket.bind(InetSocketAddress(8888))
                client = serverSocket.accept()
                inputstream = DataInputStream(client.getInputStream())
                val str = inputstream.readUTF()
                serverSocket.close()
                return str
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } finally {
                if (inputstream != null) {
                    try {
                        inputstream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                if (client != null) {
                    try {
                        client.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

        override fun onPostExecute(result: String?) {
            if (result != null) {
                val activity = mainActivity.get()
                Toast.makeText(activity, "結果" + result, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(wifiP2pDevice: WifiP2pDevice, resID: Int) {
        when(resID) {
            R.id.btn_connect -> connectWifi(wifiP2pDevice)
            R.id.btn_dis_connect -> disConnectWifi()
            R.id.btn_send -> {
                if (info.groupOwnerAddress?.hostAddress != null) {
                    SendMessageIntentService.startAction(this, "Hello World", info.groupOwnerAddress.hostAddress)
                }
            }
        }
    }

}
