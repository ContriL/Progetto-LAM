package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.travel_companion.service.ActivityRecognitionManager
import com.example.travel_companion.service.GeofencingManager

class LocationSettingsActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences("travel_companion_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            text = "📍 Location Settings"
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

        // Location settings
        mainLayout.addView(createToggleCard(
            "activity_recognition_enabled",
            "Activity Recognition",
            "Automatically detect when you're traveling"
        ) { isEnabled ->
            if (isEnabled) {
                ActivityRecognitionManager.startActivityRecognition(this)
            } else {
                ActivityRecognitionManager.stopActivityRecognition(this)
            }
        })

        mainLayout.addView(createToggleCard(
            "geofencing_enabled",
            "Geofencing",
            "Get alerts when entering points of interest"
        ) { isEnabled ->
            if (isEnabled) {
                val geofencingManager = GeofencingManager(this)
                geofencingManager.addPopularPlacesGeofences()
            } else {
                val geofencingManager = GeofencingManager(this)
                geofencingManager.removeAllGeofences()
            }
        })

        mainLayout.addView(createToggleCard(
            "background_tracking",
            "Background Tracking",
            "Track location even when app is closed"
        ))

        // Update frequency
        mainLayout.addView(createFrequencyCard())

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createToggleCard(
        prefKey: String,
        title: String,
        description: String,
        onToggle: ((Boolean) -> Unit)? = null
    ): CardView {
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
                onToggle?.invoke(isChecked)
                Toast.makeText(
                    this@LocationSettingsActivity,
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

    private fun createFrequencyCard(): CardView {
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
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val titleText = TextView(this).apply {
            text = "Location Update Frequency"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val descText = TextView(this).apply {
            text = "Higher frequency uses more battery"
            textSize = 12f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(12)
            }
        }

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        val frequencies = listOf(
            Pair("High (Every 5 seconds)", 5),
            Pair("Medium (Every 15 seconds)", 15),
            Pair("Low (Every 30 seconds)", 30)
        )

        val currentFreq = prefs.getInt("location_update_frequency", 15)

        frequencies.forEach { (label, value) ->
            val radioButton = RadioButton(this).apply {
                text = label
                isChecked = value == currentFreq
                setOnClickListener {
                    prefs.edit().putInt("location_update_frequency", value).apply()
                    Toast.makeText(
                        this@LocationSettingsActivity,
                        "Update frequency changed to $label",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            radioGroup.addView(radioButton)
        }

        content.addView(titleText)
        content.addView(descText)
        content.addView(radioGroup)
        card.addView(content)

        return card
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}