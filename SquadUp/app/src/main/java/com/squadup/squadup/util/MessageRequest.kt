package com.squadup.squadup.util

import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.squadup.squadup.manager.BackendManager
import org.json.JSONObject

class MessageRequest(
        method: Int,
        address: String,
        data: JSONObject,
        responseHandler: (response: JSONObject?) -> Unit,
        errorHandler: (error: VolleyError?) -> Unit)
    : JsonObjectRequest(method, address, data, responseHandler, errorHandler) {

    override fun getHeaders(): MutableMap<String, String> {
        val params = mutableMapOf<String, String>()
        params.put("Content-Type", "application/json")
        params.put("Authorization", "key=" + BackendManager.API_KEY)
        return params
    }

}