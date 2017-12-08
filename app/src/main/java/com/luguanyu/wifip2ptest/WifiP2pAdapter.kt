package com.luguanyu.wifip2ptest

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_wifi_p2p.view.*


class WifiP2pAdapter(
        private val wifiP2pDeviceList: MutableList<WifiP2pDevice>,
        itemClickListener : OnItemClickListener) : RecyclerView.Adapter<WifiP2pAdapter.MyViewHolder>() {

    init {
        onItemClickListener = itemClickListener
    }

    companion object {
        var onItemClickListener : OnItemClickListener? = null
    }

    interface OnItemClickListener {
        fun onItemClick(wifiP2pDevice: WifiP2pDevice, resID: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wifi_p2p, parent, false)
        val viewHolder = MyViewHolder(view)
        view.btn_send.setOnClickListener {
            onItemClickListener?.onItemClick(wifiP2pDeviceList[viewHolder.adapterPosition], view.btn_send.id)
        }

        view.btn_dis_connect.setOnClickListener {
            onItemClickListener?.onItemClick(wifiP2pDeviceList[viewHolder.adapterPosition], view.btn_dis_connect.id)
        }

        view.btn_connect.setOnClickListener {
            onItemClickListener?.onItemClick(wifiP2pDeviceList[viewHolder.adapterPosition], view.btn_connect.id)
        }

        return viewHolder
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.txtMacAddress.text = wifiP2pDeviceList[position].deviceAddress
        holder.txtName.text = wifiP2pDeviceList[position].deviceName
        if (wifiP2pDeviceList[position].status == WifiP2pDevice.CONNECTED) {
            holder.btnSend.visibility = View.VISIBLE
        } else{
            holder.btnSend.visibility = View.GONE
        }


    }


    override fun getItemCount(): Int {
        return wifiP2pDeviceList.size
    }

    class MyViewHolder(view : View) : RecyclerView.ViewHolder(view){

        val txtName = view.txt_name
        val txtMacAddress = view.txt_mac_address
        val btnSend = view.btn_send
    }
}