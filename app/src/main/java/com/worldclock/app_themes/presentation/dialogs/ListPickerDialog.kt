package com.worldclock.app_themes.presentation.dialogs

import android.R.attr.buttonTint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.DialogListPickerBinding
import com.worldclock.app_themes.core.utils.GradientTextHelper

class ListPickerDialog(
    private val title: String,
    private val options: List<String>,
    private val selectedIndex: Int = 0,
    private val onSave: (index: Int, value: String) -> Unit
) : DialogFragment() {

    private var _binding: DialogListPickerBinding? = null
    private val binding get() = _binding!!
    private var currentSelected = selectedIndex

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogListPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDialogTitle.text = title
        buildOptions()

        GradientTextHelper.apply(
            binding.tvSave,
            "#7441D0".toColorInt(),
            "#EF4A9A".toColorInt()
        )

        binding.tvSave.setOnClickListener {
            onSave(currentSelected, options[currentSelected])
            dismiss()
        }
        binding.ivClose.setOnClickListener { dismiss() }
    }

    private fun buildOptions() {
        binding.llOptions.removeAllViews()

        options.forEachIndexed { i, option ->
            val row = RelativeLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 20, 0, 20)
            }

            val tv = TextView(requireContext()).apply {
                text = option
                textSize = 13f
                setTextColor(Color.BLACK)
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_START)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }

            // Check icon — right side
            val iv = ImageView(requireContext()).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._14sdp),
                    resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._14sdp)
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                    addRule(RelativeLayout.CENTER_VERTICAL)
                }
                setImageResource(
                    if (i == currentSelected) R.drawable.ic_radio_checked
                    else R.drawable.ic_radio_unchecked
                )
            }

            row.addView(tv)
            row.addView(iv)

            row.setOnClickListener {
                currentSelected = i
                buildOptions()
            }

            binding.llOptions.addView(row)

            // Divider (except after last item)
            if (i < options.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor("#F0F0F0".toColorInt())
                }
                binding.llOptions.addView(divider)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.85).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }
}