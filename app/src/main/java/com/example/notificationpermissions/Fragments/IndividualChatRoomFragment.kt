package com.example.notificationpermissions.Fragments

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.MessageAdapter
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.Models.Message
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.MessageService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_CHAT_ROOM
import org.json.JSONObject
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException
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

        chatDetails = arguments?.getSerializable(EXTRA_CHAT_ROOM) as ChatRoom
        createWebSocketClient()

        val recieverName= view.findViewById<TextView>(R.id.bro)
        val recieverProfile= view.findViewById<ImageView>(R.id.recieverProfile)
        messageText= view.findViewById(R.id.messageText)
        val sendMessage= view.findViewById<ImageView>(R.id.sendMessage)

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
                    messageList?.layoutManager = layoutManager
                    messageAdapter.notifyDataSetChanged()
                }
            }
        }
        getUserChatRoomMessages()
        /*if (MessageService.messages.isEmpty()){
            getUserChatRoomMessages()
        }else{
            messageAdapter =
                MessageAdapter(requireContext().applicationContext, MessageService.messages)
            val messageList =
                view.findViewById<RecyclerView>(R.id.messageListView)
            messageList.adapter = messageAdapter
            val layoutManager = LinearLayoutManager(context)
            messageList.layoutManager = layoutManager
        }*/
        return view
    }

    private fun createWebSocketClient() {
        val uri: URI = try {
            // Connect to local host
            //URI("ws://192.168.1.109:8080/api/messageSocket/${App.sharedPrefs.token}")
            URI("ws://192.168.1.109:8080/api/messageSocket/${chatDetails?.chatRoomId}")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen() {
                Log.i("WebSocket", "Session is starting")
                webSocketClient.send("Hello World!")
            }

            override fun onTextReceived(s: String) {
                Log.i("WebSocket", "Message received")
                println(s)
                if (s != "Message recieved from client: Hello World!") {
                    activity?.runOnUiThread {
                        val jsonBody = JSONObject(s)
                        val id = jsonBody.getString("message_id")
                        val messageBody = jsonBody.getString("message_body")
                        val timeStamp = jsonBody.getString("timestamp")

                        val recieverId = jsonBody.getString("reciever_user_id")
                        val reciever= JSONObject(recieverId)
                        val rID= reciever.getString("user_id")

                        val senderId = jsonBody.getString("sender_user_id")
                        val sender= JSONObject(senderId)
                        val sID= sender.getString("user_id")

                        val chatRoomId = jsonBody.getString("chat_room_id")

                        println(App.sharedPrefs.token)

                        val newMessages = Message(
                            id,
                            messageBody,
                            timeStamp,
                            rID,
                            sID,
                            App.sharedPrefs.userName,
                            App.sharedPrefs.profilePicture,
                            App.sharedPrefs.token!!,
                            chatRoomId
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
        println(jsonBody)

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