package com.squadup.squadup.activity

/**
 * Created by StephenHaberle on 11/27/17.
 */

import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.squadup.squadup.R
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.utilities.DialogSelectedFriendsAdapter
import com.squadup.squadup.utilities.FriendListAdapter
import kotlinx.android.synthetic.main.row_friend.view.*
import java.util.*

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
        fun newInstance(): FriendsFragment {
            return FriendsFragment()
        }
    }

    private lateinit var baseActivity: BaseActivity

    private lateinit var friendList: ListView

    private lateinit var friendListAdapter: FriendListAdapter

    private lateinit var addFriendTextField: AutoCompleteTextView

    private lateinit var addFriendButton: Button

    private lateinit var createGroupButton: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViews()
        setupButtons()
        setupAutoCompleteTextField()
        setupFriendList()
    }

    private fun initializeViews() {
        baseActivity = activity as BaseActivity
        friendList = baseActivity.findViewById(R.id.friendListView)
        createGroupButton = baseActivity.findViewById(R.id.fabCreateGroup)
        addFriendTextField = baseActivity.findViewById(R.id.autoCompleteTextView)
        addFriendButton = baseActivity.findViewById(R.id.addFriendBtn)
    }

    private fun setupButtons() {
        addFriendButton.setOnClickListener {
            onAddFriendButtonClick()
        }
        createGroupButton.setOnClickListener {
            onCreateGroupButtonClick()
        }
    }

    private fun setupAutoCompleteTextField() {
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
    }

    private fun setupFriendList() {
        Log.i("FriendFragment", "User friends: " + baseActivity.app.user!!.friends)
        friendListAdapter = FriendListAdapter(baseActivity, this, baseActivity.app.user!!.friends)
        friendList.adapter = friendListAdapter
    }

    private fun onAddFriendButtonClick() {
        val friendEmail = addFriendTextField.text.toString()
        Log.i("FriendFragment", friendEmail)
        //check if selected friend exists
        if (friendEmail.isNotEmpty()) {
            baseActivity.app.backend.getUserRecord(friendEmail) {
                user: User? ->
                if (user != null) {
                    baseActivity.app.backend.addFriend(baseActivity.app.user!!, user)
                    refreshData()

                    //clear the name from the autoCompleteTextView
                    addFriendTextField.text.clear()

                    //remove the name from autoCompleteTextViewAdapter and notify it
                    baseActivity.app.userList.remove(friendEmail)
                    val adapterUserEmailsUpdate = ArrayAdapter<String>(baseActivity,
                            android.R.layout.simple_dropdown_item_1line, baseActivity.app.userList)
                    addFriendTextField.setAdapter(adapterUserEmailsUpdate)

                    Log.i("FriendsFragment", "Jason: " + user.registrationToken)
                    // Send a message alerting the added person that you have added them.
                    if (user.registrationToken != null) {
                        baseActivity.app.backend.sendAddedAsFriendMessage(
                                user.registrationToken!!,
                                baseActivity.app.user!!.id, baseActivity.app.user!!.name
                        )
                    }
                } else {
                    Toast.makeText(baseActivity, "User does not exist. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(baseActivity, "User does not exist. Try again.", Toast.LENGTH_SHORT).show()
        }
        baseActivity.hideKeyboard()
    }

    private fun onCreateGroupButtonClick() {
        val inputContainer = LinearLayout(baseActivity)
        inputContainer.orientation = LinearLayout.VERTICAL
        val groupNameInput = EditText(baseActivity)
        groupNameInput.hint = "Enter group name"
        groupNameInput.setSingleLine()
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        groupNameInput.layoutParams = params
        val selectedFriends = mutableListOf(baseActivity.app.user!!)
        for (friend in baseActivity.app.user!!.friends) {
            if (friend.selected) {
                selectedFriends.add(friend)
            }
        }
        val memberList = ListView(baseActivity)
        memberList.adapter = DialogSelectedFriendsAdapter(baseActivity, selectedFriends)
        inputContainer.addView(memberList)
        inputContainer.addView(groupNameInput)
        AlertDialog.Builder(baseActivity)
                .setTitle("Squad Up")
                .setMessage("Enter the name for the group:")
                .setView(inputContainer)
                .setPositiveButton("OK", {
                    _: DialogInterface, _: Int ->
                    Toast.makeText(baseActivity, "Create group: " + groupNameInput.text, Toast.LENGTH_SHORT).show()
                    //create the new group, add the userIDs, and send it to the backend
                    val groupID = groupNameInput.toString() + "-" + UUID.randomUUID().toString()
                    val createdGroup = Group(groupID, groupNameInput.text.toString())
                    //add members to group
                    createdGroup.memberIDs.add(baseActivity.app.user!!.id)
                    createdGroup.members.add(baseActivity.app.user!!)

                    //add group to user
                    baseActivity.app.user!!.groupIDs.add(createdGroup.id)
                    baseActivity.app.user!!.groups.add(createdGroup)

                    //add group to selected users
                    for (friend in selectedFriends){
                        createdGroup.memberIDs.add(friend.id)
                        createdGroup.members.add(friend)
                        friend.groupIDs.add(createdGroup.id)
                        friend.groups.add(createdGroup)
                    }

                    //update the backend for all users and the group involved
                    baseActivity.app.backend.createGroupRecord(createdGroup)
                    baseActivity.app.backend.createUserRecord(baseActivity.app.user!!)
                    val recipients = mutableListOf<String>()
                    for (friend in selectedFriends){
                        baseActivity.app.backend.createUserRecord(friend)
                        if (friend != baseActivity.app.user) {
                            if (friend.registrationToken != null) {
                                recipients.add(friend.registrationToken!!)
                            }
                        }
                    }

                    baseActivity.app.backend.sendAddedToGroupMessage(
                            recipients,
                            baseActivity.app.user!!.id, baseActivity.app.user!!.name,
                            createdGroup.id, createdGroup.name
                    )
                    //that's it for now, then work on transition to GroupFragment?
                })
                .setNegativeButton("Cancel", {
                    _: DialogInterface, _: Int ->
                })
                .show()
    }

    fun refreshData() {
        friendListAdapter.updateDataSet(baseActivity.app.user!!.friends)
    }

    fun selectFriend(friend: User) {
        friend.selected = !friend.selected
    }

    fun removeFriend(friend: User) {
        val user = baseActivity.app.user
        if (user != null) {
            AlertDialog.Builder(baseActivity)
                    .setTitle("Squad Up")
                    .setMessage("Delete this friend?")
                    .setPositiveButton("Yes", {
                        dialogInterface: DialogInterface?, i: Int ->
                        baseActivity.app.backend.unfriend(user, friend)
                        baseActivity.app.userList.add(friend.id)
                        refreshData()
                    })
                    .setNegativeButton("No", {
                        dialogInterface: DialogInterface?, i: Int ->
                    })
                    .show()
        }
    }

    fun onShowFragment() {
        for (friend in baseActivity.app.user!!.friends) {
            friend.selected = false
        }
        refreshData()
    }

}
