package com.squadup.squadup.manager

import android.app.Application
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.service.FirebaseIDService

/**
 * A manager class for handling global data throughout the app.
 */
class ApplicationManager : Application() {

    // The manager that handles Google App Engine calls and HTTP requests.
    lateinit var backend: BackendManager

    var user: User? = null

    var group: Group? = null

    //list of user Emails
    var userList: MutableList<String> = mutableListOf()

    // Sets up all the initial data for the application.
    fun setup() {
        backend = BackendManager(applicationContext)
    }

    // Updates the registration token for the current user.
    fun updateCurrentUserRegistration(callback: (() -> Unit)? = null) {
        if (user != null) {
            if (user!!.registrationToken != FirebaseIDService.getToken()) {
                user!!.registrationToken = FirebaseIDService.getToken()
                backend.createUserRecord(user!!, callback)
            }
        }
    }

}