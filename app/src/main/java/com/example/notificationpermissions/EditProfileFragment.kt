package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.hbb20.CountryCodePicker
import java.util.*


class EditProfileFragment : Fragment() {
    private var galleryRequestCode = 1000
    private lateinit var imgGallery: ImageView
    private lateinit var imgButton: Button

    private lateinit var progressBar: ProgressBar
    private lateinit var saveChanges: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        //user display picture
        imgGallery = view.findViewById<ImageView>(R.id.profile_picture)
        context?.let {
            Glide.with(it)
                .load(App.sharedPrefs.profilePicture)
                .into(imgGallery)
        }

        progressBar= view.findViewById(R.id.progressBar)
        progressBar.visibility= View.INVISIBLE

        val name= view.findViewById<TextView>(R.id.nameText)
        val email= view.findViewById<TextView>(R.id.emailText)
        val dateOfBirth= view.findViewById<TextView>(R.id.dateText)
        val location = view.findViewById<TextView>(R.id.locationText)
        val phoneNumber = view.findViewById<TextView>(R.id.phoneText)
        saveChanges=view.findViewById(R.id.saveChanges)
        val changePassword=view.findViewById<TextView>(R.id.changePassword)

        name.text=App.sharedPrefs.userName
        email.text = App.sharedPrefs.userEmail
        dateOfBirth.text=(App.sharedPrefs.dateOfBirth).subSequence(0,10)
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
                requireContext(), { view, mYear, mMonth, mDay ->
                    dateOfBirth.text = "" + mDay + "/" + (mMonth + 1) + "/" + mYear
                }, year, month, day
            )

            if (datepickerdialog != null) {
                datepickerdialog.show()
            }
        }

        val phonecode= view.findViewById<CountryCodePicker>(R.id.ccp)
        phonecode.setOnCountryChangeListener {
            val selectedCode = phonecode.selectedCountryCodeAsInt
            phoneNumber.text= "+$selectedCode"
        }

        saveChanges.setOnClickListener {
            enableSpinner(true)

            //save updated details to the database
        }

        changePassword.setOnClickListener {
            //open fragment to change password
            val changePasswordFragment = ChangePasswordFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.editProfileLayout, changePasswordFragment)
            transaction.addToBackStack("changePasswordFragment")
            transaction.commit()
            saveChanges.isVisible=false
            imgButton.isVisible=false
        }

        return view
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

        if (resultCode == RESULT_OK) {

            if (requestCode == galleryRequestCode) {
                //for gallery
                if (data != null) {
                    imgGallery.setImageURI(data.data)
                }
            }
        }
    }
}