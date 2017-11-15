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

class MessagingTestActivity : BaseActivity() {

    private lateinit var messageText: TextView

    private lateinit var sendButton: Button

    private lateinit var notificationButton: Button

    private lateinit var startListeningButton: Button

    private lateinit var stopListeningButton: Button

    private lateinit var listeningText: TextView

    private lateinit var broadcastManager: LocalBroadcastManager

    private var messagesReceived: Int = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TEXT_MESSAGE) {
                val message = intent.getStringExtra("message")
                if (message != null) {
                    messagesReceived++
                    if (messagesReceived == 1) {
                        messageText.text = "You Have $messagesReceived New Message!"
                    } else {
                        messageText.text = "You Have $messagesReceived New Messages!"
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging_test)
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
        notificationButton = findViewById(R.id.notification_button)
        startListeningButton = findViewById(R.id.start_listening_button)
        stopListeningButton = findViewById(R.id.stop_listening_button)
        listeningText = findViewById(R.id.listening_text)
    }

    private fun initializeBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(TEXT_MESSAGE)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setupButtons() {
        sendButton.setOnClickListener {
            app.backend.sendMessage("messages", "Hello World!")
        }
        notificationButton.setOnClickListener {
            app.backend.sendNotification("messages", "SquadUp", "Hey, let meet up!")
        }
        startListeningButton.setOnClickListener {
            app.backend.startListening("messages")
            listeningText.text = "Listening To: (Messages)"
        }
        stopListeningButton.setOnClickListener {
            app.backend.stopListening("messages")
            listeningText.text = "Listening To: (Nothing)"
        }
    }

}
