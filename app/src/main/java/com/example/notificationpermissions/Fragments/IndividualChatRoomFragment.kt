package com.example.notificationpermissions.Fragments

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.MessageAdapter
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.Models.Message
import com.example.notificationpermissions.Notifications.NotificationData
import com.example.notificationpermissions.Notifications.PushNotification
import com.example.notificationpermissions.Notifications.RetrofitInstance
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.MessageService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_CHAT_ROOM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class IndividualChatRoomFragment : Fragment(), OnClickListener{
    var messageText: TextView? =null
    var messageList:RecyclerView?=null
    lateinit var webSocketClient: WebSocketClient

    lateinit var messageAdapter: MessageAdapter
    var chatDetails:ChatRoom?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_individual_chat_room, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this
        (activity as DashboardActivity?)!!.supportActionBar!!.hide()

        chatDetails = arguments?.getSerializable(EXTRA_CHAT_ROOM) as ChatRoom
        createWebSocketClient()

        val recieverName= view.findViewById<TextView>(R.id.recieverUsername)
        val recieverProfile= view.findViewById<ImageView>(R.id.recieverProfile)
        messageText= view.findViewById(R.id.messageText)
        val sendMessage= view.findViewById<ImageView>(R.id.sendMessage)
        val backButton= view.findViewById<ImageView>(R.id.backButton)
        // backButton.isVisible=false

        backButton.setOnClickListener {
            //get back to chatFragment
            /*view?.findNavController()
                ?.navigate(R.id.action_individualChatRoomFragment_to_chatFragment)
            (activity as DashboardActivity?)!!.supportActionBar!!.show()
*/
            //view.findNavController().popBackStack(R.id.homeFragment, false)
            println(view.findNavController().backQueue.last().destination )
            println(view.findNavController().findDestination(R.id.individualChatRoomFragment))

            if (view.findNavController().backQueue.last().destination == view.findNavController()
                    .findDestination(R.id.individualChatRoomFragment)
            ) {
                println("user view")
                //view.findNavController().popBackStack(R.id.individualChatRoomFragment, false)
                //NavOptions.Builder().setPopUpTo(R.id.individualChatRoomFragment, true).build()
                //NavOptions.Builder().setPopUpTo(R.id.userViewProfileFragment2, true).build()
                //NavOptions.Builder().setPopUpTo(R.id.chatFragment, true).build()

                view.findNavController().navigate(
                    R.id.action_individualChatRoomFragment_to_chatFragment, null,
                    NavOptions.Builder().setPopUpTo(R.id.individualChatRoomFragment, true).build()
                )
                (activity as DashboardActivity?)!!.supportActionBar!!.show()
            }
            /*  else if (view.findNavController().backQueue.removeLast().destination.parent?.startDestinationId == (R.id.homeFragment)) {
                  println("chat room")
                  view.findNavController().navigate(
                      R.id.action_individualChatRoomFragment_to_chatFragment, null,
                      NavOptions.Builder().setPopUpTo(R.id.chatFragment, true).build()
                  )
                  (activity as DashboardActivity?)!!.supportActionBar!!.show()
              }*/
        }

        val phoneButton= view.findViewById<ImageView>(R.id.recieverPhone)
        phoneButton.setOnClickListener {
            //get receiver's phone number: intent to call the number
            val phoneNumber = chatDetails!!.recieverPhone
            val dial = Intent(Intent.ACTION_DIAL)
            dial.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            dial.data = Uri.parse("tel:$phoneNumber")
            startActivity(dial)
        }

        println(chatDetails!!.recieverPhone)
        println(chatDetails!!.chatRoomId)
        println(chatDetails!!.receiverUserId)
        println(chatDetails!!.recieverFCMtoken)
        println(chatDetails!!.recieverUserName)
        sendMessage.setOnClickListener(this)

        recieverName?.text = chatDetails!!.recieverUserName
        context?.let {
            Glide.with(it).load(chatDetails!!.recieverProfilePicture).into(recieverProfile)
        }

        fun getUserChatRoomMessages() {
            MessageService.getChatRoomMessages(chatDetails!!.chatRoomId) {
                    getChatRoomMessages ->
                println("Get Chat Room Messages success: $getChatRoomMessages")
                if (getChatRoomMessages) {
                    //adapter ma halera display
                    messageAdapter =
                        MessageAdapter(requireContext().applicationContext, MessageService.messages)
                    messageList =
                        view.findViewById(R.id.messageListView)
                    messageList?.adapter = messageAdapter
                    val layoutManager = LinearLayoutManager(context)
                    layoutManager.stackFromEnd = true;
                    messageList?.layoutManager = layoutManager
                    messageList?.scrollToPosition((messageList?.adapter?.itemCount)!! - 1);
                    messageAdapter.notifyDataSetChanged()
                }
            }
        }
        getUserChatRoomMessages()

        return view
    }

    private fun createWebSocketClient() {
        val uri: URI = try {
            // Connect to local host
            val encodedPath= URLEncoder.encode(chatDetails?.chatRoomId, "UTF-8")
            //URI("ws://192.168.1.109:8080/api/messageSocket/${chatDetails?.chatRoomId}")
            URI("ws://192.168.1.101:8080/api/messageSocket/$encodedPath")
            //URI("ws://100.64.232.254:8080/api/messageSocket/$encodedPath")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                Log.i("WebSocket", "Session is starting")
                webSocketClient.send("Hello World!")
            }

            @SuppressLint("NewApi")
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onTextReceived(s: String) {
                Log.i("WebSocket", "Message received")
                println("Response is $s")
                if (s != "Message recieved from client: Hello World!") {
                    activity?.runOnUiThread {
                        val jsonBody = JSONObject(s)
                        val id = jsonBody.getString("message_id")
                        val messageBody = jsonBody.getString("message_body")
                        var timeStamp = jsonBody.getString("timestamp")

                        /*var date="";
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                        try {
                            val date = inputFormat.parse(timeStamp).toString()
                            //date= inputFormat.format(dates)
                            //println(dates)
                            println(date)
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }*/

/*
                        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
                        val date = inputFormat.parse(timeStamp)

                        println(date)

                        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        timeStamp = outputFormat.format(date)

                        println(timeStamp)*/

                        val recieverId = jsonBody.getString("reciever_user_id")
                        val reciever= JSONObject(recieverId)
                        val rID= reciever.getString("user_id")

                        val senderId = jsonBody.getString("sender_user_id")
                        val sender= JSONObject(senderId)
                        val sID= sender.getString("user_id")

                        val chatRoomId = jsonBody.getString("chat_room_id")

                        var userName= ""
                        var profile= ""
                        var token= ""
                        var phone=""

                        if (sID==App.sharedPrefs.userID){
                            userName= App.sharedPrefs.userName
                            profile= App.sharedPrefs.profilePicture
                            token= App.sharedPrefs.token
                            phone=App.sharedPrefs.phoneNumber

                            val title = "Message from ${App.sharedPrefs.userName}"
                            val message ="Message: ${messageBody}"
                            PushNotification(
                                NotificationData(title, message),
                                chatDetails?.recieverFCMtoken.toString()
                            )
                                .also { sendNotification(it) }
                        }else{
                            userName= chatDetails?.recieverUserName.toString()
                            profile= chatDetails?.recieverProfilePicture.toString()
                            token= chatDetails?.recieverFCMtoken.toString()
                            phone= chatDetails?.recieverPhone.toString()

                            //send notification as well
                            val title = "Message from ${chatDetails?.recieverUserName.toString()}"
                            val message ="Message: ${messageBody}"
                            PushNotification(
                                NotificationData(title, message),
                                App.sharedPrefs.token
                            )
                                .also { sendNotification(it) }
                        }

                        val newMessages = Message(
                            id,
                            messageBody,
                            timeStamp,
                            rID,
                            sID,
                            userName,
                            profile,
                            token,
                            chatRoomId,
                            phone
                        );
                        MessageService.messages.add(newMessages)
                        messageAdapter.notifyDataSetChanged()
                        if (messageAdapter.itemCount >0){
                            messageList?.smoothScrollToPosition(messageAdapter.itemCount-1)
                        }
                    }
                }
                /*val handler = Handler(Looper.getMainLooper())
                handler.post {
                    serverMessage!!.text = s
                    println(s)
                }*/
            }

            override fun onBinaryReceived(data: ByteArray) {}
            override fun onPingReceived(data: ByteArray) {}
            override fun onPongReceived(data: ByteArray) {}
            override fun onException(e: Exception) {
                println(e.message)
            }

            override fun onCloseReceived() {
                Log.i("WebSocket", "Connection Closed ")
                println("onCloseReceived")
            }
        }
        webSocketClient.setConnectTimeout(10000)
        webSocketClient.setReadTimeout(60000)
        webSocketClient.enableAutomaticReconnection(5000)
        webSocketClient.connect()
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    println("Notification successfully sent")
                    println(response)
                    println(response.message().toString())
                } else {
                    println("Notification could not be sent")
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(view: View) {
        Toast.makeText(context, "Button clicked: Message Sent", Toast.LENGTH_SHORT).show()
        Log.i("WebSocket", "Send Button was clicked")

        val sdf = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        sdf.timeZone = Calendar.getInstance().timeZone
        val dateTimeWithTimezone = sdf.format(Calendar.getInstance().time)

        //send full detail for adding the msg
        val jsonBody= JSONObject()
        jsonBody.put("message_body", messageText?.text.toString())
        //jsonBody.put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time))
        jsonBody.put("timestamp", dateTimeWithTimezone)

        val reciever= JSONObject()
        reciever.put("user_id",chatDetails?.receiverUserId)
        jsonBody.put("reciever_user_id", reciever)

        val sender= JSONObject()
        sender.put("user_id",App.sharedPrefs.userID)
        jsonBody.put("sender_user_id",sender)

        jsonBody.put("chat_room_id", chatDetails?.chatRoomId )
        println("JSON BOdy is "+jsonBody)

        when (view?.id) {
            //R.id.sendMessage -> webSocketClient.send(messageText?.text.toString())
            R.id.sendMessage -> webSocketClient.send(jsonBody.toString())
        }
        messageAdapter.notifyDataSetChanged()
        if (messageAdapter.itemCount >0){
            messageList?.smoothScrollToPosition(messageAdapter.itemCount-1)
        }
        messageText?.text=""
        hideKeyboard()
    }

    fun hideKeyboard() {
        val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
    }
}