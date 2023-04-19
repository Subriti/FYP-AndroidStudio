package com.example.notificationpermissions.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.json.JSONObject
import java.util.*


class EditPostFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private lateinit var editPost: ImageView
    private lateinit var cancelPost: ImageView

    private val PERMISSION_LOCATION_REQUEST_CODE = 1
    private lateinit var locationTxt: TextView
    private lateinit var findLocation: ImageView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var picture: ImageView
    private lateinit var desc: TextView
    private  var category: String=""
    private  var itemCategory: String=""
    private  var clothCondition: String=""
    private  var clothSize: String=""
    private  var clothSeason: String=""
    private  var clothDelivery: String=""

    private lateinit var postSpinner: ProgressBar

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_post, container, false)

        postSpinner = view.findViewById(R.id.postSpinner)
        postSpinner.visibility = View.INVISIBLE

        //(activity as DashboardActivity?)!!.currentFragment = this
        (activity as DashboardActivity?)!!.supportActionBar!!.hide()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        locationTxt = view.findViewById<TextView>(R.id.locationText)
        findLocation = view.findViewById(R.id.findLocation)
        findLocation.setOnClickListener {
            if (hasLocationPermissions()) {
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Finding your Current Location")
                progressDialog.show()

                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    println(location)
                    val geoCoder = Geocoder(requireContext())
                    val currentLocation =
                        geoCoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (currentLocation!!.first().subLocality == null) {
                        locationTxt.text = currentLocation.first().locality
                    } else {
                        locationTxt.text =
                            currentLocation.first().subLocality + ", " + currentLocation.first().locality
                    }
                    progressDialog.dismiss()
                    Log.d("LOCATION", currentLocation.first().countryCode)
                    Log.d("LOCATION", currentLocation.first().locality)
                }
            } else {
                requestLocationPermission()
            }
        }

        val postDetails = arguments?.getSerializable(EXTRA_POST) as Post
        println(postDetails)

        picture = view.findViewById(R.id.picture_to_be_posted)
        context?.let {
            Glide.with(it).load(postDetails.media_file).into(picture)
        }

        desc = view.findViewById(R.id.description)

        val delivery= postDetails.description.split("\n\n")

        val deliveryy= delivery[delivery.size-1].split(": ")
        clothDelivery= deliveryy[deliveryy.size-1]

        var description= ""
        for (i in delivery){
            if (i != delivery[delivery.size-1]){
                description+=i
            }
        }
        desc.text =description

        locationTxt.text = postDetails.location

        cancelPost = view.findViewById(R.id.dont_post_picture)
        cancelPost.setOnClickListener {
            //get back to viewPostFragment
            view.findNavController().popBackStack(R.id.editPostFragment, false)
            view.findNavController()
                .navigate(R.id.action_editPostFragment_to_viewPostFragment,
                    Bundle().apply { putSerializable(EXTRA_POST, postDetails) },
                NavOptions.Builder().setPopUpTo(R.id.editPostFragment, true).build())
        }

        //submit button for adding post
        editPost = view.findViewById(R.id.edit_post)
        editPost.setOnClickListener {
            enableSpinner(true)

            //PostService edit
            val json = JSONObject(postDetails.cloth_id)
            val cloth = json.getString("cloth_id")

            PostService.updateCloth(
                cloth, category, itemCategory, clothSize, clothCondition, clothSeason
            ) { response ->
                println("Update Cloth Response $response")
                if (response) {
                    PostService.updatePost(
                        postDetails.post_id,
                        desc.text.toString() + "\n\n$clothDelivery",
                        locationTxt.text.toString(),
                        cloth,
                    ) { response ->
                        println("Update Post Response $response")
                        if (response) {
                            /*//get back to viewPostFragment
                            view.findNavController()
                                .navigate(R.id.action_editPostFragment_to_viewPostFragment,
                                    Bundle().apply { putSerializable(EXTRA_POST, postDetails) })*/

                            //get back to profileFragment
                            view.findNavController()
                                .navigate(R.id.action_editPostFragment_to_profileFragment)
                            Toast.makeText(
                                requireContext(), "Post was successfully updated", Toast.LENGTH_LONG
                            ).show()
                            enableSpinner(false)
                        }
                    }
                }

            }
        }
        val clothId = postDetails.cloth_id

        val clothJSONObject = JSONObject(clothId)
         clothSize = clothJSONObject.getString("cloth_size")
         clothCondition = clothJSONObject.getString("cloth_condition")
         clothSeason = clothJSONObject.getString("cloth_season")

        val clothCategory = clothJSONObject.getString("clothes_category_id")
        val categoryJSONObject = JSONObject(clothCategory)
        val clothesCategoryId = categoryJSONObject.getString("category_id")

        val itemCategoryId = clothJSONObject.getString("item_category_id")
        val itemCategoryJSONObject = JSONObject(itemCategoryId)
        val clothesItemCategoryId = itemCategoryJSONObject.getString("category_id")


        val spinnerCategory: Spinner = view.findViewById(R.id.spinnerCategory)

        ArrayAdapter.createFromResource(
            requireContext(), R.array.clothCategory_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerCategory.adapter = adapter

            println(clothesCategoryId.toInt())
            spinnerCategory.setSelection(clothesCategoryId.toInt() - 1)

            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    //val item = parent.getItemAtPosition(position).toString()
                    spinnerCategory.setSelection(position)
                    category = (position + 1).toString()
                    println("Selected category: $category")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
        }

        val spinnerItemCategory: Spinner = view.findViewById(R.id.spinnerItemCategory)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.itemCategory_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerItemCategory.adapter = adapter

            println(clothesItemCategoryId.toInt())
            spinnerItemCategory.setSelection(clothesItemCategoryId.toInt() - 6)

            spinnerItemCategory.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View, position: Int, id: Long
                    ) {
                        spinnerItemCategory.setSelection(position)
                        itemCategory = (position + 6).toString()
                        println("Selected item Category: $itemCategory")
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // do nothing
                    }
                }
        }

        var isInitialSelection = true // flag variable to track initial selection

        val spinnerClothSize: Spinner = view.findViewById(R.id.spinnerClothSize)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.clothSizes_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothSize.adapter = adapter

            val clothSizesArray = resources.getStringArray(R.array.clothSizes_array)
            var position = 0

            for (i in clothSizesArray.indices) {
                if (clothSizesArray[i] == clothSize) {
                    position = i
                    break
                }
            }
            spinnerClothSize.setSelection(position)

            spinnerClothSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    if (!isInitialSelection) {
                        spinnerClothSize.setSelection(position)
                        clothSize = spinnerClothSize.selectedItem.toString()
                        println("Selected cloth size: $clothSize")
                    }
                    isInitialSelection = false // set flag to false after initial selection
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
                }
            }
        }


        val spinnerClothCondition: Spinner = view.findViewById(R.id.spinnerClothCondition)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.clothCondition_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothCondition.adapter = adapter

            val clothConditionArray = resources.getStringArray(R.array.clothCondition_array)
            var position = 0

            for (i in clothConditionArray.indices) {
                if (clothConditionArray[i] == clothCondition) {
                    position = i
                    break
                }
            }
            spinnerClothCondition.setSelection(position)
        }
        spinnerClothCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                //val item = parent.getItemAtPosition(position).toString()
                spinnerClothCondition.setSelection(position)
                clothCondition = spinnerClothCondition.selectedItem.toString()
                println("Selected cloth condition: $clothCondition")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        val spinnerClothSeason: Spinner = view.findViewById(R.id.spinnerClothSeason)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.clothSeason_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothSeason.adapter = adapter

            val clothSeasonArray = resources.getStringArray(R.array.clothSeason_array)
            var position = 0

            for (i in clothSeasonArray.indices) {
                if (clothSeasonArray[i] == clothSeason) {
                    position = i
                    break
                }
            }
            spinnerClothSeason.setSelection(position)
        }
        spinnerClothSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                spinnerClothSeason.setSelection(position)
                clothSeason = spinnerClothSeason.selectedItem.toString()
                println("Selected cloth season: $clothSeason")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        val spinnerClothDelivery: Spinner = view.findViewById(R.id.spinnerClothDelivery)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.clothDelivery_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothDelivery.adapter = adapter

            val deliveryArray = resources.getStringArray(R.array.clothDelivery_array)
            var position = 0

            for (i in deliveryArray.indices) {
                if (deliveryArray[i] == clothDelivery) {
                    position = i
                    break
                }
            }
            spinnerClothDelivery.setSelection(position)
        }
        spinnerClothDelivery.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                spinnerClothDelivery.setSelection(position)
                clothDelivery = "Cloth Delivery: ${spinnerClothDelivery.selectedItem}"
                println("Selected cloth delivery: $clothDelivery")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        return view
    }

    private fun hasLocationPermissions() =
        EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without location permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            postSpinner.visibility = View.VISIBLE
        } else {
            postSpinner.visibility = View.INVISIBLE
        }
        editPost.isEnabled = !enable
        cancelPost.isEnabled = !enable
    }
}