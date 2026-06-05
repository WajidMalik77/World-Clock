package com.worldclock.app_themes.model

data class SleepSound(
    val id: Int,
    val name: String,
    val description: String,
    val previewUrl: String,
    val thumbnailUrl: String,
    val duration: Int,
    val tags: List<String>
)