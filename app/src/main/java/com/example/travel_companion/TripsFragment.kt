package com.example.travel_companion

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_companion.data.entity.Trip
import com.example.travel_companion.data.entity.TripType
import com.example.travel_companion.ui.activity.ActiveTripActivity
import com.example.travel_companion.ui.activity.TripDetailActivity
import com.example.travel_companion.ui.adapter.TripAdapter
import com.example.travel_companion.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

class TripsFragment : Fragment() {

    private val viewModel: TripViewModel by activityViewModels()
    private lateinit var adapter: TripAdapter
    private var recyclerView: RecyclerView? = null
    private var emptyStateView: CardView? = null
    private var filterChipsContainer: LinearLayout? = null

    private var currentFilter: TripType? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = createLayout()
        setupAdapter()
        observeTrips()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        emptyStateView = null
        filterChipsContainer = null
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
            text = "My Trips"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }
        mainLayout.addView(title)

        // Filters section
        val filtersTitle = TextView(context).apply {
            text = "Filter by type:"
            textSize = 14f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
        }
        mainLayout.addView(filtersTitle)

        filterChipsContainer = createFiltersContainer()
        mainLayout.addView(filterChipsContainer)

        // Date filter button
        val dateFilterBtn = Button(context).apply {
            text = "📅 Filter by Date Range"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
            setOnClickListener {
                showDateRangeFilter()
            }
        }
        mainLayout.addView(dateFilterBtn)

        // RecyclerView
        recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutManager = LinearLayoutManager(context)
        }
        mainLayout.addView(recyclerView)

        // Empty state
        emptyStateView = createEmptyStateCard()
        mainLayout.addView(emptyStateView)

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
                bottomMargin = dpToPx(16)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val scrollView = HorizontalScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val chipsLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // All filter
        chipsLayout.addView(createFilterChip("All", null, true))

        // Trip type filters
        TripType.values().forEach { type ->
            chipsLayout.addView(createFilterChip("${type.getIcon()} ${type.getDisplayName()}", type, false))
        }

        scrollView.addView(chipsLayout)
        container.addView(scrollView)

        return container
    }

    private fun createFilterChip(text: String, type: TripType?, isSelected: Boolean): CardView {
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
            this.text = text
            textSize = 14f
            setTextColor(if (isSelected) Color.WHITE else Color.BLACK)
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
        }

        chip.addView(textView)

        chip.setOnClickListener {
            currentFilter = type
            applyFilter(type)
            updateFilterChips(type)
        }

        return chip
    }

    private fun updateFilterChips(selectedType: TripType?) {
        filterChipsContainer?.let { container ->
            val scrollView = container.getChildAt(0) as? HorizontalScrollView
            val chipsLayout = scrollView?.getChildAt(0) as? LinearLayout

            chipsLayout?.let { layout ->
                for (i in 0 until layout.childCount) {
                    val chip = layout.getChildAt(i) as CardView
                    val textView = chip.getChildAt(0) as TextView

                    val isSelected = when {
                        i == 0 && selectedType == null -> true
                        i > 0 && selectedType != null && i - 1 == selectedType.ordinal -> true
                        else -> false
                    }

                    chip.setCardBackgroundColor(
                        if (isSelected) Color.parseColor("#FF6200EE")
                        else Color.WHITE
                    )
                    textView.setTextColor(if (isSelected) Color.WHITE else Color.BLACK)
                }
            }
        }
    }

    private fun createEmptyStateCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
            visibility = View.GONE
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

    private fun setupAdapter() {
        adapter = TripAdapter { trip ->
            if (trip.isActive) {
                // Open active trip activity
                val intent = Intent(requireContext(), ActiveTripActivity::class.java)
                intent.putExtra(ActiveTripActivity.EXTRA_TRIP_ID, trip.id)
                startActivity(intent)
            } else {
                // Open trip detail activity
                val intent = Intent(requireContext(), TripDetailActivity::class.java)
                intent.putExtra(TripDetailActivity.EXTRA_TRIP_ID, trip.id)
                startActivity(intent)
            }
        }
        recyclerView?.adapter = adapter
    }

    private fun observeTrips() {
        viewModel.allTrips.observe(viewLifecycleOwner) { trips ->
            if (currentFilter == null) {
                updateUI(trips)
            }
        }
    }

    private fun applyFilter(type: TripType?) {
        if (type == null) {
            // Show all trips
            viewModel.allTrips.observe(viewLifecycleOwner) { trips ->
                updateUI(trips)
            }
        } else {
            // Filter by type
            viewModel.getTripsByType(type).observe(viewLifecycleOwner) { trips ->
                updateUI(trips)
            }
        }
    }

    private fun showDateRangeFilter() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day, 0, 0, 0)
                val startDate = calendar.time

                // Show end date picker
                DatePickerDialog(
                    requireContext(),
                    { _, year2, month2, day2 ->
                        calendar.set(year2, month2, day2, 23, 59, 59)
                        val endDate = calendar.time

                        viewModel.getTripsInDateRange(startDate, endDate).observe(viewLifecycleOwner) { trips ->
                            updateUI(trips)
                            Toast.makeText(
                                requireContext(),
                                "Showing trips from ${dateFormat.format(startDate)} to ${dateFormat.format(endDate)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateUI(trips: List<Trip>) {
        if (trips.isEmpty()) {
            recyclerView?.visibility = View.GONE
            emptyStateView?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            emptyStateView?.visibility = View.GONE
            adapter.submitList(trips)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}