package com.example.travel_companion

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.travel_companion.data.entity.TripType
import com.example.travel_companion.ui.activity.TripTypeStatsActivity
import com.example.travel_companion.viewmodel.TripViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class StatsFragment : Fragment() {

    private val viewModel: TripViewModel by activityViewModels()
    private val typeCountCards = mutableMapOf<TripType, TextView>()

    private var pieChart: PieChart? = null
    private var barChart: BarChart? = null
    private var lineChart: LineChart? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = createLayout()
        observeData()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pieChart = null
        barChart = null
        lineChart = null
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

        // Title
        val title = TextView(context).apply {
            text = "Travel Statistics"
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

        // Overview section
        val overviewTitle = TextView(context).apply {
            text = "Overview"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(overviewTitle)

        val statsGrid = createStatsGrid()
        mainLayout.addView(statsGrid)

        // Pie Chart - Trip Types Distribution
        val pieChartTitle = TextView(context).apply {
            text = "Trip Types Distribution"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(pieChartTitle)

        val pieChartCard = createPieChartCard()
        mainLayout.addView(pieChartCard)

        // Bar Chart - Distance by Trip Type
        val barChartTitle = TextView(context).apply {
            text = "Distance by Trip Type"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(barChartTitle)

        val barChartCard = createBarChartCard()
        mainLayout.addView(barChartCard)

        // Line Chart - Trips Over Time (placeholder)
        val lineChartTitle = TextView(context).apply {
            text = "Trips Timeline"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(lineChartTitle)

        val lineChartCard = createLineChartCard()
        mainLayout.addView(lineChartCard)

        // Trip Types section
        val typesTitle = TextView(context).apply {
            text = "Trip Types Summary"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(12)
            }
        }
        mainLayout.addView(typesTitle)

        val tripTypesContainer = createTripTypesContainer()
        mainLayout.addView(tripTypesContainer)

        // Button to view detailed stats
        val btnDetailedStats = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(16)
                bottomMargin = dpToPx(16)
            }
            text = "View Detailed Statistics"
            setOnClickListener {
                val intent = Intent(requireContext(), TripTypeStatsActivity::class.java)
                startActivity(intent)
            }
        }
        mainLayout.addView(btnDetailedStats)

        scrollView.addView(mainLayout)
        return scrollView
    }

    private fun createStatsGrid(): LinearLayout {
        val context = requireContext()

        val grid = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // First row
        val row1 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        row1.addView(createStatCard("0", "Total Trips", "🗺️"))
        row1.addView(createStatCard("0 km", "Total Distance", "📍"))

        // Second row
        val row2 = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        }

        row2.addView(createStatCard("0", "Photos Taken", "📷"))
        row2.addView(createStatCard("0", "Countries Visited", "🌍"))

        grid.addView(row1)
        grid.addView(row2)

        return grid
    }

    private fun createStatCard(value: String, label: String, emoji: String): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        val emojiText = TextView(context).apply {
            text = emoji
            textSize = 32f
            gravity = Gravity.CENTER
        }

        val valueText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            text = value
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF6200EE"))
        }

        val labelText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
            text = label
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
        }

        content.addView(emojiText)
        content.addView(valueText)
        content.addView(labelText)
        card.addView(content)

        return card
    }

    private fun createPieChartCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(300)
            ).apply {
                bottomMargin = dpToPx(16)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        pieChart = PieChart(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            // Configurazione grafico
            description.isEnabled = false
            setDrawHoleEnabled(true)
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            centerText = "Trip Types"
            setCenterTextSize(16f)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true

            // Legenda
            legend.isEnabled = true
            legend.textSize = 12f
        }

        card.addView(pieChart)
        return card
    }

    private fun createBarChartCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(300)
            ).apply {
                bottomMargin = dpToPx(16)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        barChart = BarChart(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            // Configurazione grafico
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)

            // Asse X
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.labelRotationAngle = -45f

            // Asse Y sinistro
            axisLeft.setDrawGridLines(true)
            axisLeft.granularity = 1f
            axisLeft.axisMinimum = 0f

            // Asse Y destro
            axisRight.isEnabled = false

            // Legenda
            legend.isEnabled = false

            // Animazione
            animateY(1000)
        }

        card.addView(barChart)
        return card
    }

    private fun createLineChartCard(): CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(250)
            ).apply {
                bottomMargin = dpToPx(16)
            }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(4).toFloat()
        }

        lineChart = LineChart(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            // Configurazione grafico
            description.isEnabled = false
            setDrawGridBackground(false)

            // Asse X
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f

            // Asse Y
            axisLeft.setDrawGridLines(true)
            axisLeft.granularity = 1f
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false

            // Legenda
            legend.isEnabled = true

            // Animazione
            animateX(1000)
        }

        card.addView(lineChart)
        return card
    }

    private fun createTripTypesContainer(): LinearLayout {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Create mini card for each mandatory type
        val mandatoryTypes = listOf(TripType.LOCAL, TripType.DAY_TRIP, TripType.MULTI_DAY)

        mandatoryTypes.forEach { type ->
            container.addView(createTripTypeMiniCard(type))
        }

        return container
    }

    private fun createTripTypeMiniCard(type: TripType): CardView {
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
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }

        val icon = TextView(context).apply {
            text = type.getIcon()
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(12)
            }
        }

        val nameText = TextView(context).apply {
            text = type.getDisplayName()
            textSize = 14f
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val countText = TextView(context).apply {
            text = "0"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#FF6200EE"))
        }

        typeCountCards[type] = countText

        content.addView(icon)
        content.addView(nameText)
        content.addView(countText)
        card.addView(content)

        return card
    }

    private fun observeData() {
        // Observe counts for each trip type
        val tripTypeCounts = mutableMapOf<TripType, Int>()
        val tripTypeDistances = mutableMapOf<TripType, Double>()

        TripType.values().forEach { type ->
            // Count
            viewModel.getTripCountByType(type).observe(viewLifecycleOwner) { count ->
                tripTypeCounts[type] = count ?: 0
                typeCountCards[type]?.text = count?.toString() ?: "0"

                // Update charts when data changes
                updatePieChart(tripTypeCounts)
            }

            // Distance
            viewModel.getTotalDistanceByType(type).observe(viewLifecycleOwner) { distance ->
                tripTypeDistances[type] = distance ?: 0.0
                updateBarChart(tripTypeDistances)
            }
        }

        // Observe total trips for line chart
        viewModel.allTrips.observe(viewLifecycleOwner) { trips ->
            updateLineChart(trips.size)
        }
    }

    private fun updatePieChart(counts: Map<TripType, Int>) {
        val chart = pieChart ?: return

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        counts.forEach { (type, count) ->
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), type.getDisplayName()))
                colors.add(getColorForTripType(type))
            }
        }

        if (entries.isEmpty()) {
            chart.clear()
            chart.centerText = "No Data"
            chart.invalidate()
            return
        }

        val dataSet = PieDataSet(entries, "Trip Types").apply {
            this.colors = colors
            sliceSpace = 3f
            selectionShift = 5f
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
        }

        chart.data = data
        chart.invalidate()
    }

    private fun updateBarChart(distances: Map<TripType, Double>) {
        val chart = barChart ?: return

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        distances.entries.forEachIndexed { index, entry ->
            if (entry.value > 0) {
                entries.add(BarEntry(index.toFloat(), entry.value.toFloat()))
                labels.add(entry.key.name.take(3)) // Abbrevia i nomi
            }
        }

        if (entries.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Distance (km)").apply {
            color = Color.parseColor("#FF6200EE")
            valueTextSize = 10f
            valueTextColor = Color.BLACK
        }

        val data = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = labels.size
        chart.invalidate()
    }

    private fun updateLineChart(totalTrips: Int) {
        val chart = lineChart ?: return

        // Crea dati di esempio per la timeline (ultimi 6 mesi)
        val entries = mutableListOf<Entry>()
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")

        // Distribuisci i viaggi nei mesi (simulato)
        val tripsPerMonth = if (totalTrips > 0) {
            List(6) { (Math.random() * (totalTrips / 3)).toFloat() }
        } else {
            List(6) { 0f }
        }

        tripsPerMonth.forEachIndexed { index, value ->
            entries.add(Entry(index.toFloat(), value))
        }

        val dataSet = LineDataSet(entries, "Trips per Month").apply {
            color = Color.parseColor("#FF6200EE")
            lineWidth = 2f
            setCircleColor(Color.parseColor("#FF6200EE"))
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = Color.parseColor("#AA6200EE")
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val data = LineData(dataSet)
        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
        chart.xAxis.labelCount = 6
        chart.invalidate()
    }

    private fun getColorForTripType(type: TripType): Int {
        return when (type) {
            TripType.LOCAL -> Color.parseColor("#FF4CAF50")
            TripType.DAY_TRIP -> Color.parseColor("#FF2196F3")
            TripType.MULTI_DAY -> Color.parseColor("#FFFF9800")
            TripType.WEEKEND -> Color.parseColor("#FF9C27B0")
            TripType.BUSINESS -> Color.parseColor("#FF607D8B")
            TripType.ADVENTURE -> Color.parseColor("#FFF44336")
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}