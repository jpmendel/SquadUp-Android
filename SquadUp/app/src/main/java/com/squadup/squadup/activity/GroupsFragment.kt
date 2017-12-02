package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.squadup.squadup.R
import com.squadup.squadup.data.Group


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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.groups_frag, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()

        //fill groups list adapter
        val groupsListAdapter = ArrayAdapter<String>(baseActivity,
                android.R.layout.simple_dropdown_item_1line, baseActivity.app.user!!.groups.map { group -> group.name })

        val onClick = AdapterView.OnItemClickListener { parent, view, position, id ->
            Log.i("GroupsFragment", "Selected: " + groupsListAdapter.getItem(position).toString())

            val groupNameTxt = groupsListAdapter.getItem(position).toString()
            for (g in baseActivity.app.user!!.groups) {
                if (g.name == (groupNameTxt)) {
                    baseActivity.app.group = g

                    val intent = Intent(context, GroupViewActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        groupsList.adapter = groupsListAdapter
        groupsList.isClickable = true
        groupsList.setOnItemClickListener(onClick)


    }

    private fun initializeViews(){
        baseActivity = activity as BaseActivity
        groupsList = baseActivity.findViewById(R.id.groupsListView)
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
            return fragment
        }
    }
}// Required empty public constructor
