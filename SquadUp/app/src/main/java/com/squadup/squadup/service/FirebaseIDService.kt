package com.squadup.squadup.service

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class FirebaseIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.i("FirebaseIDService", "Token: " + refreshedToken)
        if (refreshedToken != null) {
            sendRegistrationToServer(refreshedToken)
        }
    }

    private fun sendRegistrationToServer(token: String) {

    }

}