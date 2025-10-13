package com.example.travel_companion.ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_companion.data.entity.Trip
import java.text.SimpleDateFormat
import java.util.*

class TripAdapter(
    private val onTripClick: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val context = parent.context
        val density = context.resources.displayMetrics.density

        val card = CardView(context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin = (8 * density).toInt()
                setMargins(0, margin, 0, margin)
            }
            radius = 12 * density
            cardElevation = 4 * density
            isClickable = true
            isFocusable = true
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            val padding = (16 * density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        // Header row
        val headerRow = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val iconText = TextView(context).apply {
            id = View.generateViewId()
            textSize = 28f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = (12 * density).toInt()
            }
        }

        val destinationText = TextView(context).apply {
            id = View.generateViewId()
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val statusBadge = TextView(context).apply {
            id = View.generateViewId()
            textSize = 12f
            setTextColor(Color.WHITE)
            val paddingH = (8 * density).toInt()
            val paddingV = (4 * density).toInt()
            setPadding(paddingH, paddingV, paddingH, paddingV)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        headerRow.addView(iconText)
        headerRow.addView(destinationText)
        headerRow.addView(statusBadge)

        // Info row
        val infoRow = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (8 * density).toInt()
            }
            orientation = LinearLayout.VERTICAL
        }

        val dateText = TextView(context).apply {
            id = View.generateViewId()
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        val distanceText = TextView(context).apply {
            id = View.generateViewId()
            textSize = 14f
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (4 * density).toInt()
            }
        }

        val categoryText = TextView(context).apply {
            id = View.generateViewId()
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = (4 * density).toInt()
            }
        }

        infoRow.addView(dateText)
        infoRow.addView(distanceText)
        infoRow.addView(categoryText)

        content.addView(headerRow)
        content.addView(infoRow)
        card.addView(content)

        return TripViewHolder(card, iconText, destinationText, statusBadge, dateText, distanceText, categoryText)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip, dateFormat, onTripClick)
    }

    class TripViewHolder(
        itemView: View,
        private val iconText: TextView,
        private val destinationText: TextView,
        private val statusBadge: TextView,
        private val dateText: TextView,
        private val distanceText: TextView,
        private val categoryText: TextView
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(trip: Trip, dateFormat: SimpleDateFormat, onTripClick: (Trip) -> Unit) {
            iconText.text = trip.tripType.getIcon()
            destinationText.text = trip.destination

            // Status badge
            if (trip.isActive) {
                statusBadge.text = "ACTIVE"
                statusBadge.setBackgroundColor(Color.parseColor("#FF4CAF50"))
                statusBadge.visibility = View.VISIBLE
            } else {
                statusBadge.visibility = View.GONE
            }

            // Date
            val endDateStr = trip.endDate?.let { " - ${dateFormat.format(it)}" } ?: ""
            dateText.text = "📅 ${dateFormat.format(trip.startDate)}$endDateStr"

            // Distance
            distanceText.text = if (trip.totalDistance > 0) {
                "📍 ${String.format("%.1f km", trip.totalDistance)}"
            } else {
                "📍 No distance recorded"
            }

            // Category
            trip.category?.let {
                categoryText.text = "${it.getIcon()} ${it.getDisplayName()}"
                categoryText.visibility = View.VISIBLE
            } ?: run {
                categoryText.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onTripClick(trip)
            }
        }
    }

    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}