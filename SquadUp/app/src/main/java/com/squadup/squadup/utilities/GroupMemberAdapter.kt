package com.squadup.squadup.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.squadup.squadup.R
import com.squadup.squadup.activity.BaseActivity
import com.squadup.squadup.data.User

/**
 * Created by Jason on 11/30/2017.
 *
 * Helpful Video: https://www.youtube.com/watch?v=P2I8PGLZEVc
 */

class GroupMemberAdapter(activity: BaseActivity, groupMembers: MutableList<User>) : ArrayAdapter<User>(activity, 0, groupMembers) {

    private val activity: BaseActivity = activity

    //renders each row
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val groupMember = getItem(position)
        var returnView = convertView
        if (returnView == null) {
            val layoutInflater = LayoutInflater.from(activity)
            returnView = layoutInflater.inflate(R.layout.row_friend, viewGroup, false)
        }

        val friendName = returnView!!.findViewById<TextView>(R.id.group_name)
        friendName.text = groupMember.name
        val friendEmail = returnView.findViewById<TextView>(R.id.friend_email)
        friendEmail.text = groupMember.id
//        val friendBackground = returnView.findViewById<View>(R.id.friend_background)
//        if (groupMember.selected) {
//            friendBackground.alpha = 1.0f
//        } else {
//            friendBackground.alpha = 0.0f
//        }
//        returnView.setOnClickListener {
//            if (groupMember.selected) {
//
//            } else {
//
//            }
//        }
//        returnView.setOnLongClickListener {
//
//            true
//        }

        return returnView
    }

    fun updateDataSet(friends: MutableList<User>) {
        clear()
        addAll(friends)
        notifyDataSetChanged()
    }

}