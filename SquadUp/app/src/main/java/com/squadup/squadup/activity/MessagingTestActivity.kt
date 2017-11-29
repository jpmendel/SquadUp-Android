package com.squadup.squadup.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.squadup.squadup.R
import com.squadup.squadup.data.User

class MessagingTestActivity : BaseActivity() {

    private lateinit var messageText: TextView

    private lateinit var sendTextButton: Button

    private lateinit var sendLoginButton: Button

    private lateinit var sendLocationButton: Button

    private lateinit var notificationButton: Button

    private lateinit var startListeningButton: Button

    private lateinit var stopListeningButton: Button

    private lateinit var listeningText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging_test)
        initializeViews()
        setupButtons()
    }

    override fun initializeViews() {
        super.initializeViews()
        messageText = findViewById(R.id.message_text)
        sendTextButton = findViewById(R.id.send_text_button)
        sendLoginButton = findViewById(R.id.send_login_button)
        sendLocationButton = findViewById(R.id.send_location_button)
        notificationButton = findViewById(R.id.notification_button)
        startListeningButton = findViewById(R.id.start_listening_button)
        stopListeningButton = findViewById(R.id.stop_listening_button)
        listeningText = findViewById(R.id.listening_text)
    }

    private fun setupButtons() {
        app.user = User("test@email.com", "Test User")
        app.updateCurrentUserRegistration()
        sendTextButton.setOnClickListener {
            app.backend.getUserRecord("test@email.com") {
                user: User? ->
                if (user != null) {
                    messageText.text = "Retrieved ${user.name}"
                } else {
                    messageText.text = "No user with that ID"
                }
            }
        }
        sendLoginButton.setOnClickListener {
            app.backend.createUserRecord(app.user!!)
        }
        sendLoginButton.setOnLongClickListener {
            app.user!!.friends.add("other@email.com")
            app.user!!.friends.add("another@email.com")
            app.backend.createUserRecord(app.user!!)
            true
        }
        sendLocationButton.setOnClickListener {
            app.backend.deleteUserRecord("test@email.com")
        }
        notificationButton.setOnClickListener {
            showScreen(MeetUpActivity::class.java)
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
