package com.worldclock.app_themes.presentation.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ItemLangBinding
import com.worldclock.app_themes.core.utils.Lang

// on below line we are creating
// a course rv adapter class.
class LangAdapter(
    // on below line we are passing variables
    // as course list and context
    private val courseList: ArrayList<Lang>,
    private val callBack: (String, Int) -> Unit
) : RecyclerView.Adapter<LangAdapter.CourseViewHolder>() {

    private var lastCheckedPosition = RecyclerView.NO_POSITION

    fun setPos(pos: Int) {
        lastCheckedPosition = pos
    }

    fun getSelectedPos(): Int = lastCheckedPosition

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
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            callBack.invoke(courseList[currentPosition].name, currentPosition)

            val copyOfLastCheckedPosition: Int = lastCheckedPosition
            lastCheckedPosition = currentPosition
            if (copyOfLastCheckedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(copyOfLastCheckedPosition)
            }
            notifyItemChanged(lastCheckedPosition)
        }
    }

    override fun getItemCount(): Int {
        return courseList.size
    }

    class CourseViewHolder(val binding: ItemLangBinding) :
        RecyclerView.ViewHolder(binding.root)
}
