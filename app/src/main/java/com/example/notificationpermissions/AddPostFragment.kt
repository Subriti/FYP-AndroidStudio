package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*


class AddPostFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var galleryRequestCode = 1000
    private lateinit var img: ImageView
    private lateinit var GalleryButton: Button

    private lateinit var postPicture: ImageView
    private lateinit var cancelPost: ImageView

    private lateinit var filePath: Uri

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this
        (activity as DashboardActivity?)!!.supportActionBar!!.hide()

        img = view.findViewById<ImageView>(R.id.picture_to_be_posted)
        GalleryButton = view.findViewById<Button>(R.id.btnGallery)

        GalleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select picture to post"), galleryRequestCode
            )
        }

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
        val categoryValues = ArrayList<String>()
        PostService.getCategory { complete ->
            if (complete) {
                if (PostService.categories.isNotEmpty()) {
                    for (categoryName in PostService.categories) {
                        categoryValues.add(categoryName.category_name)
                    }
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter(
                    requireContext(),
                    //R.array.clothCategory_array, // esko satta bring database bata ig
                    android.R.layout.simple_spinner_item, categoryValues
                ).also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    spinnerCategory.adapter = adapter
                    spinnerCategory.setSelection(0)
                    spinnerCategory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>, view: View, position: Int, id: Long
                            ) {
                                //val item = parent.getItemAtPosition(position).toString()
                                spinnerCategory.setSelection(position)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // do nothing
                            }
                        }
                }
            }
        }

        val spinnerItemCategory: Spinner = view.findViewById(R.id.spinnerItemCategory)
        val itemCategoryValues = ArrayList<String>()
        PostService.getItemCategory { complete ->
            if (complete) {
                if (PostService.itemcategory.isNotEmpty()) {
                    for (categoryName in PostService.itemcategory) {
                        itemCategoryValues.add(categoryName.category_name)
                    }
                }
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter(
                    requireContext(),
                    //R.array.clothCategory_array, // esko satta bring database bata ig
                    android.R.layout.simple_spinner_item, itemCategoryValues
                ).also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    spinnerItemCategory.adapter = adapter
                    spinnerItemCategory.setSelection(0)
                    spinnerItemCategory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>, view: View, position: Int, id: Long
                            ) {
                                //val item = parent.getItemAtPosition(position).toString()
                                spinnerItemCategory.setSelection(position)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // do nothing
                            }
                        }
                }
            }
        }

        val spinnerClothSize: Spinner = view.findViewById(R.id.spinnerClothSize)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.clothSizes_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothSize.adapter = adapter
            spinnerClothSize.setSelection(0)}
            spinnerItemCategory.onItemSelectedListener = this

        val spinnerClothCondition: Spinner = view.findViewById(R.id.spinnerClothCondition)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.clothCondition_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothCondition.adapter = adapter
            spinnerClothCondition.setSelection(0)}
        spinnerClothCondition.onItemSelectedListener = this

        val spinnerClothSeason: Spinner = view.findViewById(R.id.spinnerClothSeason)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.clothSeason_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothSeason.adapter = adapter
            spinnerClothSeason.setSelection(0)}
        spinnerClothSeason.onItemSelectedListener = this

        val spinnerClothDelivery: Spinner = view.findViewById(R.id.spinnerClothDelivery)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.clothDelivery_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerClothDelivery.adapter = adapter
            spinnerClothDelivery.setSelection(0)}
        spinnerClothDelivery.onItemSelectedListener = this

        return view
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

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                .addOnSuccessListener { taskSnapshot -> // Image uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss()
                    Toast.makeText(
                            context, "Image Uploaded!!", Toast.LENGTH_SHORT
                        ).show()

                    val downloadUrl: Task<Uri> = taskSnapshot.storage.downloadUrl
                    downloadUrl.addOnCompleteListener { task ->
                        Log.v(TAG, "Media is uploaded")
                        val downloadURL =
                            ("https://" + task.result.encodedAuthority + task.result.encodedPath.toString() + "?alt=media&token=" + task.result.getQueryParameters(
                                "token"
                            )[0])
                        Log.v(TAG, "downloadURL: $downloadURL")

                        //save the downloadURL to the database
                        println("Final download URL: $downloadURL")
                    }
                }.addOnFailureListener { e -> // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast.makeText(
                            context, "Failed " + e.message, Toast.LENGTH_SHORT
                        ).show()
                }.addOnProgressListener { taskSnapshot ->

                    // Progress Listener for loading
                    // percentage on the dialog box
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    progressDialog.setMessage(
                        "Uploaded " + progress.toInt() + "%"
                    )
                }
        }
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val item = parent?.selectedItem
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}