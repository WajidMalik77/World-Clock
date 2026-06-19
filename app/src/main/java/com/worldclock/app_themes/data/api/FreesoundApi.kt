package com.worldclock.app_themes.data.api

import androidx.annotation.Keep
import com.worldclock.app_themes.domain.model.FreesoundResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Keep
interface FreesoundApi {

    @GET("search/text/")
    suspend fun searchSounds(
        @Query("query") query: String,
        @Query("filter") filter: String,
        @Query("fields") fields: String,
        @Query("page_size") pageSize: Int,
        @Query("page") page: Int,
        @Query("token") token: String
    ): Response<FreesoundResponse>

    companion object {
        private const val BASE_URL = "https://freesound.org/apiv2/"
        const val API_KEY = "1VFEUyzwzDuBxY6WZw863Ylx9mmA20bnRwhAlXuQ"

        fun create(): FreesoundApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FreesoundApi::class.java)
        }
    }
}