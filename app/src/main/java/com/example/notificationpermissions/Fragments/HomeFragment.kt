package com.example.notificationpermissions.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Adapters.FeedRecyclerAdapter
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App

class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var adapter: FeedRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_home, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        val welcomeUser= view.findViewById<TextView>(R.id.welcomeUser)
        welcomeUser.text= "Welcome, ${App.sharedPrefs.userName}"

        PostService.getAllPosts() { complete ->
            if (complete) {
                var imageUrlsList = mutableListOf<String>()
                for (url in PostService.AllPosts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = FeedRecyclerAdapter(requireContext(), imageUrlsList){
                    //do something on click; open full post details
                }

                val postRV = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
                val layoutManager= LinearLayoutManager(context)
                postRV.layoutManager= layoutManager
                postRV.adapter = adapter
            }
        }

        return view
    }





    /* override fun onPrepareOptionsMenu(menu: Menu) {
         *//*menu.clear()*//*
        val item: MenuItem = menu.findItem(R.id.nav_search)
        val item1: MenuItem = menu.findItem(R.id.nav_notifications)
        if (item != null) item.isVisible = true
        if (item1 != null) item1.isVisible = true
    }*/

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val item= parent?.selectedItem
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}