package com.example. notificationpermissions.Fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.ChatRoomAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.MessageService
import com.example.notificationpermissions.Services.NotificationService
import com.example.notificationpermissions.Utilities.EXTRA_CHAT_ROOM

class ChatFragment : Fragment()/*, OnClickListener*/{
    //var serverMessage: TextView? =null
    //var messageText: TextView? =null
    //lateinit var webSocketClient: WebSocketClient


    lateinit var chatRoomAdapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this

        //createWebSocketClient()
        val messageText= view.findViewById<TextView>(R.id.messageText)
        messageText.isVisible=false
        //val serverMessage= view.findViewById<TextView>(R.id.serverMsg)
        //serverMessage.isVisible=false

        val sendMessage= view.findViewById<ImageView>(R.id.sendMessage)
        //sendMessage.setOnClickListener(this)
        sendMessage.isVisible=false

        val noDataText= view.findViewById<TextView>(R.id.noDataTextView)

        fun getUserChatRooms() {
            MessageService.getChatRooms {
                    getChatRooms ->
                println("Get Chat Rooms success: $getChatRooms ${MessageService.userChatRooms.size}")
                if (getChatRooms) {
                    if (MessageService.map.isNotEmpty()) {
                        noDataText.visibility = View.GONE
                    for (i in MessageService.map)
                    {
                        println(i.key)
                        println(i.value)
                        MessageService.findUser(i.key, i.value) { findUser ->
                            println("FInd User success: $findUser")
                            //adapter ma halera display
                            checkIfFragmentAttached {
                                chatRoomAdapter =
                                    ChatRoomAdapter(requireContext().applicationContext, MessageService.userChatRooms){
                                            userchat->
                                        //on Click do something--> open individual chat room
                                        view.findNavController()
                                            .navigate(
                                                R.id.action_chatFragment_to_individualChatRoomFragment,
                                                Bundle().apply { putSerializable(EXTRA_CHAT_ROOM, userchat) })

                                    }
                                val chatRoomList =
                                    view.findViewById<RecyclerView>(R.id.chatRoomList)
                                chatRoomList.adapter = chatRoomAdapter
                                val layoutManager = LinearLayoutManager(context)
                                chatRoomList.layoutManager = layoutManager
                            }
                        }
                    }
                } else if (MessageService.map.isEmpty()) {
                        noDataText.visibility = View.VISIBLE
                    }
                } else {
                    noDataText.visibility = View.VISIBLE
                    noDataText.text= "Chat Rooms could not be loaded"
                }
            }
        }
        getUserChatRooms()
       
        return view
    }
    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    /* private fun createWebSocketClient() {
         val uri: URI = try {
             // Connect to local host
             URI("ws://192.168.1.109:8080/api/messageSocket/${FirebaseService.token}")
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
 *//*
                try {
                    serverMessage!!.text = s
                    println(s)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }*//*
                activity?.runOnUiThread {
                    serverMessage!!.text = s
                    println(s)
                }

                *//*val handler = Handler(Looper.getMainLooper())
                handler.post {
                    serverMessage!!.text = s
                    println(s)
                }*//*
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
    }*/
    /*override fun onClick(view: View?) {
        Toast.makeText(context, "Button clicked: Message Sent", Toast.LENGTH_SHORT).show()
        Log.i("WebSocket", "Send Button was clicked")
        when (view?.id) {
            R.id.sendMessage -> webSocketClient.send(messageText?.text.toString())
        }
        messageText?.text=""
        hideKeyboard()
    }*/

    fun hideKeyboard() {
        val inputManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
    }

}