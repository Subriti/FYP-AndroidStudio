package com.example.notificationpermissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
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
    private lateinit var category: String
    private lateinit var itemCategory: String
    private lateinit var clothCondition: String
    private lateinit var clothSize: String
    private lateinit var clothSeason: String
    private lateinit var clothDelivery: String

    private lateinit var postSpinner: ProgressBar

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_post, container, false)

        postSpinner= view.findViewById(R.id.postSpinner)
        postSpinner.visibility= View.INVISIBLE

        (activity as DashboardActivity?)!!.currentFragment = this
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

        picture= view.findViewById(R.id.picture_to_be_posted)
        context?.let {
            Glide.with(it).load(postDetails.media_file).into(picture)
        }
        desc = view.findViewById(R.id.description)
        desc.text=postDetails.description

        locationTxt.text=postDetails.location

        //submit button for adding post
        editPost = view.findViewById(R.id.edit_post)
        editPost.setOnClickListener {
            enableSpinner(true)

            //PostService edit
            val json = JSONObject(postDetails.cloth_id)
            val cloth = json.getString("cloth_id")

            PostService.updateCloth(
                cloth,
                category,
                itemCategory,
                clothSize,
                clothCondition,
                clothSeason
            ) { response ->
                println("Update Cloth Response $response")
                if (response) {
                    PostService.updatePost(
                        postDetails.post_id,
                        desc.text.toString() + "\n\n$clothDelivery",
                        locationTxt.text.toString(),
                        cloth,
                       /* category,
                        itemCategory,
                        clothSize,
                        clothCondition,
                        clothSeason*/
                    ) { response ->
                        println("Update Post Response $response")
                        if (response) {
                            //get back to viewPostFragment
                            val viewFragment = ViewPostFragment()
                            val transaction: FragmentTransaction =
                                requireFragmentManager().beginTransaction()
                            transaction.replace(R.id.addPostFragment, viewFragment)
                            //transaction.addToBackStack(null)
                            //transaction.setReorderingAllowed(true)
                            transaction.commit()
                            Toast.makeText(
                                requireContext(),
                                "Post was successfully updated",
                                Toast.LENGTH_LONG
                            ).show()
                            enableSpinner(false)
                        }
                    }
                }

            }
        }
            val clothId = postDetails.cloth_id

            val clothJSONObject = JSONObject(clothId)
            val clothesSize = clothJSONObject.getString("cloth_size")
            val clothesCondition = clothJSONObject.getString("cloth_condition")
            val clothesSeason = clothJSONObject.getString("cloth_season")

            val clothCategory = clothJSONObject.getString("clothes_category_id")
            val categoryJSONObject = JSONObject(clothCategory)
            val clothesCategoryId = categoryJSONObject.getString("category_id")
            val clothesCategory = categoryJSONObject.getString("category_name")

            val itemCategoryId = clothJSONObject.getString("item_category_id")
            val itemCategoryJSONObject = JSONObject(itemCategoryId)
            val clothesItemCategoryId = itemCategoryJSONObject.getString("category_id")
            val clothesItemCategory = itemCategoryJSONObject.getString("category_name")

            cancelPost = view.findViewById(R.id.dont_post_picture)
            cancelPost.setOnClickListener {
                val intent = Intent(context, DashboardActivity::class.java)
                startActivity(intent)
            }
            val spinnerCategory: Spinner = view.findViewById(R.id.spinnerCategory)

            ArrayAdapter.createFromResource(
                requireContext(), R.array.clothCategory_array, android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerCategory.adapter = adapter

                println(clothesCategoryId.toInt())
                spinnerCategory.setSelection(clothesCategoryId.toInt()-1)
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
                spinnerItemCategory.setSelection(clothesItemCategoryId.toInt()-6)
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


            val spinnerClothSize: Spinner = view.findViewById(R.id.spinnerClothSize)
            ArrayAdapter.createFromResource(
                requireContext(), R.array.clothSizes_array, android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinnerClothSize.adapter = adapter


                for (i in 0 until spinnerClothSize.count) {
                    if (spinnerClothSize.getItemAtPosition(i) == clothesSize) {
                        spinnerClothSize.setSelection(i)
                        break
                    }
                }
                //spinnerClothSize.setSelection(0)

            }
            spinnerClothSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    //val item = parent.getItemAtPosition(position).toString()
                    spinnerClothSize.setSelection(position)
                    clothSize = spinnerClothSize.selectedItem as String
                    println("Selected cloth size: $clothSize")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // do nothing
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
                //spinnerClothCondition.setSelection(0)
                for (i in 0 until spinnerClothCondition.count) {
                    if (spinnerClothCondition.getItemAtPosition(i) == clothesCondition) {
                        spinnerClothCondition.setSelection(i)
                        break
                    }
                }
            }
            spinnerClothCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    //val item = parent.getItemAtPosition(position).toString()
                    spinnerClothCondition.setSelection(position)
                    clothCondition = spinnerClothCondition.selectedItem as String
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
                //spinnerClothSeason.setSelection(0)
                for (i in 0 until spinnerClothSeason.count) {
                    if (spinnerClothSeason.getItemAtPosition(i) == clothesSeason) {
                        spinnerClothSeason.setSelection(i)
                        break
                    }
                }
            }
            spinnerClothSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    spinnerClothSeason.setSelection(position)
                    clothSeason = spinnerClothSeason.selectedItem as String
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
                spinnerClothDelivery.setSelection(0)
            }
            spinnerClothDelivery.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    spinnerClothDelivery.setSelection(position)
                    clothDelivery = "Cloth Delivery: ${spinnerClothDelivery.selectedItem as String}"
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