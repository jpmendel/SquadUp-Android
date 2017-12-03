package com.squadup.squadup.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import com.squadup.squadup.R
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.service.FirebaseMessageService
import android.widget.EditText
import android.view.MotionEvent

class MainActivity : BaseActivity() {

    private val GROUPS_FRAGMENT = "Groups"

    private val FRIENDS_FRAGMENT = "Friends"

    private lateinit var tabs: TabLayout

    private lateinit var groupsFragmentFrame: FrameLayout

    private lateinit var groupsFragment: GroupsFragment

    private lateinit var friendsFragmentFrame: FrameLayout

    private lateinit var friendsFragment: FriendsFragment

    private lateinit var broadcastManager: LocalBroadcastManager

    private lateinit var currentFragment: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
        broadcastManager = LocalBroadcastManager.getInstance(this)
    }

    override fun onStart() {
        super.onStart()
        refreshUserData()
        initializeBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        broadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun initializeViews() {
        super.initializeViews()
        tabs = findViewById(R.id.tabs)
        tabs.addOnTabSelectedListener(tabChangeListener)
        groupsFragmentFrame = findViewById(R.id.groups_fragment_frame)
        groupsFragment = GroupsFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .replace(R.id.groups_fragment_frame, groupsFragment)
                .commit()
        friendsFragmentFrame = findViewById(R.id.friends_fragment_frame)
        friendsFragment = FriendsFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .replace(R.id.friends_fragment_frame, friendsFragment)
                .commit()
        friendsFragmentFrame.translationX = screenWidth
        currentFragment = GROUPS_FRAGMENT
    }

    // Sets up the receiver to get broadcast messages from the FirebaseMessageService.
    private fun initializeBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(FirebaseMessageService.ADDED_AS_FRIEND)
        intentFilter.addAction(FirebaseMessageService.ADDED_TO_GROUP)
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun refreshUserData() {
        if (app.user != null) {
            app.backend.getUserRecord(app.user!!.id) {
                user: User? ->
                if (user != null) {
                    app.user = user
                    app.backend.getGroupAndFriendDataForUser(app.user!!) {
                        friendsFragment.refreshData()
                        groupsFragment.refreshData()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        // Don't allow users to back out of this screen.
    }

    // The receiver to handle any broadcasts from the FirebaseMessageService.
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FirebaseMessageService.ADDED_AS_FRIEND) {
                Log.i("MainActivity", "RECEIVED")
                onAddedAsFriendMessageReceived(intent)
            } else if (intent.action == FirebaseMessageService.ADDED_TO_GROUP) {
                onAddedToGroupMessageReceived(intent)
            }
        }
    }

    private fun onAddedAsFriendMessageReceived(intent: Intent) {
        val senderID = intent.getStringExtra("senderID")
        val senderName = intent.getStringExtra("senderName")
        if (app.user != null) {
            app.user!!.friendIDs.add(senderID)
            app.backend.getUserRecord(senderID) {
                user: User? ->
                if (user != null) {
                    app.user!!.friends.add(user)
                    friendsFragment.refreshData()
                }
            }
        }
        Toast.makeText(baseContext, "$senderName added you as a friend!", Toast.LENGTH_SHORT).show()
    }

    private fun onAddedToGroupMessageReceived(intent: Intent) {
        val groupID = intent.getStringExtra("groupID")
        val groupName = intent.getStringExtra("groupName")
        if (app.user != null) {
            app.user!!.groupIDs.add(groupID)
            app.backend.getGroupRecord(groupID) {
                group: Group? ->
                if (group != null) {
                    app.user!!.groups.add(group)
                    groupsFragment.refreshData()
                }
            }
        }
        Toast.makeText(baseContext, "You have been added to the group: $groupName!", Toast.LENGTH_SHORT).show()
    }

    // Exchanges fragments depending on which tab is clicked on.
    private val tabChangeListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab != null) {
                hideKeyboard()
                if (tab.text == GROUPS_FRAGMENT) {
                    currentFragment = GROUPS_FRAGMENT
                    // Animate out the friends fragment.
                    friendsFragmentFrame.animate()
                            .setInterpolator(DecelerateInterpolator())
                            .translationX(screenWidth)
                            .duration = 300
                    // Animate in the groups fragment.
                    groupsFragmentFrame.animate()
                            .setInterpolator(DecelerateInterpolator())
                            .translationX(0f)
                            .duration = 300
                    groupsFragment.onShowFragment()
                } else if (tab.text == FRIENDS_FRAGMENT) {
                    currentFragment = FRIENDS_FRAGMENT
                    // Animate out the groups fragment.
                    groupsFragmentFrame.animate()
                            .setInterpolator(DecelerateInterpolator())
                            .translationX(-screenWidth)
                            .duration = 300
                    // Animate in the friends fragment.
                    friendsFragmentFrame.animate()
                            .setInterpolator(DecelerateInterpolator())
                            .translationX(0f)
                            .duration = 300
                    friendsFragment.onShowFragment()
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    // Makes it so that any click outside the add friend text entry will close the keyboard
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null) {
           if ((event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_MOVE) && view is EditText) {
               val position = IntArray(2)
               view.getLocationOnScreen(position)
               val x = event.rawX + view.left - position[0]
               val y = event.rawY + view.top - position[1]
               if (x < view.left || x > view.right || y < view.top || y > view.bottom) {
                   view.clearFocus()
                   hideKeyboard()
               }
           }
        }
        return super.dispatchTouchEvent(event)
    }

}

