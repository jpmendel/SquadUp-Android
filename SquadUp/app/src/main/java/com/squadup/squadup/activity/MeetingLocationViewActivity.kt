package com.squadup.squadup.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.squadup.squadup.R

class MeetingLocationViewActivity : BaseActivity() {

    private lateinit var meetingLocationText: TextView

    private lateinit var gotItButton: Button

    private lateinit var getDirectionsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_location_view)
        initializeViews()
        setupButtons()
    }

    override fun initializeViews() {
        super.initializeViews()
        meetingLocationText = findViewById(R.id.meeting_location_text)
        meetingLocationText.text = intent.extras.getString("meetingLocation")
        gotItButton = findViewById(R.id.got_it_button)
        getDirectionsButton = findViewById(R.id.get_directions_button)
    }

    private fun setupButtons() {
        gotItButton.setOnClickListener {
            onGotItButtonClick()
        }
        getDirectionsButton.setOnClickListener {
            onGetDirectionsButtonClick()
        }
    }

    private fun onGotItButtonClick() {
        backToScreen(MessagingTestActivity::class.java)
    }

    private fun onGetDirectionsButtonClick() {

    }

    override fun onBackPressed() {
        backToScreen(MessagingTestActivity::class.java)
    }

}
