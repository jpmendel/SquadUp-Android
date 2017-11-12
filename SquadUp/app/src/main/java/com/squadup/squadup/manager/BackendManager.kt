package com.squadup.squadup.manager

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squadup.squadup.data.Group
import com.squadup.squadup.data.User
import org.json.JSONArray
import org.json.JSONObject

class BackendManager(context: Context?) {

    private val SERVER_URL = "https://squadup-185416.appspot.com"

    private val CREATE = 1
    private val DELETE = 2
    private val READ = 3
    private val PUBLISH = 4
    private val SUBSCRIBE = 5

    private var httpRequestQueue: RequestQueue = Volley.newRequestQueue(context)

    private fun sendPostRequest(address: String, data: JSONObject,
                                responseHandler: (response: JSONObject?) -> Unit,
                                errorHandler: (error: VolleyError?) -> Unit) {
        val postRequest = JsonObjectRequest(Request.Method.POST, address, data, responseHandler, errorHandler)
        httpRequestQueue.add(postRequest)
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

    fun createChannel(channel: String) {
        val json = JSONObject()
        json.put("requestType", CREATE)
        json.put("content", channel)
        sendPostRequest(SERVER_URL + "/messages", json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun deleteChannel(channel: String) {
        val json = JSONObject()
        json.put("requestType", DELETE)
        json.put("content", channel)
        sendPostRequest(SERVER_URL + "/messages", json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun publishMessage(channel: String, message: String) {
        val json = JSONObject()
        json.put("requestType", PUBLISH)
        val content = JSONObject()
        content.put("channel", channel)
        content.put("message", message)
        json.put("content", content)
        sendPostRequest(SERVER_URL + "/messages", json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun listenForMessage(channel: String, timeout: Float, callback: (message: String?) -> Unit) {
        val json = JSONObject()
        json.put("requestType", SUBSCRIBE)
        val content = JSONObject()
        content.put("channel", channel)
        content.put("timeout", timeout)
        json.put("content", content)
        sendPostRequest(SERVER_URL + "/messages", json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
            if (response != null) {
                callback(response.getString("message"))
            } else {
                callback(null)
            }
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
            callback(null)
        })
    }

    fun createUserRecord(user: User) {
        val json = JSONObject()
        json.put("requestType", CREATE)
        json.put("content", buildJSONFromUser(user))
        sendPostRequest(SERVER_URL + "/users", json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun deleteUserRecord(userID: String) {
        val json = JSONObject()
        json.put("requestType", DELETE)
        json.put("content", userID)
        sendPostRequest(SERVER_URL + "/users", json, {
            response: JSONObject? ->
            Log.i("BackendManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("BackendManager", "Error: " + error)
        })
    }

    fun getUserRecord(userID: String, callback: (user: User?) -> Unit) {
        val json = JSONObject()
        json.put("requestType", READ)
        json.put("content", userID)
        sendPostRequest(SERVER_URL + "/users", json, {
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