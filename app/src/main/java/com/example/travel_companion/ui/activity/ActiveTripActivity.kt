package com.example.travel_companion.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.travel_companion.data.entity.Trip
import com.example.travel_companion.service.LocationTrackingService
import com.example.travel_companion.util.PhotoManager
import com.example.travel_companion.viewmodel.TripViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ActiveTripActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private lateinit var photoManager: PhotoManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentTrip: Trip? = null
    private var currentPhotoFile: File? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private var txtDestination: TextView? = null
    private var txtDistanceValue: TextView? = null
    private var txtDurationValue: TextView? = null
    private var txtLocationsValue: TextView? = null
    private var btnStopTrip: Button? = null
    private var btnAddPhoto: Button? = null
    private var btnAddNote: Button? = null

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    companion object {
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
        private const val CAMERA_PERMISSION_CODE = 100
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoFile?.let { file ->
                savePhotoToTrip(file.absolutePath)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!com.example.travel_companion.util.PlayServicesHelper.checkPlayServices(this)) {
            Toast.makeText(
                this,
                "Google Play Services not available. GPS tracking may be limited.",
                Toast.LENGTH_LONG
            ).show()
        }

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]
        photoManager = PhotoManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val tripId = intent.getLongExtra(EXTRA_TRIP_ID, -1)
        if (tripId == -1L) {
            finish()
            return
        }

        setContentView(createLayout())
        setupObservers(tripId)
        startLocationTracking(tripId)
        getCurrentLocation()
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

        // ✅ BACK BUTTON AGGIUNTO
        val backButton = Button(this).apply {
            text = "← Back"
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
            setOnClickListener {
                // Torna alla MainActivity senza fermare il trip
                finish()
            }
        }
        mainLayout.addView(backButton)

        // Header card
        mainLayout.addView(createHeaderCard())

        // Stats card
        mainLayout.addView(createStatsCard())

        // Actions card
        mainLayout.addView(createActionsCard())

        // Stop button
        btnStopTrip = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
            }
            text = "Stop Trip"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#FFD32F2F"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                confirmStopTrip()
            }
        }
        mainLayout.addView(btnStopTrip)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createHeaderCard(): CardView {
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

        val title = TextView(this).apply {
            text = "Trip in Progress"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        txtDestination = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            textSize = 16f
            setTextColor(Color.DKGRAY)
        }

        content.addView(title)
        txtDestination?.let { content.addView(it) }
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
        }

        val statsTitle = TextView(this).apply {
            text = "Statistics"
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
        content.addView(statsTitle)

        val distanceRow = createStatRow("📏 Distance:", "0.0 km")
        val durationRow = createStatRow("⏱️ Duration:", "00:00:00")
        val locationsRow = createStatRow("📍 Locations:", "0")

        txtDistanceValue = distanceRow.getChildAt(1) as TextView
        txtDurationValue = durationRow.getChildAt(1) as TextView
        txtLocationsValue = locationsRow.getChildAt(1) as TextView

        content.addView(distanceRow)
        content.addView(durationRow)
        content.addView(locationsRow)

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

    private fun createActionsCard(): CardView {
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

        val actionsTitle = TextView(this).apply {
            text = "Actions"
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
        content.addView(actionsTitle)

        btnAddPhoto = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            text = "📷 Take Photo"
            setOnClickListener { takePhoto() }
        }

        btnAddNote = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "📝 Add Note"
            setOnClickListener { showAddNoteDialog() }
        }

        btnAddPhoto?.let { content.addView(it) }
        btnAddNote?.let { content.addView(it) }

        card.addView(content)
        return card
    }

    private fun setupObservers(tripId: Long) {
        viewModel.getTripById(tripId).observe(this) { trip ->
            currentTrip = trip
            trip?.let {
                txtDestination?.text = "📍 ${it.destination}\n🗓️ ${it.tripType.getDisplayName()}"
                updateDuration(it.startDate)
            }
        }

        viewModel.getLocationsByTrip(tripId).observe(this) { locations ->
            txtLocationsValue?.text = "${locations.size}"

            if (locations.size >= 2) {
                var distance = 0.0
                for (i in 0 until locations.size - 1) {
                    val loc1 = locations[i]
                    val loc2 = locations[i + 1]
                    distance += calculateDistance(
                        loc1.latitude, loc1.longitude,
                        loc2.latitude, loc2.longitude
                    )
                }
                txtDistanceValue?.text = String.format("%.2f km", distance)
            }
        }
    }

    private fun updateDuration(startDate: Date) {
        val duration = Date().time - startDate.time
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration / (1000 * 60)) % 60
        val seconds = (duration / 1000) % 60

        txtDurationValue?.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun startLocationTracking(tripId: Long) {
        val serviceIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_TRIP_ID, tripId)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        currentLatitude = it.latitude
                        currentLongitude = it.longitude
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takePhoto() {
        if (!photoManager.hasCameraPermission()) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
            return
        }

        try {
            currentPhotoFile = photoManager.createImageFile()
            val photoUri = photoManager.getImageUri(currentPhotoFile!!)
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Error taking photo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoToTrip(photoPath: String) {
        currentTrip?.let { trip ->
            viewModel.addPhoto(
                tripId = trip.id,
                photoUri = photoPath,
                latitude = currentLatitude,
                longitude = currentLongitude
            )
            Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
        }
    }

    private fun showAddNoteDialog() {
        val input = EditText(this).apply {
            hint = "Enter your note..."
            minLines = 3
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val note = input.text.toString().trim()
                if (note.isNotEmpty()) {
                    currentTrip?.let { trip ->
                        viewModel.addNote(
                            tripId = trip.id,
                            content = note,
                            latitude = currentLatitude,
                            longitude = currentLongitude
                        )
                        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmStopTrip() {
        AlertDialog.Builder(this)
            .setTitle("Stop Trip")
            .setMessage("Are you sure you want to stop tracking this trip?")
            .setPositiveButton("Stop") { _, _ ->
                stopTrip()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun stopTrip() {
        currentTrip?.let { trip ->
            val serviceIntent = Intent(this, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP_TRACKING
            }
            startService(serviceIntent)

            viewModel.stopTrip(trip.id)

            Toast.makeText(this, "Trip stopped successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        txtDestination = null
        txtDistanceValue = null
        txtDurationValue = null
        txtLocationsValue = null
        btnStopTrip = null
        btnAddPhoto = null
        btnAddNote = null
    }
}