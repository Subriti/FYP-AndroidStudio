package com.example.notificationpermissions.Fragments

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
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentViewHolder
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddPostFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private var galleryRequestCode = 1000
    private lateinit var img: ImageView
    private lateinit var GalleryButton: Button

    private lateinit var postPicture: ImageView
    private lateinit var cancelPost: ImageView

    private lateinit var filePath: Uri

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val PERMISSION_LOCATION_REQUEST_CODE = 1
    private lateinit var locationTxt: TextView
    private lateinit var findLocation: ImageView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        postSpinner = view.findViewById(R.id.postSpinner)
        postSpinner.visibility = View.INVISIBLE

        (activity as DashboardActivity?)!!.currentFragment = this
        (activity as DashboardActivity?)!!.supportActionBar!!.hide()

        img = view.findViewById(R.id.picture_to_be_posted)
        GalleryButton = view.findViewById(R.id.btnGallery)

        GalleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select picture to post"), galleryRequestCode
            )
        }

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

        desc = view.findViewById(R.id.description)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        //submit button for adding post
        postPicture = view.findViewById(R.id.post_picture)
        postPicture.setOnClickListener {
            uploadImage()
        }

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
            spinnerCategory.setSelection(0)
            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view:View?, position: Int, id: Long
                ) {
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
            spinnerItemCategory.setSelection(0)
            spinnerItemCategory.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        //val item = parent.getItemAtPosition(position).toString()
                        spinnerItemCategory.setSelection(position)
                        itemCategory = (position + 6).toString()
                        //spinnerItemCategory.selectedItem as String
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
            spinnerClothSize.setSelection(0)

        }
        spinnerClothSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
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
            spinnerClothCondition.setSelection(0)
        }
        spinnerClothCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
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
            spinnerClothSeason.setSelection(0)
        }
        spinnerClothSeason.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
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
                parent: AdapterView<*>, view: View?, position: Int, id: Long
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

    // UploadImage method
    private fun uploadImage() {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            // Defining the child of storageReference
            val ref = storageReference.child(
                "images/" + UUID.randomUUID().toString()
            )

            // adding listeners on upload or failure of image
            ref.putFile(filePath)
                .addOnSuccessListener { taskSnapshot -> // Image uploaded successfully
                    // Dismiss dialog
                    enableSpinner(true)
                    progressDialog.dismiss()
                    Toast.makeText(
                        context, "Image Uploaded!!", Toast.LENGTH_SHORT
                    ).show()

                    val downloadUrl: Task<Uri> = taskSnapshot.storage.downloadUrl
                    downloadUrl.addOnCompleteListener { task ->
                        val downloadURL =
                            ("https://" + task.result.encodedAuthority + task.result.encodedPath.toString() + "?alt=media&token=" + task.result.getQueryParameters(
                                "token"
                            )[0])

                        //save the downloadURL to the database
                        println("Final download URL: $downloadURL")
                        PostService.addCloth(
                            category, itemCategory, clothSize, clothCondition, clothSeason
                        ) { createSuccess ->
                            println("Add Cloth success: $createSuccess")
                            if (createSuccess) {
                                PostService.createPost(
                                    App.sharedPrefs.userID,
                                    downloadURL,
                                    desc.text.toString() + "\n\n$clothDelivery",
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time),
                                    locationTxt.text.toString(),
                                    PostService.clothId,
                                    "1",
                                ) { createSuccess ->
                                    println("Create Post success: $createSuccess")
                                    if (createSuccess) {
                                        //get back to homeFragment
                                       /* view?.findNavController()
                                            ?.navigate(R.id.action_addPostFragment_to_homeFragment)
*/
                                        val intent= Intent(context, DashboardActivity::class.java)
                                        startActivity(intent)

                                        Toast.makeText(
                                            requireContext(),
                                            "Post was successfully created",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        enableSpinner(false)
                                    }
                                }
                            }
                        }
                    }
                }.addOnFailureListener { e -> // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast.makeText(
                        context, "Failed " + e.message, Toast.LENGTH_SHORT
                    ).show()
                }.addOnProgressListener { taskSnapshot ->

                    // Progress Listener for loading percentage on the dialog box
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    progressDialog.setMessage(
                        "Uploaded " + progress.toInt() + "%"
                    )
                }
        }
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            postSpinner.visibility = View.VISIBLE
        } else {
            postSpinner.visibility = View.INVISIBLE
        }
        postPicture.isEnabled = !enable
        cancelPost.isEnabled = !enable
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            if (requestCode == galleryRequestCode) {
                //for gallery
                if (data != null) {
                    //img.setImageURI(data.data)
                    filePath = data.data!!

                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver, filePath
                        )
                        img.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }
}