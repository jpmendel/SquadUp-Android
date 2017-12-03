package com.squadup.squadup.manager

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.ServerData
import com.squadup.squadup.data.User
import com.squadup.squadup.service.FirebaseIDService
import com.squadup.squadup.service.FirebaseMessageService
import org.json.JSONArray
import org.json.JSONObject

/**
 * A manager class to handle any data being sent to or received by the Cloud Firestore backend.
 * Also handles sending of Firebase Cloud messages.
 */
class BackendManager(context: Context?) {

    // URL for the Firebase messaging server.
    private val MESSAGING_SERVER_URL = "https://fcm.googleapis.com/fcm/send"

    // The name for the collection of user records in the Firestore database.
    private val USER_COLLECTION = "users"

    // The name for the collection of group records in the Firestore database.
    private val GROUP_COLLECTION = "groups"

    // The name for the collection/document of the list of all users in the Firestore database.
    private val USER_LIST = "user_list"

    // The reference for the Firestore database.
    private var firestoreDatabase = FirebaseFirestore.getInstance()

    // A request queue to handle HTTP requests made by the app.
    private var httpRequestQueue = Volley.newRequestQueue(context)

    // Sends a JSON post request to a server address.
    private fun sendPostRequest(address: String, data: JSONObject,
                                responseHandler: (response: JSONObject?) -> Unit,
                                errorHandler: (error: VolleyError?) -> Unit) {
        val postRequest: JsonObjectRequest
        if (address == MESSAGING_SERVER_URL) {
            postRequest = object : JsonObjectRequest(Request.Method.POST, address, data, responseHandler, errorHandler) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params = mutableMapOf<String, String>()
                    params.put("Content-Type", "application/json")
                    params.put("Authorization", "key=" + ServerData.SERVER_KEY)
                    return params
                }
            }
        } else {
            postRequest = JsonObjectRequest(Request.Method.POST, address, data, responseHandler, errorHandler)
        }
        httpRequestQueue.add(postRequest)
    }

    // Start listening to a topic. Any messages sent to the same topic will be received.
    fun startListening(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
        Log.i("BackendManager", "Started listening to: " + topic)
    }

    // Stop listening to a topic. Any messages sent to the same topic will no longer be received.
    fun stopListening(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        Log.i("BackendManager", "Stopped listening to: " + topic)
    }

    // Sends a login message to users subscribed to a certain topic.
    fun sendLoginMessage(topic: String, senderID: String, senderName: String, latitude: Double, longitude: Double) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.LOGIN)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("latitude", latitude)
        data.put("longitude", longitude)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a location message to users subscribed to a certain topic.
    fun sendLocationMessage(topic: String, senderID: String, senderName: String, latitude: Double, longitude: Double) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.LOCATION)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("latitude", latitude)
        data.put("longitude", longitude)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a ready request message to users subscribed to a certain topic.
    fun sendReadyRequestMessage(topic: String, senderID: String, senderName: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.READY_REQUEST)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a ready response message to users subscribed to a certain topic.
    fun sendReadyResponseMessage(topic: String, senderID: String, senderName: String, receiverID: String, res: Boolean) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.READY_RESPONSE)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("receiverID", receiverID)
        data.put("response", res)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a ready decision message to users subscribed to a certain topic.
    fun sendReadyDecisionMessage(topic: String, senderID: String, senderName: String, decision: Boolean) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.READY_RESPONSE)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("response", decision)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a message letting a user know you have added them as a friend.
    fun sendAddedAsFriendMessage(recipient: String, senderID: String, senderName: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", recipient)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.ADDED_AS_FRIEND)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a message letting a user know you have added them as a friend.
    fun sendRemovedAsFriendMessage(recipient: String, senderID: String, senderName: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", recipient)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.REMOVED_AS_FRIEND)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a message letting the members of a group know you created the group.
    fun sendAddedToGroupMessage(recipient: String, senderID: String, senderName: String, groupID: String, groupName: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", recipient)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.ADDED_TO_GROUP)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("groupID", groupID)
        data.put("groupName", groupName)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Sends a push notification to users subscribed to a certain topic.
    fun sendNotification(recipient: String, title: String, body: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", recipient)
        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)
        json.put("notification", notification)
        sendPostRequest(MESSAGING_SERVER_URL, json, { response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, { error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Builds a Firestore document from a user object.
    private fun buildDocumentFromUser(user: User): MutableMap<String, Any?> {
        val userData = mutableMapOf<String, Any?>()
        userData["id"] = user.id
        userData["name"] = user.name
        userData["friendIDs"] = user.friendIDs.toList()
        userData["groupIDs"] = user.groupIDs.toList()
        userData["registrationToken"] = user.registrationToken
        return userData
    }

    // Builds a user object from a Firestore document.
    private fun buildUserFromDocument(document: MutableMap<String, Any?>): User {
        val id = document["id"] as String
        val name = document["name"] as String
        val user = User(id, name)
        val friends = document["friendIDs"] as? List<String>
        if (friends != null) {
            for (i in 0 until friends.count()) {
                user.friendIDs.add(friends[i])
            }
        }
        val groups = document["groupIDs"] as? List<String>
        if (groups != null) {
            for (i in 0 until groups.count()) {
                user.groupIDs.add(groups[i])
            }
        }
        user.registrationToken = document["registrationToken"] as String?
        return user
    }

    // Creates or updates a user record in the Firestore database backend.
    fun createUserRecord(user: User, callback: (() -> Unit)? = null) {
        firestoreDatabase
                .collection(USER_COLLECTION)
                .document(user.id)
                .set(buildDocumentFromUser(user))
                .addOnCompleteListener { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Created User: " + user.id)
                    } else {
                        Log.e("BackendManager", "Failed to Create User: " + task.exception)
                    }
                    if (callback != null) {
                        callback()
                    }
                }
    }

    // Deletes a user record in the Firestore database backend.
    fun deleteUserRecord(userID: String, callback: (() -> Unit)? = null) {
        firestoreDatabase
                .collection(USER_COLLECTION)
                .document(userID)
                .delete()
                .addOnCompleteListener { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Deleted User: " + userID)
                    } else {
                        Log.e("BackendManager", "Failed to Delete User: " + task.exception)
                    }
                    if (callback != null) {
                        callback()
                    }
                }
    }

    // Retrieves a user record from the Firestore database backend.
    fun getUserRecord(userID: String, callback: (user: User?) -> Unit) {
        firestoreDatabase
                .collection(USER_COLLECTION)
                .document(userID)
                .get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            callback(buildUserFromDocument(document.data))
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                }
    }

    // Builds a Firestore document from a group object.
    private fun buildDocumentFromGroup(group: Group): MutableMap<String, Any?> {
        val groupData = mutableMapOf<String, Any?>()
        groupData["id"] = group.id
        groupData["name"] = group.name
        groupData["members"] = group.memberIDs.toList()
        return groupData
    }

    // Builds a group object from a Firestore document.
    private fun buildGroupFromDocument(document: MutableMap<String, Any?>): Group {
        val id = document["id"] as String
        val name = document["name"] as String
        val group = Group(id, name)
        val members = document["members"] as? List<String>
        if (members != null) {
            for (i in 0 until members.count()) {
                group.memberIDs.add(members[i])
            }
        }
        return group
    }

    // Creates or updates a user record in the Firestore database backend.
    fun createGroupRecord(group: Group, callback: (() -> Unit)? = null) {
        firestoreDatabase
                .collection(GROUP_COLLECTION)
                .document(group.id)
                .set(buildDocumentFromGroup(group))
                .addOnCompleteListener { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Created Group: " + group.id)
                    } else {
                        Log.e("BackendManager", "Failed to Create Group: " + task.exception)
                    }
                    if (callback != null) {
                        callback()
                    }
                }
    }

    // Deletes a user record in the Firestore database backend.
    fun deleteGroupRecord(groupID: String, callback: (() -> Unit)? = null) {
        firestoreDatabase
                .collection(GROUP_COLLECTION)
                .document(groupID)
                .delete()
                .addOnCompleteListener { task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Deleted Group: " + groupID)
                    } else {
                        Log.e("BackendManager", "Failed to Delete Group: " + task.exception)
                    }
                    if (callback != null) {
                        callback()
                    }
                }
    }

    // Retrieves a user record from the Firestore database backend.
    fun getGroupRecord(groupID: String, callback: (group: Group?) -> Unit) {
        firestoreDatabase
                .collection(GROUP_COLLECTION)
                .document(groupID)
                .get()
                .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            callback(buildGroupFromDocument(document.data))
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                }
    }

    // Pass in a given Group with only the list of the member IDs and get data for each member.
    fun getMemberInfoForGroup(group: Group, callback: ((group: Group?) -> Unit)? = null) {
        group.members = mutableListOf()
        for (i in 0 until group.memberIDs.count()) {
            getUserRecord(group.memberIDs[i]) {
                user: User? ->
                if (user != null) {
                    group.members.add(user)
                }
                if (i == group.memberIDs.count() - 1) {
                    if (callback != null) {
                        callback(group)
                    }
                }
            }
        }
    }

    // Pass in a given User object and update the user's "groups" and "friends" variable.
    fun getFriendDataForUser(updateUser: User, callback: (() -> Unit)? = null) {
        Log.i("BackendManager", "Friend Count: " + updateUser.friendIDs.count())
        updateUser.friends = mutableListOf()
        for (i in 0 until updateUser.friendIDs.count()) {
            getUserRecord(updateUser.friendIDs[i]) {
                user: User? ->
                if (user != null){
                    Log.i("BackendManager", "Adding Friend: " + user.id)
                    updateUser.friends.add(user)
                }
                if (i == updateUser.friendIDs.count() - 1) {
                    if (callback != null) {
                        callback()
                    }
                }
            }
        }
        if (updateUser.friendIDs.count() == 0) {
            if (callback != null) {
                callback()
            }
        }
    }

    fun getGroupDataForUser(updateUser: User, callback: (() -> Unit)? = null) {
        Log.i("BackendManager", "Group Count: " + updateUser.groupIDs.count())
        updateUser.groups = mutableListOf()
        for (i in 0 until updateUser.groupIDs.count()) {
            getGroupRecord(updateUser.groupIDs[i]) {
                group: Group? ->
                if (group != null) {
                    Log.i("BackendManager", "Adding Group: " + group.id)
                    updateUser.groups.add(group)
                }
                if (i == updateUser.groupIDs.count() - 1) {
                    if (callback != null) {
                        callback()
                    }
                }
            }
        }
        if (updateUser.groupIDs.count() == 0) {
            if (callback != null) {
                callback()
            }
        }
    }

    // Retrieves the list of all user IDs for the app.
    fun getUserList(callback: (userList: MutableList<String>) -> Unit) {
        firestoreDatabase
                .collection(USER_LIST)
                .document(USER_LIST)
                .get()
                .addOnCompleteListener{
                    task: Task<DocumentSnapshot> ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            val userList = document.data["user_list"] as? List<String>
                            if (userList != null) {
                                callback(userList.toMutableList())
                            } else {
                                callback(mutableListOf())
                            }
                        } else {
                            callback(mutableListOf())
                        }
                    } else {
                        callback(mutableListOf())
                    }
                }
    }

    // Adds a user ID to the list of all user IDs for the app.
    fun addUserToUserList(userID: String) {
        getUserList {
            userList: MutableList<String> ->
            if (!userList.contains(userID)) {
                userList.add(userID)
                val listData = mutableMapOf<String, Any?>()
                listData["user_list"] = userList.toList()
                firestoreDatabase
                        .collection(USER_LIST)
                        .document(USER_LIST)
                        .set(listData)
                        .addOnCompleteListener { task: Task<Void> ->
                            if (task.isSuccessful) {
                                Log.i("BackendManager", "Successfully Added User to List: " + userID)
                            } else {
                                Log.e("BackendManager", "Failed to Add User to List: " + task.exception)
                            }
                        }
            }
        }
    }

    fun addFriend(user1: User, user2: User) {
        if (user1.friendIDs.contains(user2.id) || user2.friendIDs.contains(user1.id)) {
            return
        }
        user1.friendIDs.add(user2.id)
        user1.friends.add(user2)
        user2.friendIDs.add(user1.id)
        user2.friends.add(user1)
        createUserRecord(user1)
        createUserRecord(user2)
    }

    fun unfriend(user1: User, user2: User) {
        if (!user1.friendIDs.contains(user2.id) && !user2.friendIDs.contains(user1.id)) {
            return
        }
        user1.friendIDs.remove(user2.id)
        user1.friends.remove(user2)
        user2.friendIDs.remove(user1.id)
        user2.friends.remove(user1)
        createUserRecord(user1)
        createUserRecord(user2)
    }

}