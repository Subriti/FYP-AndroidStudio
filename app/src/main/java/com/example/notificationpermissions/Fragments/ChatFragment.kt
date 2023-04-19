package com.example.notificationpermissions.Fragments

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Adapters.ChatRoomAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.MessageService
import com.example.notificationpermissions.Utilities.EXTRA_CHAT_ROOM

class ChatFragment : Fragment() {
    lateinit var chatRoomAdapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        val noDataText = view.findViewById<TextView>(R.id.noDataTextView)

        fun getUserChatRooms() {
            MessageService.getChatRooms { getChatRooms ->
                if (getChatRooms) {
                    if (MessageService.map.isNotEmpty()) {
                        noDataText.visibility = View.GONE
                        for (i in MessageService.map) {
                            MessageService.findUser(i.key, i.value) { findUser ->
                                checkIfFragmentAttached {
                                    chatRoomAdapter =
                                        ChatRoomAdapter(
                                            requireContext().applicationContext,
                                            MessageService.userChatRooms
                                        ) { userchat ->
                                            //on Click do something--> open individual chat room
                                            view.findNavController()
                                                .navigate(
                                                    R.id.action_chatFragment_to_individualChatRoomFragment,
                                                    Bundle().apply {
                                                        putSerializable(
                                                            EXTRA_CHAT_ROOM,
                                                            userchat
                                                        )
                                                    })

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
                    noDataText.text = "Chat Rooms could not be loaded"
                }
            }
        }
        getUserChatRooms()
        return view
    }

    override fun onResume() {
        super.onResume()
        // Invalidate the options menu to force onPrepareOptionsMenu to be called again
        activity?.invalidateOptionsMenu()
    }

    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }
}