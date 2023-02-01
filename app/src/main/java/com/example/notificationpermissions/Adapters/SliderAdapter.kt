package com.example.notificationpermissions.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.notificationpermissions.R
import com.smarteist.autoimageslider.SliderViewAdapter

internal class SliderAdapter: SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder>{
    lateinit var images: Array<Int>
    constructor(images: Array<Int>){
        this.images =images
    }

    internal class SliderAdapterViewHolder(itemView: View) :
        ViewHolder(itemView) {
        // Adapter class for initializing
        // the views of our slider view.
        lateinit var item: View
        var imageViewBackground: ImageView

        init {
            imageViewBackground = itemView.findViewById(R.id.imageView)
            this.item= itemView
        }
    }

    override fun getCount(): Int {
        return images.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup?): SliderAdapterViewHolder {
        var view= LayoutInflater.from(parent?.context).inflate(R.layout.slider_item, parent, false)
        return SliderAdapterViewHolder(view)
    }

    override fun onBindViewHolder(
        viewHolder: SliderAdapterViewHolder?,
        position: Int
    ) {
        if (viewHolder != null) {
            viewHolder.imageViewBackground.setImageResource(images[position])
            viewHolder.imageViewBackground.adjustViewBounds
        }
    }
}