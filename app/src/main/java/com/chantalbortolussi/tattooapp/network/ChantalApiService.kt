package com.chantalbortolussi.tattooapp.network

import com.chantalbortolussi.tattooapp.model.GalleryResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

// Data classes for request and response mapping of the Booking system
data class BookingRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("style") val style: String,
    @SerializedName("description") val description: String
)

data class BookingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String? = null
)

interface ChantalApiService {

    @GET("api/app/galleries.php")
    suspend fun getGalleries(
        @Header("X-App-Token") appToken: String = "ChantalApp_2026_Secr3t!"
    ): Response<GalleryResponse>

    @POST("api/app/booking.php")
    suspend fun submitBooking(
        @Body request: BookingRequest,
        @Header("X-App-Token") appToken: String = "ChantalApp_2026_Secr3t!",
        @Header("Authorization") bearerToken: String = "Bearer ChantalApp_2026_Secr3t!"
    ): Response<BookingResponse>
}
