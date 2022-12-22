package com.example.notificationpermissions.Utilities

//const val BASE_URL = "http://10.0.2.2:8080/api/"
//const val BASE_URL = "http://127.0.0.1:8080/api/"
const val BASE_URL = "http://192.168.137.26:8080/api/"

const val URL_LOGIN= "${BASE_URL}user/loginUser"
const val URL_CREATE_USER= "${BASE_URL}user/addUsers"
const val URL_UPDATE_USER= "${BASE_URL}user/updateUsers/"

const val URL_FIND_USER= "${BASE_URL}user/findUser/"

const val URL_RESET_PASSWORD= "${BASE_URL}user/resetPassword/"

const val URL_CREATE_POST= "${BASE_URL}post/addPost"
const val URL_GET_USER_POSTS= "${BASE_URL}post/showUserPosts/"
const val URL_UPDATE_POST= "${BASE_URL}post/updatePost/"
const val URL_DELETE_POST= "${BASE_URL}post/deletePost/"

const val URL_FIND_POST= "${BASE_URL}post/findPost/"

//Broadcast constants
const val BROADCAST_USER_DATA_CHANGE= "BROADCAST_USER_DATA_CHANGE"