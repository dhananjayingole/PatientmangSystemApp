package eu.tutorials.patientmanagementapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import eu.tutorials.patientmanagementapp.MainActivity
import eu.tutorials.patientmanagementapp.R

class AppFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID_GENERAL = "apollo_general"
        private const val CHANNEL_ID_EMERGENCY = "apollo_emergency"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save new FCM token to Firebase for current user
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("fcmToken")
            .setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Apollo Care"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val type = message.data["type"] ?: "general"

        createNotificationChannels()
        sendNotification(title, body, type)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General channel
            val general = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "Apollo Care Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Appointment and prescription notifications"
                enableVibration(true)
            }

            // Emergency channel — high priority
            val emergency = NotificationChannel(
                CHANNEL_ID_EMERGENCY,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical emergency SOS notifications"
                enableVibration(true)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }

            manager.createNotificationChannels(listOf(general, emergency))
        }
    }

    private fun sendNotification(title: String, body: String, type: String) {
        val isEmergency = type == "emergency"
        val channelId = if (isEmergency) CHANNEL_ID_EMERGENCY else CHANNEL_ID_GENERAL

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (isEmergency) NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_DEFAULT
            )

        if (isEmergency) {
            notificationBuilder
                .setColor(android.graphics.Color.RED)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}