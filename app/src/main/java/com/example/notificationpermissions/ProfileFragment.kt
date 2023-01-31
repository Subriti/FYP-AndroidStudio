package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ProfileFragment : Fragment() {
    private var galleryRequestCode = 1000
    private lateinit var imgGallery: ImageView
    private lateinit var imgButton: Button

    private lateinit var imgView: ImageView

    lateinit var adapter: PostRecycleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        /*imgView = view.findViewById(R.id.downloadImageView)
        LoadImageFromFirebase("https://firebasestorage.googleapis.com/v0/b/notificationpermissions.appspot.com/o/images%2F2e5039ed-2a1c-433f-8427-a47aee5a42c9?alt=media&token=fdc0ac76-9425-4a63-8db6-f6746f80dd6e")
        *///recycler view halna parcha
        //LoadImageFromFirebase(App.sharedPrefs.profilePicture)

        //user display picture
        imgGallery = view.findViewById(R.id.profile_image)
        context?.let {
            Glide.with(it).load(App.sharedPrefs.profilePicture).into(imgGallery)
        }

        val location = view.findViewById<TextView>(R.id.UserLocation)
        val phoneNumber = view.findViewById<TextView>(R.id.UserPhone)
        val email = view.findViewById<TextView>(R.id.UserEmail)

        email.text = "  ${App.sharedPrefs.userEmail}"
        phoneNumber.text = "  ${App.sharedPrefs.phoneNumber}"
        location.text = "  ${App.sharedPrefs.location}"

        /* imgView.setOnClickListener {
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
         }*/


        imgButton = view.findViewById<Button>(R.id.editProfile)
        imgButton.setOnClickListener {
            imgButton.isVisible=false
            val editProfileFragment = EditProfileFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.profile_fragment, editProfileFragment)
            //transaction.addToBackStack("editProfileFragment")
            transaction.addToBackStack(null)
            transaction.setReorderingAllowed(true)
            transaction.commit()
        }


        //logout is fixed in navBar

        /* val logoutBtn= view.findViewById<ImageView>(R.id.logoutBtn)
         logoutBtn.setOnClickListener{
             UserDataService.logout()
             val intent= Intent(context, MainActivity::class.java)
             startActivity(intent)
         }*/


        PostService.getUserPosts(App.sharedPrefs.userID) { complete ->
            if (complete) {
                var imageUrlsList = mutableListOf<String>()
                for (url in PostService.posts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = PostRecycleAdapter(requireContext(), imageUrlsList) {
                    //do something on click; open full post details
                }
                var spanCount = 2
                val orientation = resources.configuration.orientation
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    spanCount = 3
                }

                val layoutManager = GridLayoutManager(context, spanCount)
                val postRV = view.findViewById<RecyclerView>(R.id.userPostsRecyclerView)
                postRV.layoutManager = layoutManager
                postRV.adapter = adapter
            }
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
            Glide.with(it).load(downloadURL).into(imgView)
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