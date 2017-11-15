package com.squadup.squadup.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squadup.squadup.activity.BaseActivity
import com.squadup.squadup.activity.MessagingTestActivity

/**
 * The messaging service for Firebase, responsible for handling messages sent by other
 * instances of the app.
 */
class FirebaseMessageService : FirebaseMessagingService() {

    // Handle a data message or push notification sent by another app.
    override fun onMessageReceived(message: RemoteMessage?) {
        if (message != null) {
            if (message.data.isNotEmpty()) {
                Log.i("FirebaseMessageService", "Data: " + message.data)
                if (message.data["text"] != null) {
                    broadcastTextMessage(message.data["text"]!!)
                }
            }
            if (message.notification != null) {
                Log.i("FirebaseMessageService", "Notification: " + message.notification.toString())
            }
        }
    }

    // Broadcasts a text message so any activity listening for it can access the data.
    private fun broadcastTextMessage(message: String) {
        val intent = Intent(BaseActivity.TEXT_MESSAGE)
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}