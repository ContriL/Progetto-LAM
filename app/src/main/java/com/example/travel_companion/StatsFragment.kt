package com.example.travel_companion

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.travel_companion.data.entity.TripType
import com.example.travel_companion.ui.activity.TripTypeStatsActivity
import com.example.travel_companion.viewmodel.TripViewModel

class StatsFragment : Fragment() {

    private val viewModel: TripViewModel by activityViewModels()
    private val typeCountCards = mutableMapOf<TripType, TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = createLayout()
        observeData()
        return view
    }

    private fun createLayout(): ScrollView {
        val context = requireContext()

        val scrollView = ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }

        val mainLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // Title
        val title = TextView(context).apply {
            text = "Travel Statistics"
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

        // Overview section
        val overviewTitle = TextView(context).apply {
            text = "Overview"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(overviewTitle)

        val statsGrid = createStatsGrid()
        mainLayout.addView(statsGrid)

        // Trip Types section
        val typesTitle = TextView(context).apply {
            text = "Trip Types Distribution"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(typesTitle)

        val tripTypesContainer = createTripTypesContainer()
        mainLayout.addView(tripTypesContainer)

        // Button to view detailed stats
        val btnDetailedStats = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
                bottomMargin = dpToPx(16)
            }
            text = "View Detailed Statistics"
            setOnClickListener {
                val intent = Intent(requireContext(), TripTypeStatsActivity::class.java)
                startActivity(intent)
            }
        }
        mainLayout.addView(btnDetailedStats)

        // Charts placeholder
        val chartsTitle = TextView(context).apply {
            text = "Charts & Visualizations"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(chartsTitle)

        val chartPlaceholder = createChartPlaceholder()
        mainLayout.addView(chartPlaceholder)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createStatsGrid(): LinearLayout {
        val context = requireContext()

        val grid = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // First row
        val row1 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        row1.addView(createStatCard("0", "Total Trips", "🗺️"))
        row1.addView(createStatCard("0 km", "Total Distance", "📍"))

        // Second row
        val row2 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        row2.addView(createStatCard("0", "Photos Taken", "📷"))
        row2.addView(createStatCard("0", "Countries Visited", "🌍"))

        grid.addView(row1)
        grid.addView(row2)

        return grid
    }

    private fun createStatCard(value: String, label: String, emoji: String): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val emojiText = TextView(context).apply {
            text = emoji
            textSize = 32f
            gravity = Gravity.CENTER
        }

        val valueText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            text = value
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF6200EE"))
        }

        val labelText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = label
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        content.addView(emojiText)
        content.addView(valueText)
        content.addView(labelText)
        card.addView(content)

        return card
    }

    private fun createTripTypesContainer(): LinearLayout {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Create mini card for each mandatory type
        val mandatoryTypes = listOf(TripType.LOCAL, TripType.DAY_TRIP, TripType.MULTI_DAY)

        mandatoryTypes.forEach { type ->
            container.addView(createTripTypeMiniCard(type))
        }

        return container
    }

    private fun createTripTypeMiniCard(type: TripType): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val icon = TextView(context).apply {
            text = type.getIcon()
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(12)
            }
        }

        val nameText = TextView(context).apply {
            text = type.getDisplayName()
            textSize = 14f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val countText = TextView(context).apply {
            text = "0"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF6200EE"))
        }

        typeCountCards[type] = countText

        content.addView(icon)
        content.addView(nameText)
        content.addView(countText)
        card.addView(content)

        return card
    }

    private fun createChartPlaceholder(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
            )
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
        }

        val icon = TextView(context).apply {
            text = "📊"
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val message = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            text = "Charts will appear here"
            textSize = 16f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = "Start tracking trips to see visualizations"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        content.addView(icon)
        content.addView(message)
        content.addView(subtitle)
        card.addView(content)

        return card
    }

    private fun observeData() {
        // Observe counts for each trip type
        TripType.values().take(3).forEach { type ->
            viewModel.getTripCountByType(type).observe(viewLifecycleOwner) { count ->
                typeCountCards[type]?.text = count?.toString() ?: "0"
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}