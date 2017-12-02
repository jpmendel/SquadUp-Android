package com.squadup.squadup.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
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

        val friendName = returnView!!.findViewById<TextView>(R.id.friend_name)
        friendName.text = friend.name
        val friendEmail = returnView.findViewById<TextView>(R.id.friend_email)
        friendEmail.text = friend.id
        returnView.setOnClickListener {
            fragment.selectFriend(friend)
            val friendBackground = returnView.findViewById<LinearLayout>(R.id.friend_background)
            if (!fragment.selectedFriends.contains(friend)) {
                friendBackground.setBackgroundResource(android.R.color.holo_green_dark)
            } else {
                friendBackground.setBackgroundResource(android.R.color.white)
            }
        }
        returnView.setOnLongClickListener {
            fragment.removeFriend(friend)
            true
        }

        return returnView
    }

    fun updateDataSet(friends: MutableList<User>) {
        clear()
        addAll(friends)
        notifyDataSetChanged()
    }

}