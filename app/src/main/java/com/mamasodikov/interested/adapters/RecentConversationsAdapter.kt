package com.mamasodikov.interested.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mamasodikov.interested.databinding.RecentUserItemBinding
import com.mamasodikov.interested.databinding.UserItemBinding
import com.mamasodikov.interested.listeners.ConversionListener
import com.mamasodikov.interested.models.Message
import com.mamasodikov.interested.models.User
import java.util.*

class RecentConversationsAdapter(val chatMessages:List<Message>, val conversionListener: ConversionListener):RecyclerView.Adapter<RecentConversationsAdapter.ConversionItemHolder>() {

    inner class ConversionItemHolder(val bnd: RecentUserItemBinding) : RecyclerView.ViewHolder(bnd.root)
    {
        fun setData(message: Message){
            bnd.imageProfile.setImageBitmap(getUserImage(message.conversationImage))
            bnd.textName.text = message.conversationName
            bnd.textRecentMessage.text = message.message
            bnd.root.setOnClickListener{v->
               val user = User()
               user.id = message.conversationId
               user.name = message.conversationName
               user.image = message.conversationImage
               conversionListener.onConversionClicked(user)
            }
        }
    }


    fun getUserImage(encodedImage:String): Bitmap {

        val bytes:ByteArray

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bytes = Base64.getDecoder().decode(encodedImage)
            return BitmapFactory.decodeByteArray(bytes, 0 , bytes.size)

        } else {
            bytes = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0 , bytes.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionItemHolder {
        val binding = RecentUserItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ConversionItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversionItemHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }
}