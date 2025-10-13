package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.travel_companion.data.entity.Trip
import com.example.travel_companion.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

class TripDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private var currentTrip: Trip? = null

    private val dateFormat = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        val tripId = intent.getLongExtra(EXTRA_TRIP_ID, -1)
        if (tripId == -1L) {
            finish()
            return
        }

        setContentView(createLayout())
        observeTrip(tripId)
    }

    private fun createLayout(): ScrollView {
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val mainLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // Back button
        val backBtn = Button(this).apply {
            text = "← Back"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
            setOnClickListener { finish() }
        }
        mainLayout.addView(backBtn)

        // Trip info card
        val infoCard = createTripInfoCard()
        mainLayout.addView(infoCard)

        // Stats card
        val statsCard = createStatsCard()
        mainLayout.addView(statsCard)

        // Map placeholder
        val mapCard = createMapCard()
        mainLayout.addView(mapCard)

        // Photos section
        val photosCard = createPhotosCard()
        mainLayout.addView(photosCard)

        // Notes section
        val notesCard = createNotesCard()
        mainLayout.addView(notesCard)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createTripInfoCard(): CardView {
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
            id = android.view.View.generateViewId()
        }

        card.addView(content)
        return card
    }

    private fun createStatsCard(): CardView {
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
            id = android.view.View.generateViewId()
        }

        val title = TextView(this).apply {
            text = "Trip Statistics"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
        }
        content.addView(title)

        card.addView(content)
        return card
    }

    private fun createMapCard(): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(250)
            ).apply {
                bottomMargin = dpToPx(16)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val icon = TextView(this).apply {
            text = "🗺️"
            textSize = 48f
            gravity = Gravity.CENTER
        }

        val message = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            text = "Route Map"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = "Map integration coming in next step"
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        content.addView(icon)
        content.addView(message)
        content.addView(subtitle)
        card.addView(content)

        return card
    }

    private fun createPhotosCard(): CardView {
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
            id = android.view.View.generateViewId()
        }

        val title = TextView(this).apply {
            text = "📷 Photos"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }
        content.addView(title)

        card.addView(content)
        return card
    }

    private fun createNotesCard(): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
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
            id = android.view.View.generateViewId()
        }

        val title = TextView(this).apply {
            text = "📝 Notes"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }
        content.addView(title)

        card.addView(content)
        return card
    }

    private fun observeTrip(tripId: Long) {
        viewModel.getTripById(tripId).observe(this) { trip ->
            trip?.let {
                currentTrip = it
                updateTripInfo(it)
                loadStatistics(tripId)
                loadPhotos(tripId)
                loadNotes(tripId)
            } ?: run {
                Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateTripInfo(trip: Trip) {
        val infoCard = findViewById<CardView>(android.R.id.content).let { root ->
            (root as ViewGroup).getChildAt(0).let { scroll ->
                ((scroll as ScrollView).getChildAt(0) as LinearLayout).getChildAt(1) as CardView
            }
        }

        val content = infoCard.getChildAt(0) as LinearLayout
        content.removeAllViews()

        // Icon and destination
        val header = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = trip.tripType.getIcon()
            textSize = 36f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(12)
            }
        }

        val destination = TextView(this).apply {
            text = trip.destination
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        header.addView(icon)
        header.addView(destination)
        content.addView(header)

        // Divider
        val divider = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(1)
            ).apply {
                topMargin = dpToPx(12)
                bottomMargin = dpToPx(12)
            }
            setBackgroundColor(Color.LTGRAY)
        }
        content.addView(divider)

        // Trip type
        content.addView(createInfoRow("Type:", trip.tripType.getDisplayName()))

        // Dates
        val dateStr = if (trip.endDate != null) {
            "${dateOnlyFormat.format(trip.startDate)} - ${dateOnlyFormat.format(trip.endDate)}"
        } else {
            dateOnlyFormat.format(trip.startDate)
        }
        content.addView(createInfoRow("Dates:", dateStr))

        // Category
        trip.category?.let {
            content.addView(createInfoRow("Category:", "${it.getIcon()} ${it.getDisplayName()}"))
        }

        // Budget
        trip.budget?.let {
            content.addView(createInfoRow("Budget:", String.format("%.2f €", it)))
        }

        // Description
        trip.description?.let {
            val descLabel = TextView(this).apply {
                text = "Description:"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.DKGRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(12)
                    bottomMargin = dpToPx(4)
                }
            }
            content.addView(descLabel)

            val descText = TextView(this).apply {
                text = it
                textSize = 14f
                setTextColor(Color.DKGRAY)
            }
            content.addView(descText)
        }
    }

    private fun createInfoRow(label: String, value: String): LinearLayout {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val labelText = TextView(this).apply {
            text = label
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(100),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val valueText = TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(Color.BLACK)
        }

        row.addView(labelText)
        row.addView(valueText)

        return row
    }

    private fun loadStatistics(tripId: Long) {
        val statsCard = findViewById<CardView>(android.R.id.content).let { root ->
            (root as ViewGroup).getChildAt(0).let { scroll ->
                ((scroll as ScrollView).getChildAt(0) as LinearLayout).getChildAt(2) as CardView
            }
        }

        val content = statsCard.getChildAt(0) as LinearLayout

        viewModel.getLocationsByTrip(tripId).observe(this) { locations ->
            // Remove old stats if any
            while (content.childCount > 1) {
                content.removeViewAt(1)
            }

            content.addView(createStatRow("📍 Waypoints:", "${locations.size}"))
            content.addView(createStatRow("📏 Distance:", String.format("%.2f km", currentTrip?.totalDistance ?: 0.0)))

            if (locations.isNotEmpty()) {
                val firstLoc = locations.first()
                val lastLoc = locations.last()
                content.addView(createStatRow("Start:", "${firstLoc.latitude.format(4)}, ${firstLoc.longitude.format(4)}"))
                content.addView(createStatRow("End:", "${lastLoc.latitude.format(4)}, ${lastLoc.longitude.format(4)}"))
            }
        }
    }

    private fun loadPhotos(tripId: Long) {
        val photosCard = findViewById<CardView>(android.R.id.content).let { root ->
            (root as ViewGroup).getChildAt(0).let { scroll ->
                ((scroll as ScrollView).getChildAt(0) as LinearLayout).getChildAt(4) as CardView
            }
        }

        val content = photosCard.getChildAt(0) as LinearLayout

        viewModel.getPhotosByTrip(tripId).observe(this) { photos ->
            // Remove old photos if any
            while (content.childCount > 1) {
                content.removeViewAt(1)
            }

            if (photos.isEmpty()) {
                val emptyText = TextView(this).apply {
                    text = "No photos yet"
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(8)
                    }
                }
                content.addView(emptyText)
            } else {
                val countText = TextView(this).apply {
                    text = "${photos.size} photo(s) taken"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(8)
                    }
                }
                content.addView(countText)
            }
        }
    }

    private fun loadNotes(tripId: Long) {
        val notesCard = findViewById<CardView>(android.R.id.content).let { root ->
            (root as ViewGroup).getChildAt(0).let { scroll ->
                ((scroll as ScrollView).getChildAt(0) as LinearLayout).getChildAt(5) as CardView
            }
        }

        val content = notesCard.getChildAt(0) as LinearLayout

        viewModel.getNotesByTrip(tripId).observe(this) { notes ->
            // Remove old notes if any
            while (content.childCount > 1) {
                content.removeViewAt(1)
            }

            if (notes.isEmpty()) {
                val emptyText = TextView(this).apply {
                    text = "No notes yet"
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(8)
                    }
                }
                content.addView(emptyText)
            } else {
                notes.forEach { note ->
                    val noteCard = createNoteCard(note.content, note.timestamp)
                    content.addView(noteCard)
                }
            }
        }
    }

    private fun createStatRow(label: String, value: String): LinearLayout {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val labelText = TextView(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.GRAY)
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
            setTextColor(Color.BLACK)
        }

        row.addView(labelText)
        row.addView(valueText)

        return row
    }

    private fun createNoteCard(content: String, timestamp: Date): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()
            setCardBackgroundColor(Color.parseColor("#FFF5F5F5"))
        }

        val layout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val noteText = TextView(this).apply {
            text = content
            textSize = 14f
            setTextColor(Color.BLACK)
        }

        val timeText = TextView(this).apply {
            text = dateFormat.format(timestamp)
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
        }

        layout.addView(noteText)
        layout.addView(timeText)
        card.addView(layout)

        return card
    }

    private fun Double.format(decimals: Int): String {
        return String.format("%.${decimals}f", this)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}