package com.worldclock.app_themes.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.SleepSoundAdapter
import com.worldclock.app_themes.databinding.ActivitySleepSoundBinding
import com.worldclock.app_themes.viewmodel.SleepSoundViewModel

class SleepSoundActivity : BaseActivity() {

    private val binding by lazy { ActivitySleepSoundBinding.inflate(layoutInflater) }
    private val viewModel: SleepSoundViewModel by viewModels()
    private lateinit var adapter: SleepSoundAdapter

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Storage permission needed for download", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        binding.toolbar.back.setOnClickListener { finish() }
        binding.toolbar.title.text = getString(R.string.sleep_sound)

        checkStoragePermission()
        setupRecycler()
        observeViewModel()
        setupSearch()
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setupRecycler() {
        adapter = SleepSoundAdapter(
            onPlay = { sound ->
                startActivity(
                    Intent(this, PlaySoundActivity::class.java).apply {
                        putExtra("sound_name", sound.name)
                        putExtra("preview_url", sound.previewUrl)
                        putExtra("thumbnail_res", R.drawable.ic_sleep_placeholder)
                    }
                )
            }
        )
        binding.rvSleepSounds.adapter = adapter

        binding.rvSleepSounds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItems = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (lastVisible >= totalItems - 4) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.sounds.observe(this) { sounds ->
            adapter.submitList(sounds)
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.isLoadingMore.observe(this) { loadingMore ->
            binding.progressBar.visibility = if (loadingMore) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(binding.etSearch.text.toString())
                true
            } else false
        }
    }
}