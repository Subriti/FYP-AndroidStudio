package com.example.notificationpermissions.Utilities

const val URL= "192.168.1.101"
const val BASE_URL = "http://$URL:8080/api/"

const val URL_LOGIN= "${BASE_URL}user/loginUser"
const val URL_CREATE_USER= "${BASE_URL}user/addUsers"
const val URL_UPDATE_USER= "${BASE_URL}user/updateUsers/"
const val URL_UPDATE_FCM_TOKEN= "${BASE_URL}user/updateFCMToken/"
const val URL_GET_FCM_TOKEN= "${BASE_URL}user/getFCMToken"
const val URL_FIND_USER= "${BASE_URL}user/findUser/"
const val URL_FIND_USER_BY_ID= "${BASE_URL}user/findUserByID/"
const val URL_FIND_USER_BY_NAME= "${BASE_URL}user/findUserByName"
const val URL_GET_ALL_USERS= "${BASE_URL}user/showUsers"
const val URL_RESET_PASSWORD= "${BASE_URL}user/resetPassword/"
const val URL_FORGOT_PASSWORD="${BASE_URL}user/forgotPassword/"

const val URL_BLOCK_USER= "${BASE_URL}blockList/blockUser"
const val URL_UNBLOCK_USER= "${BASE_URL}blockList/unblockUser/"
const val URL_SHOW_USER_BLOCK_LIST= "${BASE_URL}blockList/showUserBlockList/"
const val URL_SHOW_BLOCKED_LIST= "${BASE_URL}blockList/showBlockedList/"

const val URL_ADD_CLOTH= "${BASE_URL}clothes/addClothes"
const val URL_UPDATE_CLOTH= "${BASE_URL}clothes/updateClothes/"
const val URL_FIND_POST= "${BASE_URL}post/findPost/"
const val URL_GET_ALL_POST= "${BASE_URL}post/showPosts"
const val URL_GET_USER_POSTS= "${BASE_URL}post/showUserPosts/"
const val URL_CREATE_POST= "${BASE_URL}post/addPost"
const val URL_UPDATE_POST= "${BASE_URL}post/updatePost/"
const val URL_UPDATE_DONATION_STATUS= "${BASE_URL}post/updateDonationStatus/"
const val URL_DELETE_POST= "${BASE_URL}post/deletePost/"

const val URL_GET_REPORTS= "${BASE_URL}report/showReport/"
const val URL_ADD_REPORT= "${BASE_URL}report/addReport"
const val URL_REVIEW_REPORT= "${BASE_URL}report/reviewReport/"

const val URL_GET_USER_NOTIFICATIONS= "${BASE_URL}notification/showUserNotifications/"
const val URL_CREATE_NOTIFICATIONS= "${BASE_URL}notification/addNotification"

const val URL_GET_INTERESTED_USERS_BY_POST= "${BASE_URL}interestedUsers/getUsersByPost/"
const val URL_ADD_INTERESTED_USERS= "${BASE_URL}interestedUsers/addInterestedUsers"
const val URL_DELETE_INTERESTED_USERS= "${BASE_URL}interestedUsers/deleteInterestedUsers"

const val URL_ADD_TRANSACTION= "${BASE_URL}transaction/addTransaction"
const val URL_UPDATE_RATING= "${BASE_URL}transaction/updateTransaction"
const val URL_UPDATE_TRANSACTION= "${BASE_URL}transaction/updateTransactionStatus/"
const val URL_GET_RATING= "${BASE_URL}transaction/getRating/"
const val URL_GET_RECIEVED_DONATIONS= "${BASE_URL}transaction/showRecievedTransactions/"
const val URL_GET_GIVEN_DONATIONS= "${BASE_URL}transaction/showGivenTransactions/"
const val URL_GET_ONGOING_TRANSACTIONS= "${BASE_URL}transaction/showOngoingTransactions/"

const val URL_GET_USER_CHAT_ROOMS= "${BASE_URL}message/showUserChatRooms/"
const val URL_GET_USER_CHAT_ROOM_MESSAGES= "${BASE_URL}message/showUserChatRoomMessages"
const val URL_GET_USER_CHAT_ROOM_ID= "${BASE_URL}message/getUserChatRoomId/?senderName="

//for fcm
const val FCM_BASE_URL = "https://fcm.googleapis.com"
const val SERVER_KEY = "AAAAc0zxpnI:APA91bHUgdgzTUJTuPqURBN4kzN9919_PZdfSCqfEweLOKdTAUkg4DxaIEePY0gif13FtWRuk1m4kRDNxmStUgf5K9bWeDnV48GPARgWXsMnrn5oQ-YhFghIb4B3jy3UjSUDS18_ZZBu"
const val CONTENT_TYPE = "application/json"

//Broadcast constants
const val BROADCAST_USER_DATA_CHANGE= "BROADCAST_USER_DATA_CHANGE"

//for viewing user's individual post
const val EXTRA_POST= "post"

//for viewing user's profile
const val EXTRA_USER= "user"

//for viewing user's individual chat room
const val EXTRA_CHAT_ROOM= "chat"

//for viewing notification related post
const val POST_ID_EXTRA= "postId"