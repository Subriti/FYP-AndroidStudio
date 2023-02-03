package com.example.notificationpermissions.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.R
import tech.gusavila92.websocketclient.WebSocketClient
import java.net.URI
import java.net.URISyntaxException

class ChatFragment : Fragment(), OnClickListener{
    var serverMessage: TextView? =null
    var messageText: TextView? =null
    lateinit var webSocketClient: WebSocketClient

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
        return view
    }
    private fun createWebSocketClient() {
        val uri: URI = try {
            // Connect to local host
            URI("ws://192.168.1.109:8080/api/messageSocket")
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

                    try {
                        serverMessage?.text = s
                        println(s)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
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
    }
}