package com.example.notificationpermissions.Fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Activities.LoginActivity
import com.example.notificationpermissions.Adapters.FeedGridRecyclerAdapter
import com.example.notificationpermissions.Adapters.FeedRecyclerAdapter
import com.example.notificationpermissions.Adapters.PostRecycleAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST

class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var adapter: FeedRecyclerAdapter
    var selected_filter= ""
    var selected_item="General Category"

    var filterAdapterItem: Int = 1

    var viewSelected= "List"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_home, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

       /* val welcomeUser= view.findViewById<TextView>(R.id.welcomeUser)
        welcomeUser.text= "Welcome, ${App.sharedPrefs.userName}"*/

        // Code for showing progressDialog while getting posts from server
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Refreshing your feed...")
        progressDialog.show()

        val spinnerFilter: Spinner = view.findViewById(R.id.spinner)
        val selectedText: TextView = view.findViewById(R.id.selectedItem)
        val spinnerItem: Spinner = view.findViewById(R.id.spinner2)

        ArrayAdapter.createFromResource(
            requireContext(), R.array.clothCategory_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerItem.adapter = adapter
            spinnerItem.setSelection(0)
            spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view:View?, position: Int, id: Long
                ) {
                    spinnerItem.setSelection(position)
                    selected_item = (position + 1).toString()
                    println("Selected item : $selected_item")
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
        }

        ArrayAdapter.createFromResource(
            requireContext(), R.array.filterBy_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerFilter.adapter = adapter
            spinnerFilter.setSelection(0)
            spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view:View?, position: Int, id: Long
                ) {
                    spinnerFilter.setSelection(position)
                    selected_filter = (position + 1).toString()

                    if (selected_filter == "1") {
                        selectedText.text = "General Category: "
                        filterAdapterItem= R.array.clothCategory_array
                    } else if (selected_filter == "2") {
                        selectedText.text = "Item Category: "
                        filterAdapterItem= R.array.itemCategory_array
                    } else if (selected_filter == "3") {
                        selectedText.text = "Available Locations: "
                        filterAdapterItem= R.array.location_array
                    } else if (selected_filter == "4") {
                        selectedText.text = "Cloth Sizes: "
                        filterAdapterItem= R.array.clothSizes_array
                    } else if (selected_filter == "5") {
                        selectedText.text = "Cloth Condition: "
                        filterAdapterItem= R.array.clothCondition_array
                    } else if (selected_filter == "6") {
                        selectedText.text = "Cloth Season: "
                        filterAdapterItem= R.array.clothSeason_array
                    }

                    loadAdapterItem(spinnerItem, filterAdapterItem)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
        }

        val postRV = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        var imageUrlsList = mutableListOf<String>()

        PostService.getAllPosts { complete ->
            if (complete) {
                for (url in PostService.AllPosts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = FeedRecyclerAdapter(requireContext(), imageUrlsList){}

               /* if (viewSelected=="Grid") {
                    var spanCount = 4
                    val orientation = resources.configuration.orientation
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        spanCount = 6
                    }
                    val layoutManager = GridLayoutManager(context, spanCount)
                    postRV.layoutManager = layoutManager
                    postRV.adapter = adapter
                }
                else{*/
                    val layoutManager = LinearLayoutManager(context)
                    postRV.layoutManager = layoutManager
                    postRV.adapter = adapter
                //}
                progressDialog.dismiss()
            }
            if (PostService.getAllPostError is AuthFailureError) {
                progressDialog.dismiss()
                println("Session expired. Please Login again.")
                if (activity != null) {
                    activity?.runOnUiThread {
                        alertDialog()
                    }
                }
            }
            if (PostService.getAllPostError is NoConnectionError){
                progressDialog.dismiss()
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
        PostService.getAllPostError=null

        val listView= view.findViewById<ImageView>(R.id.listView)
        val gridView= view.findViewById<ImageView>(R.id.gridView)

        listView.setOnClickListener {
            listView.setImageResource(R.drawable.selected_list)
            gridView.setImageResource(R.drawable.unselected_grid)
            val layoutManager = LinearLayoutManager(context)
            postRV.layoutManager = layoutManager
            postRV.adapter = adapter
        }

        gridView.setOnClickListener {
            gridView.setImageResource(R.drawable.selected_grid)
            listView.setImageResource(R.drawable.unselected_list)
            val adapter = FeedGridRecyclerAdapter(requireContext(), imageUrlsList){ post ->
                println(post.post_id)
                println(post.post_by)
                println(post.created_datetime)
                println(post.location)
                println(post.description)
                println(post.media_file)
                println(post.cloth_id)
                //do something on click; open full post details
                view.findNavController()
                    .navigate(
                        R.id.action_homeFragment_to_viewFeedItemFragment,
                        Bundle().apply { putSerializable(EXTRA_POST, post) })
            }
            var spanCount = 3
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                spanCount = 5
            }
            val layoutManager = GridLayoutManager(context, spanCount)
            postRV.layoutManager = layoutManager
            postRV.adapter = adapter
        }

        return view
    }

    private fun loadAdapterItem(spinnerItem: Spinner, filterAdapterItem: Int) {
        ArrayAdapter.createFromResource(
            requireContext(),
            filterAdapterItem,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerItem.adapter = adapter
            spinnerItem.setSelection(0)
            spinnerItem.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        spinnerItem.setSelection(position)
                        selected_item = (position + 1).toString()
                        println("Selected item : $selected_item")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // do nothing
                    }
                }
        }
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
                PostService.getAllPostError=null
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