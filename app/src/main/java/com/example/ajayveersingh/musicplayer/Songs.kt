package com.example.ajayveersingh.musicplayer

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

@SuppressLint("ParcelCreator")
class Songs(var songID: Long, var songTitle: String, var artist: String, var songData: String, var dataAdded: Long) : Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
    }

    override fun describeContents(): Int {
        return 0
    }

}