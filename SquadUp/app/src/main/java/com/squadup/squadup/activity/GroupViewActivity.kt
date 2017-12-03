package com.squadup.squadup.activity

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.squadup.squadup.R
import com.squadup.squadup.utilities.GroupMemberAdapter
import org.w3c.dom.Text

class GroupViewActivity : BaseActivity() {

    //setup the recycler view, layout manager, adapter needed to display the pizzas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_view)

        setSupportActionBar(findViewById(R.id.toolbar))

        super.initializeViews()

        //establish the name of the group
        val textView = findViewById<TextView>(R.id.textViewGroupName)
        textView.text = app.group?.name ?: "GroupName"

        //set up list view for members of group
        var groupMemberListView = findViewById<ListView>(R.id.groupMemberListView)
        Log.i("GROUPVIEW", app.group!!.members.toString())
        groupMemberListView.adapter = GroupMemberAdapter(this, app.group!!.members)


        findViewById<FloatingActionButton>(R.id.fabSquadUp).setOnClickListener {
            view : View ->
            Handler().postDelayed({
                showScreen(MeetUpActivity::class.java)
            }, 0)
        }
    }

}
