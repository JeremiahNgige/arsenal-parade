package com.example.data.remote_src

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class ITunesResponse(
    val resultCount: Int,
    val results: List<ITunesTrack>
)

@JsonClass(generateAdapter = true)
data class ITunesTrack(
    val trackId: Long,
    val trackName: String?,
    val artistName: String?,
    val previewUrl: String?,
    val trackTimeMillis: Long?
)

interface ITunesApiService {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 10
    ): ITunesResponse
}

object ITunesApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: ITunesApiService by lazy {
        retrofit.create(ITunesApiService::class.java)
    }
}
