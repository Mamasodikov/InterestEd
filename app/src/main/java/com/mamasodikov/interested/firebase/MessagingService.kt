package com.mamasodikov.interested.firebase

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mamasodikov.interested.MainActivity
import com.mamasodikov.interested.R
import com.mamasodikov.interested.models.User
import com.mamasodikov.interested.utilities.Constants

class MessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "onNewToken: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "onMessageReceived: ${remoteMessage.notification?.body}")
        val user:User = User()
        user.id = remoteMessage.data.get(Constants.KEY_USER_ID)!!
        user.name = remoteMessage.data.get(Constants.KEY_NAME)!!
        user.token = remoteMessage.data.get(Constants.KEY_FCM_TOKEN)!!

        //HardCode for some problems - Fix it
        user.image = "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"

        val data:Bundle = Bundle()
        data.putSerializable(Constants.KEY_USER, user)

        val notificationId = java.util.Random().nextInt()
        val channelId= "chat_message"

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.navigation)
            .setDestination(R.id.chatActivity)
            .setArguments(data)
            .createPendingIntent()

//then

        val notifBuilder = NotificationCompat.Builder (this, channelId)
        notifBuilder.setSmallIcon(R.drawable.ic_notification)
        notifBuilder.setContentInfo(user.name)
        notifBuilder.setContentText(remoteMessage.data.get(Constants.KEY_MESSAGE))
        notifBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(remoteMessage.data.get(Constants.KEY_MESSAGE)))
        notifBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notifBuilder.setContentIntent(pendingIntent)
        notifBuilder.setAutoCancel(true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName:CharSequence = "Chat Message"
            val channelDesription = "This notifications used for chat message notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDesription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, notifBuilder.build())
    }
}