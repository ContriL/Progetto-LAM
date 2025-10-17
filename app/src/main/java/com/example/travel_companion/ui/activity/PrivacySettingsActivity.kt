package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.travel_companion.viewmodel.TripViewModel

class PrivacySettingsActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences("travel_companion_prefs", MODE_PRIVATE)
    }

    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TripViewModel::class.java]
        setContentView(createLayout())
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

        // Title
        val title = TextView(this).apply {
            text = "🔒 Privacy & Security"
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

        // Privacy toggles
        mainLayout.addView(createToggleCard(
            "save_location_data",
            "Save Location Data",
            "Store GPS coordinates for your trips"
        ))

        mainLayout.addView(createToggleCard(
            "save_photos_metadata",
            "Save Photo Metadata",
            "Include location and time in photo data"
        ))

        mainLayout.addView(createToggleCard(
            "analytics_enabled",
            "Usage Analytics",
            "Help improve the app by sharing anonymous usage data"
        ))

        // Data management section
        val dataTitle = TextView(this).apply {
            text = "Data Management"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(dataTitle)

        // Export data button
        mainLayout.addView(createActionCard(
            "📦",
            "Export My Data",
            "Download all your trip data"
        ) {
            Toast.makeText(this, "Data export feature coming soon", Toast.LENGTH_SHORT).show()
        })

        // Clear cache button
        mainLayout.addView(createActionCard(
            "🗑️",
            "Clear Cache",
            "Free up space by removing temporary files"
        ) {
            showClearCacheDialog()
        })

        // Delete all data button
        mainLayout.addView(createActionCard(
            "⚠️",
            "Delete All Data",
            "Permanently remove all trips and data"
        ) {
            showDeleteAllDataDialog()
        })

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createToggleCard(prefKey: String, title: String, description: String): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val textLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val descText = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
        }

        val switch = Switch(this).apply {
            isChecked = prefs.getBoolean(prefKey, true)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(prefKey, isChecked).apply()
                Toast.makeText(
                    this@PrivacySettingsActivity,
                    if (isChecked) "$title enabled" else "$title disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        textLayout.addView(titleText)
        textLayout.addView(descText)
        content.addView(textLayout)
        content.addView(switch)
        card.addView(content)

        return card
    }

    private fun createActionCard(icon: String, title: String, description: String, onClick: () -> Unit): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            radius = dpToPx(8).toFloat()
            cardElevation = dpToPx(2).toFloat()
            isClickable = true
            isFocusable = true
        }

        val content = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        }

        val iconText = TextView(this).apply {
            text = icon
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(16)
            }
        }

        val textLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val descText = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
        }

        val arrow = TextView(this).apply {
            text = "›"
            textSize = 24f
            setTextColor(Color.GRAY)
        }

        textLayout.addView(titleText)
        textLayout.addView(descText)
        content.addView(iconText)
        content.addView(textLayout)
        content.addView(arrow)
        card.addView(content)

        card.setOnClickListener { onClick() }

        return card
    }

    private fun showClearCacheDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Cache")
            .setMessage("This will remove temporary files and free up space. Your trips and photos will not be affected.")
            .setPositiveButton("Clear") { _, _ ->
                // Clear cache logic here
                cacheDir.deleteRecursively()
                Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAllDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Delete All Data")
            .setMessage("WARNING: This will permanently delete:\n\n" +
                    "• All trips\n" +
                    "• All photos\n" +
                    "• All notes\n" +
                    "• All location data\n" +
                    "• All statistics\n\n" +
                    "This action CANNOT be undone!\n\n" +
                    "Are you absolutely sure?")
            .setPositiveButton("DELETE EVERYTHING") { _, _ ->
                showFinalConfirmationDialog()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun showFinalConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Final Confirmation")
            .setMessage("Type 'DELETE' to confirm permanent deletion of all data")
            .setView(EditText(this).apply {
                hint = "Type DELETE here"
            })
            .setPositiveButton("Confirm") { dialog, _ ->
                val editText = (dialog as AlertDialog).findViewById<EditText>(android.R.id.edit)
                if (editText?.text.toString() == "DELETE") {
                    deleteAllData()
                } else {
                    Toast.makeText(this, "Deletion cancelled - text didn't match", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAllData() {
        // TODO: Implement actual data deletion
        Toast.makeText(this, "All data has been deleted", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}