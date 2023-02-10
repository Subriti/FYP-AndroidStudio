package com.example.notificationpermissions.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Activities.LoginActivity
import com.example.notificationpermissions.Adapters.FeedRecyclerAdapter
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

        PostService.getAllPosts { complete ->
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
            if (PostService.getAllPostError is AuthFailureError) {
                println("Session expired. Please Login again.")
                if (activity != null) {
                    activity?.runOnUiThread {
                        alertDialog()
                    }
                }
            }
            if (PostService.getAllPostError is NoConnectionError){
                println("you aren't connected to internet try again later")
                val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                builder.setMessage("You seem to have problems with your internet connection")
                    .setCancelable(false)
                    .setPositiveButton("RETRY") { _, _ ->
                        // stay on Home Fragment until internet restores
                        view?.findNavController()
                            ?.navigate(R.id.action_homeFragment_self)
                    }
                builder.create().show()
            }
        }
        return view
    }

    private fun alertDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        if (activity == null) {
            return
        }
        builder.setMessage("Session has expired. Please login again.")
            .setCancelable(false)
            .setPositiveButton("LOGIN") { _, _ ->
                // redirect to the login page because JWT has expired
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        builder.create().show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val item= parent?.selectedItem
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}