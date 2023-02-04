package com.example. notificationpermissions.Fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.ChatRoomAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.FirebaseService
import com.example.notificationpermissions.Services.MessageService
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException

class ChatFragment : Fragment(), OnClickListener{
    var serverMessage: TextView? =null
    var messageText: TextView? =null
    lateinit var webSocketClient: WebSocketClient


    lateinit var chatRoomAdapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this

        createWebSocketClient()
        messageText= view.findViewById(R.id.messageText)
        serverMessage= view.findViewById(R.id.serverMsg)

        val sendMessage= view.findViewById<ImageView>(R.id.sendMessage)
        sendMessage.setOnClickListener(this)

        fun getUserChatRooms() {
            MessageService.getChatRooms {
                    getChatRooms ->
                println("Get Chat Rooms success: $getChatRooms")
                if (getChatRooms) {
                    println(MessageService.userChatRooms)
                    for (i in MessageService.map)
                    {
                        println(i.key)
                        println(i.value)
                        MessageService.findUser(i.key, i.value) { findUser ->
                            println("FInd User success: $findUser")
                            if (getChatRooms) {
                                println(MessageService.userChatRooms)
                            }
                            println(MessageService.userChatRooms)
                            //adapter ma halera display
                            checkIfFragmentAttached {
                                println(context)
                                println(requireContext())
                                chatRoomAdapter =
                                    ChatRoomAdapter(requireContext().applicationContext, MessageService.userChatRooms)
                                val chatRoomList =
                                    view.findViewById<RecyclerView>(R.id.chatRoomList)
                                chatRoomList.adapter = chatRoomAdapter
                                val layoutManager = LinearLayoutManager(context)
                                chatRoomList.layoutManager = layoutManager
                            }
                        }
                    }
                }
            }
        }

        if (MessageService.userChatRooms.isEmpty()){
            getUserChatRooms()
        }else{
            chatRoomAdapter =
                ChatRoomAdapter(requireContext().applicationContext, MessageService.userChatRooms)
            val chatRoomList =
                view.findViewById<RecyclerView>(R.id.chatRoomList)
            chatRoomList.adapter = chatRoomAdapter
            val layoutManager = LinearLayoutManager(context)
            chatRoomList.layoutManager = layoutManager
        }
        return view
    }
    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    private fun createWebSocketClient() {
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
/*
                try {
                    serverMessage!!.text = s
                    println(s)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }*/
                activity?.runOnUiThread {
                    serverMessage!!.text = s
                    println(s)
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
    override fun onClick(view: View?) {
        Toast.makeText(context, "Button clicked: Message Sent", Toast.LENGTH_SHORT).show()
        Log.i("WebSocket", "Send Button was clicked")
        when (view?.id) {
            R.id.sendMessage -> webSocketClient.send(messageText?.text.toString())
        }
        messageText?.text=""
        hideKeyboard()
    }

    fun hideKeyboard() {
        val inputManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
    }

}