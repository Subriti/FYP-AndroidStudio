package com.example.notificationpermissions

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment


class AddPostFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var galleryRequestCode= 1000;
    private lateinit var img: ImageView
    private lateinit var GalleryButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_add_post, container, false)

        (activity as DashboardActivity?)!!.currentFragment = this

       /* //works
        setHasOptionsMenu(true);*/

        //removing existing toolbar from Dashboard Activity
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.addPost_toolbar)
        (activity as DashboardActivity?)!!.supportActionBar!!.hide()
        //(activity as DashboardActivity?)!!.setSupportActionBar(toolbar)




        
        /*val ParentToolbar = requireActivity().findViewById<View>(R.id.toolbar) as androidx.appcompat.widget.Toolbar
        println(ParentToolbar)
        var menu= ParentToolbar.menu
        println(menu)
        menu.findItem(R.id.nav_notifications).isVisible = true
        menu.findItem(R.id.nav_search).isVisible = false*/
        
        //but aba place it back in other activities


        img= view.findViewById<ImageView>(R.id.picture_to_be_posted)
        GalleryButton= view.findViewById<Button>(R.id.btnGallery)

        GalleryButton.setOnClickListener {
            val intent= Intent(Intent.ACTION_PICK)
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,galleryRequestCode)
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

            spinner.onItemSelectedListener=this
        }
    return view
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
        super.onActivityResult(requestCode,resultCode,data)

        if (resultCode== RESULT_OK){

            if (requestCode== galleryRequestCode){
                //for gallery
                if (data != null) {
                    img.setImageURI(data.data)
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val item= parent?.selectedItem
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


}