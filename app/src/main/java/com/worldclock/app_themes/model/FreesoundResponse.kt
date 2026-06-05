package com.worldclock.app_themes.model

import com.google.gson.annotations.SerializedName

data class FreesoundResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("results") val results: List<FreesoundResult>
)

data class FreesoundResult(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("previews") val previews: Previews,
    @SerializedName("images") val images: Images,
    @SerializedName("duration") val duration: Double,
    @SerializedName("tags") val tags: List<String>
)

data class Previews(
    @SerializedName("preview-hq-mp3") val hqMp3: String,
    @SerializedName("preview-lq-mp3") val lqMp3: String
)

data class Images(
    @SerializedName("waveform_m") val waveformMedium: String,
    @SerializedName("spectral_m") val spectralMedium: String
)