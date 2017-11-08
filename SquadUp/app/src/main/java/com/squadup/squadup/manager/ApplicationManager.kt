package com.squadup.squadup.manager

import android.app.Application

class ApplicationManager : Application() {

    var database: DatabaseManager = DatabaseManager(applicationContext)

}