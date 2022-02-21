package com.mamasodikov.interested.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mamasodikov.interested.databinding.ReceivedItemBinding
import com.mamasodikov.interested.databinding.SentItemBinding
import com.mamasodikov.interested.databinding.UserItemBinding
import com.mamasodikov.interested.models.Message

class MessageAdapter(val messages:List<Message>, var receiverProfileImage: Bitmap, val senderId:String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @JvmName("setReceiverProfileImage1")
    fun setReceiverProfileImage(bitmap:Bitmap){
        receiverProfileImage = bitmap
    }


    inner class SentMessageItemHolder(private val bnd:SentItemBinding):RecyclerView.ViewHolder(bnd.root)
    {
        fun setData(chatMessage:Message){

            bnd.message.text = chatMessage.message
            bnd.time.text = chatMessage.time

        }
    }
    inner class ReceivedMessageItemHolder(private val bnd:ReceivedItemBinding):RecyclerView.ViewHolder(bnd.root)
    {
        fun setData(chatMessage:Message, receiverProfileImage:Bitmap){
            bnd.message.text = chatMessage.message
            bnd.time.text = chatMessage.time

            bnd.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if(viewType == VIEW_TYPE_SENT){
            val binding = SentItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)
            SentMessageItemHolder(binding) }

        else{
            val binding = ReceivedItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)
            ReceivedMessageItemHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position)== VIEW_TYPE_SENT){
            (holder as SentMessageItemHolder).setData(messages[position])
        }
        else
            (holder as ReceivedMessageItemHolder).setData(messages[position], receiverProfileImage)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if( messages[position].senderId == senderId) {
            VIEW_TYPE_SENT
        } else
            VIEW_TYPE_RECEIVED
    }

    companion object {
        const val VIEW_TYPE_SENT: Int = 1
        const val VIEW_TYPE_RECEIVED: Int = 2
    }

}