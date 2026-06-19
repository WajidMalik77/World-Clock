package com.worldclock.app_themes.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldclock.app_themes.domain.model.SleepSound
import com.worldclock.app_themes.data.repostories.SleepSoundRepository
import com.worldclock.app_themes.domain.states.SleepSoundResult
import kotlinx.coroutines.launch

class SleepSoundViewModel(
    private val repository: SleepSoundRepository = SleepSoundRepository()
) : ViewModel() {

    private val _sounds = MutableLiveData<List<SleepSound>>()
    val sounds: LiveData<List<SleepSound>> = _sounds

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoadingMore = MutableLiveData<Boolean>()
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _playingSound = MutableLiveData<SleepSound?>()
    val playingSound: LiveData<SleepSound?> = _playingSound

    private var currentPage = 1
    private var currentQuery = "sleep ambient nature"
    private var hasMore = true
    private var isCurrentlyLoading = false

    init { fetchSounds() }

    fun fetchSounds(query: String = "sleep") {
        currentQuery = query
        currentPage = 1
        hasMore = true
        _sounds.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.fetchSleepSounds(currentQuery, currentPage)) {
                is SleepSoundResult.Success -> {
                    _sounds.value = result.sounds
                    hasMore = result.hasMore
                    _isLoading.value = false
                }
                is SleepSoundResult.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
                SleepSoundResult.Loading -> Unit
            }
            isCurrentlyLoading = false
        }
    }

    fun loadMore() {
        if (!hasMore || isCurrentlyLoading) return

        isCurrentlyLoading = true
        currentPage++

        viewModelScope.launch {
            _isLoadingMore.value = true
            when (val result = repository.fetchSleepSounds(currentQuery, currentPage)) {
                is SleepSoundResult.Success -> {
                    val current = _sounds.value ?: emptyList()
                    _sounds.value = current + result.sounds  // ← append
                    hasMore = result.hasMore
                    _isLoadingMore.value = false
                }
                is SleepSoundResult.Error -> {
                    _error.value = result.message
                    currentPage-- // rollback on error
                    _isLoadingMore.value = false
                }
                SleepSoundResult.Loading -> Unit
            }
            isCurrentlyLoading = false
        }
    }

    fun search(query: String) {
        if (query.isNotBlank()) fetchSounds(query)
    }

    fun setPlayingSound(sound: SleepSound?) {
        _playingSound.value = sound
    }
}