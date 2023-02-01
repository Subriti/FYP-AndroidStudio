package com.example.notificationpermissions.Fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.R

class ChatFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this
        /* //adding back the appbar
         (activity as DashboardActivity?)!!.supportActionBar!!.show()*/
        return view
    }
}