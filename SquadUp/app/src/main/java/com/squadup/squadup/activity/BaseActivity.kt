package com.squadup.squadup.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.squadup.squadup.R
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.manager.ApplicationManager
import com.squadup.squadup.service.FirebaseMessageService

/**
 * A base activity class that all other activities should inherit from.
 * Includes basic navigation and data storage functionality.
 */
open class BaseActivity : AppCompatActivity() {

    // The application manager to manage global data.
    lateinit var app: ApplicationManager

    // The sign in client used to sign out of the app.
    private var googleSignInClient: GoogleSignInClient? = null

    // The button used to sign out from any screen.
    private lateinit var signOutButton: Button

    protected lateinit var broadcastManager: LocalBroadcastManager

    var screenWidth: Float = 0f

    var screenHeight: Float = 0f

    // Runs when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as ApplicationManager
        getScreenDimensions()
        broadcastManager = LocalBroadcastManager.getInstance(this)
    }

    override fun onStart() {
        super.onStart()
        initializeBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        broadcastManager.unregisterReceiver(broadcastReceiver)
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
        signOutButton.setOnLongClickListener {
            showScreen(MeetUpActivity::class.java)
            true
        }
    }

    // Sets up the receiver to get broadcast messages from the FirebaseMessageService.
    protected open fun initializeBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(FirebaseMessageService.ADDED_AS_FRIEND)
        intentFilter.addAction(FirebaseMessageService.REMOVED_AS_FRIEND)
        intentFilter.addAction(FirebaseMessageService.ADDED_TO_GROUP)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    // Transition from the current activity to a new one.
    fun showScreen(screen: Class<*>, setup: ((intent: Intent) -> Unit)? = null) {
        val intent = Intent(this, screen)
        if (setup != null) {
            setup(intent)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.screen_enter_from_right, R.anim.screen_exit_to_left)
    }

    // Close the current activity and present a new one.
    fun presentScreen(screen: Class<*>, setup: ((intent: Intent) -> Unit)? = null) {
        val intent = Intent(this, screen)
        if (setup != null) {
            setup(intent)
        }
        finish()
        startActivity(intent)
    }

    // Go back one screen to the previous activity.
    fun backScreen() {
        finish()
        overridePendingTransition(R.anim.screen_enter_from_left, R.anim.screen_exit_to_right)
    }

    // Go back to a specific activity, and close others in between.
    fun backToScreen(screen: Class<*>) {
        val intent = Intent(this, screen)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.screen_enter_from_left, R.anim.screen_exit_to_right)
    }

    fun hideKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
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

    // The receiver to handle any broadcasts from the FirebaseMessageService.
    protected open val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FirebaseMessageService.ADDED_AS_FRIEND) {
                onAddedAsFriendMessageReceived(intent)
            } else if (intent.action == FirebaseMessageService.REMOVED_AS_FRIEND) {
                onRemovedAsFriendMessageReceived(intent)
            } else if (intent.action == FirebaseMessageService.ADDED_TO_GROUP) {
                onAddedToGroupMessageReceived(intent)
            }
        }
    }

    protected fun onAddedAsFriendMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        val senderName = intent.getStringExtra("senderName")
        if (app.user != null) {
            if (!app.user!!.friendIDs.contains(senderID)) {
                app.user!!.friendIDs.add(senderID)
            }
            app.backend.getUserRecord(senderID) {
                user: User? ->
                if (user != null) {
                    if (!app.user!!.friends.contains(user)) {
                        app.user!!.friends.add(user)
                    }
                    if (this is MainActivity) {
                        friendsFragment.refreshData()
                    }
                }
            }
        }
        Toast.makeText(baseContext, "$senderName added you as a friend!", Toast.LENGTH_SHORT).show()
    }

    protected fun onRemovedAsFriendMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        val senderName = intent.getStringExtra("senderName")
        if (app.user != null) {
            app.user!!.friendIDs.remove(senderID)
            for (i in 0 until app.user!!.friends.count()) {
                if (app.user!!.friends[i].id == senderID) {
                    app.user!!.friends.removeAt(i)
                    break
                }
            }
            if (this is MainActivity) {
                friendsFragment.refreshData()
            }
        }
        Toast.makeText(baseContext, "$senderName unfriended you!", Toast.LENGTH_SHORT).show()
    }

    protected fun onAddedToGroupMessageReceived(intent: Intent) {
        val groupID = intent.getStringExtra("groupID")
        val groupName = intent.getStringExtra("groupName")
        if (app.user != null) {
            if (!app.user!!.groupIDs.contains(groupID)) {
                app.user!!.groupIDs.add(groupID)
            }
            app.backend.getGroupRecord(groupID) {
                group: Group? ->
                if (group != null) {
                    if (!app.user!!.groups.contains(group)) {
                        app.user!!.groups.add(group)
                    }
                    if (this is MainActivity) {
                        groupsFragment.refreshData()
                    }
                }
            }
        }
        Toast.makeText(baseContext, "You have been added to the group: $groupName!", Toast.LENGTH_SHORT).show()
    }

}
