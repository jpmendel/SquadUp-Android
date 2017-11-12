package com.squadup.squadup.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.squadup.squadup.R

class PubSubTestActivity : BaseActivity() {

    lateinit var messageText: TextView

    lateinit var testButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub_sub_test)
        initializeViews()
        setupButtons()
        app.backend.startListening("mytopic")
    }

    private fun initializeViews() {
        messageText = findViewById(R.id.message_text)
        testButton = findViewById(R.id.test_button)
    }

    private fun setupButtons() {
        testButton.setOnClickListener {
            onTestButtonClick()
        }
    }

    private fun onTestButtonClick() {
        app.backend.sendMessage("mytopic", "Hello There!")
    }
}
