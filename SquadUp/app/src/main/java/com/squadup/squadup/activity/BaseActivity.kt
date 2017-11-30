package com.squadup.squadup.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.squadup.squadup.R
import com.squadup.squadup.manager.ApplicationManager

/**
 * A base activity class that all other activities should inherit from.
 * Includes basic navigation and data storage functionality.
 */
open class BaseActivity : AppCompatActivity() {

    companion object {
        val TEXT_MESSAGE = "TEXT_MESSAGE"
        val LOGIN_MESSAGE = "LOGIN_MESSAGE"
        val LOCATION_MESSAGE = "LOCATION_MESSAGE"
        val READY_REQUEST_MESSAGE = "READY_REQUEST_MESSAGE"
        val READY_RESPONSE_MESSAGE = "READY_RESPONSE_MESSAGE"
        val READY_DECISION_MESSAGE = "READY_DECISION_MESSAGE"
    }

    // The application manager to manage global data.
    lateinit var app: ApplicationManager

    // The sign in client used to sign out of the app.
    private var googleSignInClient: GoogleSignInClient? = null

    // The button used to sign out from any screen.
    private lateinit var signOutButton: Button

    var screenWidth: Float = 0f

    var screenHeight: Float = 0f

    // Runs when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ApplicationManager
        getScreenDimensions()
    }

    private fun getScreenDimensions() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels.toFloat()
        screenHeight = displayMetrics.heightPixels.toFloat()
    }

    protected open fun initializeViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        signOutButton = findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            onSignOutButtonClick()
        }
    }

    // Transition from the current activity to a new one.
    protected fun showScreen(screen: Class<*>, setup: ((intent: Intent) -> Unit)? = null) {
        val intent = Intent(this, screen)
        if (setup != null) {
            setup(intent)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.screen_enter_from_right, R.anim.screen_exit_to_left)
    }

    // Close the current activity and present a new one.
    protected fun presentScreen(screen: Class<*>, setup: ((intent: Intent) -> Unit)? = null) {
        val intent = Intent(this, screen)
        if (setup != null) {
            setup(intent)
        }
        finish()
        startActivity(intent)
    }

    // Go back one screen to the previous activity.
    protected fun backScreen() {
        finish()
        overridePendingTransition(R.anim.screen_enter_from_left, R.anim.screen_exit_to_right)
    }

    // Go back to a specific activity, and close others in between.
    protected fun backToScreen(screen: Class<*>) {
        val intent = Intent(this, screen)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.screen_enter_from_left, R.anim.screen_exit_to_right)
    }

    protected fun hideSignOut() {
        signOutButton.visibility = View.INVISIBLE
    }

    private fun onSignOutButtonClick() {
        if (googleSignInClient == null) {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            googleSignInClient = GoogleSignIn.getClient(this, options)
        }
        googleSignInClient!!.signOut().addOnCompleteListener(this) {
            backToScreen(LoginActivity::class.java)
        }
    }

    // When the top left back button is pressed.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Runs when the back button is pressed. Show new transition animation.
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.screen_enter_from_left, R.anim.screen_exit_to_right)
    }

}
