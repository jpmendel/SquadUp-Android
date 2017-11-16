package com.squadup.squadup.service

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squadup.squadup.activity.BaseActivity

/**
 * The messaging service for Firebase, responsible for handling messages sent by other
 * instances of the app.
 */
class FirebaseMessageService : FirebaseMessagingService() {

    companion object {
        val LOGIN = "LOGIN"
        val LOCATION = "LOCATION"
        val TEXT = "TEXT"
    }

    // Handle a data message or push notification sent by another app.
    override fun onMessageReceived(message: RemoteMessage?) {
        if (message != null) {
            if (message.data.isNotEmpty()) {
                Log.i("FirebaseMessageService", "Data: " + message.data)
                if (message.data["type"] == LOGIN) {
                    broadcastLoginMessage(message.data["sender"]!!, message.data["latitude"]!!.toDouble(), message.data["longitude"]!!.toDouble())
                } else if (message.data["type"] == LOCATION) {
                    broadcastLocationMessage(message.data["sender"]!!, message.data["latitude"]!!.toDouble(), message.data["longitude"]!!.toDouble())
                } else if (message.data["type"] == TEXT) {
                    broadcastTextMessage(message.data["sender"]!!, message.data["text"]!!)
                }
            }
            if (message.notification != null) {
                Log.i("FirebaseMessageService", "Notification: " + message.notification.toString())
            }
        }
    }

    // Broadcasts a text message so any activity listening for it can access the data.
    private fun broadcastTextMessage(sender: String, text: String) {
        val intent = Intent(BaseActivity.TEXT_MESSAGE)
        intent.putExtra("sender", sender)
        intent.putExtra("text", text)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLoginMessage(sender: String, latitude: Double, longitude: Double) {
        val intent = Intent(BaseActivity.LOGIN_MESSAGE)
        intent.putExtra("sender", sender)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLocationMessage(sender: String, latitude: Double, longitude: Double) {
        val intent = Intent(BaseActivity.LOCATION_MESSAGE)
        intent.putExtra("sender", sender)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}