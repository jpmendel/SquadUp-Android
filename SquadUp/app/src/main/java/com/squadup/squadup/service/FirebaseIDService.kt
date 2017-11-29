package com.squadup.squadup.service

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * The ID service for Firebase responsible for identifying app users when sending and
 * receiving messages.
 */
class FirebaseIDService : FirebaseInstanceIdService() {

    companion object {
        // Static function to get the token for the instance of the app being used.
        fun getToken(): String? = FirebaseInstanceId.getInstance().token
    }

    // Runs whenever Firebase refreshes the user's token.
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.i("FirebaseIDService", "Token: " + refreshedToken)
    }

}