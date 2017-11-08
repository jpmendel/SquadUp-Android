package com.squadup.squadup.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage?) {
        if (message != null) {
            if (message.data.isNotEmpty()) {
                Log.i("FirebaseMessageService", "Data: " + message.data)
            }

            if (message.notification != null) {
                Log.i("FirebaseMessageService", "Notification: " + message.notification.toString())
            }
        }
    }

}