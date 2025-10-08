package com.example.travel_companion.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.travel_companion.data.entity.TripType
import com.example.travel_companion.viewmodel.TripViewModel
import java.text.SimpleDateFormat
import java.util.*

class CreateTripDialog : DialogFragment() {

    private val viewModel: TripViewModel by activityViewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var startDate: Date = Date()
    private var endDate: Date? = null
    private var selectedTripType: TripType = TripType.LOCAL

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        // Main layout
        val mainLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(16), dpToPx(24), dpToPx(16))
        }

        // Title
        val title = TextView(context).apply {
            text = "Create New Trip"
            textSize = 20f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }
        mainLayout.addView(title)

        // Destination input
        val destinationLabel = createLabel("Destination *")
        mainLayout.addView(destinationLabel)

        val destinationInput = EditText(context).apply {
            hint = "Enter destination"
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }
        }
        mainLayout.addView(destinationInput)

        // Trip type selector
        val tripTypeLabel = createLabel("Trip Type *")
        mainLayout.addView(tripTypeLabel)

        val tripTypeSpinner = Spinner(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }

            val types = TripType.values().map { it.getDisplayName() }
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, types).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedTripType = TripType.values()[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        mainLayout.addView(tripTypeSpinner)

        // Start date
        val startDateLabel = createLabel("Start Date *")
        mainLayout.addView(startDateLabel)

        val startDateButton = Button(context).apply {
            text = dateFormat.format(startDate)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }

            setOnClickListener {
                showDatePicker { date ->
                    startDate = date
                    text = dateFormat.format(date)
                }
            }
        }
        mainLayout.addView(startDateButton)

        // End date (optional for multi-day trips)
        val endDateLabel = createLabel("End Date (Optional)")
        mainLayout.addView(endDateLabel)

        val endDateButton = Button(context).apply {
            text = "Not set"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(16)
            }

            setOnClickListener {
                showDatePicker { date ->
                    endDate = date
                    text = dateFormat.format(date)
                }
            }
        }
        mainLayout.addView(endDateButton)

        // Description (optional)
        val descriptionLabel = createLabel("Description (Optional)")
        mainLayout.addView(descriptionLabel)

        val descriptionInput = EditText(context).apply {
            hint = "Add notes or description"
            minLines = 3
            maxLines = 5
            gravity = Gravity.TOP or Gravity.START
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        mainLayout.addView(descriptionInput)

        // Create dialog
        return AlertDialog.Builder(context)
            .setView(mainLayout)
            .setPositiveButton("Create") { _, _ ->
                val destination = destinationInput.text.toString().trim()

                if (destination.isEmpty()) {
                    Toast.makeText(context, "Please enter a destination", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val description = descriptionInput.text.toString().trim().ifEmpty { null }

                viewModel.createTrip(
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    tripType = selectedTripType,
                    description = description
                )

                Toast.makeText(context, "Trip created successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun createLabel(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.DKGRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}