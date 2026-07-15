package com.chantalbortolussi.tattooapp.model

import com.google.gson.annotations.SerializedName

data class GalleryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("galleries") val galleries: List<Gallery>
)

data class Gallery(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("images") val images: List<GalleryImage>
)

data class GalleryImage(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String,
    @SerializedName("name") val name: String
)
