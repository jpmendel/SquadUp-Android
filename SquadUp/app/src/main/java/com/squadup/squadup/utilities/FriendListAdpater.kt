package com.squadup.squadup.utilities

import android.content.ClipData
import android.widget.ArrayAdapter
import android.support.v4.app.NotificationCompat.getCategory
import android.R.attr.description
import android.widget.TextView
import android.content.ClipData.Item
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.squadup.squadup.R
import com.squadup.squadup.data.User


/**
 * Created by Jason on 11/30/2017.
 *
 * Helpful Video: https://www.youtube.com/watch?v=P2I8PGLZEVc
 */

class FriendListAdpater(context : Context, friends: MutableList<User>) : BaseAdapter(){

    val adapterContext : Context
    val adapterFriends : MutableList<User>

    init {
        adapterContext = context
        adapterFriends = friends
    }

    //renders each row
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(adapterContext)
        val rows = layoutInflater.inflate(R.layout.friend_frag_row, viewGroup, false)

        rows.findViewById<TextView>(R.id.textViewFriendListName).text = adapterFriends[position].name
        rows.findViewById<TextView>(R.id.textViewFriendListEmail).text = adapterFriends[position].id

        return rows
    }


    override fun getItem(p0: Int): Any {
        return adapterFriends[p0]
    }


    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    //identifies number of rows in a list
    override fun getCount(): Int {
        return adapterFriends.size
    }

}