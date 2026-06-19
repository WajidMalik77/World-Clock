package com.worldclock.app_themes.presentation.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.databinding.ItemHomeBinding
import com.worldclock.app_themes.databinding.ItemHomeNativeCenterBinding
import com.worldclock.app_themes.databinding.ItemHomeWideBinding
import com.worldclock.app_themes.core.utils.HomeItem

class HomeAdapter(
    private val courseList: ArrayList<HomeItem>,
    private val showCenterNative: Boolean = false,
    private val onBindCenterNative: ((ItemHomeNativeCenterBinding) -> Unit)? = null,
    private val callBack: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GRID = 0
        private const val TYPE_WIDE = 1
        private const val TYPE_CENTER_NATIVE = 2
        const val CENTER_NATIVE_POSITION = 3
    }

    override fun getItemViewType(position: Int): Int {
        if (isCenterNativePosition(position)) return TYPE_CENTER_NATIVE
        return if (toDataPosition(position) >= 6) TYPE_WIDE else TYPE_GRID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CENTER_NATIVE -> CenterNativeVH(
                ItemHomeNativeCenterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            TYPE_WIDE -> WideVH(
                ItemHomeWideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> GridVH(
                ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ctx = holder.itemView.context

        if (holder is CenterNativeVH) {
            onBindCenterNative?.invoke(holder.binding)
            return
        }

        val dataPosition = toDataPosition(position)
        val item = courseList[dataPosition]

        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = ctx.resources.getDimension(com.intuit.sdp.R.dimen._16sdp)
            setColor(ContextCompat.getColor(ctx, item.bgColor))
            setStroke(2, "#E0E0E0".toColorInt())
        }

        when (holder) {
            is GridVH -> {
                holder.binding.folderImg.setImageResource(item.res)
                holder.binding.folderTv.text = item.name
                holder.binding.folderTv.isSelected = true
                holder.binding.root.background = bgDrawable
                holder.binding.root.setOnClickListener { callBack.invoke(dataPosition) }
            }

            is WideVH -> {
                holder.binding.folderImg.setImageResource(item.res)
                holder.binding.folderTv.text = item.name
                holder.binding.folderTv.isSelected = true
                holder.binding.root.background = bgDrawable
                holder.binding.root.setOnClickListener { callBack.invoke(dataPosition) }
            }
        }
    }

    override fun getItemCount() = courseList.size + if (showCenterNative) 1 else 0

    fun getSpanSize(position: Int): Int {
        if (isCenterNativePosition(position)) return 6
        return if (toDataPosition(position) >= 6) 3 else 2
    }

    private fun isCenterNativePosition(position: Int): Boolean =
        showCenterNative && position == CENTER_NATIVE_POSITION

    private fun toDataPosition(adapterPosition: Int): Int =
        if (showCenterNative && adapterPosition > CENTER_NATIVE_POSITION) {
            adapterPosition - 1
        } else {
            adapterPosition
        }

    class GridVH(val binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root)
    class WideVH(val binding: ItemHomeWideBinding) : RecyclerView.ViewHolder(binding.root)
    class CenterNativeVH(val binding: ItemHomeNativeCenterBinding) : RecyclerView.ViewHolder(binding.root)
}
