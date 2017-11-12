package com.squadup.squadup.manager

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import com.squadup.squadup.data.ServerData
import com.squadup.squadup.data.User
import com.squadup.squadup.service.FirebaseIDService
import com.squadup.squadup.service.FirebaseMessageService
import org.json.JSONArray
import org.json.JSONObject

class BackendManager(context: Context?) {

    private val DATASTORE_SERVER_URL = "https://squadup-185416.appspot.com/"
    private val MESSAGING_SERVER_URL = "https://fcm.googleapis.com/fcm/send"

    private val USER = 1
    private val GROUP = 2

    private val CREATE = 1
    private val DELETE = 2
    private val READ = 3

    private var httpRequestQueue: RequestQueue = Volley.newRequestQueue(context)

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

    fun startListening(channel: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(channel)
        Log.i("BackendManager", "Started listening to: " + channel)
    }

    fun stopListening(channel: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(channel)
        Log.i("BackendManager", "Stopped listening to: " + channel)
    }

    fun sendMessage(channel: String, message: String) {
        val json = JSONObject()
        json.put("token", FirebaseIDService.getToken())
        json.put("to", "/topics/" + channel)
        val data = JSONObject()
        data.put("text", message)
        json.put("data", data)
        sendPostRequest(MESSAGING_SERVER_URL, json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

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