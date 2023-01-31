package com.example.notificationpermissions.Utilities


//const val BASE_URL = "http://10.0.2.2:8080/api/"
//const val BASE_URL = "http://127.0.0.1:8080/api/"
//const val BASE_URL = "http://192.168.137.26:8080/api/"
const val BASE_URL = "http://192.168.1.109:8080/api/"
//const val BASE_URL = "http://192.168.137.77:8080/api/"

const val URL_LOGIN= "${BASE_URL}user/loginUser"
const val URL_CREATE_USER= "${BASE_URL}user/addUsers"
const val URL_UPDATE_USER= "${BASE_URL}user/updateUsers/"

const val URL_FIND_USER= "${BASE_URL}user/findUser/"

const val URL_RESET_PASSWORD= "${BASE_URL}user/resetPassword/"

const val URL_GET_CATEGORY= "${BASE_URL}category/showCategory/1"
const val URL_GET_ITEMCATEGORY= "${BASE_URL}category/showCategory/2"

const val URL_GET_TYPE= "${BASE_URL}type/showType"
const val URL_GET_CLOTH= "${BASE_URL}clothes/showClothes"
const val URL_GET_DONATION_STATUS= "${BASE_URL}donationStatus/showDonationStatus"
const val URL_ADD_CLOTH= "${BASE_URL}clothes/addClothes"
const val URL_CREATE_POST= "${BASE_URL}post/addPost"
const val URL_GET_ALL_POST= "${BASE_URL}post/showPosts"
const val URL_GET_USER_POSTS= "${BASE_URL}post/showUserPosts/"

const val URL_GET_INTERESTED_USERS_BY_POST= "${BASE_URL}interestedUsers/getUsersByPost/"
const val URL_ADD_INTERESTED_USERS= "${BASE_URL}interestedUsers/addInterestedUsers"
const val URL_DELETE_INTERESTED_USERS= "${BASE_URL}interestedUsers/deleteInterestedUsers"

const val URL_UPDATE_POST= "${BASE_URL}post/updatePost/"
const val URL_DELETE_POST= "${BASE_URL}post/deletePost/"

const val URL_FIND_POST= "${BASE_URL}post/findPost/"

//Broadcast constants
const val BROADCAST_USER_DATA_CHANGE= "BROADCAST_USER_DATA_CHANGE"
