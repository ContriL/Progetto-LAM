package com.example.travel_companion.ui.activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_companion.data.entity.TripPhoto
import com.example.travel_companion.ui.adapter.PhotoGalleryAdapter
import com.example.travel_companion.viewmodel.TripViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoGalleryActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private lateinit var adapter: PhotoGalleryAdapter
    private var recyclerView: RecyclerView? = null
    private var emptyStateView: LinearLayout? = null
    private var tripId: Long = -1

    private val dateFormat = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault())

    companion object {
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tripId = intent.getLongExtra(EXTRA_TRIP_ID, -1)
        if (tripId == -1L) {
            finish()
            return
        }

        setContentView(createLayout())

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]

        setupRecyclerView()
        observePhotos()
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
            text = "📷 Photo Gallery"
            textSize = 24f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }
        mainLayout.addView(title)

        // RecyclerView
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(recyclerView)

        // Empty state
        emptyStateView = createEmptyState()
        mainLayout.addView(emptyStateView)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createEmptyState(): LinearLayout {
        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), dpToPx(32), dpToPx(32), dpToPx(32))
            visibility = android.view.View.GONE
        }

        val icon = TextView(this).apply {
            text = "📷"
            textSize = 64f
            gravity = Gravity.CENTER
        }

        val message = TextView(this).apply {
            text = "No photos yet"
            textSize = 18f
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
        }

        container.addView(icon)
        container.addView(message)

        return container
    }

    private fun setupRecyclerView() {
        adapter = PhotoGalleryAdapter(
            onPhotoClick = { photo -> showPhotoDetail(photo) },
            onPhotoLongClick = { photo -> showDeleteConfirmation(photo) }
        )

        recyclerView?.apply {
            layoutManager = GridLayoutManager(this@PhotoGalleryActivity, 3)
            adapter = this@PhotoGalleryActivity.adapter
        }
    }

    private fun observePhotos() {
        viewModel.getPhotosByTrip(tripId).observe(this) { photos ->
            if (photos.isEmpty()) {
                recyclerView?.visibility = android.view.View.GONE
                emptyStateView?.visibility = android.view.View.VISIBLE
            } else {
                recyclerView?.visibility = android.view.View.VISIBLE
                emptyStateView?.visibility = android.view.View.GONE
                adapter.submitList(photos)
            }
        }
    }

    private fun showPhotoDetail(photo: TripPhoto) {
        val dialog = AlertDialog.Builder(this)
        val dialogView = createPhotoDetailView(photo)

        dialog.setView(dialogView)
            .setPositiveButton("Close", null)
            .setNegativeButton("Delete") { _, _ ->
                showDeleteConfirmation(photo)
            }
            .show()
    }

    private fun createPhotoDetailView(photo: TripPhoto): LinearLayout {
        val layout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // Photo image
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(300)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP

            try {
                val file = File(Uri.parse(photo.photoUri).path ?: "")
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    setImageBitmap(bitmap)
                } else {
                    setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (e: Exception) {
                setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
        layout.addView(imageView)

        // Photo info card
        val infoCard = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
            }
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()
        }

        val infoLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        // Date
        val dateText = TextView(this).apply {
            text = "📅 ${dateFormat.format(photo.timestamp)}"
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }
        infoLayout.addView(dateText)

        // Location if available
        if (photo.latitude != null && photo.longitude != null) {
            val locationText = TextView(this).apply {
                text = "📍 ${String.format("%.4f", photo.latitude)}, ${String.format("%.4f", photo.longitude)}"
                textSize = 12f
                setTextColor(Color.GRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(4)
                }
            }
            infoLayout.addView(locationText)
        }

        // Caption if available
        photo.caption?.let { caption ->
            val captionText = TextView(this).apply {
                text = caption
                textSize = 14f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(8)
                }
            }
            infoLayout.addView(captionText)
        }

        infoCard.addView(infoLayout)
        layout.addView(infoCard)

        return layout
    }

    private fun showDeleteConfirmation(photo: TripPhoto) {
        AlertDialog.Builder(this)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete this photo? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deletePhoto(photo)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deletePhoto(photo: TripPhoto) {
        // Delete physical file
        try {
            val file = File(Uri.parse(photo.photoUri).path ?: "")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Delete from database
        viewModel.deletePhoto(photo)
        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView = null
        emptyStateView = null
    }
}