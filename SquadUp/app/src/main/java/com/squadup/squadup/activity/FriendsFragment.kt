package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */

import android.app.Activity
import android.content.DialogInterface
import android.graphics.ColorFilter
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squadup.squadup.R
import com.squadup.squadup.data.User
import com.squadup.squadup.utilities.FriendListAdpater
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.friend_frag_row.view.*


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

    private lateinit var createGroupButton: FloatingActionButton

    private lateinit var groupName : TextView

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
//        val adapterUserFriends = ArrayAdapter<String>(baseActivity,
//                android.R.layout.simple_dropdown_item_1line, baseActivity.app.user!!.friendIDs)


        Log.i("FriendFragment", "User friends: " + baseActivity.app.user!!.friends)
        val adapterUserFriends = FriendListAdpater(activity.applicationContext, baseActivity.app.user!!.friends)

        //setup onclick listener
        friendList.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        val mItemClickedListener = OnItemClickListener { parent, v, position, id ->
            Log.i("FriendFragment", "number of items selected: " + friendList.checkedItemCount)
            Log.i("FriendFragment", "Item selected: " + baseActivity.app.user!!.friends[position].id)
            if (parent.getChildAt(position).checkBoxFriendListSelected.isChecked){
                parent.getChildAt(position).checkBoxFriendListSelected.isChecked = false
            }
            else{
                parent.getChildAt(position).checkBoxFriendListSelected.isChecked = true
            }
//            friendList.setSelection(position)
//            friendList
        }
        friendList.adapter = adapterUserFriends
        friendList.setOnItemClickListener(mItemClickedListener)

        //////////////////////////////////////////////////////////////////
        //                    Handling AddFriend                       //
        //////////////////////////////////////////////////////////////////
        //Set onClick listener for the "add friend" button
        addFriendButton.setOnClickListener {
            val friendEmail = addFriendTextField.text.toString()
            Log.i("FriendFragment", friendEmail)
            //check if selected friend exists
            var friendAdded: User?
            if (friendEmail.length > 0) {
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
                        val adapterUserEmailsUpdate = ArrayAdapter<String>(baseActivity,
                                android.R.layout.simple_dropdown_item_1line, baseActivity.app.userList)
                        addFriendTextField.setAdapter(adapterUserEmailsUpdate)

                        //add User to friend's friends and update user on backend
                        friendAdded!!.friendIDs.add(baseActivity.app.user!!.id)
                        friendAdded!!.friends.add(baseActivity.app.user!!)
                        baseActivity.app.backend.createUserRecord(baseActivity.app.user!!)
                        baseActivity.app.backend.createUserRecord(friendAdded!!)

                        // Send a message alerting the added person that you have added them.
                        if (friendAdded!!.registrationToken != null) {
                            baseActivity.app.backend.sendAddedAsFriendMessage(
                                    friendAdded!!.registrationToken!!,
                                    baseActivity.app.user!!.id, baseActivity.app.user!!.name
                            )
                        }
                    } else {
                        Toast.makeText(baseActivity, "User email does not exist. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(baseActivity, "User email does not exist. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        //////////////////////////////////////////////////////////////////
        //                    Handling FAB                              //
        //////////////////////////////////////////////////////////////////
        createGroupButton.setOnClickListener {
            //find all items in the list view that have been "checked off"
            var selectedFriends : MutableList<User> = mutableListOf()
            for (position in 0 until friendList.count){
                if (friendList.getChildAt(position).checkBoxFriendListSelected.isChecked){
                    Log.i("FriendFragment", "Checked Friend is: " + friendList.getChildAt(position).textViewFriendListName.text)
                    selectedFriends.add(baseActivity.app.user!!.friends[position])
                }
            }
            //pass these items to a new layout inflater: where a request is made to name and create a group

            fun buildView(): LinearLayout {
                val groupCreator = LinearLayout(activity.applicationContext)
                groupCreator.orientation = LinearLayout.VERTICAL

                //load all selected friends to be visually appealing
                val adapterUserFriends = FriendListAdpater(activity.applicationContext, selectedFriends)
                var selectedFriends : ListView = ListView(activity.applicationContext)
                selectedFriends.adapter = adapterUserFriends

//                var cb : CheckBox
//                toppingCheckBoxes = Array(pizza_toppings.values().size) {idx ->
//                    cb = CheckBox(this)
//                    cb.setText(pizza_toppings.values()[idx].title)
//                    toppingList.addView(cb)
                groupCreator.addView(groupName)
                groupCreator.addView(selectedFriends)
                return groupCreator
            }

            val groupCreationDialogBuilder = AlertDialog.Builder(activity.applicationContext, R.style.Base_ThemeOverlay_AppCompat_Dialog_Alert)
            var dialogView = buildView()
            groupCreationDialogBuilder.setView(dialogView)
            val posClick = { dialog: DialogInterface, id: Int ->
                Toast.makeText(baseActivity, "Create group: " + groupName.text, Toast.LENGTH_SHORT).show()
            }
            val negClick = { dialog: DialogInterface, id: Int ->

            }
            groupCreationDialogBuilder.setPositiveButton("Create Group", posClick)
            groupCreationDialogBuilder.setNegativeButton("Cancel", negClick)

            groupCreationDialogBuilder.create().show()

        }
    }

    private fun initializeViews() {
        baseActivity = activity as BaseActivity
        friendList = baseActivity.findViewById(R.id.friendListView)
        createGroupButton = baseActivity.findViewById(R.id.fabCreateGroup)
        addFriendTextField = baseActivity.findViewById(R.id.autoCompleteTextView)
        addFriendButton = baseActivity.findViewById(R.id.addFriendBtn)
        groupName = TextView(baseActivity.applicationContext)
        groupName.hint = "Choose a name for your group..."
    }

    fun onShowFragment() {
        Log.i("FriendsFragment", "Fragment Selected")
        refreshData()
    }

    fun refreshData() {

    }

}// Required empty public constructor
