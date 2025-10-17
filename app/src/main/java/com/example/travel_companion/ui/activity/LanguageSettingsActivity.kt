package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class LanguageSettingsActivity : AppCompatActivity() {

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
            text = "🌐 Language Settings"
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

        // Languages
        val languages = listOf(
            Pair("🇬🇧", "English"),
            Pair("🇮🇹", "Italiano"),
            Pair("🇪🇸", "Español"),
            Pair("🇫🇷", "Français"),
            Pair("🇩🇪", "Deutsch"),
            Pair("🇵🇹", "Português"),
            Pair("🇯🇵", "日本語"),
            Pair("🇨🇳", "中文")
        )

        val currentLang = prefs.getString("app_language", "English") ?: "English"

        languages.forEach { (flag, language) ->
            mainLayout.addView(createLanguageCard(flag, language, language == currentLang))
        }

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createLanguageCard(flag: String, language: String, isSelected: Boolean): CardView {
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        }

        val flagText = TextView(this).apply {
            text = flag
            textSize = 32f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(16)
            }
        }

        val languageText = TextView(this).apply {
            text = language
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

        content.addView(flagText)
        content.addView(languageText)
        content.addView(checkmark)
        card.addView(content)

        card.setOnClickListener {
            prefs.edit().putString("app_language", language).apply()
            Toast.makeText(this, "Language changed to $language (Restart required)", Toast.LENGTH_LONG).show()
            recreate() // Ricrea l'activity per mostrare il cambiamento
        }

        return card
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}