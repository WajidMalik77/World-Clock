package com.worldclock.app_themes.repostories

import android.util.Log
import com.worldclock.app_themes.api.FreesoundApi
import com.worldclock.app_themes.model.SleepSound
import com.worldclock.app_themes.states.SleepSoundResult

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
            val response = api.searchSounds(query = query, page = page)
            if (response.isSuccessful) {
                val body = response.body()

                // ✅ Log total count
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "Query      : $query")
                Log.d(TAG, "Page       : $page")
                Log.d(TAG, "Total count: ${body?.count}")
                Log.d(TAG, "Has next   : ${body?.next != null}")
                Log.d(TAG, "Results    : ${body?.results?.size}")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

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

                Log.d(TAG, "━━ Top Tags/Categories ━━━━━━━")
                allTags?.forEach { (tag, count) ->
                    Log.d(TAG, "  #$tag → $count sounds")
                }
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

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