package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class ThemeSettingsActivity : AppCompatActivity() {

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
            text = "🎨 Theme Settings"
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

        // Theme options
        val themes = listOf(
            Triple("☀️", "Light Mode", AppCompatDelegate.MODE_NIGHT_NO),
            Triple("🌙", "Dark Mode", AppCompatDelegate.MODE_NIGHT_YES),
            Triple("🔄", "System Default", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        )

        val currentTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        themes.forEach { (icon, themeName, mode) ->
            mainLayout.addView(createThemeCard(icon, themeName, mode, mode == currentTheme))
        }

        // Color scheme section
        val colorTitle = TextView(this).apply {
            text = "Color Scheme"
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
        mainLayout.addView(colorTitle)

        val colors = listOf(
            Pair("Default Purple", "#6200EE"),
            Pair("Ocean Blue", "#0288D1"),
            Pair("Forest Green", "#388E3C"),
            Pair("Sunset Orange", "#F57C00"),
            Pair("Ruby Red", "#D32F2F")
        )

        val currentColor = prefs.getString("color_scheme", "#6200EE") ?: "#6200EE"

        colors.forEach { (colorName, colorHex) ->
            mainLayout.addView(createColorCard(colorName, colorHex, colorHex == currentColor))
        }

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createThemeCard(icon: String, themeName: String, mode: Int, isSelected: Boolean): CardView {
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
            setCardBackgroundColor(
                if (isSelected) Color.parseColor("#E8EAF6")
                else Color.WHITE
            )
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
            textSize = 32f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(16)
            }
        }

        val themeText = TextView(this).apply {
            text = themeName
            textSize = 16f
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(if (isSelected) Color.parseColor("#3F51B5") else Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val checkmark = TextView(this).apply {
            text = if (isSelected) "✓" else ""
            textSize = 24f
            setTextColor(Color.parseColor("#3F51B5"))
        }

        content.addView(iconText)
        content.addView(themeText)
        content.addView(checkmark)
        card.addView(content)

        card.setOnClickListener {
            prefs.edit().putInt("theme_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            Toast.makeText(this, "Theme changed to $themeName", Toast.LENGTH_SHORT).show()
        }

        return card
    }

    private fun createColorCard(colorName: String, colorHex: String, isSelected: Boolean): CardView {
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
            setCardBackgroundColor(
                if (isSelected) Color.parseColor("#E8EAF6")
                else Color.WHITE
            )
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

        val colorPreview = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(40),
                dpToPx(40)
            ).apply {
                rightMargin = dpToPx(16)
            }
            setBackgroundColor(Color.parseColor(colorHex))
        }

        val colorText = TextView(this).apply {
            text = colorName
            textSize = 16f
            setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            setTextColor(if (isSelected) Color.parseColor("#3F51B5") else Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val checkmark = TextView(this).apply {
            text = if (isSelected) "✓" else ""
            textSize = 24f
            setTextColor(Color.parseColor("#3F51B5"))
        }

        content.addView(colorPreview)
        content.addView(colorText)
        content.addView(checkmark)
        card.addView(content)

        card.setOnClickListener {
            prefs.edit().putString("color_scheme", colorHex).apply()
            Toast.makeText(this, "$colorName selected (Restart required)", Toast.LENGTH_LONG).show()
        }

        return card
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}