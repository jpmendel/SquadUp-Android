package com.squadup.squadup.manager

import android.app.Application

class ApplicationManager : Application() {

    lateinit var backend: BackendManager

    fun setup() {
        backend = BackendManager(applicationContext)
    }

}