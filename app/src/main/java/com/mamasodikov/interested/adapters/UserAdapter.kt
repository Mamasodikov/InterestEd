package com.mamasodikov.interested.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mamasodikov.interested.databinding.UserItemBinding
import com.mamasodikov.interested.listeners.UserListener
import com.mamasodikov.interested.models.User
import java.util.*

class UserAdapter (var users:List<User>, var userListener:UserListener):  RecyclerView.Adapter <UserAdapter.ItemHolder>() {
    inner class ItemHolder(val bnd: UserItemBinding) : RecyclerView.ViewHolder(bnd.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {


        with(holder.bnd)
        {
            textName.setText(users[position].name)
            textEmail.setText(users[position].email)
            imageProfile.setImageBitmap(getUserImage(users[position].image))
            root.setOnClickListener{unused->
                userListener.onUserClick(users[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
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

}