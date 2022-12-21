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
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.util.*


class AddPostFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var galleryRequestCode = 1000
    private lateinit var img: ImageView
    private lateinit var GalleryButton: Button

    private lateinit var postPicture: ImageView

    private lateinit var filePath: Uri

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_post, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this
        (activity as DashboardActivity?)!!.supportActionBar!!.hide()

        /* //works
         setHasOptionsMenu(true);*/

        //removing existing toolbar from Dashboard Activity
        //val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.addPost_toolbar)

        //(activity as DashboardActivity?)!!.setSupportActionBar(toolbar)


        /*val ParentToolbar = requireActivity().findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        println(ParentToolbar)
        var menu= ParentToolbar.menu
        println(menu)
        menu.findItem(R.id.nav_notifications).isVisible = true
        menu.findItem(R.id.nav_search).isVisible = false*/

        //but aba place it back in other activities


        img = view.findViewById<ImageView>(R.id.picture_to_be_posted)
        GalleryButton = view.findViewById<Button>(R.id.btnGallery)

        GalleryButton.setOnClickListener {
            //val intent= Intent(Intent.ACTION_PICK)
            //intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select your profile picture"),
                galleryRequestCode
            )
        }

        storage = FirebaseStorage.getInstance()
        //storage = Firebase.storage
        storageReference = storage.reference

        //submit button for adding post
        postPicture = view.findViewById(R.id.post_picture)
        postPicture.setOnClickListener {
            uploadImage()
        }

        val spinner: Spinner = view.findViewById(R.id.spinnerCategory)
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

            spinner.onItemSelectedListener = this
        }
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
            val ref = storageReference
                .child(
                    "images/"
                            + UUID.randomUUID().toString()
                )

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                .addOnSuccessListener { taskSnapshot -> // Image uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss()
                    Toast
                        .makeText(
                            context,
                            "Image Uploaded!!",
                            Toast.LENGTH_SHORT
                        )
                        .show()

                    val downloadUrl: Task<Uri> = taskSnapshot.storage.downloadUrl
                    downloadUrl.addOnCompleteListener { task ->
                        Log.v(TAG, "Media is uploaded")
                        val downloadURL = ("https://" + task.result.encodedAuthority
                                + task.result.encodedPath
                            .toString() + "?alt=media&token="
                                + task.result.getQueryParameters("token")[0])
                        Log.v(TAG, "downloadURL: $downloadURL")

                        //save the downloadURL to the database
                        println("Final download URL: $downloadURL")
                    }
                }
                .addOnFailureListener { e -> // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast
                        .makeText(
                            context,
                            "Failed " + e.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
                .addOnProgressListener { taskSnapshot ->

                    // Progress Listener for loading
                    // percentage on the dialog box
                    val progress = (100.0
                            * taskSnapshot.bytesTransferred
                            / taskSnapshot.totalByteCount)
                    progressDialog.setMessage(
                        "Uploaded "
                                + progress.toInt() + "%"
                    )
                }
        }
    }

    //works
    /*override fun onPrepareOptionsMenu(menu: Menu) {
        *//*menu.clear()*//*
        val item: MenuItem = menu.findItem(R.id.nav_search)
        val item1: MenuItem = menu.findItem(R.id.nav_notifications)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.setVisible(false)
    }
*/
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
                            requireContext().contentResolver,
                            filePath
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