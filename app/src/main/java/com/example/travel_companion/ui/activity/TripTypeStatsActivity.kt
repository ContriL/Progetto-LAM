package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.travel_companion.data.entity.TripType
import com.example.travel_companion.viewmodel.TripViewModel

class TripTypeStatsActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private val statCards = mutableMapOf<TripType, StatCardViews>()

    data class StatCardViews(
        val countText: TextView,
        val distanceText: TextView,
        val avgDistanceText: TextView
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        setContentView(createLayout())
        observeData()
    }

    private fun createLayout(): ScrollView {
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // Title
        val title = TextView(this).apply {
            text = "Trip Statistics by Type"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
        }
        mainLayout.addView(title)

        // Create card for each trip type
        TripType.values().forEach { type ->
            mainLayout.addView(createTripTypeCard(type))
        }

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createTripTypeCard(type: TripType): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // Header with icon and name
        val header = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = type.getIcon()
            textSize = 32f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(12)
            }
        }

        val nameLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }

        val name = TextView(this).apply {
            text = type.getDisplayName()
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val description = TextView(this).apply {
            text = type.getDescription()
            textSize = 12f
            setTextColor(Color.GRAY)
        }

        nameLayout.addView(name)
        nameLayout.addView(description)
        header.addView(icon)
        header.addView(nameLayout)
        content.addView(header)

        // Divider
        val divider = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
            ).apply {
                topMargin = dpToPx(8)
                bottomMargin = dpToPx(12)
            }
            setBackgroundColor(Color.LTGRAY)
        }
        content.addView(divider)

        // Stats
        val countText = createStatRow("Total Trips:", "0")
        val distanceText = createStatRow("Total Distance:", "0.0 km")
        val avgDistanceText = createStatRow("Avg Distance:", "0.0 km")

        content.addView(countText)
        content.addView(distanceText)
        content.addView(avgDistanceText)

        // Store references
        statCards[type] = StatCardViews(
            countText = countText.getChildAt(1) as TextView,
            distanceText = distanceText.getChildAt(1) as TextView,
            avgDistanceText = avgDistanceText.getChildAt(1) as TextView
        )

        card.addView(content)
        return card
    }

    private fun createStatRow(label: String, value: String): LinearLayout {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val labelText = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val valueText = TextView(this).apply {
            text = value
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF6200EE"))
        }

        row.addView(labelText)
        row.addView(valueText)

        return row
    }

    private fun observeData() {
        TripType.values().forEach { type ->
            val views = statCards[type] ?: return@forEach

            // Observe count
            viewModel.getTripCountByType(type).observe(this) { count ->
                views.countText.text = count?.toString() ?: "0"
            }

            // Observe total distance
            viewModel.getTotalDistanceByType(type).observe(this) { distance ->
                val distStr = if (distance != null && distance > 0) {
                    String.format("%.1f km", distance)
                } else {
                    "0.0 km"
                }
                views.distanceText.text = distStr
            }

            // Observe average distance
            viewModel.getAverageDistanceByType(type).observe(this) { avgDistance ->
                val avgStr = if (avgDistance != null && avgDistance > 0) {
                    String.format("%.1f km", avgDistance)
                } else {
                    "0.0 km"
                }
                views.avgDistanceText.text = avgStr
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}