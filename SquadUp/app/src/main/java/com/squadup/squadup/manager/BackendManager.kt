package com.squadup.squadup.manager

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.squadup.squadup.data.Constants
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

    // URL for the Google App Engine datastore.
    private val DATASTORE_SERVER_URL = "https://squadup-185416.appspot.com/"

    // URL for the Firebase messaging server.
    private val MESSAGING_SERVER_URL = "https://fcm.googleapis.com/fcm/send"

    // Data types stored in GAE.
    private val USER = 1
    private val GROUP = 2

    // Operations for data in GAE.
    private val CREATE = 1
    private val DELETE = 2
    private val READ = 3

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
    fun sendTextMessage(topic: String, sender: String, text: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.TEXT)
        data.put("sender", sender)
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

    fun sendLoginMessage(topic: String, sender: String, latitude: Double, longitude: Double) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.LOGIN)
        data.put("sender", sender)
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

    fun sendLocationMessage(topic: String, sender: String, latitude: Double, longitude: Double) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + topic)
        val data = JSONObject()
        data.put("type", FirebaseMessageService.LOCATION)
        data.put("sender", sender)
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

    // Build a JSON object from a User object.
    private fun buildJSONFromUser(user: User): JSONObject {
        val userObj = JSONObject()
        userObj.put("id", user.id)
        userObj.put("name", user.name)
        val friendArray = JSONArray()
        for (friend in user.friends) {
            friendArray.put(friend)
        }
        userObj.put("friends", friendArray)
        val groupArray = JSONArray()
        for (group in user.groups) {
            groupArray.put(group)
        }
        userObj.put("groups", user.groups)
        return userObj
    }

    // Build a User object from a JSON object.
    private fun buildUserFromJSON(json: JSONObject): User {
        val id = json.getString("id")
        val name = json.getString("name")
        val user = User(id, name)
        val friends = json.getJSONArray("friends")
        for (i in 0 until friends.length()) {
            user.friends.add(friends[i] as String)
        }
        val groups = json.getJSONArray("groups")
        for (i in 0 until groups.length()) {
            user.groups.add(groups[i] as String)
        }
        return user
    }

    // Create a record for a User in Google App Engine.
    fun createUserRecord(user: User) {
        val json = JSONObject()
        json.put("dataType", USER)
        json.put("requestType", CREATE)
        json.put("content", buildJSONFromUser(user))
        sendPostRequest(DATASTORE_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response?.getString("content"))
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Delete a record for a User in Google App Engine.
    fun deleteUserRecord(userID: String) {
        val json = JSONObject()
        json.put("dataType", USER)
        json.put("requestType", DELETE)
        json.put("content", userID)
        sendPostRequest(DATASTORE_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response?.getString("content"))
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    // Retrieve a record for a User in Google App Engine.
    fun getUserRecord(userID: String, callback: (user: User?) -> Unit) {
        val json = JSONObject()
        json.put("dataType", USER)
        json.put("requestType", READ)
        json.put("content", userID)
        sendPostRequest(DATASTORE_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
            if (response != null) {
                callback(buildUserFromJSON(response))
            } else {
                callback(null)
            }
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
            callback(null)
        })
    }

}