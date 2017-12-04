package com.squadup.squadup.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.squadup.squadup.R
import com.squadup.squadup.activity.BaseActivity
import com.squadup.squadup.activity.FriendsFragment
import com.squadup.squadup.data.User

/**
 * Created by Jason on 11/30/2017.
 *
 * Helpful Video: https://www.youtube.com/watch?v=P2I8PGLZEVc
 */

class FriendListAdapter(activity: BaseActivity, fragment: FriendsFragment, friends: MutableList<User>) : ArrayAdapter<User>(activity, 0, friends) {

    private val activity: BaseActivity = activity
    private val fragment: FriendsFragment = fragment

    //renders each row
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val friend = getItem(position)
        var returnView = convertView
        if (returnView == null) {
            val layoutInflater = LayoutInflater.from(activity)
            returnView = layoutInflater.inflate(R.layout.row_friend, viewGroup, false)
        }

        val friendName = returnView!!.findViewById<TextView>(R.id.group_name)
        friendName.text = friend.name
        val friendEmail = returnView.findViewById<TextView>(R.id.friend_email)
        friendEmail.text = friend.id
        val friendBackground = returnView.findViewById<View>(R.id.friend_background)
        if (friend.selected) {
            friendBackground.alpha = 1.0f
        } else {
            friendBackground.alpha = 0.0f
        }
        returnView.setOnClickListener {
            fragment.selectFriend(friend)
            if (friend.selected) {
                friendBackground.animate().alpha(1.0f).duration = 200
            } else {
                friendBackground.animate().alpha(0.0f).duration = 200
            }
        }
        returnView.setOnLongClickListener {
            fragment.removeFriend(friend)
            true
        }

        return returnView
    }

}