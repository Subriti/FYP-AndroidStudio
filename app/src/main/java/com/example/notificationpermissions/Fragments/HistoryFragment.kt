package com.example.notificationpermissions.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.R

class HistoryFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this
        return view
    }
}