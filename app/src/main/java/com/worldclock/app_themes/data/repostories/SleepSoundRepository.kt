package com.worldclock.app_themes.data.repostories

import android.util.Log
import com.worldclock.app_themes.data.api.FreesoundApi
import com.worldclock.app_themes.domain.model.SleepSound
import com.worldclock.app_themes.domain.states.SleepSoundResult

class SleepSoundRepository(
    private val api: FreesoundApi = FreesoundApi.create()
) {
    companion object {
        private const val TAG = "SleepSoundRepo"
    }

    suspend fun fetchSleepSounds(
        query: String = "sleep",
        page: Int = 1
    ): SleepSoundResult {
        return try {
            val response = api.searchSounds(
                query = query,
                filter = "duration:[30 TO 300]",
                fields = "id,name,description,previews,images,duration,tags",
                pageSize = 20,
                page = page,
                token = FreesoundApi.API_KEY
            )
            if (response.isSuccessful) {
                val body = response.body()

                val sounds = body?.results?.map { result ->
                    SleepSound(
                        id = result.id,
                        name = result.name.removeSuffix(".mp3").removeSuffix(".wav"),
                        description = result.description,
                        previewUrl = result.previews.hqMp3,
                        thumbnailUrl = result.images.spectralMedium,
                        duration = result.duration.toInt(),
                        tags = result.tags.take(3)
                    )
                } ?: emptyList()

                // ✅ Log each sound name + tags (acts as categories)
                sounds.forEachIndexed { index, sound ->
                    Log.d(TAG, "[$index] ${sound.name} | tags: ${sound.tags.joinToString(", ")}")
                }

                // ✅ Log all unique tags across results (category overview)
                val allTags = body?.results
                    ?.flatMap { it.tags }
                    ?.groupingBy { it }
                    ?.eachCount()
                    ?.entries
                    ?.sortedByDescending { it.value }
                    ?.take(20)

                allTags?.forEach { (tag, count) ->
                    Log.d(TAG, "  #$tag → $count sounds")
                }

                SleepSoundResult.Success(
                    sounds = sounds,
                    hasMore = body?.next != null
                )
            } else {
                Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
                SleepSoundResult.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            SleepSoundResult.Error(e.message ?: "Unknown error")
        }
    }
}