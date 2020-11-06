package com.example.myspotify.data.entity

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("imageUrl")
    val imageUrl: String = "",
    @SerializedName("mediaId")
    val mediaId: String = "",
    @SerializedName("songUrl")
    val songUrl: String = "",
    @SerializedName("subtitle")
    val subtitle: String = "",
    @SerializedName("title")
    val title: String = ""
)