package com.squadup.squadup.activity

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squadup.squadup.R
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import com.squadup.squadup.utilities.DialogSelectedFriendsAdapter
import com.squadup.squadup.utilities.FriendListAdapter

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

    private lateinit var createGroupButton: Button

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
        createGroupButton = baseActivity.findViewById(R.id.createGroupButton)
        addFriendTextField = baseActivity.findViewById(R.id.autoCompleteTextView)
        addFriendButton = baseActivity.findViewById(R.id.addFriendBtn)
    }

    private fun setupButtons() {
        addFriendButton.setOnClickListener {
            onAddFriendButtonClick()
        }
        createGroupButton.setOnClickListener {
            if (isAnyFriendSelected()) {
                onCreateGroupButtonClick()
            }
        }
        addFriendTextField.setOnFocusChangeListener {
            view: View, focus: Boolean ->
            if (focus) {
                refreshAutoCompleteTextField()
            }
        }
    }

    private fun setupAutoCompleteTextField() {
        refreshAutoCompleteTextField()
        addFriendTextField.maxLines = 1
        addFriendTextField.threshold = 1
    }

    private fun refreshAutoCompleteTextField() {
        val addableUsers = mutableListOf<String>()
        for (user in baseActivity.app.userList) {
            if (user != baseActivity.app.user!!.id) {
                if (!baseActivity.app.user!!.friendIDs.contains(user)) {
                    addableUsers.add(user)
                }
            }
        }
        val adapterUserEmails = ArrayAdapter<String>(baseActivity,
                android.R.layout.simple_dropdown_item_1line, addableUsers)
        addFriendTextField.setAdapter(adapterUserEmails)
    }

    private fun setupFriendList() {
        friendListAdapter = FriendListAdapter(baseActivity, this, baseActivity.app.user!!.friends)
        friendList.adapter = friendListAdapter
    }

    private fun onAddFriendButtonClick() {
        val friendEmail = addFriendTextField.text.toString()
        //check if selected friend exists
        if (friendEmail.isNotEmpty()) {
            baseActivity.app.backend.getUserRecord(friendEmail) {
                user: User? ->
                if (user != null) {
                    baseActivity.app.backend.addFriend(baseActivity.app.user!!, user)
                    refreshData()

                    // Send a message alerting the added person that you have added them.
                    if (user.registrationToken != null) {
                        baseActivity.app.backend.sendAddedAsFriendMessage(
                                user.registrationToken!!,
                                baseActivity.app.user!!.id, baseActivity.app.user!!.name
                        )
                    }
                } else {
                    Toast.makeText(baseActivity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(baseActivity, "Enter a user email", Toast.LENGTH_SHORT).show()
        }
        addFriendTextField.text.clear()
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
                    dialog, i ->
                    if (groupNameInput.text.length > 1) {
                        Toast.makeText(baseActivity, "Created group: ${groupNameInput.text}", Toast.LENGTH_SHORT).show()
                        //create the new group, add the userIDs, and send it to the backend
                        val createdGroup = Group(groupNameInput.text.toString())

                        //add group to selected users
                        for (friend in selectedFriends) {
                            createdGroup.memberIDs.add(friend.id)
                            createdGroup.members.add(friend)
                            friend.groupIDs.add(createdGroup.id)
                            friend.groups.add(createdGroup)
                        }

                        //update the backend for all users and the group involved
                        baseActivity.app.backend.createGroupRecord(createdGroup)

                        val recipients = mutableListOf<String>()
                        for (friend in selectedFriends) {
                            baseActivity.app.backend.createUserRecord(friend)
                            if (friend != baseActivity.app.user) {
                                if (friend.registrationToken != null) {
                                    recipients.add(friend.registrationToken!!)
                                }
                            }
                        }

                        for (recipient in recipients) {
                            baseActivity.app.backend.sendAddedToGroupMessage(
                                    recipient,
                                    baseActivity.app.user!!.id, baseActivity.app.user!!.name,
                                    createdGroup.id, createdGroup.name
                            )
                        }

                        deselectAllFriends()
                        refreshData()
                    } else {
                        Toast.makeText(baseActivity, "Enter a name of at least 2 characters", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton("Cancel", {
                    dialog, i ->
                })
                .show()
    }

    fun refreshData() {
        setupFriendList()
        friendListAdapter.notifyDataSetChanged()
    }

    fun selectFriend(friend: User) {
        friend.selected = !friend.selected
        updateCreateGroupButtonColor()
    }

    private fun deselectAllFriends() {
        for (friend in baseActivity.app.user!!.friends) {
            friend.selected = false
        }
        createGroupButton.setBackgroundResource(R.drawable.shape_round_button_gray)
    }

    private fun isAnyFriendSelected(): Boolean {
        return baseActivity.app.user!!.friends.any { it.selected }
    }

    private fun updateCreateGroupButtonColor() {
        if (isAnyFriendSelected()) {
            createGroupButton.setBackgroundResource(R.drawable.shape_round_button_blue)
        } else {
            createGroupButton.setBackgroundResource(R.drawable.shape_round_button_gray)
        }
    }

    fun removeFriend(friend: User) {
        val user = baseActivity.app.user
        if (user != null) {
            AlertDialog.Builder(baseActivity)
                    .setTitle("Squad Up")
                    .setMessage("Remove this friend?")
                    .setPositiveButton("Yes", {
                        dialog, i ->
                        baseActivity.app.backend.unfriend(user, friend)
                        refreshData()
                        if (friend.registrationToken != null) {
                            baseActivity.app.backend.sendRemovedAsFriendMessage(
                                    friend.registrationToken!!, user.id, user.name
                            )
                        }
                    })
                    .setNegativeButton("No", {
                        dialog, i ->
                    })
                    .show()
        }
    }

    fun onShowFragment() {
        deselectAllFriends()
        refreshData()
    }

}
