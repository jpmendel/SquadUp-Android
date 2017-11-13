package com.squadup.squadup.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squadup.squadup.R
import com.squadup.squadup.manager.ApplicationManager

/**
 * A base activity class that all other activities should inherit from.
 * Includes basic navigation and data storage functionality.
 */
open class BaseActivity : AppCompatActivity() {

    companion object {
        val TEXT_MESSAGE = "TEXT_MESSAGE"
        val LOCATION_MESSAGE = "LOCATION_MESSAGE"
    }

    // The application manager to manage global data.
    lateinit var app: ApplicationManager

    // Runs when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ApplicationManager
        app.setup()
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

    // Runs when the back button is pressed. Show new transition animation.
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.screen_enter_from_left, R.anim.screen_exit_to_right)
    }

}
