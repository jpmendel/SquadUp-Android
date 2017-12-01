package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squadup.squadup.R


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

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_ITEM_NUMBER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.groups_frag, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()
    }

    private fun initializeViews() {

    }

    fun onShowFragment() {
        Log.i("GroupsFragment", "Fragment Selected")
        refreshData()
    }

    fun refreshData() {

    }

}// Required empty public constructor
