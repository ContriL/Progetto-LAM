package com.example.travel_companion.ui.activity

import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
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

    // Riferimenti alle card
    private var infoCardContent: LinearLayout? = null
    private var statsCardContent: LinearLayout? = null
    private var photosCardContent: LinearLayout? = null
    private var notesCardContent: LinearLayout? = null

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

        // Map card
        val mapCard = createMapCard()
        mainLayout.addView(mapCard)

        // Photos section
        val photosCard = createPhotosCard()
        mainLayout.addView(photosCard)

        // Notes section
        val notesCard = createNotesCard()
        mainLayout.addView(notesCard)

        // Delete button
        val deleteBtn = Button(this).apply {
            text = "🗑️ Delete Trip"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#FFD32F2F"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
            }
            setOnClickListener {
                showDeleteConfirmation()
            }
        }
        mainLayout.addView(deleteBtn)

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

        infoCardContent = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        card.addView(infoCardContent)
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

        statsCardContent = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
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
        statsCardContent?.addView(title)

        card.addView(statsCardContent)
        return card
    }

    private fun createMapCard(): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
            ).apply {
                bottomMargin = dpToPx(16)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
            isClickable = true
            isFocusable = true
            setOnClickListener {
                val intent = Intent(this@TripDetailActivity, TripMapActivity::class.java)
                intent.putExtra(TripMapActivity.EXTRA_TRIP_ID, currentTrip?.id ?: return@setOnClickListener)
                startActivity(intent)
            }
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
            text = "View Route Map"
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
            text = "Tap to see your travel route"
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

        photosCardContent = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // Header con pulsante
        val headerRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "📷 Photos"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val viewAllBtn = Button(this).apply {
            text = "View Gallery"
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                val intent = Intent(this@TripDetailActivity, PhotoGalleryActivity::class.java)
                intent.putExtra(PhotoGalleryActivity.EXTRA_TRIP_ID, currentTrip?.id ?: return@setOnClickListener)
                startActivity(intent)
            }
        }

        headerRow.addView(title)
        headerRow.addView(viewAllBtn)
        photosCardContent?.addView(headerRow)

        card.addView(photosCardContent)
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

        notesCardContent = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val title = TextView(this).apply {
            text = "📝 Notes"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }
        notesCardContent?.addView(title)

        card.addView(notesCardContent)
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

    private fun showDeleteConfirmation() {
        val trip = currentTrip ?: return

        if (trip.isActive) {
            AlertDialog.Builder(this)
                .setTitle("Cannot Delete Active Trip")
                .setMessage("You cannot delete a trip that is currently active. Please stop the trip first.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete \"${trip.destination}\"?\n\n" +
                    "This will permanently delete:\n" +
                    "• All GPS locations (${trip.totalDistance.toInt()} km tracked)\n" +
                    "• All photos\n" +
                    "• All notes\n\n" +
                    "This action CANNOT be undone!")
            .setPositiveButton("Delete") { _, _ ->
                deleteTrip(trip)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteTrip(trip: Trip) {
        viewModel.deleteTrip(trip.id)
        Toast.makeText(
            this,
            "Trip \"${trip.destination}\" deleted successfully",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    private fun updateTripInfo(trip: Trip) {
        val content = infoCardContent ?: return
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
        val content = statsCardContent ?: return

        viewModel.getLocationsByTrip(tripId).observe(this) { locations ->
            // Remove old stats (except title)
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
        val content = photosCardContent ?: return

        viewModel.getPhotosByTrip(tripId).observe(this) { photos ->
            // Remove old content (except header)
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
        val content = notesCardContent ?: return

        viewModel.getNotesByTrip(tripId).observe(this) { notes ->
            // Remove old content (except title)
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

    override fun onDestroy() {
        super.onDestroy()
        infoCardContent = null
        statsCardContent = null
        photosCardContent = null
        notesCardContent = null
    }
}
