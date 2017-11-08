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

class DatabaseManager(context: Context?) {

    enum class RequestType(value: Int) {
        CREATE(1),
        DELETE(2),
        READ(3)
    }

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

    fun createUserRecord(user: User) {
        val json = JSONObject()
        json.put("requestType", RequestType.CREATE)
        json.put("content", buildJSONFromUser(user))
        sendPostRequest("url", json, {
            response: JSONObject? ->
            Log.i("DatabaseManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("DatabaseManager", "Error: " + error)
        })
    }

    fun deleteUserRecord(userID: String) {
        val json = JSONObject()
        json.put("requestType", RequestType.DELETE)
        json.put("content", userID)
        sendPostRequest("url", json, {
            response: JSONObject? ->
            Log.i("DatabaseManager", "Response: " + response)
        }, {
            error: VolleyError? ->
            Log.e("DatabaseManager", "Error: " + error)
        })
    }

    fun getUserRecord(userID: String, callback: (user: User?) -> Unit) {
        val json = JSONObject()
        json.put("requestType", RequestType.READ)
        json.put("content", userID)
        sendPostRequest("url", json, {
            response: JSONObject? ->
            if (response != null) {
                callback(buildUserFromJSON(response))
            } else {
                callback(null)
            }
        }, {
            error: VolleyError? ->
            Log.e("DatabaseManager", "Error: " + error)
            callback(null)
        })
    }

}