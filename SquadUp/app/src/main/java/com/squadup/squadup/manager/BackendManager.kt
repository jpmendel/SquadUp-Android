package com.squadup.squadup.manager

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.squadup.squadup.data.Constants
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.ServerData
import com.squadup.squadup.data.User
import com.squadup.squadup.service.FirebaseIDService
import com.squadup.squadup.service.FirebaseMessageService
import org.json.JSONArray
import org.json.JSONObject

/**
 * A manager class to handle any data being sent to or received by the Google App Engine backend.
 * Also handles sending of Firebase messages.
 */
class BackendManager(context: Context?) {

    // URL for the Firebase messaging server.
    private val MESSAGING_SERVER_URL = "https://fcm.googleapis.com/fcm/send"

    private var firestoreDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

    // A request queue to handle HTTP requests made by the app.
    private var httpRequestQueue: RequestQueue = Volley.newRequestQueue(context)

    // Sends a JSON post request to a server address.
    private fun sendPostRequest(address: String, data: JSONObject,
                                responseHandler: (response: JSONObject?) -> Unit,
                                errorHandler: (error: VolleyError?) -> Unit) {
        if (address == MESSAGING_SERVER_URL) {
            val postRequest = object : JsonObjectRequest(Request.Method.POST, address, data, responseHandler, errorHandler) {
                override fun getHeaders(): MutableMap<String, String> {
                    val params = mutableMapOf<String, String>()
                    params.put("Content-Type", "application/json")
                    params.put("Authorization", "key=" + ServerData.SERVER_KEY)
                    return params
                }
            }
            httpRequestQueue.add(postRequest)
        } else {
            val postRequest = JsonObjectRequest(Request.Method.POST, address, data, responseHandler, errorHandler)
            httpRequestQueue.add(postRequest)
        }
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

    // Send a message to a certain topic. Any users listening to that topic will receive the message.
    fun sendTextMessage(topic: String, senderID: String, senderName: String, text: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.TEXT)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("text", text)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

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
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

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
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun sendReadyRequestMessage(topic: String, senderID: String, senderName: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.READY_REQUEST)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun sendReadyResponseMessage(topic: String, senderID: String, senderName: String, receiverID: String, response: Boolean) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.READY_RESPONSE)
        data.put("senderID", senderID)
        data.put("senderName", senderName)
        data.put("receiverID", receiverID)
        data.put("response", response)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

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
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun sendNotification(topic: String, title: String, body: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)
        json.put("notification", notification)
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    private fun buildDocumentFromUser(user: User): MutableMap<String, Any> {
        val userData = mutableMapOf<String, Any>()
        userData["id"] = user.id
        userData["name"] = user.name
        userData["friends"] = user.friends.toList()
        userData["groupIDs"] = user.groupIDs.toList()
        return userData
    }

    private fun buildUserFromDocument(document: MutableMap<String, Any>): User {
        val id = document["id"] as String
        val name = document["name"] as String
        val user = User(id, name)
        val friends = document["friends"] as? List<String>
        if (friends != null) {
            for (i in 0 until friends.count()) {
                user.friends.add(friends[i])
            }
        }
        val groups = document["groupIDs"] as? List<String>
        if (groups != null) {
            for (i in 0 until groups.count()) {
                user.groupIDs.add(groups[i])
            }
        }
        return user
    }

    fun createUserRecord(user: User) {
        firestoreDatabase
                .collection("users")
                .document(user.id)
                .set(buildDocumentFromUser(user))
                .addOnCompleteListener {
                    task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Created User: " + user.id)
                    } else {
                        Log.e("BackendManager", "Failed To Create User: " + task.exception)
                    }
                }
    }

    fun deleteUserRecord(userID: String) {
        firestoreDatabase
                .collection("users")
                .document(userID)
                .delete()
                .addOnCompleteListener {
                    task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Deleted User: " + userID)
                    } else {
                        Log.e("BackendManager", "Failed To Delete User: " + task.exception)
                    }
                }
    }

    fun getUserRecord(userID: String, callback: (user: User?) -> Unit) {
        firestoreDatabase
                .collection("users")
                .document(userID)
                .get()
                .addOnCompleteListener{
                    task: Task<DocumentSnapshot> ->
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

    private fun buildDocumentFromGroup(group: Group): MutableMap<String, Any> {
        val groupData = mutableMapOf<String, Any>()
        groupData["id"] = group.id
        groupData["name"] = group.name
        groupData["members"] = group.memberIDs.toList()
        return groupData
    }

    private fun buildGroupFromDocument(document: MutableMap<String, Any>): Group {
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

    fun createGroupRecord(group: Group) {
        firestoreDatabase
                .collection("groups")
                .document(group.id)
                .set(buildDocumentFromGroup(group))
                .addOnCompleteListener {
                    task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Created Group: " + group.id)
                    } else {
                        Log.e("BackendManager", "Failed To Create Group: " + task.exception)
                    }
                }
    }

    fun deleteGroupRecord(groupID: String) {
        firestoreDatabase
                .collection("groups")
                .document(groupID)
                .delete()
                .addOnCompleteListener {
                    task: Task<Void> ->
                    if (task.isSuccessful) {
                        Log.i("BackendManager", "Successfully Deleted Group: " + groupID)
                    } else {
                        Log.e("BackendManager", "Failed To Delete Group: " + task.exception)
                    }
                }
    }

    fun getGroupRecord(groupID: String, callback: (group: Group?) -> Unit) {
        firestoreDatabase
                .collection("groups")
                .document(groupID)
                .get()
                .addOnCompleteListener{
                    task: Task<DocumentSnapshot> ->
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

}