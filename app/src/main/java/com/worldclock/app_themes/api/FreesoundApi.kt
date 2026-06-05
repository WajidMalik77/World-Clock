package com.worldclock.app_themes.api

import com.worldclock.app_themes.model.FreesoundResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FreesoundApi {

    @GET("search/text/")
    suspend fun searchSounds(
        @Query("query") query: String = "sleep",
        @Query("filter") filter: String = "duration:[30 TO 300]",
        @Query("fields") fields: String = "id,name,description,previews,images,duration,tags",
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
        @Query("token") token: String = API_KEY
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