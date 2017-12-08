package com.luguanyu.wifip2ptest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log


class WiFiDirectBroadcastReceiver(
        private val mManager : WifiP2pManager,
        private val channel : WifiP2pManager.Channel ,
        private val activity : MainActivity) : BroadcastReceiver() {

    val TAG : String = "WiFiDirectBroadcast"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when(action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d(TAG, "// Wifi P2P is enabled")
                } else{
                    Log.d(TAG, "// Wifi P2P not enabled")
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                   mManager.requestPeers(channel, activity.peerListListener)
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                Log.d(TAG, "connect status = " + networkInfo.isConnected.toString())
                if (!networkInfo.isConnected) {
                    activity.disConnect()
                } else {
                    activity.connectSuccess()
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION ->{

                activity.updateP2pStatus(intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE))
            }

        }
    }
}
