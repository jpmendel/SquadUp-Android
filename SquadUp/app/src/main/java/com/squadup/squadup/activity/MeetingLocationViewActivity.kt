package com.squadup.squadup.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.squadup.squadup.R
import android.content.Intent
import android.net.Uri
import android.widget.ImageView

/**
 * An activity to display the meeting location to the user and provide options to get directions
 * or return to the menu.
 */
class MeetingLocationViewActivity : BaseActivity() {

    // The text for the name of the meeting location.
    private lateinit var meetingLocationText: TextView

    // The button to return to the menu without directions.
    private lateinit var gotItButton: Button

    // The button to get directions to your destination.
    private lateinit var getDirectionsButton: Button

    // Runs when the activity is loaded and created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_location_view)
        initializeViews()
        setupButtons()
    }

    // Sets up all the views on the screen.
    override fun initializeViews() {
        super.initializeViews()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        meetingLocationText = findViewById(R.id.meeting_location_text)
        meetingLocationText.text = intent.extras.getString("meetingLocation")
        gotItButton = findViewById(R.id.got_it_button)
        getDirectionsButton = findViewById(R.id.get_directions_button)
    }

    // Sets up the buttons on the screen.
    private fun setupButtons() {
        gotItButton.setOnClickListener {
            onGotItButtonClick()
        }
        getDirectionsButton.setOnClickListener {
            onGetDirectionsButtonClick()
        }
    }

    // Return to the menu when the user clicks the got it button.
    private fun onGotItButtonClick() {
        backToScreen(MainActivity::class.java)
    }

    // Open the Google Maps app and provide the user with directions.
    private fun onGetDirectionsButtonClick() {
        val startLatitude = intent.extras.getDouble("startLatitude")
        val startLongitude = intent.extras.getDouble("startLongitude")
        val destinationLatitude = intent.extras.getDouble("destinationLatitude")
        val destinationLongitude = intent.extras.getDouble("destinationLongitude")
        val directionsURL = String.format("http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f&mode=walking",
                startLatitude, startLongitude, destinationLatitude, destinationLongitude)
        val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(directionsURL))
        startActivity(intent)
    }

    // Go back to the main screen when back is pressed.
    override fun onBackPressed() {
        backToScreen(MainActivity::class.java)
    }

}
