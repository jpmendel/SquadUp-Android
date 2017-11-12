package com.squadup.squadup.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.squadup.squadup.R

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        Handler().postDelayed({
            val intent = Intent(this, PubSubTestActivity::class.java)
            startActivity(intent)
        }, 1500)
    }
}
