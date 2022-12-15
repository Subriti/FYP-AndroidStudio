package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ChatFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this
        /* //adding back the appbar
         (activity as DashboardActivity?)!!.supportActionBar!!.show()*/
        return view
    }
}