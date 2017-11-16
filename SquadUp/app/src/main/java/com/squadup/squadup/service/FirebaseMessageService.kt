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
        val READY_REQUEST = "READY_REQUEST"
        val READY_RESPONSE = "READY_RESPONSE"
        val READY_DECISION = "READY_DECISION"
    }

    // Handle a data message or push notification sent by another app.
    override fun onMessageReceived(message: RemoteMessage?) {
        if (message != null) {
            if (message.data.isNotEmpty()) {
                Log.i("FirebaseMessageService", "Data: " + message.data)
                if (message.data["type"] == LOGIN) {
                    broadcastLoginMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!,
                            message.data["latitude"]!!.toDouble(), message.data["longitude"]!!.toDouble()
                    )
                } else if (message.data["type"] == LOCATION) {
                    broadcastLocationMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!,
                            message.data["latitude"]!!.toDouble(), message.data["longitude"]!!.toDouble()
                    )
                } else if (message.data["type"] == TEXT) {
                    broadcastTextMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!,
                            message.data["text"]!!
                    )
                } else if (message.data["type"] == READY_REQUEST) {
                    broadcastReadyRequestMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!
                    )
                } else if (message.data["type"] == READY_RESPONSE) {
                    broadcastReadyResponseMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!,
                            message.data["receiverID"]!!, message.data["response"]!!.toBoolean()
                    )
                } else if (message.data["type"] == READY_DECISION) {
                    broadcastReadyDecisionMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!,
                            message.data["decision"]!!.toBoolean()
                    )
                }
            }
            if (message.notification != null) {
                Log.i("FirebaseMessageService", "Notification: " + message.notification.toString())
            }
        }
    }

    // Broadcasts a text message so any activity listening for it can access the data.
    private fun broadcastTextMessage(senderID: String, senderName: String, text: String) {
        val intent = Intent(BaseActivity.TEXT_MESSAGE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("text", text)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLoginMessage(senderID: String, senderName: String, latitude: Double, longitude: Double) {
        val intent = Intent(BaseActivity.LOGIN_MESSAGE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLocationMessage(senderID: String, senderName: String, latitude: Double, longitude: Double) {
        val intent = Intent(BaseActivity.LOCATION_MESSAGE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastReadyRequestMessage(senderID: String, senderName: String) {
        val intent = Intent(BaseActivity.READY_REQUEST_MESSAGE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastReadyResponseMessage(senderID: String, senderName: String, receiverID: String, response: Boolean) {
        val intent = Intent(BaseActivity.READY_RESPONSE_MESSAGE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("receiverID", receiverID)
        intent.putExtra("response", response)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastReadyDecisionMessage(senderID: String, senderName: String, decision: Boolean) {
        val intent = Intent(BaseActivity.READY_DECISION_MESSAGE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("decision", decision)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}