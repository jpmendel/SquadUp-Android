package com.squadup.squadup.manager

import android.app.Application
import com.squadup.squadup.data.User

/**
 * A manager class for handling global data throughout the app.
 */
class ApplicationManager : Application() {

    // The manager that handles Google App Engine calls and HTTP requests.
    lateinit var backend: BackendManager

    var user: User? = null

    // Sets up all the initial data for the application.
    fun setup() {
        backend = BackendManager(applicationContext)
    }

}