package com.example.travel_companion.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.travel_companion.data.entity.TripLocation
import com.example.travel_companion.viewmodel.TripViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class TripMapActivity : AppCompatActivity() {

    private lateinit var viewModel: TripViewModel
    private var mapView: MapView? = null
    private var tripId: Long = -1
    private var locations: List<TripLocation> = emptyList()

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

        // Configura osmdroid
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        Configuration.getInstance().userAgentValue = packageName

        // Crea la mappa
        mapView = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 3.0
            maxZoomLevel = 19.0
        }

        setContentView(mapView)

        viewModel = ViewModelProvider(this)[TripViewModel::class.java]
        observeTripLocations()
    }

    private fun observeTripLocations() {
        viewModel.getLocationsByTrip(tripId).observe(this) { locs ->
            if (locs.isEmpty()) {
                Toast.makeText(this, "No location data available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            locations = locs
            drawRouteOnMap(locs)
        }
    }

    private fun drawRouteOnMap(locations: List<TripLocation>) {
        val map = mapView ?: return

        if (locations.isEmpty()) return

        // Crea la polyline per il percorso
        val polyline = Polyline().apply {
            outlinePaint.color = Color.parseColor("#FF6200EE")
            outlinePaint.strokeWidth = 10f
        }

        // Aggiungi tutti i punti
        val geoPoints = mutableListOf<GeoPoint>()
        locations.forEach { location ->
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            geoPoints.add(geoPoint)
            polyline.addPoint(geoPoint)
        }

        // Aggiungi la polyline alla mappa
        map.overlays.add(polyline)

        // Aggiungi marker per inizio e fine
        val startLocation = locations.first()
        val endLocation = locations.last()

        // Marker di partenza (verde)
        val startMarker = Marker(map).apply {
            position = GeoPoint(startLocation.latitude, startLocation.longitude)
            title = "Start"
            snippet = "Trip starting point"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = resources.getDrawable(android.R.drawable.ic_menu_compass, null)
        }
        map.overlays.add(startMarker)

        // Marker di arrivo (rosso)
        val endMarker = Marker(map).apply {
            position = GeoPoint(endLocation.latitude, endLocation.longitude)
            title = "End"
            snippet = "Trip ending point"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = resources.getDrawable(android.R.drawable.ic_menu_mylocation, null)
        }
        map.overlays.add(endMarker)

        // Aggiungi marker per punti intermedi ogni N punti
        if (locations.size > 4) {
            val step = locations.size / 4
            for (i in step until locations.size - 1 step step) {
                val loc = locations[i]
                val waypoint = Marker(map).apply {
                    position = GeoPoint(loc.latitude, loc.longitude)
                    title = "Waypoint ${i / step}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = resources.getDrawable(android.R.drawable.ic_menu_info_details, null)
                }
                map.overlays.add(waypoint)
            }
        }

        // Centra la mappa sul percorso
        try {
            // Calcola il bounding box
            val minLat = locations.minOf { it.latitude }
            val maxLat = locations.maxOf { it.latitude }
            val minLon = locations.minOf { it.longitude }
            val maxLon = locations.maxOf { it.longitude }

            val center = GeoPoint(
                (minLat + maxLat) / 2,
                (minLon + maxLon) / 2
            )

            // Centra e calcola lo zoom appropriato
            map.controller.setCenter(center)

            // Calcola lo zoom basato sulla distanza
            val latDiff = maxLat - minLat
            val lonDiff = maxLon - minLon
            val maxDiff = maxOf(latDiff, lonDiff)

            val zoom = when {
                maxDiff > 1.0 -> 8.0
                maxDiff > 0.5 -> 10.0
                maxDiff > 0.1 -> 12.0
                maxDiff > 0.05 -> 14.0
                else -> 16.0
            }

            map.controller.setZoom(zoom)

        } catch (e: Exception) {
            // Se c'è un solo punto, centra su quello
            val geoPoint = GeoPoint(startLocation.latitude, startLocation.longitude)
            map.controller.setCenter(geoPoint)
            map.controller.setZoom(15.0)
        }

        // Forza il refresh della mappa
        map.invalidate()

        // Mostra info
        val distance = calculateTotalDistance(locations)
        Toast.makeText(
            this,
            "Route: ${locations.size} points, ${String.format("%.2f", distance)} km",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun calculateTotalDistance(locations: List<TripLocation>): Double {
        if (locations.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until locations.size - 1) {
            val loc1 = locations[i]
            val loc2 = locations[i + 1]
            totalDistance += calculateDistance(
                loc1.latitude, loc1.longitude,
                loc2.latitude, loc2.longitude
            )
        }
        return totalDistance
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDetach()
        mapView = null
    }
}