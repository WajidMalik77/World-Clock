package com.worldclock.app_themes.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.DialogTimePickerBinding
import com.worldclock.app_themes.utils.GradientTextHelper
import java.util.Locale

class TimePickerDialog(
    private val onSave: (hour: Int, minute: Int, isAm: Boolean) -> Unit
) : DialogFragment() {

    private var _binding: DialogTimePickerBinding? = null
    private val binding get() = _binding!!
    private var isAm = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup pickers
        binding.npHour.apply {
            minValue = 1; maxValue = 12; value = 3; wrapSelectorWheel = true
        }
        binding.npMinute.apply {
            minValue = 0; maxValue = 59; value = 0
            wrapSelectorWheel = true
            setFormatter { String.format(Locale.getDefault(), "%02d", it) }
        }

        // Init AM selected
        updateAmPm()

        // AM click
        binding.llAm.setOnClickListener {
            isAm = true
            updateAmPm()
        }

        // PM click
        binding.llPm.setOnClickListener {
            isAm = false
            updateAmPm()
        }

        GradientTextHelper.apply(
            binding.tvSave,
            "#7441D0".toColorInt(),
            "#EF4A9A".toColorInt()
        )

        binding.tvSave.setOnClickListener {
            onSave(binding.npHour.value, binding.npMinute.value, isAm)
            dismiss()
        }
        binding.ivClose.setOnClickListener { dismiss() }
    }

    private fun updateAmPm() {
        binding.ivAmCheck.setImageResource(
            if (isAm) R.drawable.ic_radio_checked else R.drawable.ic_radio_unchecked
        )
        binding.ivPmCheck.setImageResource(
            if (isAm) R.drawable.ic_radio_unchecked else R.drawable.ic_radio_checked
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }
}