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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private var txtWelcome: TextView? = null
    private var txtActiveTrip: TextView? = null
    private var recyclerRecentTrips: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = createLayout()
            setupUI()
            loadActiveTrip()
            loadRecentTrips()
            view
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback view in caso di errore
            TextView(requireContext()).apply {
                text = "Error loading home screen"
                gravity = Gravity.CENTER
                setPadding(32, 32, 32, 32)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        txtWelcome = null
        txtActiveTrip = null
        recyclerRecentTrips = null
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

        // Welcome text
        txtWelcome = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        // Active Trip Card
        val activeTripCard = createActiveTripCard()

        // Quick Stats title
        val statsTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            text = "Quick Stats"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        // Stats cards container
        val statsContainer = createStatsCards()

        // Recent trips title
        val recentTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            text = "Recent Trips"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        // RecyclerView
        recyclerRecentTrips = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Add all views
        txtWelcome?.let { mainLayout.addView(it) }
        mainLayout.addView(activeTripCard)
        mainLayout.addView(statsTitle)
        mainLayout.addView(statsContainer)
        mainLayout.addView(recentTitle)
        recyclerRecentTrips?.let { mainLayout.addView(it) }

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createActiveTripCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val cardContent = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val title = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            text = "Active Trip"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        txtActiveTrip = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        cardContent.addView(title)
        txtActiveTrip?.let { cardContent.addView(it) }
        card.addView(cardContent)

        return card
    }

    private fun createStatsCards(): LinearLayout {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
        }

        container.addView(createStatCard("0", "Total Trips"))
        container.addView(createStatCard("0 km", "Distance"))
        container.addView(createStatCard("0", "This Month"))

        return container
    }

    private fun createStatCard(value: String, label: String): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val valueText = TextView(context).apply {
            text = value
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF6200EE"))
        }

        val labelText = TextView(context).apply {
            text = label
            textSize = 12f
            setTextColor(Color.GRAY)
        }

        content.addView(valueText)
        content.addView(labelText)
        card.addView(content)

        return card
    }

    private fun setupUI() {
        txtWelcome?.text = "Welcome back, Traveler!"

        // Setup RecyclerView
        recyclerRecentTrips?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // TODO: Impostare adapter quando disponibile
        }
    }

    private fun loadActiveTrip() {
        txtActiveTrip?.text = "No active trip. Start a new adventure!"
    }

    private fun loadRecentTrips() {
        // TODO: Caricare gli ultimi viaggi dal database
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}