package com.worldclock.app_themes.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ItemLangBinding
import com.worldclock.app_themes.databinding.ItemMenuBinding
import com.worldclock.app_themes.utils.Lang

class MenuAdapter(
    private val courseList: ArrayList<Lang>,
    private val callBack: (Int) -> Unit
) : RecyclerView.Adapter<MenuAdapter.CourseViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {

        return CourseViewHolder(
            ItemMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {

        holder.binding.imgLang.setImageResource(courseList[position].res)
        holder.binding.tvLang.text = (courseList[position].name)
        holder.binding.root.setOnClickListener {
            callBack.invoke(position)

        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root)
}
