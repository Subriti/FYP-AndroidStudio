package com.example.notificationpermissions.Services

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Models.Reports
import com.example.notificationpermissions.Models.Transaction
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject


object ReportService {
    val reports = ArrayList<Reports>()

    fun reportPost(
        reported_by: String, post_id: String, feedback:String, reportDate: String, isReviewed: Boolean, complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        //bc it takes object of UserId
        val userId = JSONObject()
        userId.put("user_id", reported_by)

        //bc it takes object of PostId
        val postId = JSONObject()
        postId.put("post_id", post_id)

        jsonBody.put("reported_by", userId)
        jsonBody.put("post_id", postId)
        jsonBody.put("feedback",feedback)
        jsonBody.put("report_date", reportDate)
        jsonBody.put("is_reviewed", isReviewed)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val addReport = object : JsonObjectRequest(
            Method.POST,
            URL_ADD_REPORT,
            null,
            Response.Listener { response ->
                println("Add Report Response " + response)
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not add report: $error")
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
        App.sharedPrefs.requestQueue.add(addReport)
    }

    fun getReports(complete: (Boolean) -> Unit) {
        reports.clear()
        val getReports = object : JsonArrayRequest(Method.GET,
            "$URL_GET_REPORTS",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val report = response.getJSONObject(x)
                        val reportId = report.getString("report_id")
                        val reportedBy = report.getString("reported_by")
                        val postId = report.getString("post_id")
                        val feedback = report.getString("feedback")
                        val reportDate = report.getString("report_date")

                        val newReport = Reports( reportId, reportedBy,postId,feedback,reportDate)
                        reports.add(newReport)
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
                Log.d("ERROR", "Could not retrieve reports: $error")
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
        getReports.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getReports)
    }

    fun reviewReport(
        reportId: String,
        isReviewed:Boolean,
        complete: (Boolean) -> Unit
    ) {
        val updateRequest = object :
            JsonObjectRequest(
                Method.PUT,
                "$URL_REVIEW_REPORT$reportId/?isReviewed=$isReviewed",
                null,
                Response.Listener { response ->
                    println("Report Review Response $response")
                    complete(true)
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not review report status: $error")
                    complete(false)
                }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
        }
        updateRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(updateRequest)
    }
}