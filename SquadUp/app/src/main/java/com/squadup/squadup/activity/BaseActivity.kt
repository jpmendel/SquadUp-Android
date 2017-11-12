package com.squadup.squadup.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import com.squadup.squadup.R
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

    protected fun showScreen(screen: Class<*>, data: MutableMap<String, Parcelable>? = null) {
        val intent = Intent(this, screen)
        if (data != null) {
            for (item in data) {
                intent.putExtra(item.key, item.value)
            }
        }
        startActivity(intent)
        overridePendingTransition(R.anim.screen_enter, R.anim.screen_exit)
    }

    protected fun backScreen() {
        finish()
    }

    protected fun backToScreen(screen: Class<*>) {

    }

}
