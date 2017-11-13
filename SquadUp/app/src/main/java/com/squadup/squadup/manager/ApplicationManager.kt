package com.squadup.squadup.manager

import android.app.Application

/**
 * A manager class for handling global data throughout the app.
 */
class ApplicationManager : Application() {

    // The manager that handles Google App Engine calls and HTTP requests.
    lateinit var backend: BackendManager

    // Sets up all the initial data for the application.
    fun setup() {
        backend = BackendManager(applicationContext)
    }

}