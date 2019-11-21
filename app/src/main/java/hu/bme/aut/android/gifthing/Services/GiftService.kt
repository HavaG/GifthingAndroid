package hu.bme.aut.android.gifthing.Services

import hu.bme.aut.android.gifthing.models.Gift
import retrofit2.http.*

interface GiftService {
    @GET("gift/{id}")
    suspend fun getById(@Path("id") id: Long): Gift?

    @DELETE("gift/delete/{id}")
    suspend fun deleteById(@Path("id") id: Long): Boolean

    @POST("gift/create")
    suspend fun create(@Body newGift: Gift): Boolean
}