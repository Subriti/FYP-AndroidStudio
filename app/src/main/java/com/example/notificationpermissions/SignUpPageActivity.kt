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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class SignUpPageActivity : AppCompatActivity() {

    lateinit var name: EditText
    lateinit var email: EditText
    lateinit var phone: EditText
    lateinit var password: EditText
    lateinit var repassword: EditText
    var randomNum = 123456

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_page)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        name = findViewById<EditText>(R.id.nameText)
        email = findViewById<EditText>(R.id.emailText)
        phone = findViewById<EditText>(R.id.phoneText)
        password = findViewById<EditText>(R.id.passwordText)
        repassword = findViewById<EditText>(R.id.repasswordText)

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

                //request OTP
                sendSms()

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

    fun showAlertDialog() {
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
                    val OTPinput = dialogView.findViewById<EditText>(R.id.OTPinput)
                    if (OTPinput.text.toString() == "") {
                        //removing previously set dialog to bring an new one
                        dialogView.removeSelf()
                        Toast.makeText(
                            this,
                            "Please input the OTP",
                            Toast.LENGTH_LONG
                        ).show()
                        builder.setView(dialogView).show()
                    } else {
                        //validate the OTP here
                        if (randomNum == OTPinput.text.toString().toInt()) {
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
                        }

                    }
                }
                .show()
        }
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