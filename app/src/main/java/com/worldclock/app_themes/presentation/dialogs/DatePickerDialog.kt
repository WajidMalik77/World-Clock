package com.worldclock.app_themes.presentation.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.DialogDatePickerBinding
import com.worldclock.app_themes.core.utils.GradientTextHelper
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.drawable.toDrawable

class DatePickerDialog(
    private val onSave: (startDate: Calendar, endDate: Calendar) -> Unit
) : DialogFragment() {

    private var _binding: DialogDatePickerBinding? = null
    private val binding get() = _binding!!

    private val displayCal = Calendar.getInstance()
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateGrid()

        binding.ivPrevMonth.setOnClickListener {
            displayCal.add(Calendar.MONTH, -1); updateGrid()
        }
        binding.ivNextMonth.setOnClickListener {
            displayCal.add(Calendar.MONTH, 1); updateGrid()
        }

        GradientTextHelper.apply(
            binding.tvSave,
            "#7441D0".toColorInt(),
            "#EF4A9A".toColorInt()
        )

        binding.tvSave.setOnClickListener {
            val s = startDate ?: Calendar.getInstance()
            val e = endDate ?: s
            onSave(s, e); dismiss()
        }
        binding.ivClose.setOnClickListener { dismiss() }
    }

    private fun updateGrid() {
        binding.tvMonthYear.text =
            SimpleDateFormat("MMMM", Locale.getDefault()).format(displayCal.time)

        val cal = displayCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<Int?>()
        repeat(firstDow) { days.add(null) }
        (1..daysInMonth).forEach { days.add(it) }

        binding.gridCalendar.adapter = DayAdapter(
            requireContext(), days, displayCal, startDate, endDate
        ) { day ->
            val clicked = (displayCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, day)
            }
            when {
                startDate == null || (startDate != null && endDate != null) -> {
                    startDate = clicked; endDate = null
                }

                clicked.before(startDate) -> {
                    endDate = startDate; startDate = clicked
                }

                else -> endDate = clicked
            }
            updateGrid()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }

    // ── Inner Adapter ──────────────────────────────────────────────────────
    private class DayAdapter(
        ctx: Context,
        private val days: List<Int?>,
        private val displayCal: Calendar,
        private val startDate: Calendar?,
        private val endDate: Calendar?,
        private val onClick: (Int) -> Unit
    ) : BaseAdapter() {

        private val inflater = LayoutInflater.from(ctx)

        override fun getCount() = days.size
        override fun getItem(pos: Int) = days[pos]
        override fun getItemId(pos: Int) = pos.toLong()

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val tv = (convertView as? SquareTextView) ?: SquareTextView(parent.context).apply {
                gravity = Gravity.CENTER
                layoutParams = AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.MATCH_PARENT
                )
                textSize = 12f
            }

            val day = days[pos]
            tv.text = day?.toString() ?: ""

            if (day != null) {
                val thisCal = (displayCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, day)
                }
                val isStart = startDate?.let { isSameDay(thisCal, it) } == true
                val isEnd = endDate?.let { isSameDay(thisCal, it) } == true
                val inRange = startDate != null && endDate != null &&
                        thisCal.after(startDate) && thisCal.before(endDate)

                when {
                    isStart || isEnd -> {
                        tv.setBackgroundResource(R.drawable.bg_calendar_selected)
                        tv.setTextColor(Color.BLACK)
                    }

                    inRange -> {
                        tv.setBackgroundColor(0x1A7441D0.toInt())
                        tv.setTextColor(Color.BLACK)
                    }

                    else -> {
                        tv.background = null
                        tv.setTextColor(Color.BLACK)
                    }
                }
                tv.setOnClickListener { onClick(day) }
            } else {
                tv.background = null
                tv.setOnClickListener(null)
            }
            return tv
        }

        private fun isSameDay(a: Calendar, b: Calendar) =
            a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }


    private class SquareTextView(context: Context) : AppCompatTextView(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }
    }

}