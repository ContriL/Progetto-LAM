package com.example.travel_companion

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class TripsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = createLayout()
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
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
            text = "My Trips"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        // Filters section
        val filtersTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            text = "Filter by type:"
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        val filtersContainer = createFiltersContainer()

        // Empty state message
        val emptyCard = createEmptyStateCard()

        // Add all views
        mainLayout.addView(title)
        mainLayout.addView(filtersTitle)
        mainLayout.addView(filtersContainer)
        mainLayout.addView(emptyCard)

        // TODO: Qui andrà la RecyclerView con i viaggi quando implementata

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createFiltersContainer(): LinearLayout {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val filters = listOf("All", "Local", "Day Trip", "Multi-Day")

        filters.forEach { filterName ->
            val filterChip = createFilterChip(filterName, filterName == "All")
            container.addView(filterChip)
        }

        return container
    }

    private fun createFilterChip(text: String, isSelected: Boolean): CardView {
        val context = requireContext()

        val chip = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, dpToPx(8), 0)
            }
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(2).toFloat()
            setCardBackgroundColor(
                if (isSelected) Color.parseColor("#FF6200EE")
                else Color.WHITE
            )
        }

        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.text = text
            textSize = 14f
            setTextColor(if (isSelected) Color.WHITE else Color.BLACK)
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
        }

        chip.addView(textView)

        // TODO: Aggiungere onClick listener per filtrare

        return chip
    }

    private fun createEmptyStateCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(32)
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
            setPadding(dpToPx(32), dpToPx(32), dpToPx(32), dpToPx(32))
        }

        val icon = TextView(context).apply {
            text = "✈️"
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val message = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
            text = "No trips yet"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val subtitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            text = "Start your first adventure by tapping the + button"
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        content.addView(icon)
        content.addView(message)
        content.addView(subtitle)
        card.addView(content)

        return card
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}