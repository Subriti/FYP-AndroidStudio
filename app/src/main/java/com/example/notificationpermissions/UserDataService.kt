package com.example.notificationpermissions

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
        userName= ""
        email= ""
        phoneNumber= ""
        location= ""
        birthDate=""
        signupDate=""
        profilePicture=""

        App.sharedPrefs.authToken=""
        App.sharedPrefs.userEmail=""
        App.sharedPrefs.userID= ""
        App.sharedPrefs.isLoggedIn= false

        //PostService.clearPosts()
        //MessageService.clearMessages()
    }
}
