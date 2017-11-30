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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.squadup.squadup.R


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FriendsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.friends_frag, container, false)
        Log.i("FriendFragment", (view.context as BaseActivity).app.userList.toString())
        val adapter = ArrayAdapter<String>(view.context,
                android.R.layout.simple_dropdown_item_1line, (view.context as BaseActivity).app.userList)
        view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).setAdapter(adapter)
        view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).threshold = 1

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment FriendsFragment.
         */
        fun newInstance(): FriendsFragment {
            val fragment = FriendsFragment()
            return fragment
        }
    }
}// Required empty public constructor
