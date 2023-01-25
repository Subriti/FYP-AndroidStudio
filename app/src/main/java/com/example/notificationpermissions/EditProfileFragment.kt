package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hbb20.CountryCodePicker
import java.io.IOException
import java.util.*


class EditProfileFragment : Fragment() {
    private var galleryRequestCode = 1000
    private lateinit var imgGallery: ImageView
    private lateinit var imgButton: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var saveChanges: Button

    private var filePath: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var imgURL: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        //user display picture
        imgGallery = view.findViewById<ImageView>(R.id.profile_picture)
        context?.let {
            Glide.with(it).load(App.sharedPrefs.profilePicture).into(imgGallery)
        }

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        val name = view.findViewById<TextView>(R.id.nameText)
        val email = view.findViewById<TextView>(R.id.emailText)
        val dateOfBirth = view.findViewById<TextView>(R.id.dateText)
        val location = view.findViewById<TextView>(R.id.locationText)
        val phoneNumber = view.findViewById<TextView>(R.id.phoneText)
        saveChanges = view.findViewById(R.id.saveChanges)
        val changePassword = view.findViewById<TextView>(R.id.changePassword)

        name.text = App.sharedPrefs.userName
        email.text = App.sharedPrefs.userEmail
        dateOfBirth.text = (App.sharedPrefs.dateOfBirth).subSequence(0, 10)
        phoneNumber.text = App.sharedPrefs.phoneNumber
        location.text = App.sharedPrefs.location

        imgButton = view.findViewById<Button>(R.id.choosePhoto)
        imgButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            startActivityForResult(intent, galleryRequestCode)
        }

        val pickDatebtn = view.findViewById<ImageView>(R.id.pickDatebtn)
        pickDatebtn.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datepickerdialog = DatePickerDialog(
                requireContext(), { _, mYear, mMonth, mDay ->
                    dateOfBirth.text = "" + mDay + "/" + (mMonth + 1) + "/" + mYear
                }, year, month, day
            )

            if (datepickerdialog != null) {
                datepickerdialog.show()
            }
        }

        val phonecode = view.findViewById<CountryCodePicker>(R.id.ccp)
        phonecode.setOnCountryChangeListener {
            val selectedCode = phonecode.selectedCountryCodeAsInt
            phoneNumber.text = "+$selectedCode"
        }

        saveChanges.setOnClickListener {
            enableSpinner(true)

            println(filePath)
            //upload image first
            if (filePath != null) {
                // Defining the child of storageReference
                val ref = storageReference.child("images/" + UUID.randomUUID().toString())

                // adding listeners on upload or failure of image
                ref.putFile(filePath!!)
                    .addOnSuccessListener { taskSnapshot -> // Image uploaded successfully
                        Toast.makeText(context, "Image Uploaded!!", Toast.LENGTH_SHORT).show()

                        val downloadUrl: Task<Uri> = taskSnapshot.storage.downloadUrl
                        downloadUrl.addOnCompleteListener { task ->
                            Log.v(ContentValues.TAG, "Media is uploaded")
                            imgURL =
                                ("https://" + task.result.encodedAuthority + task.result.encodedPath.toString() + "?alt=media&token=" + task.result.getQueryParameters(
                                    "token"
                                )[0])
                            println("downloadURL: $imgURL")

                            //save updated details to the database
                            updateUser(name, email, dateOfBirth, phoneNumber, location)
                        }
                    }.addOnFailureListener { e -> // Error, Image not uploaded
                        Toast.makeText(
                            context, "Failed " + e.message, Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                imgURL=App.sharedPrefs.profilePicture
                updateUser(name, email, dateOfBirth, phoneNumber, location)
            }
        }

        changePassword.setOnClickListener {
            //open fragment to change password
            val changePasswordFragment = ChangePasswordFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.editProfileLayout, changePasswordFragment)
            transaction.addToBackStack("changePasswordFragment")
            transaction.commit()
            saveChanges.isVisible = false
            imgButton.isVisible = false
        }

        return view
    }

    private fun updateUser(name:TextView,email:TextView,dateOfBirth:TextView,phoneNumber:TextView,location:TextView) {
        AuthService.updateUser(
            name.text.toString(),
            email.text.toString(),
            dateOfBirth.text.toString(),
            phoneNumber.text.toString(),
            location.text.toString(),
            imgURL
        ) { updateSuccess ->
            println("Update User success: $updateSuccess")
            if (updateSuccess) {
                //App.shared.prefs ma ni update
                App.sharedPrefs.userName = name.text.toString()
                App.sharedPrefs.userEmail = email.text.toString()
                App.sharedPrefs.dateOfBirth = dateOfBirth.text.toString()
                App.sharedPrefs.phoneNumber = phoneNumber.text.toString()
                App.sharedPrefs.location = location.text.toString()
                App.sharedPrefs.profilePicture = imgURL

                imgButton.isVisible = false
                saveChanges.isVisible = false

                //changing label to user name in profile
                /*DashboardActivity().destination!!.label =
                    "${App.sharedPrefs.userName}"*/

               /* val profileFrag = ProfileFragment()
                val profile: FragmentTransaction =
                    requireFragmentManager().beginTransaction()
                profile.replace(R.id.editProfileLayout, profileFrag)
                profile.addToBackStack(null)
                profile.commit()*//*

                HomeFragment().adapter.notifyDataSetChanged()
                ProfileFragment().adapter.notifyDataSetChanged()*/

                val profileFragment = ProfileFragment()
                val transaction: FragmentTransaction =
                    requireFragmentManager().beginTransaction()
                transaction.replace(R.id.editProfileLayout, profileFragment)
                transaction.addToBackStack(null)
                transaction.setReorderingAllowed(true)
                transaction.commit()

                enableSpinner(false)
            }
        }
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }
        saveChanges.isEnabled = !enable
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*if (resultCode == RESULT_OK) {

            if (requestCode == galleryRequestCode) {
                //for gallery
                if (data != null) {
                    imgGallery.setImageURI(data.data)
                    filePath=data.data
                    println(filePath)
                }
            }
        }*/

        if (resultCode == RESULT_OK) {
            if (requestCode == galleryRequestCode) {
                //for gallery
                if (data != null) {
                    //img.setImageURI(data.data)
                    filePath = data.data!!
                    println("OnActivityResult: filePath is : $filePath")
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver, filePath
                        )
                        imgGallery.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }
}