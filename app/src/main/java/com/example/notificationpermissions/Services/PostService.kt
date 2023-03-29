package com.example.notificationpermissions.Services

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.notificationpermissions.Models.*
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject


object PostService {
    val posts = ArrayList<Post>()

    val AllPosts = ArrayList<Post>()

    val DetailedPosts = ArrayList<PostDetails>()

    val PostHolders= ArrayList<Post>()

    var InterestedUsers = ArrayList<InterestedUsers>()
    //var InterestedUsers: MutableList<InterestedUsers>()

    var InterestedUsersMapList = mutableMapOf<String, List<InterestedUsers>>()

    val clothes = ArrayList<Clothes>()
    var isUploaded = false

    var categories = ArrayList<Category>()
    var itemcategory = ArrayList<Category>()

    var getAllPostError: VolleyError? = null

    var clothId = ""

    var notificationPost: Post? = null

    fun findPost(post_id: String, complete: (Boolean) -> Unit) {
        val findRequest = object : JsonObjectRequest(
            Method.GET,
            "$URL_FIND_POST$post_id",
            null,
            Response.Listener { response ->
                println("Find Post Response " + response)
                try {
                    val postId = response.getString("post_id")
                    val postBy = response.getString("post_by")
                    val media_file=  response.getString("media_file")
                    val description=  response.getString("description")
                    val created_datetime= response.getString("created_datetime")
                    val location= response.getString("location")
                    val cloth_id= response.getString("cloth_id")
                    val donation_status= response.getString("donation_status")

                    notificationPost= Post(post_id, postBy,media_file, description, created_datetime, location, cloth_id, donation_status)

                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not find post: $error")
                complete(false)
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(findRequest)
    }

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

    fun updatePost(
        postId: String,
        description: String,
        location: String,
        cloth_id: String,
        /* category:String,
         itemCategory:String,
         clothSize: String,
         clothCondition:String,
         clothSeason: String,*/
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        jsonBody.put("description", description)
        jsonBody.put("location", location)

        /*val json= JSONObject(cloth_id)
        val cloth= json.getString("cloth_id")

        println("Cloth_id: "+cloth_id)
        println("Cloth: "+cloth)

        //cloth ma yo id le update then balla yo id halne post ma
        updateCloth(cloth,category,itemCategory,clothSize,clothCondition,clothSeason){
            updateCloth ->  println("Update Cloth Response $updateCloth")
        }*/

        //bc it takes object of Cloth
        val clothId = JSONObject()
        clothId.put("cloth_id", cloth_id)
        jsonBody.put("cloth_id", clothId)

        val requestBody = jsonBody.toString()
        println("Update Post: " + requestBody)

        val updateRequest = object :
            JsonObjectRequest(
                Method.PUT,
                "$URL_UPDATE_POST$postId",
                null,
                Response.Listener { response ->
                    println("Update Post Response $response")
                    complete(true)

                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not update Post: $error")
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
        updateRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(updateRequest)
    }

    fun updateDonationStatus(
        postId: String,
        donationStatus: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("donation_status_id", donationStatus)

        val requestBody = jsonBody.toString()
        println("Update Donation Status: " + requestBody)

        val updateRequest = object :
            JsonObjectRequest(
                Method.PUT,
                "$URL_UPDATE_DONATION_STATUS$postId",
                null,
                Response.Listener { response ->
                    println("Update Donation Status Response $response")
                    complete(true)
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not update donation status: $error")
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
        updateRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(updateRequest)
    }

    fun createTransaction(
        post_id: String,
        reciever_user_id: String,
        transaction_datetime: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        //bc it takes object of User
        val user = JSONObject()
        user.put("user_id", reciever_user_id)
        jsonBody.put("reciever_user_id", user)

        //bc it takes object of Post
        val post = JSONObject()
        post.put("post_id", post_id)
        jsonBody.put("post_id", post)

        jsonBody.put("transaction_date", transaction_datetime)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val createRequest = object :
            JsonObjectRequest(Method.POST, URL_ADD_TRANSACTION, null, Response.Listener { response ->
                println("Create Transaction Response $response")
                complete(true)
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not add Transaction: $error")
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

    fun updateRating(
        postId: String,
        rating: Float,
        transaction_datetime: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        //bc it takes object of Post
        val post = JSONObject()
        post.put("post_id", postId)
        jsonBody.put("post_id", post)

        jsonBody.put("rating", rating)
        jsonBody.put("transaction_date", transaction_datetime)

        val requestBody = jsonBody.toString()
        println("Update Rating Request: " + requestBody)

        val updateRequest = object :
            JsonObjectRequest(
                Method.PUT,
                "$URL_UPDATE_RATING",
                null,
                Response.Listener { response ->
                    println("Update Rating Response $response")
                    complete(true)
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not update transaction rating: $error")
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
        updateRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(updateRequest)
    }

    fun getRating(userId: String, complete: (Boolean) -> Unit) {
        val getRatingRequest = object :
            JsonObjectRequest(Method.GET, "$URL_GET_RATING$userId", null, Response.Listener {
                //this is where we parse the json object
                    response ->
                        println("GET Rating Response " + response)
                        try {
                            App.sharedPrefs.clothDonated = response.getInt("cloth_donated")
                            App.sharedPrefs.rating = response.getDouble("rating").toFloat()
                            complete(true)
                        } catch (e: JSONException) {
                            Log.d("JSON", "EXC: " + e.localizedMessage)
                            complete(false)
                        }
                    },
                    Response.ErrorListener {
                        //this is where we deal with our error
                            error ->
                        Log.d("ERROR", "Could not get rating: $error")
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
        getRatingRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getRatingRequest)
    }

    fun deletePost(postId: String, complete: (Boolean) -> Unit) {
        val deletePostRequest =
            object : StringRequest(
                Method.DELETE,
                "$URL_DELETE_POST$postId",
                Response.Listener { response ->
                    println("Delete Post Response $response")
                    complete(true)
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not delete post: $error")
                    complete(false)
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                    return headers
                }
            }
        App.sharedPrefs.requestQueue.add(deletePostRequest)
    }

    fun getUserPosts(userId: String, complete: (Boolean) -> Unit) {
        /*if (posts.size > 0) {
            complete(true)
        } else {*/
        posts.clear()
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
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getPostRequest)
    }


    fun getOtherUserPosts(userId: String, complete: (Boolean) -> Unit) {
        posts.clear()
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

                        val donationJSONObject = JSONObject(donationStatus)
                        val status = donationJSONObject.getString("donation_status")

                        //excluding donated and ongoing status posts from user's profile
                        if (status!="Ongoing" && status!="Donated"){
                            posts.add(newPost)
                        }
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
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getPostRequest)
    }

    fun getAllPosts(complete: (Boolean) -> Unit) {
        AllPosts.clear()
        DetailedPosts.clear()
        clothes.clear()
        val getPostRequest =
            object : JsonArrayRequest(Method.GET, URL_GET_ALL_POST, null, Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        val post = response.getJSONObject(x)
                        val postId = post.getString("post_id")
                        val postBy = post.getString("post_by")

                        val userJSONObject = JSONObject(postBy)
                        val userId = userJSONObject.getString("user_id")
                        val username = userJSONObject.getString("user_name")
                        val userEmail = userJSONObject.getString("email")
                        val phoneNum = userJSONObject.getString("phone_number")
                        val profilePicture = userJSONObject.getString("profile_picture")
                        val fcmToken = userJSONObject.getString("fcm_token")
                        val hideEmail = userJSONObject.getString("hide_email")
                        val hidePhone = userJSONObject.getString("hide_phone")

                        val mediaFile = post.getString("media_file")
                        val description = post.getString("description")
                        val createdDatetime = post.getString("created_datetime")
                        val location = post.getString("location")
                        val clothId = post.getString("cloth_id")

                        val clothJSONObject = JSONObject(clothId)
                        val cloth_Id= clothJSONObject.getString("cloth_id")
                        val clothSize = clothJSONObject.getString("cloth_size")
                        val clothCondition = clothJSONObject.getString("cloth_condition")
                        val clothSeason = clothJSONObject.getString("cloth_season")

                        val clothCategory = clothJSONObject.getString("clothes_category_id")
                        val categoryJSONObject = JSONObject(clothCategory)
                        val categoryId= categoryJSONObject.getString("category_id")
                        val category = categoryJSONObject.getString("category_name")

                        val itemCategoryId = clothJSONObject.getString("item_category_id")
                        val itemCategoryJSONObject = JSONObject(itemCategoryId)
                        val itemcategoryId= itemCategoryJSONObject.getString("category_id")
                        val itemCategory = itemCategoryJSONObject.getString("category_name")

                        val donationStatus = post.getString("donation_status")
                        val donationJSONObject = JSONObject(donationStatus)
                        val status = donationJSONObject.getString("donation_status")



                        val customDescription =
                            "$description\nCloth Category: $category \nItem Category: $itemCategory \nCloth Size: $clothSize \nCloth Condition: $clothCondition \nCloth Season: $clothSeason \nDonation Status: $status \nLocation: $location"

                        val newPost = PostDetails(
                            postId,
                            username,
                            userId,
                            userEmail,
                            profilePicture,
                            phoneNum,
                            fcmToken,
                            mediaFile,
                            customDescription,
                            createdDatetime,
                            location,
                            cloth_Id,
                            categoryId,
                            itemcategoryId,
                            clothSize,
                            clothCondition,
                            clothSeason,
                            donationStatus,
                            hideEmail,
                            hidePhone
                        )

                        val newPosts = Post(
                            postId,
                            username,
                            mediaFile,
                            customDescription,
                            createdDatetime,
                            location,
                            profilePicture,
                            donationStatus
                        )

                        val newCloth = Clothes(
                            cloth_Id,
                            categoryId,
                            itemcategoryId,
                            clothSize,
                            clothCondition,
                            clothSeason,
                            mediaFile
                        )

                        //excluding donated and ongoing status posts from feed
                        if (status!="Ongoing" && status!="Donated"){
                            AllPosts.add(newPosts)
                            clothes.add(newCloth)
                            DetailedPosts.add(newPost)
                        }
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
                getAllPostError= error
                println(getAllPostError)
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
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getPostRequest)
    }

    fun getCloth(userId: String, complete: (Boolean) -> Unit) {
        val getClothRequest =
            object : JsonArrayRequest(Method.GET, "$URL_GET_CLOTH", null, Response.Listener {
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
                            clothSeason,
                            ""
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

    fun updateCloth(
        cloth_id: String,
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
        println("Update cloth: " + requestBody)

        val updateClothRequest = object :
            JsonObjectRequest(
                Method.PUT,
                URL_UPDATE_CLOTH + cloth_id,
                null,
                Response.Listener { response ->
                    println("Update Cloth Response " + response)
                    complete(true)
                },
                Response.ErrorListener { error ->
                    Log.d("ERROR", "Could not update cloth: $error")
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
        App.sharedPrefs.requestQueue.add(updateClothRequest)
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

    fun getInterestedUserByPosts(postId: String, complete: (Boolean) -> Unit) {
        /*if (InterestedUsers.size > 0) {
            complete(true)
        } else {*/
        val getInterestedUsersRequest = object : JsonArrayRequest(
            Method.GET,
            "$URL_GET_INTERESTED_USERS_BY_POST$postId",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    //InterestedUsers.clear()

                    InterestedUsersMapList.remove(postId)
                    val currentInterestedUsers = InterestedUsersMapList[postId] ?: mutableListOf()
                    InterestedUsers = ArrayList(mutableListOf())

                    for (x in 0 until response.length()) {
                        val interestedUsers = response.getJSONObject(x)
                        val userId = interestedUsers.getString("user_id")

                        val userJSONObject = JSONObject(userId)
                        val user_id = userJSONObject.getString("user_id")
                        val username = userJSONObject.getString("user_name")
                        val profilePicture = userJSONObject.getString("profile_picture")

                        val postId = interestedUsers.getString("post_id")
                        val postJSONObject = JSONObject(postId)
                        val user = postJSONObject.getString("post_by")
                        val post_id = postJSONObject.getString("post_id")
                        val usernameJSONObject = JSONObject(user)
                        val name = usernameJSONObject.getString("user_name")
                        val profile = usernameJSONObject.getString("profile_picture")

                        val newInterestedUser = InterestedUsers(
                            user_id, username, profilePicture, post_id, name, profile
                        )
                        InterestedUsers.add(newInterestedUser)
                    }
                    if (InterestedUsers.isNotEmpty()) {
                        val newInterestedUserList = currentInterestedUsers + InterestedUsers
                        InterestedUsersMapList[postId] = newInterestedUserList
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
                Log.d("ERROR", "Could not retrieve interested users: $error")
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
        getInterestedUsersRequest.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getInterestedUsersRequest)
    }

    fun deleteInterestedUserByPosts(postId: String, userId: String, complete: (Boolean) -> Unit) {
        val url = "$URL_DELETE_INTERESTED_USERS/$postId/$userId"
        val deleteInterestedUsersRequest =
            object : StringRequest(Method.DELETE, url, Response.Listener { response ->
                println("Delete Interested User Response " + response)
                complete(true)
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not delete interested User: $error")
                complete(false)
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                    return headers
                }
            }
        App.sharedPrefs.requestQueue.add(deleteInterestedUsersRequest)
    }


    fun addInterestedUser(
        user_id: String, post_id: String, complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()

        //bc it takes object of UserId
        val userId = JSONObject()
        userId.put("user_id", user_id)

        //bc it takes object of PostId
        val postId = JSONObject()
        postId.put("post_id", post_id)

        jsonBody.put("user_id", userId)
        jsonBody.put("post_id", postId)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val addInterestedUsers = object : JsonObjectRequest(
            Method.POST,
            URL_ADD_INTERESTED_USERS,
            null,
            Response.Listener { response ->
                println("Add Interested User Response " + response)
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not add interested User: $error")
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
        App.sharedPrefs.requestQueue.add(addInterestedUsers)
    }
}
