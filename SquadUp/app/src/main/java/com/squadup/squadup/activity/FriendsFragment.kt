package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squadup.squadup.R
import com.squadup.squadup.data.User

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FriendsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment : Fragment() {

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment FriendsFragment.
         */
        fun newInstance(): FriendsFragment {
            return FriendsFragment()
        }
    }

    private lateinit var baseActivity: BaseActivity

    private lateinit var friendList: ListView

    private lateinit var addFriendTextField: AutoCompleteTextView

    private lateinit var addFriendButton: Button

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.friends_frag, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()

        //////////////////////////////////////////////////////////////////
        //                    Handling AutoCompleteTextView             //
        //////////////////////////////////////////////////////////////////
        //set up the AutoCompleteTextView for finding friends
//        Thread.sleep(5000)
        //TODO also remove all of a given user's' friends from the list
        baseActivity.app.userList.remove(baseActivity.app.user!!.id)
        for (i in 0 until baseActivity.app.user!!.friendIDs.size){
            baseActivity.app.userList.remove(baseActivity.app.user!!.friendIDs[i])
        }
        Log.i("FriendFragment", baseActivity.app.userList.toString())
        val adapterUserEmails = ArrayAdapter<String>(baseActivity,
                android.R.layout.simple_dropdown_item_1line, baseActivity.app.userList)
        addFriendTextField.setAdapter(adapterUserEmails)
        addFriendTextField.maxLines = 1
        addFriendTextField.threshold = 1

        //////////////////////////////////////////////////////////////////
        //                    Handling FriendList                       //
        //////////////////////////////////////////////////////////////////
        //load all of a users friends into the friend list
        val adapterUserFriends = ArrayAdapter<String>(baseActivity,
                android.R.layout.simple_dropdown_item_1line, baseActivity.app.user!!.friendIDs)
        friendList.adapter = adapterUserFriends


        //////////////////////////////////////////////////////////////////
        //                    Handling AddFriend                       //
        //////////////////////////////////////////////////////////////////
        //Set onClick listener for the "add friend" button
        addFriendButton.setOnClickListener {
            val friendEmail = addFriendTextField.text.toString()
            Log.i("FriendFragment", friendEmail)
            //check if selected friend exists
            var friendAdded: User?
            baseActivity.app.backend.getUserRecord(friendEmail) { user: User? ->
                if (user != null) {
                    Log.i("FriendFragment", "DNE")
                    friendAdded = user
                    //add friend to user's list
                    baseActivity.app.user!!.friends.add(friendAdded!!)
                    baseActivity.app.user!!.friendIDs.add(friendAdded!!.id)
                    //notify the friendList Adapater
                    adapterUserFriends.notifyDataSetChanged()

                    //clear the name from the autoCompleteTextView
                    addFriendTextField.text.clear()

                    //remove the name from autoCompleteTextViewAdapter and notify it
                    baseActivity.app.userList.remove(friendEmail)
                    adapterUserEmails.notifyDataSetChanged()

                    //add User to friend's friends and update user on backend
                    friendAdded!!.friendIDs.add(baseActivity.app.user!!.id)
                    friendAdded!!.friends.add(baseActivity.app.user!!)
                    baseActivity.app.backend.createUserRecord(baseActivity.app.user!!)
                    baseActivity.app.backend.createUserRecord(friendAdded!!)
                } else {
                    Toast.makeText(baseActivity, "User email does not exist. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeViews() {
        baseActivity = activity as BaseActivity
        friendList = baseActivity.findViewById(R.id.friendListView)
        addFriendTextField = baseActivity.findViewById(R.id.autoCompleteTextView)
        addFriendButton = baseActivity.findViewById(R.id.addFriendBtn)
    }

}// Required empty public constructor
