package com.squadup.squadup.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.squadup.squadup.R
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter



class PubSubTestActivity : BaseActivity() {

    lateinit var messageText: TextView

    lateinit var sendButton: Button

    lateinit var startListeningButton: Button

    lateinit var stopListeningButton: Button

    lateinit var broadcastManager: LocalBroadcastManager

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TEXT_MESSAGE) {
                val message = intent.getStringExtra("message")
                if (message != null) {
                    messageText.text = message
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pub_sub_test)
        initializeViews()
        initializeBroadcastReceiver()
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }

    private fun initializeViews() {
        messageText = findViewById(R.id.message_text)
        sendButton = findViewById(R.id.send_button)
        startListeningButton = findViewById(R.id.start_listening_button)
        stopListeningButton = findViewById(R.id.stop_listening_button)
    }

    private fun initializeBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(TEXT_MESSAGE)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setupButtons() {
        sendButton.setOnClickListener {
            app.backend.sendMessage("messages", "You Have 1 New Message!")
        }
        startListeningButton.setOnClickListener {
            app.backend.startListening("messages")
        }
        stopListeningButton.setOnClickListener {
            app.backend.stopListening("messages")
        }
    }

}
