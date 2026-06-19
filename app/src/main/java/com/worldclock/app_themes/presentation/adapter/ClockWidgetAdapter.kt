package com.worldclock.app_themes.presentation.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextClock
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.core.utils.AddWidgetDialog
import com.worldclock.app_themes.core.utils.ClockCanvasView
import com.worldclock.app_themes.core.utils.ClockWidget
import java.util.Calendar

class ClockWidgetAdapter(
    private val items: List<ClockWidget>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ANALOG = 0
        private const val TYPE_DIGITAL = 1
    }

    inner class AnalogVH(val container: RelativeLayout) : RecyclerView.ViewHolder(container) {
        private val handler = Handler(Looper.getMainLooper())
        private var ticker: Runnable? = null

        fun bind(widget: ClockWidget.Analog) {
            stopTicking()
            redraw(widget)
            ticker = object : Runnable {
                override fun run() {
                    redraw(widget)
                    handler.postDelayed(this, 1_000L)
                }
            }
            ticker?.let { handler.postDelayed(it, 1_000L) }

            // Pass full widget — dialog needs needles for preview
            container.setOnClickListener {
                AddWidgetDialog(container.context, widget).show()
            }
        }

        private fun redraw(widget: ClockWidget.Analog) {
            val cal = Calendar.getInstance()
            val sec = cal.get(Calendar.SECOND)
            val min = cal.get(Calendar.MINUTE)
            val hour = cal.get(Calendar.HOUR)
            val hourAngle = hour * 30f + min * 0.5f
            val minuteAngle = min * 6f + sec * 0.1f
            val secondAngle = sec * 6f

            container.removeAllViews()
            container.addView(
                ClockCanvasView(
                    context = container.context,
                    startDegHr = hourAngle,
                    startDegMin = minuteAngle,
                    startDegSec = secondAngle,
                    clockImage = widget.faceRes,
                    hourImage = widget.hourRes,
                    minImage = widget.minuteRes,
                    secImage = widget.secRes
                )
            )
        }

        fun stopTicking() {
            ticker?.let { handler.removeCallbacks(it) }
            ticker = null
        }
    }

    class DigitalVH(
        val root: RelativeLayout,
        val bg: ImageView,
        val time: TextClock
    ) : RecyclerView.ViewHolder(root)

    override fun getItemViewType(position: Int) =
        if (items[position] is ClockWidget.Analog) TYPE_ANALOG else TYPE_DIGITAL

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val ctx = parent.context
        val sizePx = parent.resources.getDimensionPixelSize(R.dimen.clock_cell_size)
        val lp = ViewGroup.MarginLayoutParams(sizePx, sizePx).apply { setMargins(8, 8, 8, 8) }

        return when (viewType) {
            TYPE_ANALOG -> AnalogVH(RelativeLayout(ctx).apply { layoutParams = lp })
            else -> {
                val view = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_clock_widget_digital, parent, false)
                view.layoutParams = lp
                DigitalVH(
                    root = view as RelativeLayout,
                    bg = view.findViewById(R.id.ivDigitalBg),
                    time = view.findViewById(R.id.tvDigitalTime)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ctx = holder.itemView.context
        when (val widget = items[position]) {
            is ClockWidget.Analog -> {
                (holder as AnalogVH).bind(widget)
            }

            is ClockWidget.Digital -> {
                val vh = holder as DigitalVH
                vh.bg.setImageDrawable(ContextCompat.getDrawable(ctx, widget.backgroundRes))
                vh.time.setTextColor(widget.timeColor)

                // Pass full widget to dialog
                vh.root.setOnClickListener {
                    AddWidgetDialog(ctx, widget).show()
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder); if (holder is AnalogVH) holder.stopTicking()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder); if (holder is AnalogVH) holder.stopTicking()
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is AnalogVH) {
            val widget = items[holder.bindingAdapterPosition] as? ClockWidget.Analog ?: return
            holder.bind(widget)
        }
    }
}
