package com.example.notificationpermissions

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.hbb20.CountryCodePicker
import com.squareup.okhttp.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SignUpPageActivity : AppCompatActivity() {

    lateinit var name: EditText
    lateinit var email: EditText
    lateinit var phone: EditText
    lateinit var password: EditText
    lateinit var repassword: EditText
    var randomNum = 123456

    lateinit var phonecode: CountryCodePicker

    lateinit var mAuth: FirebaseAuth

    // string for storing our verification ID
    lateinit var verificationId: String

    private lateinit var createSpinner: ProgressBar

    private lateinit var signupbtn:Button

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


        val pickDatebtn = findViewById<ImageView>(R.id.pickDatebtn)
        val birthDate = findViewById<TextView>(R.id.dateText)

        pickDatebtn.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datepickerdialog =
                DatePickerDialog(
                    this, { view, mYear, mMonth, mDay ->
                        birthDate.text= ""+ mDay + "/" + (mMonth + 1) + "/" + mYear
                    }, year, month, day
                )

            if (datepickerdialog != null) {
                datepickerdialog.show()
            }
        }

        signupbtn = findViewById(R.id.signUp)

        signupbtn.setOnClickListener {
            if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() == repassword.text.toString()) {
                indicatorText.visibility = View.VISIBLE
                indicatorText.text = "Password Match"
                indicatorText.setTextColor(getColor(R.color.darkgreen))

                val selectedCode = phonecode.selectedCountryCodeAsInt
                val number = "+$selectedCode${phone.text}"

                enableSpinner(true)
                sendVerificationCode(number)

            } else if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() != repassword.text.toString()) {
                indicatorText.visibility = View.VISIBLE
                indicatorText.text = "Password didn't match"
                indicatorText.setTextColor(getColor(R.color.falured))
            } else {
                Toast.makeText(
                    this,
                    "Please fill all the fields",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    fun enableSpinner(enable: Boolean) {
        println(enable)
        if (enable) {
            createSpinner.visibility = View.VISIBLE
            println("if true spinner visible")
        } else {
            println("if false spinner invisible")
            createSpinner.visibility = View.INVISIBLE
        }
        signupbtn.isEnabled = !enable
    }

    //using firebase
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // if the code is correct and the task is successful
                    // we are sending our user to new activity.
                    val i = Intent(this@SignUpPageActivity, NotificationActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    // if the code is not correct then we are
                    // displaying an error message to the user.
                    Toast.makeText(
                        this@SignUpPageActivity,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
    }

    private fun sendVerificationCode(phone: String) {
        // this method is used for getting
        // OTP on user phone number.
        // this method is used for getting
        // OTP on user phone number.
        println(phone)
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // below method is use to verify code from Firebase.
    // callback method is called on Phone auth provider.
    // initializing our callbacks for on verification callback method.
    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            // below method is used when OTP is sent from Firebase
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                // when we receive the OTP it
                // contains a unique id which
                // we are storing in our string
                // which we have already created.
                verificationId = s
            }

            // this method is called when user
            // receive OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                enableSpinner(false)
                //show OTP verification box
                showAlertDialog(phoneAuthCredential)
            }

            // this method is called when firebase doesn't
            // sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                // displaying error message with firebase exception.
                Toast.makeText(this@SignUpPageActivity, e.message, Toast.LENGTH_LONG).show()
                enableSpinner(false)
            }
        }


    private fun verifyCode(verifyOTP: String) {
        // below line is used for getting
        // credentials from our verification id and code.
        val credential = PhoneAuthProvider.getCredential(verificationId, verifyOTP)

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential)
    }


    private fun showAlertDialog(phoneAuthCredential: PhoneAuthCredential) {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.otp_verification_layout, null)

        fun View?.removeSelf() {
            this ?: return
            val parentView = parent as? ViewGroup ?: return
            parentView.removeView(this)
        }

        if (builder != null) {
            builder.setView(dialogView)
                .setPositiveButton("Verify") { _, i ->
                    val digit1= dialogView.findViewById<EditText>(R.id.etC1)
                    val digit2= dialogView.findViewById<EditText>(R.id.etC2)
                    val digit3= dialogView.findViewById<EditText>(R.id.etC3)
                    val digit4= dialogView.findViewById<EditText>(R.id.etC4)
                    val digit5= dialogView.findViewById<EditText>(R.id.etC5)
                    val digit6= dialogView.findViewById<EditText>(R.id.etC6)

                    val verifyOTP =
                            digit1.text.toString()+
                            digit2.text.toString()+
                            digit3.text.toString()+
                            digit4.text.toString()+
                            digit5.text.toString()+
                            digit6.text.toString()

                    if (verifyOTP == "") {
                        //removing previously set dialog to bring an new one
                        dialogView.removeSelf()
                        Toast.makeText(
                            this,
                            "Please input the OTP",
                            Toast.LENGTH_LONG
                        ).show()
                        builder.setView(dialogView).show()
                    }
                    else {
                        // below line is used for getting OTP code
                        // which is sent in phone auth credentials.
                        val code = phoneAuthCredential.smsCode

                        // checking if the code
                        // is null or not.
                        if (code != null) {
                            // if the code is not null then
                            // we are setting that code to
                            // our OTP edittext field.
                            digit1.setText(code)

                            // after setting this code
                            // to OTP edittext field we
                            // are calling our verifycode method.
                            verifyCode(code)
                        }
                    }
                }
                .show()
        }
    }
}