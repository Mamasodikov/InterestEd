package com.mamasodikov.interested

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.mamasodikov.interested.adapters.MessageAdapter
import com.mamasodikov.interested.databinding.FragmentChatActivityBinding
import com.mamasodikov.interested.models.Message
import com.mamasodikov.interested.models.User
import com.mamasodikov.interested.network.ApiClient
import com.mamasodikov.interested.network.ApiService
import com.mamasodikov.interested.utilities.Constants
import com.mamasodikov.interested.utilities.PreferenceManager
import org.jetbrains.annotations.NotNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : BaseFragment() {

    lateinit var bnd: FragmentChatActivityBinding
    lateinit var receiverUser: User
    lateinit var messages: ArrayList<Message>
    lateinit var chatAdapter: MessageAdapter
    lateinit var preferenceManager: PreferenceManager
    lateinit var database: FirebaseFirestore
    var conversionId:String? = null
    var isReceiverAvailable = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bnd = FragmentChatActivityBinding.inflate(inflater, container, false)

        database = FirebaseFirestore.getInstance()
        preferenceManager = PreferenceManager(requireContext())

        setListeners()
        loadReceiverDetails()
        listenMessages()
        listenAvailabilityOfReceiver()
        init()
        return bnd.root
    }

    fun sendMessage(){
        val message:HashMap<String, Any> = HashMap()
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)!!)
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id)
        message.put(Constants.KEY_MESSAGE, bnd.inputMessage.text.toString())
        message.put(Constants.KEY_TIMESTAMP, Date())
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)

        if(conversionId!=null) {
            updateConversion(bnd.inputMessage.text.toString())
        }
        else{
            val conversion:HashMap<String, Any> = HashMap()
            conversion.put(Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)!!)
            conversion.put(Constants.KEY_SENDER_NAME,
                preferenceManager.getString(Constants.KEY_NAME)!!)
            conversion.put(Constants.KEY_SENDER_IMAGE,
                preferenceManager.getString(Constants.KEY_IMAGE)!!)
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id)
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name)
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image)
            conversion.put(Constants.KEY_LAST_MESSAGE, bnd.inputMessage.text.toString())
            conversion.put(Constants.KEY_TIMESTAMP, Date())
            addConversion(conversion)
        }

        if(!isReceiverAvailable)
        {
            try {

                val tokens:JSONArray = JSONArray()
                tokens.put(receiverUser.token)

                val data:JSONObject = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE, bnd.inputMessage.text.toString())

                val body = JSONObject()
                body.put(Constants.REMOTE_MSG_DATA,data)
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens)
                sendNotification(body.toString())

            }catch (exception:Exception){
                showToast(exception.message.toString())
            }
        }

        bnd.inputMessage.setText(null)
    }

    fun listenAvailabilityOfReceiver(){

        database.collection(Constants.KEY_COLLECTION_USERS)
            .document(receiverUser.id)
            .addSnapshotListener { value, error ->


                if(error!=null){
                    return@addSnapshotListener
                }
                if(value!=null){
                    if(value.getLong(Constants.KEY_AVAILABILITY)!=null){
                        val availability = Objects.requireNonNull(
                           value.getLong(Constants.KEY_AVAILABILITY)!!).toInt()
                    isReceiverAvailable = availability ==1
                }
                    if(value.getString(Constants.KEY_FCM_TOKEN)!=null) {
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN)!!
                    }
                        receiverUser.image = value.getString(Constants.KEY_IMAGE)!!
                        chatAdapter.setReceiverProfileImage(getUserImage(receiverUser.image))
                        chatAdapter.notifyItemRangeChanged(0, messages.size)
                }

        if(isReceiverAvailable){
            bnd.textAvailability.visibility = View.VISIBLE
        }
        else{
            bnd.textAvailability.visibility = View.GONE
        }
    }

    }


    @SuppressLint("NotifyDataSetChanged")
    private val eventListener:EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error!= null){
            return@EventListener
        }
        if(value!= null){
            val count:Int = messages.size
            for(documentChanges in value.documentChanges){

                if(documentChanges.type == DocumentChange.Type.ADDED){
                    val message:Message = Message()
                    message.senderId= documentChanges.document.getString(Constants.KEY_SENDER_ID)!!
                    message.receiverId= documentChanges.document.getString(Constants.KEY_RECEIVER_ID)!!
                    message.message= documentChanges.document.getString(Constants.KEY_MESSAGE)!!
                    message.time= getReadableDateTime(documentChanges.document.getDate(Constants.KEY_TIMESTAMP)!!)
                    message.dateObject= documentChanges.document.getDate(Constants.KEY_TIMESTAMP)!!
                    messages.add(message)
                }

            }

            messages.sortWith(compareBy { it.dateObject })
            if(count==0)
            {
                chatAdapter.notifyDataSetChanged()
            }
            else{
                chatAdapter.notifyItemRangeInserted(messages.size, messages.size)
                bnd.recView.smoothScrollToPosition(messages.size-1)
            }
            bnd.recView.visibility = View.VISIBLE
        }
        bnd.progressBar.visibility = View.GONE
        if(conversionId==null){
            checkForConversion()
        }
    }

    fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun sendNotification(messageBody: String){

        ApiClient.getClient().create(ApiService::class.java)
            .sendMessage(Constants.getRemoteMessageHeaders(),messageBody)
            .enqueue(object : Callback<String>{
                override fun onResponse(@NotNull call: Call<String>,@NotNull response: Response<String>) {
                    if(response.isSuccessful){
                        try {
                            if(response.body()!=null){
                                val responseJson = JSONObject(response.body().toString())
                                val results:JSONArray = responseJson.getJSONArray("results")
                                if(responseJson.getInt("failure")==1){
                                    val error:JSONObject = results.get(0) as JSONObject
                                    showToast(error.getString("error"))
                                    return
                                }
                            }
                        }catch (e:JSONException){
                            e.printStackTrace()
                        }

//                        showToast("Notification sent")
                    }
                    else{
                        showToast("Error:" + response.code())
                    }
                }

                override fun onFailure(@NotNull call: Call<String>,@NotNull t: Throwable) {
                    showToast(t.message.toString())
                }
            })
    }

    private fun listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)

        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private fun setListeners() {
        bnd.imageBack.setOnClickListener{clicked->
            requireActivity().onBackPressed()
        }
        bnd.send.setOnClickListener{clicked->
            sendMessage()
        }
        bnd.imageInfo.setOnClickListener{clicked->
           //
        }
    }


    fun init(){
        messages = ArrayList()
        chatAdapter = MessageAdapter(messages, getUserImage(receiverUser.image),
        preferenceManager.getString(Constants.KEY_USER_ID)!!)
        bnd.recView.adapter = chatAdapter
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

    fun getReadableDateTime(date:Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    fun addConversion(conversion:HashMap<String,Any>){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { docRef->
                conversionId = docRef.id
            }
    }

    fun updateConversion (message:String){
            val documentReference: DocumentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .document(conversionId!!)
            documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, Date()
            )
    }

    fun checkForConversion(){
        if(messages.size!=0){
            checkForConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID)!!,
                receiverUser.id)
        }
            checkForConversionRemotely(
            receiverUser.id,
            preferenceManager.getString(Constants.KEY_USER_ID)!!)
    }

    fun checkForConversionRemotely(senderId:String, receiverId:String)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    var conversionOnCompleteListener: OnCompleteListener<QuerySnapshot> = OnCompleteListener { task->
        if(task.isSuccessful && task.result !=null && task.result!!.documents.size >0)
        {
            val documentSnapshot:DocumentSnapshot = task.result!!.documents.get(0)
            conversionId = documentSnapshot.id
        }
    }

    private fun loadReceiverDetails(){

        receiverUser = requireArguments().getSerializable(Constants.KEY_USER) as User
        bnd.textName.text = receiverUser.name
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }

}