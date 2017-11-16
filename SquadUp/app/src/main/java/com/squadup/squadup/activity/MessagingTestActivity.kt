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
import android.widget.Toast

class MessagingTestActivity : BaseActivity() {

    private lateinit var messageText: TextView

    private lateinit var sendTextButton: Button

    private lateinit var sendLoginButton: Button

    private lateinit var sendLocationButton: Button

    private lateinit var notificationButton: Button

    private lateinit var startListeningButton: Button

    private lateinit var stopListeningButton: Button

    private lateinit var listeningText: TextView

    private lateinit var broadcastManager: LocalBroadcastManager

    private var messagesReceived: Int = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TEXT_MESSAGE) {
                val message = intent.getStringExtra("text")
                if (message != null) {
                    messagesReceived++
                    if (messagesReceived == 1) {
                        messageText.text = "You Have $messagesReceived New Message!"
                    } else {
                        messageText.text = "You Have $messagesReceived New Messages!"
                    }
                }
            } else if (intent.action == LOGIN_MESSAGE) {
                val sender = intent.getStringExtra("sender")
                Toast.makeText(baseContext, String.format("%s has joined", sender), Toast.LENGTH_SHORT).show()
            } else if (intent.action == LOCATION_MESSAGE) {
                val sender = intent.getStringExtra("sender")
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                Toast.makeText(baseContext, String.format("%s is at (%f, %f)", sender, latitude, longitude), Toast.LENGTH_SHORT).show()
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
        sendTextButton = findViewById(R.id.send_text_button)
        sendLoginButton = findViewById(R.id.send_login_button)
        sendLocationButton = findViewById(R.id.send_location_button)
        notificationButton = findViewById(R.id.notification_button)
        startListeningButton = findViewById(R.id.start_listening_button)
        stopListeningButton = findViewById(R.id.stop_listening_button)
        listeningText = findViewById(R.id.listening_text)
    }

    private fun initializeBroadcastReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(TEXT_MESSAGE)
        intentFilter.addAction(LOGIN_MESSAGE)
        intentFilter.addAction(LOCATION_MESSAGE)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setupButtons() {
        sendTextButton.setOnClickListener {
            app.backend.sendTextMessage("messages", "Jacob", "Hello World!")
        }
        sendLoginButton.setOnClickListener {
            app.backend.sendLoginMessage("messages", "Jacob", 1.0, 1.0)
            showScreen(MeetUpActivity::class.java)
        }
        sendLocationButton.setOnClickListener {
            app.backend.sendLocationMessage("messages", "Jacob", 1.0, 1.0)
        }
        notificationButton.setOnClickListener {
            app.backend.sendNotification("messages", "SquadUp", "Hey, lets meet up!")
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
