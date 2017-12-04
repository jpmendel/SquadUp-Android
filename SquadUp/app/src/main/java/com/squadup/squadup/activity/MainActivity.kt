package com.squadup.squadup.activity

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import com.squadup.squadup.R

class MainActivity : BaseActivity() {

    private val GROUPS_FRAGMENT = "Groups"

    private val FRIENDS_FRAGMENT = "Friends"

    private lateinit var tabs: TabLayout

    private lateinit var groupsFragmentFrame: FrameLayout

    lateinit var groupsFragment: GroupsFragment

    private lateinit var friendsFragmentFrame: FrameLayout

    lateinit var friendsFragment: FriendsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
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
    }

    override fun onBackPressed() {
        // Don't allow users to back out of this screen.
    }

    // Exchanges fragments depending on which tab is clicked on.
    private val tabChangeListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab != null) {
                hideKeyboard()
                if (tab.text == GROUPS_FRAGMENT) {
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

