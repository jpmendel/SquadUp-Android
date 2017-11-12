package com.squadup.squadup.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.squadup.squadup.R
import com.squadup.squadup.manager.ApplicationManager

open class BaseActivity : AppCompatActivity() {

    lateinit var app: ApplicationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ApplicationManager
        app.setup()
    }

}
