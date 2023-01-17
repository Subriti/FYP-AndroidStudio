package com.example.notificationpermissions

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject


object PostService {
    val posts = ArrayList<Post>()
    val clothes = ArrayList<Clothes>()
    var isUploaded = false

    var categories = ArrayList<Category>()
    var itemcategory = ArrayList<Category>()


    var clothId = ""

    fun createPost(
        post_by: String,
        media_file: String,
        description: String,
        created_datetime: String,
        location: String,
        cloth_id: String,
        donation_status: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        //bc it takes object of User
        val user = JSONObject()
        user.put("user_id", post_by)
        jsonBody.put("post_by", user)

        //jsonBody.put("post_by", post_by) //, App.sharedPrefs.userID)
        jsonBody.put("media_file", media_file)
        jsonBody.put("description", description)
        jsonBody.put("created_datetime", created_datetime)
        jsonBody.put("location", location)

        //bc it takes object of Cloth
        val clothId = JSONObject()
        clothId.put("cloth_id", cloth_id)
        jsonBody.put("cloth_id", clothId)

        //bc it takes object of Donation Status
        val donationStatus = JSONObject()
        donationStatus.put("donation_status_id", donation_status)
        jsonBody.put("donation_status", donationStatus)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val createRequest = object :
            JsonObjectRequest(Method.POST, URL_CREATE_POST, null, Response.Listener { response ->
                println("Create Post Response $response")
                complete(true)

            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not add Post: $error")
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
        createRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(createRequest)
    }

    fun getUserPosts(userId: String, complete: (Boolean) -> Unit) {
        val getPostRequest = object :
            JsonArrayRequest(Method.GET, "$URL_GET_USER_POSTS$userId", null, Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val post = response.getJSONObject(x)
                        val postId = post.getString("post_id")
                        val postBy = post.getString("post_by")
                        val mediaFile = post.getString("media_file")
                        val description = post.getString("description")
                        val createdDatetime = post.getString("created_datetime")
                        val location = post.getString("location")
                        val clothId = post.getString("cloth_id")
                        val donationStatus = post.getString("donation_status")

                        val newPost = Post(
                            postId,
                            postBy,
                            mediaFile,
                            description,
                            createdDatetime,
                            location,
                            clothId,
                            donationStatus
                        )
                        posts.add(newPost)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve posts: $error")
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
        getPostRequest.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getPostRequest)
    }

    fun getCloth(userId: String, complete: (Boolean) -> Unit) {
        val getClothRequest = object :
            JsonArrayRequest(Method.GET, "$URL_GET_CLOTH", null, Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val cloth = response.getJSONObject(x)
                        val clothId = cloth.getString("cloth_id")
                        val clothesCategory = cloth.getString("clothes_category_id")
                        val itemCategory = cloth.getString("item_category_id")
                        val clothSize = cloth.getString("cloth_size")
                        val clothCondition = cloth.getString("cloth_condition")
                        val clothSeason = cloth.getString("cloth_season")

                        val newCloth = Clothes(
                            clothId,
                            clothesCategory,
                            itemCategory,
                            clothSize,
                            clothCondition,
                            clothSeason
                        )
                        clothes.add(newCloth)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve clothes: $error")
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
        App.sharedPrefs.requestQueue.add(getClothRequest)
    }

    fun addCloth(
        clothes_category_id: String,
        item_category_id: String,
        cloth_size: String,
        cloth_condition: String,
        cloth_season: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        //bc it takes object of ClothCategory
        val clothCategoryId = JSONObject()
        clothCategoryId.put("category_id", clothes_category_id)
        jsonBody.put("clothes_category_id", clothCategoryId)

        //bc it takes object of ClothItemCategory
        val clothItemCategoryId = JSONObject()
        clothItemCategoryId.put("category_id", item_category_id)
        jsonBody.put("item_category_id", clothItemCategoryId)

        jsonBody.put("cloth_size", cloth_size)
        jsonBody.put("cloth_condition", cloth_condition)
        jsonBody.put("cloth_season", cloth_season)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val createChannel = object :
            JsonObjectRequest(Method.POST, URL_ADD_CLOTH, null, Response.Listener { response ->
                println("Add Cloth Response " + response)
                clothId = response.getString("cloth_id")
                complete(true)
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not add cloth: $error")
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
        App.sharedPrefs.requestQueue.add(createChannel)
    }

    fun getCategory(complete: (Boolean) -> Unit) {
        if (categories.size > 0) {
            categories.clear()
            /*  //}
            complete(true)
        } else {*/
        }
            val getCategoryRequest =
                object : JsonArrayRequest(Method.GET, URL_GET_CATEGORY, null, Response.Listener {
                    //this is where we parse the json object
                        response ->
                    try {
                        for (x in 0 until response.length()) {
                            val category = response.getJSONObject(x)
                            val categoryId = category.getString("category_id")
                            val categoryName = category.getString("category_name")
                            val categoryType = category.getString("category_type")

                            val newCategory = Category(categoryId, categoryName, categoryType)

                            categories.add(newCategory)
                            println(categories)

                        }
                        complete(true)
                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: " + e.localizedMessage)
                        complete(false)
                    }
                }, Response.ErrorListener {
                    //this is where we deal with our error
                        error ->
                    Log.d("ERROR", "Could not retrieve categories: $error")
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
            App.sharedPrefs.requestQueue.add(getCategoryRequest)
    }

    fun getItemCategory(complete: (Boolean) -> Unit) {
        if (itemcategory.size > 0) {
            complete(true)
        } else {
            val getItemCategoryRequest = object :
                JsonArrayRequest(Method.GET, URL_GET_ITEMCATEGORY, null, Response.Listener {
                    //this is where we parse the json object
                        response ->
                    try {
                        for (x in 0 until response.length()) {
                            val category = response.getJSONObject(x)
                            val categoryId = category.getString("category_id")
                            val categoryName = category.getString("category_name")
                            val categoryType = category.getString("category_type")

                            val newCategory = Category(categoryId, categoryName, categoryType)
                            itemcategory.add(newCategory)
                        }
                        complete(true)
                    } catch (e: JSONException) {
                        Log.d("JSON", "EXC: " + e.localizedMessage)
                        complete(false)
                    }
                }, Response.ErrorListener {
                    //this is where we deal with our error
                        error ->
                    Log.d("ERROR", "Could not retrieve item categories: $error")
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
            App.sharedPrefs.requestQueue.add(getItemCategoryRequest)
        }
    }
}
