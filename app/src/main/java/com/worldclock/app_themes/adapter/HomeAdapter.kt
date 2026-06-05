package com.worldclock.app_themes.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.databinding.ItemHomeBinding
import com.worldclock.app_themes.databinding.ItemHomeWideBinding
import com.worldclock.app_themes.utils.HomeItem

class HomeAdapter(
    private val courseList: ArrayList<HomeItem>,
    private val callBack: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GRID = 0
        private const val TYPE_WIDE = 1
    }

    override fun getItemViewType(position: Int) =
        if (position >= 6) TYPE_WIDE else TYPE_GRID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_WIDE -> WideVH(
                ItemHomeWideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> GridVH(
                ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = courseList[position]
        val ctx = holder.itemView.context

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
                holder.binding.root.setOnClickListener { callBack.invoke(position) }
            }

            is WideVH -> {
                holder.binding.folderImg.setImageResource(item.res)
                holder.binding.folderTv.text = item.name
                holder.binding.folderTv.isSelected = true
                holder.binding.root.background = bgDrawable
                holder.binding.root.setOnClickListener { callBack.invoke(position) }
            }
        }
    }

    override fun getItemCount() = courseList.size

    class GridVH(val binding: ItemHomeBinding) : RecyclerView.ViewHolder(binding.root)
    class WideVH(val binding: ItemHomeWideBinding) : RecyclerView.ViewHolder(binding.root)
}