package com.example.notificationpermissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Utilities.BROADCAST_USER_DATA_CHANGE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hbb20.CountryCodePicker
import com.squareup.okhttp.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SignUpPageActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    lateinit var name: EditText
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var repassword: EditText
    lateinit var locationTxt: TextView
    lateinit var birthDate: TextView

    lateinit var phonecode: CountryCodePicker
    lateinit var phone: EditText
    lateinit var number: String

    private lateinit var signupbtn: Button

    lateinit var mAuth: FirebaseAuth

    // string for storing our verification ID
    lateinit var verificationId: String

    lateinit var img: ImageView
    lateinit var imgURL: String
    private var galleryRequestCode = 1000
    private var filePath: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var imageUrl: String

    private val PERMISSION_LOCATION_REQUEST_CODE = 1
    private lateinit var findLocation: ImageView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var createSpinner: ProgressBar

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_page)

        createSpinner = findViewById(R.id.progressBar)
        createSpinner.visibility = View.INVISIBLE

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
        mAuth = FirebaseAuth.getInstance()

        name = findViewById<EditText>(R.id.nameText)
        email = findViewById<EditText>(R.id.emailText)
        phone = findViewById<EditText>(R.id.phoneText)
        password = findViewById<EditText>(R.id.passwordText)
        repassword = findViewById<EditText>(R.id.repasswordText)
        phonecode = findViewById<CountryCodePicker>(R.id.ccp)

        val indicatorText = findViewById<TextView>(R.id.passwordIndicatorText)
        indicatorText.visibility = View.GONE

        img = findViewById(R.id.profile_picture)
        imgURL =
            "https://firebasestorage.googleapis.com/v0/b/notificationpermissions.appspot.com/o/images%2Fprofile_picture.png?alt=media&token=3c31f157-a0a5-42e8-83b4-d27bcac83be6"
        Glide.with(this).load(imgURL).into(img)

        img.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(
                Intent.createChooser(intent, "Select your profile picture"), galleryRequestCode
            )
        }

        storage = FirebaseStorage.getInstance()
        println(storage)
        storageReference = storage.reference
        println(storageReference)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationTxt = findViewById<TextView>(R.id.locationText)
        findLocation = findViewById(R.id.findLocation)
        findLocation.setOnClickListener {
            if (hasLocationPermissions()) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Finding your Current Location")
                progressDialog.show()

                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    println(location)
                    val geoCoder = Geocoder(this)
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

        val pickDatebtn = findViewById<ImageView>(R.id.pickDatebtn)
        birthDate = findViewById<TextView>(R.id.dateText)

        pickDatebtn.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datepickerdialog = DatePickerDialog(
                this, { view, mYear, mMonth, mDay ->
                    birthDate.text = "" + mDay + "/" + (mMonth + 1) + "/" + mYear
                }, year, month, day
            )

            if (datepickerdialog != null) {
                datepickerdialog.show()
            }
        }

        signupbtn = findViewById(R.id.saveChanges)

        signupbtn.setOnClickListener {
            if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() == repassword.text.toString()) {
                indicatorText.visibility = View.VISIBLE
                indicatorText.text = "Password Matched"
                indicatorText.setTextColor(getColor(R.color.darkgreen))

                val selectedCode = phonecode.selectedCountryCodeAsInt
                number = "+$selectedCode${phone.text}"

                enableSpinner(true)
                sendVerificationCode(number)

                //sendVerification is correct: just trying this
                //register()
            } else if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() != repassword.text.toString()) {
                indicatorText.visibility = View.VISIBLE
                indicatorText.text = "Password didn't match"
                indicatorText.setTextColor(getColor(R.color.falured))
            } else {
                Toast.makeText(
                    this, "Please fill all the fields", Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun register() {
        if (filePath == null) {
            registerUser()
        }
        println(filePath)
        if (filePath != null) {
            // Code for showing progressDialog while uploading
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()                   //later i guess no need to show this progress dialog

            // Defining the child of storageReference
            val ref = storageReference.child("images/" + UUID.randomUUID().toString())

            // adding listeners on upload or failure of image
            ref.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot -> // Image uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss()
                    Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show()

                    val downloadUrl: Task<Uri> = taskSnapshot.storage.downloadUrl
                    downloadUrl.addOnCompleteListener { task ->
                        Log.v(ContentValues.TAG, "Media is uploaded")
                        imageUrl =
                            ("https://" + task.result.encodedAuthority + task.result.encodedPath.toString() + "?alt=media&token=" + task.result.getQueryParameters(
                                "token"
                            )[0])
                        Log.v(ContentValues.TAG, "downloadURL: $imageUrl")

                        //save the imageUrl to the database
                        imgURL = imageUrl
                        registerUser()
                    }
                }.addOnFailureListener { e -> // Error, Image not uploaded
                    progressDialog.dismiss()
                    Toast.makeText(
                        this, "Failed " + e.message, Toast.LENGTH_SHORT
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

    private fun registerUser() {
        AuthService.registerUser(
            name.text.toString(),
            email.text.toString(),
            password.text.toString(),
            SimpleDateFormat("yyyy-MM-dd").format(SimpleDateFormat("dd/MM/yyyy").parse(birthDate.text.toString())),
            number,
            locationTxt.text.toString(),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time),
            //imageUrl //should me imgURL ig bc img selected chaina vane feri error aucha
            imgURL
        ) { createSuccess ->
            println("Register User success: $createSuccess")
            if (createSuccess) {
                AuthService.loginUser(
                    email.text.toString(), password.text.toString()
                ) { loginSuccess ->
                    println(loginSuccess)
                    if (loginSuccess) {
                        val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                        // we are sending our user to new activity.
                        val i = Intent(this@SignUpPageActivity, DashboardActivity::class.java)
                        startActivity(i)
                        finish()
                        enableSpinner(false)
                        finish()
                    } else {
                        errorToast()
                    }
                }
            } else {
                Toast.makeText(
                    this, "Make sure all the fields are filled in.", Toast.LENGTH_LONG
                ).show()
                enableSpinner(false)
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
                    println("OnActivityResult: filePath is : $filePath")
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver, filePath
                        )
                        img.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun hasLocationPermissions() =
        EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)

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
            SettingsDialog.Builder(this).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }
        signupbtn.isEnabled = !enable
    }

    //using firebase
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        // inside this method we are checking if the code entered is correct or not.

        mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // if the OTP code is correct and the task is successful, storing the user details into the database
                register()
            }
        }
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun sendVerificationCode(phone: String) {
        // this method is used for getting OTP on user phone number.
        val options =
            PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phone) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // below method is use to verify code from Firebase; callback method is called on Phone auth provider.
    // initializing our callbacks for on verification callback method.
    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {

            // below method is used when OTP is sent from Firebase
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                // when we receive the OTP it contains a unique id which we are storing in our string which we have already created.
                verificationId = s
            }

            // this method is called when user receive OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                enableSpinner(false)
                //show OTP verification dialogBox
                showAlertDialog(phoneAuthCredential)
            }

            // this method is called when firebase doesn't send our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                // displaying error message with firebase exception.
                Toast.makeText(this@SignUpPageActivity, e.message, Toast.LENGTH_LONG).show()
                enableSpinner(false)
            }
        }

    private fun verifyCode(verifyOTP: String) {
        // below line is used for getting credentials from our verification id and code.
        enableSpinner(true)

        val credential = PhoneAuthProvider.getCredential(verificationId, verifyOTP)
        // after getting credential we are calling sign in method.
        signInWithCredential(credential)
    }

    private fun showAlertDialog(phoneAuthCredential: PhoneAuthCredential) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.otp_verification_layout, null)

        val resendOTP = dialogView.findViewById<TextView>(R.id.tvResendBtn)
        resendOTP.setOnClickListener {
            sendVerificationCode(number)
        }

        fun View?.removeSelf() {
            this ?: return
            val parentView = parent as? ViewGroup ?: return
            parentView.removeView(this)
        }
        enableSpinner(false)

        if (builder != null) {
            builder.setView(dialogView).setPositiveButton("Verify") { _, i ->
                val digit1 = dialogView.findViewById<EditText>(R.id.etC1)
                val digit2 = dialogView.findViewById<EditText>(R.id.etC2)
                val digit3 = dialogView.findViewById<EditText>(R.id.etC3)
                val digit4 = dialogView.findViewById<EditText>(R.id.etC4)
                val digit5 = dialogView.findViewById<EditText>(R.id.etC5)
                val digit6 = dialogView.findViewById<EditText>(R.id.etC6)

                val verifyOTP =
                    digit1.text.toString() + digit2.text.toString() + digit3.text.toString() + digit4.text.toString() + digit5.text.toString() + digit6.text.toString()

                if (verifyOTP == "") {
                    //removing previously set dialog to bring an new one
                    dialogView.removeSelf()
                    Toast.makeText(
                        this, "Please input the OTP", Toast.LENGTH_LONG
                    ).show()
                    builder.setView(dialogView).show()
                } else {
                    // below line is used for getting OTP code which is sent in phone auth credentials.
                    val code = phoneAuthCredential.smsCode
                    println(code)
                    // checking if the code is null or not.
                    if (code != null) {
                        // if the code is not null then we are setting that code to our OTP edittext field.
                        digit1.setText(code)
                        // after setting this code to OTP edittext field we are calling our verifycode method.
                        verifyCode(code)
                    }
                }
            }.show()
        }
    }
}