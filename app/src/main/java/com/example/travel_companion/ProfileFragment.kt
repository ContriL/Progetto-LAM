package com.example.travel_companion

import android.content.Intent
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
import androidx.fragment.app.activityViewModels
import com.example.travel_companion.ui.activity.*
import com.example.travel_companion.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private val viewModel: TripViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = createLayout()
        observeData()
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

        // Profile header
        val profileHeader = createProfileHeader()

        // User info card
        val userInfoCard = createUserInfoCard()

        // Settings section
        val settingsTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
            text = "Settings"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val settingsContainer = createSettingsContainer()

        // About section
        val aboutTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
            text = "About"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val aboutCard = createAboutCard()

        // Add all views
        mainLayout.addView(profileHeader)
        mainLayout.addView(userInfoCard)
        mainLayout.addView(settingsTitle)
        mainLayout.addView(settingsContainer)
        mainLayout.addView(aboutTitle)
        mainLayout.addView(aboutCard)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createProfileHeader(): LinearLayout {
        val context = requireContext()

        val header = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        // Profile picture placeholder
        val profilePic = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(80),
                dpToPx(80)
            )
            text = "👤"
            textSize = 48f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#FFEEEEEE"))
        }

        val userName = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            text = "Traveler"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val userEmail = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = "traveler@example.com"
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        header.addView(profilePic)
        header.addView(userName)
        header.addView(userEmail)

        return header
    }

    private fun createUserInfoCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val memberSinceRow = createInfoRow("Member since", dateFormat.format(Date()))
        val totalTripsRow = createInfoRow("Total trips", "Loading...")
        val favDestRow = createInfoRow("Favorite destination", "Not set")

        content.addView(memberSinceRow)
        content.addView(totalTripsRow)
        content.addView(favDestRow)

        card.addView(content)
        return card
    }

    private fun createInfoRow(label: String, value: String): LinearLayout {
        val context = requireContext()

        val row = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val labelText = TextView(context).apply {
            text = label
            textSize = 14f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val valueText = TextView(context).apply {
            text = value
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            tag = label // Per recuperarlo dopo
        }

        row.addView(labelText)
        row.addView(valueText)

        return row
    }

    private fun createSettingsContainer(): LinearLayout {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        val settings = listOf(
            Triple("🔔", "Notifications", NotificationsSettingsActivity::class.java),
            Triple("📍", "Location Settings", LocationSettingsActivity::class.java),
            Triple("🌐", "Language", LanguageSettingsActivity::class.java),
            Triple("🎨", "Theme", ThemeSettingsActivity::class.java),
            Triple("🔒", "Privacy & Security", PrivacySettingsActivity::class.java)
        )

        settings.forEach { (icon, title, activityClass) ->
            container.addView(createSettingItem(icon, title, activityClass))
        }

        return container
    }

    private fun createSettingItem(icon: String, title: String, activityClass: Class<*>): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
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

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        }

        val iconText = TextView(context).apply {
            text = icon
            textSize = 24f
        }

        val titleText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dpToPx(16)
            }
            text = title
            textSize = 16f
            setTextColor(Color.BLACK)
        }

        val arrow = TextView(context).apply {
            text = "›"
            textSize = 24f
            setTextColor(Color.GRAY)
        }

        content.addView(iconText)
        content.addView(titleText)
        content.addView(arrow)
        card.addView(content)

        card.setOnClickListener {
            val intent = Intent(requireContext(), activityClass)
            startActivity(intent)
        }

        return card
    }

    private fun createAboutCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val appName = TextView(context).apply {
            text = "Travel Companion"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
        }

        val version = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = "Version 1.0.0"
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        val description = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            text = "Your personal travel companion for tracking adventures and creating memories."
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        content.addView(appName)
        content.addView(version)
        content.addView(description)
        card.addView(content)

        return card
    }

    private fun observeData() {
        viewModel.tripCount.observe(viewLifecycleOwner) { count ->
            view?.findViewWithTag<TextView>("Total trips")?.text = "$count trips"
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}