package com.example.notificationpermissions.Services

import com.example.notificationpermissions.Utilities.App

object UserDataService {
    var userId = ""
    var userName= ""
    var email= ""
    var phoneNumber= ""
    var location= ""
    var birthDate=""
    var signupDate=""
    var profilePicture=""


    fun logout(){
        userId = ""
        userName = ""
        email = ""
        phoneNumber = ""
        location = ""
        birthDate =""
        signupDate =""
        profilePicture =""

        App.sharedPrefs.authToken=""
        App.sharedPrefs.userEmail=""
        App.sharedPrefs.userID= ""
        App.sharedPrefs.profilePicture= ""
        App.sharedPrefs.location=""
        App.sharedPrefs.phoneNumber=""
        App.sharedPrefs.dateOfBirth=""
        App.sharedPrefs.isLoggedIn= false
        PostService.posts.clear()
        MessageService.clearMessages()
        MessageService.clearChatRooms()
        MessageService.map.clear()
    }
}
