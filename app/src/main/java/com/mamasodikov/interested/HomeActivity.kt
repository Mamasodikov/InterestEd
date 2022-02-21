package com.mamasodikov.interested

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mamasodikov.interested.adapters.RecentConversationsAdapter
import com.mamasodikov.interested.databinding.FragmentHomeActivityBinding
import com.mamasodikov.interested.listeners.ConversionListener
import com.mamasodikov.interested.models.Message
import com.mamasodikov.interested.models.User
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager
import java.util.*

class HomeActivity : BaseFragment(), ConversionListener {


    lateinit var bnd:FragmentHomeActivityBinding
    lateinit var preferenceManager: PreferenceManager
    lateinit var conversations: ArrayList <Message>
    lateinit var conversationAdapter:RecentConversationsAdapter
    lateinit var database:FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        bnd = FragmentHomeActivityBinding.inflate(inflater, container, false)
        preferenceManager = PreferenceManager(requireContext())

        setListener()
        loadUserDetails()
        getToken()
        init()
        listenConversations()

        return bnd.root


    }

    private fun setListener() {
        bnd.imageSignOut.setOnClickListener{press->
            signOut()
        }
        bnd.fabNewChat.setOnClickListener{press->

            findNavController().navigate(R.id.action_homeActivity_to_userActivity)

        }

    }

    fun init () {
        conversations = ArrayList<Message>()
        conversationAdapter = RecentConversationsAdapter(conversations, this)
        bnd.recView.adapter = conversationAdapter
        database = FirebaseFirestore.getInstance()
    }

    fun listenConversations (){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private fun loadUserDetails() {
        bnd.textName.setText(preferenceManager.getString(Constants.KEY_NAME))

        if(preferenceManager.getString(Constants.KEY_IMAGE)!=null) {
            val bytes: ByteArray =
                Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bnd.imageProfile.setImageBitmap(bitmap)
        }
    }

    fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun updateToken(newToken:String){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, newToken)
        val database:FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference:DocumentReference = database
            .collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)

        documentReference.update(Constants.KEY_FCM_TOKEN, newToken)
            .addOnFailureListener { unused -> showToast("Unable to update token") }

    }

    fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener (this::updateToken)
    }

    @SuppressLint("NotifyDataSetChanged")
    var eventListener:EventListener<QuerySnapshot> = EventListener { value, error ->
        if(error!=null){
            return@EventListener
        }
        if(value!=null){
            for (documentChange in value.getDocumentChanges()){
                if(documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId:String = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    val receiverId:String = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!

                    val message:Message = Message()
                    message.senderId = senderId
                    message.receiverId = receiverId

                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        message.conversationImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                        message.conversationName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME)!!
                        message.conversationId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    }
                    else if(documentChange.type == DocumentChange.Type.MODIFIED)
                    {
                        for(i in conversations){
                            val senderId:String = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                            val receiverId:String = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                            if(i.senderId.equals(senderId) && i.receiverId.equals(receiverId))
                            {
                                i.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                                i.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!

                                break
                            }
                        }
                    }

                    else{

                        message.conversationImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE)!!
                        message.conversationName = documentChange.document.getString(Constants.KEY_SENDER_NAME)!!
                        message.conversationId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    }

                    message.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                    message.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    conversations.add(message)

                }
            }
            conversations.sortWith(compareBy { it.dateObject })
            conversationAdapter.notifyDataSetChanged()
            bnd.recView.smoothScrollToPosition(0)
            bnd.recView.visibility = View.VISIBLE
            bnd.progressBar.visibility = View.GONE
        }
    }

    fun signOut(){
        showToast("Signing out...")

        val database:FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference:DocumentReference = database
            .collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        val updates = hashMapOf<String, Any>()
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())
        documentReference.update(updates)
            .addOnSuccessListener { unused->
                preferenceManager.clear()
                findNavController().navigate(R.id.action_homeActivity_to_signIn)
            }
            .addOnFailureListener{unused->
                showToast("Unable to sign out")
            }
    }

    override fun onConversionClicked(user: User) {
        val data:Bundle = Bundle()
        data.putSerializable(Constants.KEY_USER, user)
        findNavController().navigate(R.id.action_homeActivity_to_chatActivity, data)
    }

}