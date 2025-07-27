package com.predapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class PredApp : Application() {

    companion object {
        const val CHANNEL_ID_PREDICTIONS = "predictions_channel"
        const val CHANNEL_ID_PAYMENTS = "payments_channel"
        const val CHANNEL_ID_REMINDERS = "reminders_channel"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Configure Firestore settings for offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
        
        // Create notification channels for Android O and above
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Prediction of the Day channel
            val predictionChannel = NotificationChannel(
                CHANNEL_ID_PREDICTIONS,
                "Predictions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new predictions and Prediction of the Day"
            }
            
            // Payment notifications channel
            val paymentsChannel = NotificationChannel(
                CHANNEL_ID_PAYMENTS,
                "Payments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for payment confirmations and subscription status"
            }
            
            // Reminders channel
            val remindersChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reminders for subscription expiry and new features"
            }
            
            notificationManager.createNotificationChannels(listOf(
                predictionChannel,
                paymentsChannel,
                remindersChannel
            ))
        }
    }
}