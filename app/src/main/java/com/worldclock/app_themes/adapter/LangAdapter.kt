package com.worldclock.app_themes.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ItemLangBinding
import com.worldclock.app_themes.utils.Lang

// on below line we are creating
// a course rv adapter class.
class LangAdapter(
    // on below line we are passing variables
    // as course list and context
    private val courseList: ArrayList<Lang>,
    private val callBack: (String, Int) -> Unit
) : RecyclerView.Adapter<LangAdapter.CourseViewHolder>() {

    private var lastCheckedPosition = 0

    fun setPos(pos: Int) {
        lastCheckedPosition = pos
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {
        return CourseViewHolder(
            ItemLangBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (lastCheckedPosition == position) {
            holder.binding.imgRadio.setBackgroundResource(R.drawable.radio_selected)

        } else {
            holder.binding.imgRadio.setBackgroundResource(R.drawable.radio_unselected)

        }
        holder.binding.imgLang.setImageResource(courseList[position].res)
        holder.binding.tvLang.text = (courseList[position].name)
        holder.binding.root.setOnClickListener {
            callBack.invoke(courseList[position].name, position)


            val copyOfLastCheckedPosition: Int = lastCheckedPosition
            lastCheckedPosition = holder.adapterPosition
            notifyItemChanged(copyOfLastCheckedPosition)
            notifyItemChanged(lastCheckedPosition)
        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemLangBinding) :
        RecyclerView.ViewHolder(binding.root)
}
