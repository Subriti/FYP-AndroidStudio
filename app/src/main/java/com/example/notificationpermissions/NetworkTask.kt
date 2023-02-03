package com.example.notificationpermissions

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

class NetworkTask : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            val SERVER_IP = "192.168.1.109"
            val SERVER_PORT = 9090

            val clientSocket = Socket(SERVER_IP, SERVER_PORT)
            val outToServer = DataOutputStream(clientSocket.getOutputStream())
            val inFromServer = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

            // Your code to send and receive data through the socket connection
            // Send the message to the server
            val message = "client_1: Hello from Android Studio!"
            outToServer.writeBytes(
                """
            $message
            """.trimIndent()
            )
            // Receive a response from the server
            val response: String = inFromServer.readLine()
            println("FROM SERVER: $response")
            clientSocket.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
