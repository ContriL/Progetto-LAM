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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_companion.ui.activity.ActiveTripActivity
import com.example.travel_companion.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var txtWelcome: TextView? = null
    private var txtActiveTrip: TextView? = null
    private var btnStopTrip: Button? = null
    private var recyclerRecentTrips: RecyclerView? = null
    private var statsCard1: TextView? = null
    private var statsCard2: TextView? = null
    private var statsCard3: TextView? = null

    private val viewModel: TripViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = createLayout()
            setupUI()
            observeData()
            view
        } catch (e: Exception) {
            e.printStackTrace()
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
        btnStopTrip = null
        recyclerRecentTrips = null
        statsCard1 = null
        statsCard2 = null
        statsCard3 = null
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
            ).apply {
                bottomMargin = dpToPx(12)
            }
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        btnStopTrip = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "View Details"
            visibility = View.GONE
            setOnClickListener {
                viewModel.activeTrip.value?.let { trip ->
                    val intent = Intent(requireContext(), ActiveTripActivity::class.java)
                    intent.putExtra(ActiveTripActivity.EXTRA_TRIP_ID, trip.id)
                    startActivity(intent)
                }
            }
        }

        cardContent.addView(title)
        txtActiveTrip?.let { cardContent.addView(it) }
        btnStopTrip?.let { cardContent.addView(it) }
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

            // Store reference for updates
            when (label) {
                "Total Trips" -> statsCard1 = this
                "Distance" -> statsCard2 = this
                "This Month" -> statsCard3 = this
            }
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
        }
    }

    private fun observeData() {
        // Observe active trip
        viewModel.activeTrip.observe(viewLifecycleOwner) { trip ->
            if (trip != null && trip.isActive) {
                txtActiveTrip?.text = """
                    📍 ${trip.destination}
                    🗓️ Started: ${dateFormat.format(trip.startDate)}
                    🚗 Type: ${trip.tripType.getDisplayName()}
                    ${trip.description?.let { "\n📝 $it" } ?: ""}
                """.trimIndent()
                btnStopTrip?.visibility = View.VISIBLE
            } else {
                txtActiveTrip?.text = "No active trip. Start a new adventure!"
                btnStopTrip?.visibility = View.GONE
            }
        }

        // Observe trip count
        viewModel.tripCount.observe(viewLifecycleOwner) { count ->
            statsCard1?.text = count?.toString() ?: "0"
        }

        // Observe total distance
        viewModel.totalDistance.observe(viewLifecycleOwner) { distance ->
            val distanceStr = if (distance != null && distance > 0) {
                String.format("%.1f km", distance)
            } else {
                "0 km"
            }
            statsCard2?.text = distanceStr
        }

        // Observe trips this month
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.time

        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.time

        viewModel.getTripsInDateRange(startOfMonth, endOfMonth).observe(viewLifecycleOwner) { trips ->
            statsCard3?.text = trips?.size?.toString() ?: "0"
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}