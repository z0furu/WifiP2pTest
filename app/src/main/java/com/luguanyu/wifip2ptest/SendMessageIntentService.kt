package com.luguanyu.wifip2ptest

import android.app.IntentService
import android.content.Intent
import android.content.Context
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


class SendMessageIntentService : IntentService("SendMessageIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_TRAIN == action) {
                val train = intent.getStringExtra(EXTRA_PARAM)
                val ip = intent.getStringExtra(EXTRAS_GROUP_OWNER_ADDRESS)
                handleActionFoo(train, ip)
            }
        }
    }

    private fun handleActionFoo(train: String, ip : String) {
        val socket = Socket()
        var dataOutputStream : DataOutputStream? = null

        try {
            socket.bind(null)
            socket.connect(InetSocketAddress(ip, PORT), SOCKET_TIMEOUT)
            dataOutputStream = DataOutputStream(socket.getOutputStream())
            dataOutputStream.writeUTF(train)
        } catch (e : Exception) {
            e.printStackTrace()
        } finally {
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close()
                } catch (e : IOException) {
                    e.printStackTrace()
                }
            }

            if(socket.isConnected) {
                try {
                    socket.close()
                } catch (e : IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    companion object {

        private val SOCKET_TIMEOUT = 5000

        private var PORT = 8888

        private val ACTION_TRAIN = "com.luguanyu.wifip2ptest.action.train"

        private val EXTRA_PARAM = "com.luguanyu.wifip2ptest.extra.PARAM"

        private var EXTRAS_GROUP_OWNER_ADDRESS = "com.luguanyu.wifip2ptest.extra.address"

        fun startAction(context: Context, train: String, ip : String) {
            val intent = Intent(context, SendMessageIntentService::class.java)
            intent.action = ACTION_TRAIN
            intent.putExtra(EXTRA_PARAM, train)
            intent.putExtra(EXTRAS_GROUP_OWNER_ADDRESS, ip)
            context.startService(intent)
        }
    }
}
