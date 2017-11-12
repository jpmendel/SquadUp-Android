package com.squadup.squadup.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squadup.squadup.manager.ApplicationManager

open class BaseActivity : AppCompatActivity() {

    companion object {
        val TEXT_MESSAGE = "TEXT_MESSAGE"
        val LOCATION_MESSAGE = "LOCATION_MESSAGE"
    }

    lateinit var app: ApplicationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ApplicationManager
        app.setup()
    }

}
