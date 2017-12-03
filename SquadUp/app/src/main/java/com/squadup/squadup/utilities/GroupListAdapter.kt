package com.squadup.squadup.utilities

import android.content.Intent
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.squadup.squadup.R
import com.squadup.squadup.activity.BaseActivity
import com.squadup.squadup.activity.GroupViewActivity
import com.squadup.squadup.activity.GroupsFragment
import com.squadup.squadup.data.Group

/**
 * Created by StephenHaberle on 12/2/17.
 */

class GroupListAdapter(activity: BaseActivity, fragment: GroupsFragment, groups: MutableList<Group>) : ArrayAdapter<Group>(activity, 0, groups) {

    private val activity: BaseActivity = activity
    private val fragment: GroupsFragment = fragment
    private val groups: MutableList<Group> = groups

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val group = getItem(position)
        var returnView = convertView

        if (returnView == null) {
            val layoutInflater = LayoutInflater.from(activity)
            returnView = layoutInflater.inflate(R.layout.row_group, parent, false)
        }

        returnView!!.setOnClickListener {
            fragment.showGroupView(group)
            }

        val groupName = returnView.findViewById<TextView>(R.id.group_name)
        groupName.text = group.name

        return returnView
    }


    fun updateDataSet(groups: MutableList<Group>) {
        clear()
        addAll(groups)
        notifyDataSetChanged()
    }
}
