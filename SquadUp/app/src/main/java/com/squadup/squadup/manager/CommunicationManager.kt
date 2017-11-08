package com.squadup.squadup.manager

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class CommunicationManager {

    companion object {

        fun startListening(channel: String) {
            FirebaseMessaging.getInstance().subscribeToTopic(channel)
        }

        fun stopListening(channel: String) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(channel)
        }

        fun sendMessage(data: String) {

        }
    }

}