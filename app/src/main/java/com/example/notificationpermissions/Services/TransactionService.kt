package com.example.notificationpermissions.Services

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.example.notificationpermissions.Models.Transaction
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.URL_GET_GIVEN_DONATIONS
import com.example.notificationpermissions.Utilities.URL_GET_ONGOING_TRANSACTIONS
import com.example.notificationpermissions.Utilities.URL_GET_RECIEVED_DONATIONS
import org.json.JSONException


object TransactionService {
    val givenTransaction = ArrayList<Transaction>()
    val recievedTransactions = ArrayList<Transaction>()
    val onGoingTransactions= ArrayList<Transaction>()

    fun findGiven(complete: (Boolean) -> Unit) {
        givenTransaction.clear()
        val getGivenDonations = object : JsonArrayRequest(Method.GET,
            "$URL_GET_GIVEN_DONATIONS${App.sharedPrefs.userID}",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val transaction = response.getJSONObject(x)
                        val transactionId = transaction.getString("transaction_id")
                        val recieverUserId = transaction.getString("reciever_user_id")
                        val postId = transaction.getString("post_id")
                        val rating = transaction.getString("rating")
                        val transactionDate = transaction.getString("transaction_date")

                        val newTransaction = Transaction(
                            transactionId, rating, transactionDate, postId, recieverUserId
                        )
                        givenTransaction.add(newTransaction)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve given transactions: $error")
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
        getGivenDonations.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getGivenDonations)
    }

    fun findRecieved(complete: (Boolean) -> Unit) {
        recievedTransactions.clear()
        val getRecievedDonations = object : JsonArrayRequest(Method.GET,
            "$URL_GET_RECIEVED_DONATIONS${App.sharedPrefs.userID}",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val transaction = response.getJSONObject(x)
                        val transactionId = transaction.getString("transaction_id")
                        val recieverUserId = transaction.getString("reciever_user_id")
                        val postId = transaction.getString("post_id")
                        val rating = transaction.getString("rating")
                        val transactionDate = transaction.getString("transaction_date")

                        val newTransaction = Transaction(
                            transactionId, rating, transactionDate, postId, recieverUserId
                        )
                        recievedTransactions.add(newTransaction)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve Recieved transactions: $error")
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
        getRecievedDonations.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getRecievedDonations)
    }

    fun findOngoingTransactions(complete: (Boolean) -> Unit) {
        onGoingTransactions.clear()
        val getOngoingTransactions = object : JsonArrayRequest(Method.GET,
            "$URL_GET_ONGOING_TRANSACTIONS${App.sharedPrefs.userID}",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val transaction = response.getJSONObject(x)
                        val transactionId = transaction.getString("transaction_id")
                        val recieverUserId = transaction.getString("reciever_user_id")
                        val postId = transaction.getString("post_id")
                        val rating = transaction.getString("rating")
                        val transactionDate = transaction.getString("transaction_date")

                        val newTransaction = Transaction(
                            transactionId, rating, transactionDate, postId, recieverUserId
                        )
                        onGoingTransactions.add(newTransaction)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve ongoing transactions: $error")
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
        getOngoingTransactions.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getOngoingTransactions)
    }
}