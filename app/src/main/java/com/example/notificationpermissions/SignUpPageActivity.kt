package com.example.notificationpermissions

import android.app.DatePickerDialog
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
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.entity.UrlEncodedFormEntity
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.CloseableHttpResponse
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.CloseableHttpClient
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.HttpClients
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils
import com.hbb20.CountryCodePicker
import com.squareup.okhttp.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
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

    lateinit var OTPinput: EditText

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_page)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
        mAuth = FirebaseAuth.getInstance();

        name = findViewById<EditText>(R.id.nameText)
        email = findViewById<EditText>(R.id.emailText)
        phone = findViewById<EditText>(R.id.phoneText)
        password = findViewById<EditText>(R.id.passwordText)
        repassword = findViewById<EditText>(R.id.repasswordText)

        phonecode= findViewById<CountryCodePicker>(R.id.ccp)

        val indicatorText = findViewById<TextView>(R.id.passwordIndicatorText)
        indicatorText.visibility = View.GONE


        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val pickDatebtn = findViewById<ImageView>(R.id.pickDatebtn)
        val birthDate = findViewById<TextView>(R.id.dateText)
        pickDatebtn.setOnClickListener {
            val datepickerdialog =
                DatePickerDialog(
                    this, { view, mYear, mMonth, mDay ->
                        birthDate.text = "" + mDay + "/" + mMonth + "/" + mYear
                    }, year, month, day
                )

            if (datepickerdialog != null) {
                datepickerdialog.show()
            }
        }



        var signupbtn = findViewById<Button>(R.id.signUp)

        signupbtn.setOnClickListener {
            if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() == repassword.text.toString()) {
                indicatorText.visibility = View.VISIBLE
                indicatorText.text = "Password Match"
                indicatorText.setTextColor(getColor(R.color.darkgreen))

                val selectedCode= phonecode.selectedCountryCodeAsInt
                println(selectedCode)
                val number= "+$selectedCode${phone.text}"

                //request OTP from txtlocal
                //sendSms()

                //using wipple from rapidApi
                //SendOTP("977${phone.text}")

                ///OTP()
                sendVerificationCode(number);

                //using textbelt
                //SendOtpFromTextBelt()

                //ClickSendOTP(phone.text.toString())

                //show OTP verification box
                showAlertDialog()

            } else if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() != repassword.text.toString()) {
                indicatorText.visibility = View.VISIBLE
                indicatorText.text = "Password didn't match"
                indicatorText.setTextColor(getColor(R.color.falured))
            }
            else{
                Toast.makeText(
                    this,
                    "Please fill all the fields",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
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


    /* //clickSend sms
     protected open fun ClickSendOTP(toString: String) {
         val from: String = txtFrom.getText().toString()
         val message: String = txtMessage.getText().toString()
         val defaultClient = ApiClient()
         defaultClient.setUsername("leiiqtqt16@gmail.com")
         defaultClient.setPassword("Tecno_0116")
         val smsApi = SmsApi(defaultClient)
         val smsMessage = SmsMessage()
         smsMessage.to("+977$toString")
         smsMessage.body(message)
         smsMessage.setFrom(from)
         val smsMessageList: List<SmsMessage> = Arrays.asList(smsMessage)
         val smsMessageCollection = SmsMessageCollection()
         smsMessageCollection.messages(smsMessageList)
         try {
             var res: String = smsApi.smsSendPost(smsMessageCollection)
             txtMessage.setText("")
             txtTo.setText("")
             txtFrom.setText("")
             res = res.uppercase(Locale.getDefault())
             Toast.makeText(applicationContext, "SENT SUCCESSFULLY", Toast.LENGTH_SHORT)
             if (res.contains("INSUFFICIENT")) {
                 Toast.makeText(applicationContext, "INSUFFICIENT BALANCE", Toast.LENGTH_SHORT)
             } else {
                 Toast.makeText(applicationContext, "SENT SUCCESSFULLY", Toast.LENGTH_SHORT)
             }
         } catch (err: ApiException) {
             Toast.makeText(applicationContext, "ERROR$err", Toast.LENGTH_SHORT)
         }
     }*/

    //said successful but no msg received
    private fun SendOtpFromTextBelt() {
        val data = arrayOf<NameValuePair>(
            BasicNameValuePair("phone", "9843346520"),
            BasicNameValuePair("message", "Your verification code is 123451"),
            BasicNameValuePair("key", "textbelt")
        )
        val httpClient: CloseableHttpClient? = HttpClients.createMinimal()
        val httpPost = HttpPost("https://textbelt.com/text")
        httpPost.entity = UrlEncodedFormEntity((data.toMutableList()))
        val httpResponse: CloseableHttpResponse? =
            httpClient?.execute(httpPost)

        val responseString = EntityUtils.toString(httpResponse?.entity)
        val response = JSONObject(responseString)
        println(response)
    }

    private fun SendOTP(phonenum:String){
        val mediaType: MediaType = MediaType.parse("application/json")
        //val JSON= "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val json= "{\r" +
                "\"app_name\": \"exampleapp\",\r" +
                "\"code_length\": 6,\r" +
                "\"code_type\": \"number\",\r" +
                "\"expiration_second\": 86000,\r" +
                "\"phone_number\": \"$phonenum\"\r}"
        println(json)
        //val body = json.toRequestBody(JSON);
        val jsonbody = RequestBody.create(mediaType, json)
        println(jsonbody)
        val request = Request.Builder()
                .url("https://wipple-sms-verify-otp.p.rapidapi.com/send")
                .post(jsonbody)
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", "e57a49aeeamshce97655db6baa20p129a29jsn9eb15225a2b6")
                .addHeader("X-RapidAPI-Host", "wipple-sms-verify-otp.p.rapidapi.com")
                .build();
        println(request)
        val response: Response = client.newCall(request).execute()
        println(response)
    }

    fun OTP(){
        val client = OkHttpClient()

        val mediaType = MediaType.parse("application/json")
        val body = RequestBody.create(mediaType, "{\r\n    \"app_name\": \"exampleapp\",\r\n    \"code_length\": 6,\r\n    \"code_type\": \"number\",\r\n    \"expiration_second\": 86000,\r\n    \"phone_number\": \"977${phone.text}\"\r\n}")
        val request = Request.Builder()
            .url("https://wipple-sms-verify-otp.p.rapidapi.com/send")
            .post(body)
            .addHeader("content-type", "application/json")
            .addHeader("X-RapidAPI-Key", "be5e79b418msh788862789ceb619p19945djsnc26a5ab036bc")
            .addHeader("X-RapidAPI-Host", "wipple-sms-verify-otp.p.rapidapi.com")
            .build()

        println(request)
        val response = client.newCall(request).execute()
        println(response)
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.otp_verfication_layout, null)

        fun View?.removeSelf() {
            this ?: return
            val parentView = parent as? ViewGroup ?: return
            parentView.removeView(this)
        }

        if (builder != null) {
            builder.setView(dialogView)
                .setTitle("OTP Verification")
                .setPositiveButton("Verify") { _, i ->
                    OTPinput = dialogView.findViewById(R.id.OTPinput)
                    val verifyOTP= OTPinput.text.toString()
                    println(verifyOTP)
                    if (verifyOTP== "") {
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
                        //validate the OTP here
                       // verifyOTP(verifyOTP)

                        //using firebase
                        verifyCode(verifyOTP);


                       /* if (randomNum == OTPinput.text.toString().toInt()) {
                            Toast.makeText(
                                this,
                                "Successfully Logged in",
                                Toast.LENGTH_LONG
                            ).show()

                            //login vayesi go in some other activity
                            val intent = Intent(this, NotificationActivity::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(
                                this,
                                "The verification OTP is incorrect",
                                Toast.LENGTH_LONG
                            ).show()
                        }*/

                    }
                }
                .show()
        }
}

    // below method is use to verify code from Firebase.


    // callback method is called on Phone auth provider.
    private val   // initializing our callbacks for on
    // verification callback method.
            mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            // below method is used when
            // OTP is sent from Firebase
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
                // below line is used for getting OTP code
                // which is sent in phone auth credentials.
                val code = phoneAuthCredential.smsCode

                // checking if the code
                // is null or not.
                if (code != null) {
                    // if the code is not null then
                    // we are setting that code to
                    // our OTP edittext field.
                    OTPinput.setText(code)

                    // after setting this code
                    // to OTP edittext field we
                    // are calling our verifycode method.
                    verifyCode(code)
                }
            }

            // this method is called when firebase doesn't
            // sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                // displaying error message with firebase exception.
                Toast.makeText(this@SignUpPageActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }


    private fun verifyCode(verifyOTP: String) {
        // below line is used for getting
        // credentials from our verification id and code.
        val credential = PhoneAuthProvider.getCredential(verificationId, verifyOTP)

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }

    private fun verifyOTP(OTPinput:String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://wipple-sms-verify-otp.p.rapidapi.com/verify?phone_number=977${phone.text.toString()}&verification_code=$OTPinput&app_name=exampleapp")
            .get()
            .addHeader("X-RapidAPI-Key", "e57a49aeeamshce97655db6baa20p129a29jsn9eb15225a2b6")
            .addHeader("X-RapidAPI-Host", "wipple-sms-verify-otp.p.rapidapi.com")
            .build()

        val response = client.newCall(request).execute()
        println(response)
    }


    /*  //request OTP
    sendSms()*/

    /* val mBuilder= AlertDialog.Builder(this)
        .setTitle("Verification")
        .setPositiveButton("Verify",null)
        .show()
    val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE)
    mPositiveButton.setOnClickListener {
        // Do something
        // As we do not want the Alert Dialog to close,
        // we will only display a Toast and do nothing else.
        Toast.makeText(this, "Can't Exit", Toast.LENGTH_SHORT).show()
    }
*/


    //api keys
    //NjM3MTQzNTE2YzM2NWE3MDUzNTg2YTQ0NzE3MzZjMzU=

    private fun sendSms(): String {
        return try {
            // Construct data
            val apiKey = "apikey=" + "MzM3NjVhMzU0ZDM3NjU3NjRkNTM3ODc3NjQzMzQ4NDc="

            //generating random OTP
            val random = Random()
            randomNum = random.nextInt(999999)


            val message = "&message=" + "Hey ${name.text} Your OTP is $randomNum"
            println(message)
            val sender = "&sender=" + "TSN"
            val numbers = "&numbers=" + phone.text.toString()
            println(numbers)

            // Send data
            val conn: HttpURLConnection =
                URL("https://api.txtlocal.com/send/?").openConnection() as HttpURLConnection
            val data = apiKey + numbers + message + sender

            /* val data = "https://api.txtlocal.com/send/?apikey=$apiKey 1&sender=$sender &numbers=$numbers &message=$message"
             val url = URL(data)
             val conn: URLConnection = url.openConnection()*/

            conn.doOutput = true
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Length", data.length.toString())
            conn.outputStream.write(data.toByteArray(charset("UTF-8")))
            val rd = BufferedReader(InputStreamReader(conn.inputStream))
            val stringBuffer = StringBuffer()
            var line: String?
            while (rd.readLine().also { line = it } != null) {
                stringBuffer.append(line)
            }
            rd.close()
            Toast.makeText(this, "OTP was sent successfully", Toast.LENGTH_LONG).show()
            stringBuffer.toString()
        } catch (e: java.lang.Exception) {
            println("Error SMS $e")
            Toast.makeText(this, "Error sending SMS: $e", Toast.LENGTH_LONG).show()
            "Error $e"
        }
    }
}