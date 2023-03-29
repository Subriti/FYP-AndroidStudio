package com.example.notificationpermissions.Services

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Models.BlockList
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject


object BlockService {
    val userBlockList = ArrayList<BlockList>()
    val blockedList= ArrayList<BlockList>()

    fun blockUser(
        blocked_user: String, blocked_by: String, complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        //bc it takes object of UserId
        val userId = JSONObject()
        userId.put("user_id", blocked_user)

        val blockedBy= JSONObject()
        blockedBy.put("user_id",blocked_by)

        jsonBody.put("blocked_user_id",userId)
        jsonBody.put("blocked_by_id",blockedBy)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val blockUser = object : JsonObjectRequest(
            Method.POST,
            URL_BLOCK_USER,
            null,
            Response.Listener { response ->
                println("Block User Response " + response)
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not block user: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(blockUser)
    }

    fun unblockUser(
        blocked_user: String, blocked_by: String, complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        //bc it takes object of UserId
        val userId = JSONObject()
        userId.put("user_id", blocked_user)

        val blockedBy= JSONObject()
        blockedBy.put("user_id",blocked_by)

        jsonBody.put("blocked_user_id",userId)
        jsonBody.put("blocked_by_id",blockedBy)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val unblockUser = object : JsonObjectRequest(
            Method.POST,
            URL_UNBLOCK_USER,
            null,
            Response.Listener { response ->
                println("Unblock User Response " + response)
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not unblock user: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(unblockUser)
    }

    /*fun unblockUser(
        block_id:String, complete: (Boolean) -> Unit
    ) {
        val unblockUser = object : JsonObjectRequest(
            Method.DELETE,
            URL_UNBLOCK_USER,
            null,
            Response.Listener { response ->
                println("Unblock User Response " + response)
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not unblock user: $error")
                complete(false)
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(unblockUser)
    }*/

    fun getUserBlockList(complete: (Boolean) -> Unit) {
        userBlockList.clear()
        val getUserBlockList = object : JsonArrayRequest(Method.GET,
            "$URL_SHOW_USER_BLOCK_LIST${App.sharedPrefs.userID}",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                    println(response)
                try {
                    for (x in 0 until response.length()) {
                        val blockList = response.getJSONObject(x)
                        val blockId = blockList.getString("block_id")
                        val blockedUser = blockList.getString("blocked_user_id")
                        val blockedBy = blockList.getString("blocked_by_id")

                        val newBlockList = BlockList( blockId, blockedUser, blockedBy)
                        userBlockList.add(newBlockList)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    println("JSON EXC: ${e.localizedMessage}")
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve block list: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }
        getUserBlockList.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getUserBlockList)
    }

    fun getBlockedList(complete: (Boolean) -> Unit) {
        blockedList.clear()
        val getBlockedList = object : JsonArrayRequest(Method.GET,
            "$URL_SHOW_BLOCKED_LIST${App.sharedPrefs.userID}",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                println(response)
                try {
                    for (x in 0 until response.length()) {
                        val blockList = response.getJSONObject(x)
                        val blockId = blockList.getString("block_id")
                        val blockedUser = blockList.getString("blocked_user_id")
                        val blockedBy = blockList.getString("blocked_by_id")

                        val newBlockList = BlockList( blockId, blockedUser, blockedBy)
                        blockedList.add(newBlockList)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    println("JSON EXC: ${e.localizedMessage}")
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve blocked list: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }
        getBlockedList.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getBlockedList)
    }
}