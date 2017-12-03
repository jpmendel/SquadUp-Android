package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.squadup.squadup.R
import com.squadup.squadup.data.Group
import com.squadup.squadup.utilities.GroupListAdapter


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GroupsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GroupsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupsFragment : Fragment() {

    companion object {
        fun newInstance(): GroupsFragment {
            return GroupsFragment()
        }
    }

    private lateinit var baseActivity: BaseActivity
    private lateinit var groupsList: ListView
    private lateinit var groupsListAdapter: GroupListAdapter


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()
        setupGroupList()
        groupsList.isClickable = true
    }

    private fun initializeViews() {
        baseActivity = activity as BaseActivity
        groupsList = baseActivity.findViewById(R.id.groupsListView)
    }

    private fun setupGroupList() {
        Log.i("FriendFragment", "User friends: " + baseActivity.app.user!!.groups)
        groupsListAdapter = GroupListAdapter(baseActivity, this, baseActivity.app.user!!.groups)
        groupsList.adapter = groupsListAdapter
    }

    fun showGroupView(g : Group){
        baseActivity.app.backend.getMemberInfoForGroup(g) {
            group: Group? ->
            if (group != null) {
                baseActivity.app.group = group
                Log.i("GROUPSFRAG: ShouldHave", baseActivity.app.group!!.members.toString())
                val intent = Intent(baseActivity, GroupViewActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun onShowFragment() {
        refreshData()
    }


    fun refreshData() {
        groupsListAdapter.updateDataSet(baseActivity.app.user!!.groups)
    }

}
