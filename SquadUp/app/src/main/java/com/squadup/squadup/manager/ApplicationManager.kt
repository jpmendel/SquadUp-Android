package com.squadup.squadup.manager

import android.app.Application

class ApplicationManager : Application() {

    var backend: BackendManager = BackendManager(applicationContext)

}