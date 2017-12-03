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
        val READY_REQUEST = "READY_REQUEST"
        val READY_RESPONSE = "READY_RESPONSE"
        val READY_DECISION = "READY_DECISION"
        val ADDED_AS_FRIEND = "ADDED_AS_FRIEND"
        val REMOVED_AS_FRIEND = "REMOVED_AS_FRIEND"
        val ADDED_TO_GROUP = "ADDED_TO_GROUP"
        val REMOVED_FROM_GROUP = "REMOVED_FROM_GROUP"
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
                } else if (message.data["type"] == ADDED_AS_FRIEND) {
                    broadcastAddedAsFriendMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!
                    )
                } else if (message.data["type"] == REMOVED_AS_FRIEND) {
                    broadcastRemovedAsFriendMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!
                    )
                } else if (message.data["type"] == ADDED_TO_GROUP) {
                    broadcastAddedToGroupMessage(
                            message.data["senderID"]!!, message.data["senderName"]!!,
                            message.data["groupID"]!!, message.data["groupName"]!!
                    )
                }
            }
            if (message.notification != null) {
                Log.i("FirebaseMessageService", "Notification: " + message.notification.toString())
            }
        }
    }

    private fun broadcastLoginMessage(senderID: String, senderName: String, latitude: Double, longitude: Double) {
        val intent = Intent(LOGIN)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastLocationMessage(senderID: String, senderName: String, latitude: Double, longitude: Double) {
        val intent = Intent(LOCATION)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastReadyRequestMessage(senderID: String, senderName: String) {
        val intent = Intent(READY_REQUEST)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastReadyResponseMessage(senderID: String, senderName: String, receiverID: String, response: Boolean) {
        val intent = Intent(READY_RESPONSE)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("receiverID", receiverID)
        intent.putExtra("response", response)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastReadyDecisionMessage(senderID: String, senderName: String, decision: Boolean) {
        val intent = Intent(READY_DECISION)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("decision", decision)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastAddedAsFriendMessage(senderID: String, senderName: String) {
        val intent = Intent(ADDED_AS_FRIEND)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastRemovedAsFriendMessage(senderID: String, senderName: String) {
        val intent = Intent(REMOVED_AS_FRIEND)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastAddedToGroupMessage(senderID: String, senderName: String, groupID: String, groupName: String) {
        val intent = Intent(ADDED_TO_GROUP)
        intent.putExtra("senderID", senderID)
        intent.putExtra("senderName", senderName)
        intent.putExtra("groupID", groupID)
        intent.putExtra("groupName", groupName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}