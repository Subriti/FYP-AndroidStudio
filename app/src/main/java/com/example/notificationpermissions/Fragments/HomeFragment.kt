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
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.NoConnectionError
import com.example.notificationpermissions.Activities.LoginActivity
import com.example.notificationpermissions.Adapters.FeedGridRecyclerAdapter
import com.example.notificationpermissions.Adapters.FeedRecyclerAdapter
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.EXTRA_POST


class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener{

    lateinit var feedAdapter: FeedRecyclerAdapter
    lateinit var gridAdapter: FeedGridRecyclerAdapter
    var selected_filter = "1"
    var selected_item = "1"

    var filterAdapterItem: Int = R.array.clothCategory_array

    var viewSelected = "List"

    lateinit var postRV: RecyclerView
    lateinit var noDataText: TextView

    lateinit var filterCard: CardView


    var imageUrlsList = mutableListOf<String>()
    var filteredPostList = arrayListOf<Post>()

    var initialLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        //(activity as DashboardActivity?)!!.currentFragment = this


        //gives a spinner search text for searching in array
        val spinnerSearch = view.findViewById<AutoCompleteTextView>(R.id.spinner_search)
        spinnerSearch.isVisible = false

        val array = resources.getStringArray(R.array.clothCategory_array)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, array)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSearch.setAdapter(adapter)
        spinnerSearch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                spinnerSearch.setSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //Rough Codes
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.isVisible = false

        val searchTextView = searchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
        searchTextView.isVisible = false


        filterCard = view.findViewById(R.id.filterCard)
        filterCard.isVisible = false

        getAllPost()

        //val layoutChooser= view.findViewById<LinearLayout>(R.id.linearLayout)

        //broad filter
        val spinnerFilter: Spinner = view.findViewById(R.id.spinner)
        val selectedText: TextView = view.findViewById(R.id.selectedItem)
        //selected filter items
        val spinnerItem: Spinner = view.findViewById(R.id.spinner2)

        //adding adapter to the filter array
        ArrayAdapter.createFromResource(
            requireContext(), R.array.filterBy_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerFilter.adapter = adapter
            spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    spinnerFilter.setSelection(position)
                    selected_filter = (position + 1).toString()

                    when (selected_filter) {
                        "1" -> {
                            selectedText.text = "General Category: "
                            filterAdapterItem = R.array.clothCategory_array
                        }
                        "2" -> {
                            selectedText.text = "Item Category: "
                            filterAdapterItem = R.array.itemCategory_array
                        }
                        "3" -> {
                            selectedText.text = "Available Locations: "
                            filterAdapterItem = R.array.location_array
                        }
                        "4" -> {
                            selectedText.text = "Cloth Sizes: "
                            filterAdapterItem = R.array.clothSizes_array
                        }
                        "5" -> {
                            selectedText.text = "Cloth Condition: "
                            filterAdapterItem = R.array.clothCondition_array
                        }
                        "6" -> {
                            selectedText.text = "Cloth Season: "
                            filterAdapterItem = R.array.clothSeason_array
                        }
                    }
                    loadAdapterItem(spinnerItem, filterAdapterItem)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
            spinnerFilter.setSelection(0)
        }


        noDataText = view.findViewById(R.id.noDataTextView)

        postRV = view.findViewById(R.id.feedRecyclerView)


        val listView = view.findViewById<ImageView>(R.id.listView)
        val gridView = view.findViewById<ImageView>(R.id.gridView)

        listView.setOnClickListener {
            listView.setImageResource(R.drawable.selected_list)
            gridView.setImageResource(R.drawable.unselected_grid)
            viewSelected = "List"

            setAdapter(imageUrlsList)
        }

        gridView.setOnClickListener {
            gridView.setImageResource(R.drawable.selected_grid)
            listView.setImageResource(R.drawable.unselected_list)
            viewSelected = "Grid"

            setGridAdapter(imageUrlsList)
        }
        return view
    }

    private fun getAllPost() {
        imageUrlsList.clear()
        filteredPostList.clear()
        selected_filter = "1"
        initialLoad = true
        selected_item = "1"

        filterAdapterItem = R.array.clothCategory_array
        println("in getAll Post")

        // Code for showing progressDialog while getting posts from server
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Refreshing your feed...")
        progressDialog.show()

        PostService.getAllPosts { complete ->
            if (complete) {
                if (PostService.AllPosts.isNotEmpty()) {
                    for (post in PostService.AllPosts) {
                        imageUrlsList.add(post.media_file)
                        filteredPostList.add(post)
                        println(imageUrlsList.size)
                        println(filteredPostList.size)
                    }
                    noDataText.visibility = View.GONE
                    setAdapter(imageUrlsList)
                    progressDialog.dismiss()
                } else if (PostService.AllPosts.isEmpty()) {
                    noDataText.visibility = View.VISIBLE
                }
            } else {
                noDataText.visibility = View.VISIBLE
                noDataText.text = "Feed could not be loaded"
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
            if (PostService.getAllPostError is NoConnectionError) {
                progressDialog.dismiss()
                println("you aren't connected to internet try again later")
                val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                builder.setMessage("You seem to have problems with your internet connection")
                    .setCancelable(false).setPositiveButton("RETRY") { _, _ ->
                        // stay on Home Fragment until internet restores
                        view?.findNavController()?.navigate(R.id.action_homeFragment_self)
                    }
                builder.create().show()
            }
        }
        PostService.getAllPostError = null
    }

    private fun loadAdapterItem(spinnerItem: Spinner, filterAdapterItem: Int) {
        ArrayAdapter.createFromResource(
            requireContext(), filterAdapterItem, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerItem.adapter = adapter
            spinnerItem.setSelection(0)
            spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    spinnerItem.setSelection(position)
                    if (selected_filter == "2") {
                        selected_item = (position + 6).toString()
                        println("Selected item : $selected_item")

                        imageUrlsList.clear()
                        filteredPostList.clear()

                        //sort the feed according to selected item category.
                        for (p in PostService.AllPosts) {
                            for (c in PostService.clothes) {
                                if (p.media_file == c.cloth_media && !filteredPostList.contains(p)) {
                                    println("Item Category is " + c.item_category_id)
                                    println("Selected item is " + selected_item)
                                    if (c.item_category_id == selected_item) {
                                        imageUrlsList.add(p.media_file)
                                        filteredPostList.add(p)
                                    }

                                    if (imageUrlsList.isEmpty()) {
                                        noDataText.visibility = View.VISIBLE
                                    } else {
                                        noDataText.visibility = View.GONE
                                    }
                                    if (viewSelected == "List") {
                                        setAdapter(imageUrlsList)
                                    }
                                    if (viewSelected == "Grid") {
                                        setGridAdapter(imageUrlsList)
                                    }
                                }
                            }
                        }
                    } else {
                        selected_item = (position + 1).toString()

                        //sort on the basis of men's, women's or unisex clothing
                        when (selected_filter) {
                            "1" -> {
                                if (!initialLoad) {
                                    imageUrlsList.clear()
                                    filteredPostList.clear()

                                    for (p in PostService.AllPosts) {
                                        for (c in PostService.clothes) {
                                            if (p.media_file == c.cloth_media && !filteredPostList.contains(
                                                    p
                                                )
                                            ) {
                                                if (c.clothes_category_id == selected_item) {
                                                    imageUrlsList.add(p.media_file)
                                                    filteredPostList.add(p)
                                                }
                                                if (imageUrlsList.isEmpty()) {
                                                    noDataText.visibility = View.VISIBLE
                                                } else {
                                                    noDataText.visibility = View.GONE
                                                }
                                                if (viewSelected == "List") {
                                                    setAdapter(imageUrlsList)
                                                }
                                                if (viewSelected == "Grid") {
                                                    setGridAdapter(imageUrlsList)
                                                }
                                            }
                                        }
                                    }
                                }
                                //now if some filter selected, sort on the basis of it
                                initialLoad = false
                            }
                            //sort on the basis of location
                            "3" -> {
                                imageUrlsList.clear()
                                filteredPostList.clear()

                                for (p in PostService.AllPosts) {
                                    println("Post Id is " + p.post_id)
                                    println("Location is " + p.post_id)
                                    print("Selected Location is " + spinnerItem.selectedItem.toString())
                                    if (p.location.contains(spinnerItem.selectedItem.toString()) && !filteredPostList.contains(
                                            p
                                        )
                                    ) {
                                        imageUrlsList.add(p.media_file)
                                        filteredPostList.add(p)
                                    }
                                    if (imageUrlsList.isEmpty()) {
                                        noDataText.visibility = View.VISIBLE
                                    } else {
                                        noDataText.visibility = View.GONE
                                    }

                                    if (viewSelected == "List") {
                                        setAdapter(imageUrlsList)
                                    }
                                    if (viewSelected == "Grid") {
                                        setGridAdapter(imageUrlsList)
                                    }
                                }
                            }
                            //sort on the basis of size
                            "4" -> {
                                imageUrlsList.clear()
                                filteredPostList.clear()

                                for (p in PostService.AllPosts) {
                                    println("Post Id is " + p.post_id)
                                    for (c in PostService.clothes) {
                                        if (p.media_file == c.cloth_media && !filteredPostList.contains(
                                                p
                                            )
                                        ) {
                                            println("Cloth Size is " + c.cloth_size)
                                            print("Selected Size is " + spinnerItem.selectedItem.toString())
                                            if (c.cloth_size == spinnerItem.selectedItem.toString()) {
                                                imageUrlsList.add(p.media_file)
                                                filteredPostList.add(p)
                                            }
                                            if (imageUrlsList.isEmpty()) {
                                                noDataText.visibility = View.VISIBLE
                                            } else {
                                                noDataText.visibility = View.GONE
                                            }

                                            if (viewSelected == "List") {
                                                setAdapter(imageUrlsList)
                                            }
                                            if (viewSelected == "Grid") {
                                                setGridAdapter(imageUrlsList)
                                            }
                                        }
                                    }
                                }
                            }
                            //sort on the basis of condition
                            "5" -> {
                                imageUrlsList.clear()
                                filteredPostList.clear()

                                for (p in PostService.AllPosts) {
                                    println("Post Id is " + p.post_id)
                                    for (c in PostService.clothes) {
                                        if (p.media_file == c.cloth_media && !filteredPostList.contains(
                                                p
                                            )
                                        ) {
                                            println("Cloth Condition is " + c.cloth_condition)
                                            print("Selected Condition is " + spinnerItem.selectedItem.toString())
                                            if (c.cloth_condition == spinnerItem.selectedItem.toString()) {
                                                println("equal")
                                                imageUrlsList.add(p.media_file)
                                                filteredPostList.add(p)
                                            }
                                            if (imageUrlsList.isEmpty()) {
                                                noDataText.visibility = View.VISIBLE
                                            } else {
                                                noDataText.visibility = View.GONE
                                            }

                                            if (viewSelected == "List") {
                                                setAdapter(imageUrlsList)
                                            }
                                            if (viewSelected == "Grid") {
                                                setGridAdapter(imageUrlsList)
                                            }
                                        }
                                    }
                                }
                            }

                            //sort on the basis of season
                            "6" -> {
                                imageUrlsList.clear()
                                filteredPostList.clear()

                                for (p in PostService.AllPosts) {
                                    println("Post Id is " + p.post_id)
                                    for (c in PostService.clothes) {
                                        if (p.media_file == c.cloth_media && !filteredPostList.contains(
                                                p
                                            )
                                        ) {
                                            println("Cloth Season is " + c.cloth_season)
                                            print("Selected Season is " + spinnerItem.selectedItem.toString())
                                            if (c.cloth_season == spinnerItem.selectedItem.toString()) {
                                                println("equal")
                                                imageUrlsList.add(p.media_file)
                                                filteredPostList.add(p)
                                            }
                                            if (imageUrlsList.isEmpty()) {
                                                noDataText.visibility = View.VISIBLE
                                            } else {
                                                noDataText.visibility = View.GONE
                                            }

                                            if (viewSelected == "List") {
                                                setAdapter(imageUrlsList)
                                            }
                                            if (viewSelected == "Grid") {
                                                setGridAdapter(imageUrlsList)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
        }
    }


    private fun setAdapter(imageUrlsList: List<String>) {
        feedAdapter = FeedRecyclerAdapter(requireContext(), imageUrlsList, filteredPostList) {}
        val layoutManager = LinearLayoutManager(context)
        postRV.layoutManager = layoutManager
        postRV.adapter = feedAdapter
    }

    private fun setGridAdapter(imageUrlsList: List<String>) {
        gridAdapter =
            FeedGridRecyclerAdapter(requireContext(), imageUrlsList, filteredPostList) { post ->
                //do something on click; open full post details
                view?.findNavController()
                    ?.navigate(R.id.action_homeFragment_to_viewFeedItemFragment,
                        Bundle().apply { putSerializable(EXTRA_POST, post) })
                viewSelected = "List"
            }
        var spanCount = 3
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 5
        }
        val layoutManager = GridLayoutManager(context, spanCount)
        postRV.layoutManager = layoutManager
        postRV.adapter = gridAdapter
    }

    private fun alertDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        if (activity == null) {
            return
        }
        builder.setMessage("Session has expired. Please login again.").setCancelable(false)
            .setPositiveButton("LOGIN") { _, _ ->
                // redirect to the login page because JWT has expired
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                PostService.getAllPostError = null
            }
        builder.create().show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val item = parent?.selectedItem
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}