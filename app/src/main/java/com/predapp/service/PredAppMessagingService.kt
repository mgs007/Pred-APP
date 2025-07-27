package com.predapp.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.predapp.PredApp.Companion.CHANNEL_ID_PAYMENTS
import com.predapp.PredApp.Companion.CHANNEL_ID_PREDICTIONS
import com.predapp.PredApp.Companion.CHANNEL_ID_REMINDERS
import com.predapp.R
import com.predapp.ui.main.MainActivity
import java.util.concurrent.atomic.AtomicInteger

class PredAppMessagingService : FirebaseMessagingService() {

    companion object {
        private val notificationIdGenerator = AtomicInteger(0)
        
        // Notification types
        const val TYPE_POTD = "potd"
        const val TYPE_PAYMENT = "payment"
        const val TYPE_SUBSCRIPTION = "subscription"
        const val TYPE_GENERAL = "general"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Check if message contains a data payload
        remoteMessage.data.let { data ->
            if (data.isNotEmpty()) {
                // Handle based on notification type
                val type = data["type"] ?: TYPE_GENERAL
                val title = data["title"] ?: getString(R.string.app_name)
                val message = data["message"] ?: ""
                
                // Send notification based on type
                when (type) {
                    TYPE_POTD -> sendNotification(title, message, CHANNEL_ID_PREDICTIONS)
                    TYPE_PAYMENT -> sendNotification(title, message, CHANNEL_ID_PAYMENTS)
                    TYPE_SUBSCRIPTION -> sendNotification(title, message, CHANNEL_ID_REMINDERS)
                    else -> sendNotification(title, message, CHANNEL_ID_PREDICTIONS)
                }
            }
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            sendNotification(it.title ?: getString(R.string.app_name), 
                           it.body ?: "", 
                           CHANNEL_ID_PREDICTIONS)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server for this user
        // This would typically be handled by a repository class
    }

    private fun sendNotification(title: String, messageBody: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            
        // For longer messages, use big text style
        if (messageBody.length > 50) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationIdGenerator.incrementAndGet(), notificationBuilder.build())
    }
}