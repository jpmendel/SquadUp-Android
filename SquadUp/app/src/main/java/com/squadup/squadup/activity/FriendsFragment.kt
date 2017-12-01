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

//    private var baseActivity = activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.friends_frag, container, false)




        //////////////////////////////////////////////////////////////////
        //                    Handling AutoCompleteTextView             //
        //////////////////////////////////////////////////////////////////
        //set up the AutoCompleteTextView for finding friends
//        Thread.sleep(5000)
        Log.i("FriendFragment", "User List: " + (view.context as BaseActivity).app.userList)
        Log.i("FriendFragment", "User id: " + (view.context as BaseActivity).app.user!!.id)
        //TODO also remove all of a given user's' friends from the list
        (view.context as BaseActivity).app.userList.remove((view.context as BaseActivity).app.user!!.id)
        Log.i("FriendFragment", (view.context as BaseActivity).app.userList.toString())
        val adapterUserEmails = ArrayAdapter<String>(view.context,
                android.R.layout.simple_dropdown_item_1line, (view.context as BaseActivity).app.userList)
        view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).setAdapter(adapterUserEmails)
        view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).threshold = 1

        //////////////////////////////////////////////////////////////////
        //                    Handling FriendList                       //
        //////////////////////////////////////////////////////////////////
        //load all of a users friends into the friend list
        val adapterUserFriends = ArrayAdapter<String>(view.context,
                android.R.layout.simple_dropdown_item_1line, (view.context as BaseActivity).app.user!!.friendIDs)
        view.findViewById<ListView>(R.id.friendListView).adapter = adapterUserFriends


        //////////////////////////////////////////////////////////////////
        //                    Handling AddFriend                       //
        //////////////////////////////////////////////////////////////////
        //Set onClick listener for the "add friend" button
        view.findViewById<Button>(R.id.addFriendBtn).setOnClickListener {
            val friendEmail : String = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).text.toString()
            Log.i("FriendFragment", friendEmail)
            //check if selected friend exists
            var friendAdded : User? = null
            (view.context as BaseActivity).app.backend.getUserRecord(friendEmail) {
                user: User? ->
                if (user != null){
                    Log.i("FriendFragment", "DNE")
                    friendAdded = user
                    //add friend to user's list
                    (view.context as BaseActivity).app.user!!.friends.add(friendAdded!!)
                    (view.context as BaseActivity).app.user!!.friendIDs.add(friendAdded!!.id)
                    //notify the friendList Adapater
                    adapterUserFriends.notifyDataSetChanged()

                    //clear the name from the autoCompleteTextView
                    view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView).text.clear()

                    //remove the name from autoCompleteTextViewAdapter and notify it
                    (view.context as BaseActivity).app.userList.remove(friendEmail)
                    adapterUserEmails.notifyDataSetChanged()

                    //add User to friend's friends and update user on backend
                    friendAdded!!.friendIDs.add((view.context as BaseActivity).app.user!!.id)
                    friendAdded!!.friends.add((view.context as BaseActivity).app.user!!)
                    (view.context as BaseActivity).app.backend.createUserRecord((view.context as BaseActivity).app.user!!)
                    (view.context as BaseActivity).app.backend.createUserRecord(friendAdded!!)
                }
                else{
                    Toast.makeText(view.context, "User email does not exist. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

        }

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
