package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide


class ProfileFragment : Fragment() {
    private var galleryRequestCode = 1000
    private lateinit var imgGallery: ImageView
    private lateinit var imgButton: Button

    private lateinit var imgView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        imgView = view.findViewById(R.id.downloadImageView)
        LoadImageFromFirebase("https://firebasestorage.googleapis.com/v0/b/notificationpermissions.appspot.com/o/images%2F2e5039ed-2a1c-433f-8427-a47aee5a42c9?alt=media&token=fdc0ac76-9425-4a63-8db6-f6746f80dd6e")


        imgGallery = view.findViewById<ImageView>(R.id.profile_image)
        imgButton = view.findViewById<Button>(R.id.btnGallery)

        imgView.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), imgView)

            // Inflating popup menu from popup_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.post_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                // Toast message on menu item clicked
                Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT).show()
                true
            }
            // Showing the popup menu
            popupMenu.show()
        }

        imgButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, galleryRequestCode)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            if (requestCode == galleryRequestCode) {
                //for gallery
                if (data != null) {
                    imgGallery.setImageURI(data.data)
                }
            }
        }
    }


    private fun LoadImageFromFirebase(downloadURL: String) {
        println(downloadURL)
        context?.let {
            Glide.with(it)
                .load(downloadURL)
                .into(imgView)
        }

        //database bata take out all urls ani then store it in an array
        //then recycler view huncha. tesko adapter banaudaii loop through the pictures ani add it using glide


        /*//For downloading image
          //gives image path
          println("Ref is $ref")
          // Create a reference to a file from a Google Cloud Storage URI
          val gsReference = storage.getReferenceFromUrl(ref.toString())

          val ONE_MEGABYTE: Long = 1024 * 1024
          gsReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
              // Data for "images" is returned, use this as needed
              val imageView = requireView().findViewById<ImageView>(R.id.downloadImageView2)

              // Download directly from StorageReference using Glide
              // (See MyAppGlideModule for Loader registration)
              Glide.with(this *//* context *//*)
                  .load(gsReference)
                  .into(imageView)
          }.addOnFailureListener {
              // Handle any errors
          }
*/

        //val Reference = storage.getReferenceFromUrl(ref.toString())

        //val httpsReference = storage.getReferenceFromUrl(ref.toString())

        // Reference to an image file in Cloud Storage
        //val storageReference = Firebase.storage.reference
        //println(storageReference)


        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
    }
}