package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    private lateinit var baseActivity: BaseActivity
    private lateinit var groupsList: ListView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var groupsListAdapter: GroupListAdapter


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_groups, container, false)
    }

    private fun setupGroupList() {
        Log.i("FriendFragment", "User friends: " + baseActivity.app.user!!.groups)
        groupsListAdapter = GroupListAdapter(baseActivity, this, baseActivity.app.user!!.groups)
        groupsList.adapter = groupsListAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initializeViews()
        setupGroupList()
        groupsList.isClickable = true

        refreshLayout.setOnRefreshListener {
            Log.i("GroupsFragment", "Heard listener")
            refreshData()
        }
    }

    fun showGroupView(g : Group){
        val intent = Intent(context, GroupViewActivity::class.java)
        startActivity(intent)
    }

    private fun initializeViews(){
        baseActivity = activity as BaseActivity
        groupsList = baseActivity.findViewById(R.id.groupsListView)
        refreshLayout = baseActivity.findViewById(R.id.swiperefresh)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_ITEM_NUMBER = "item_number"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment GroupsFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): GroupsFragment {
            val fragment = GroupsFragment()
            return GroupsFragment()
        }
    }

    fun onShowFragment() {
        Log.i("GroupsFragment", "Fragment Selected")
        refreshData()
    }


    fun refreshData() {
        groupsListAdapter.updateDataSet(baseActivity.app.user!!.groups, refreshLayout)
    }

}// Required empty public constructor
