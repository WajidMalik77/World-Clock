package com.worldclock.app_themes.presentation.adapter

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ItemSleepSoundBinding
import com.worldclock.app_themes.domain.model.SleepSound
import java.io.File

class SleepSoundAdapter(
    private val onPlay: (SleepSound) -> Unit
) : ListAdapter<SleepSound, SleepSoundAdapter.ViewHolder>(DiffCallback()) {

    private var playingId: Int = -1

    inner class ViewHolder(private val binding: ItemSleepSoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sound: SleepSound) {
            binding.tvSoundName.text = sound.name

            binding.ivThumbnail.setImageResource(R.drawable.ic_sleep_placeholder)

            binding.root.setOnClickListener { onPlay(sound) }
            binding.ivDownload.setOnClickListener {
                downloadSound(binding.root.context, sound)
            }
        }

        private fun downloadSound(context: Context, sound: SleepSound) {
            try {
                val fileName = "${
                    sound.name.replace(" ", "_")
                        .replace("/", "_")
                        .replace("\\", "_")
                }.mp3"

                val request = DownloadManager.Request(Uri.parse(sound.previewUrl)).apply {
                    setTitle(sound.name)
                    setDescription("Downloading sleep sound...")
                    setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(false)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_MUSIC,
                            fileName
                        )
                    } else {
                        val dir = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MUSIC
                            ), "SleepSounds"
                        )
                        if (!dir.exists()) dir.mkdirs()
                        setDestinationUri(Uri.fromFile(File(dir, fileName)))
                    }
                }

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                Toast.makeText(context, "Downloading: ${sound.name}", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setPlayingId(id: Int) {
        playingId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemSleepSoundBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<SleepSound>() {
        override fun areItemsTheSame(a: SleepSound, b: SleepSound) = a.id == b.id
        override fun areContentsTheSame(a: SleepSound, b: SleepSound) = a == b
    }
}