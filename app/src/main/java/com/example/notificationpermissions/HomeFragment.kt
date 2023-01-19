package com.example.notificationpermissions

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

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


       /* //adding back the appbar
        (activity as DashboardActivity?)!!.supportActionBar!!.show()*/

        /*val spinner: Spinner = view.findViewById(R.id.spinnerCategoryHome)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.clothCategory_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner

            spinner.adapter = adapter

            spinner.onItemSelectedListener=this
        }*/
        PostService.getAllPosts() { complete ->
            if (complete) {
                var imageUrlsList = mutableListOf<String>()
                for (url in PostService.AllPosts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = FeedRecyclerAdapter(requireContext(), imageUrlsList) {
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