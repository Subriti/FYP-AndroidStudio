package com.example.notificationpermissions.Models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Post (val post_id:String, val post_by:String, val media_file: String, val description: String, val created_datetime:String, val location: String, val cloth_id: String, val donation_status: String) :
    Serializable /*,Parcelable {
    constructor(parcel: Parcel) : this(
    parcel.readString()!!,
    parcel.readString()!!,
    parcel.readString()!!,
    parcel.readString()!!,
    parcel.readString()!!,
    parcel.readString()!!,
    parcel.readString()!!,
    parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(post_id)
        parcel.writeString(post_by)
        parcel.writeString(media_file)
        parcel.writeString(description)
        parcel.writeString(created_datetime)
        parcel.writeString(location)
        parcel.writeString(cloth_id)
        parcel.writeString(donation_status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }*/
{
    override fun toString(): String {
        return media_file
    }
}
