package com.example.travel_companion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.example.travel_companion.service.ActivityRecognitionManager
import com.example.travel_companion.util.NotificationHelper
import com.example.travel_companion.util.PermissionHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var bottomNav: BottomNavigationView? = null
    private var fabStartTrip: FloatingActionButton? = null
    private var fragmentContainer: FrameLayout? = null
    private var currentFragmentTag: String = "HOME"

    companion object {
        private const val TAG = "MainActivity"
        private const val TAG_HOME = "HOME"
        private const val TAG_TRIPS = "TRIPS"
        private const val TAG_STATS = "STATS"
        private const val TAG_PROFILE = "PROFILE"

        // Menu item IDs
        private const val MENU_HOME = 1
        private const val MENU_TRIPS = 2
        private const val MENU_STATS = 3
        private const val MENU_PROFILE = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "onCreate started")

            // IMPORTANTE: Inizializza i canali di notifica prima di tutto
            NotificationHelper.createNotificationChannels(this)

            // Crea il layout programmaticamente
            val rootLayout = createLayout()
            setContentView(rootLayout)

            Log.d(TAG, "Layout created successfully")

            // Setup Bottom Navigation
            setupBottomNavigation()

            // Setup FAB
            setupFab()

            // Carica il fragment iniziale o ripristina lo stato
            if (savedInstanceState == null) {
                Log.d(TAG, "Loading initial fragment")
                loadFragment(HomeFragment(), TAG_HOME)
                bottomNav?.post {
                    bottomNav?.selectedItemId = MENU_HOME  // CORRETTO
                }
            } else {
                currentFragmentTag = savedInstanceState.getString("CURRENT_FRAGMENT", TAG_HOME)
                Log.d(TAG, "Restored fragment tag: $currentFragmentTag")
            }

            // Richiedi permessi essenziali
            checkAndRequestPermissions()

            Log.d(TAG, "onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CURRENT_FRAGMENT", currentFragmentTag)
    }

    private fun createLayout(): CoordinatorLayout {
        Log.d(TAG, "Creating layout")
        val context = this

        // Root layout
        val root = CoordinatorLayout(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Toolbar
        val toolbar = Toolbar(context).apply {
            val toolbarHeight = getActionBarHeight()
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                toolbarHeight
            )
            title = "Travel Companion"
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            elevation = dpToPx(4).toFloat()
        }

        try {
            setSupportActionBar(toolbar)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error setting support action bar", e)
            // Il tema probabilmente ha già un ActionBar, continuiamo senza
        }

        // Fragment Container
        fragmentContainer = FrameLayout(context).apply {
            id = ViewCompat.generateViewId()
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                CoordinatorLayout.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = getActionBarHeight()
                bottomMargin = dpToPx(56)
            }
        }

        // Bottom Navigation
        bottomNav = BottomNavigationView(context).apply {
            id = ViewCompat.generateViewId()
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.MATCH_PARENT,
                dpToPx(56)
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }

            // Crea menu programmaticamente
            menu.add(0, MENU_HOME, 0, "Home").setIcon(android.R.drawable.ic_menu_compass)
            menu.add(0, MENU_TRIPS, 0, "Trips").setIcon(android.R.drawable.ic_menu_mapmode)
            menu.add(0, MENU_STATS, 0, "Stats").setIcon(android.R.drawable.ic_menu_sort_by_size)
            menu.add(0, MENU_PROFILE, 0, "Profile").setIcon(android.R.drawable.ic_menu_myplaces)
        }

        // FAB
        fabStartTrip = FloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                setMargins(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(72))
            }
            setImageResource(android.R.drawable.ic_input_add)
            contentDescription = "Start new trip"
        }

        // Aggiungi le view al layout
        root.addView(toolbar)
        fragmentContainer?.let { root.addView(it) }
        bottomNav?.let { root.addView(it) }
        fabStartTrip?.let { root.addView(it) }

        return root
    }

    private fun setupBottomNavigation() {
        bottomNav?.setOnItemSelectedListener { item ->
            try {
                when (item.itemId) {
                    MENU_HOME -> {
                        loadFragment(HomeFragment(), TAG_HOME)
                        updateToolbarTitle("Home")
                        true
                    }
                    MENU_TRIPS -> {
                        loadFragment(TripsFragment(), TAG_TRIPS)
                        updateToolbarTitle("My Trips")
                        true
                    }
                    MENU_STATS -> {
                        loadFragment(StatsFragment(), TAG_STATS)
                        updateToolbarTitle("Statistics")
                        true
                    }
                    MENU_PROFILE -> {
                        loadFragment(ProfileFragment(), TAG_PROFILE)
                        updateToolbarTitle("Profile")
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error switching fragment", e)
                Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun setupFab() {
        fabStartTrip?.setOnClickListener {
            // Apri il dialog per creare un nuovo viaggio
            val dialog = com.example.travel_companion.ui.dialog.CreateTripDialog()
            dialog.show(supportFragmentManager, "CreateTripDialog")
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        try {
            Log.d(TAG, "Loading fragment: $tag")

            val containerId = fragmentContainer?.id ?: return

            supportFragmentManager.beginTransaction()
                .replace(containerId, fragment, tag)
                .commitAllowingStateLoss()

            currentFragmentTag = tag

            Log.d(TAG, "Fragment loaded successfully: $tag")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading fragment: $tag", e)
            Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun checkAndRequestPermissions() {
        if (!PermissionHelper.hasLocationPermission(this) ||
            !PermissionHelper.hasCameraPermission(this) ||
            !PermissionHelper.hasNotificationPermission(this) ||
            !PermissionHelper.hasActivityRecognitionPermission(this)) {

            if (PermissionHelper.shouldShowLocationRationale(this)) {
                showPermissionRationale()
            } else {
                PermissionHelper.requestEssentialPermissions(this)
            }
        } else {
            // Tutti i permessi essenziali sono concessi
            initializeServicesWithPermissions()
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Travel Companion needs the following permissions:\n\n" +
                    "• Location: Track your trips\n" +
                    "• Camera: Capture travel memories\n" +
                    "• Notifications: Trip alerts and reminders\n" +
                    "• Activity Recognition: Auto-detect when you're traveling\n\n" +
                    "Background location is needed to track your journey even when the app is closed.")
            .setPositiveButton("Grant") { _, _ ->
                PermissionHelper.requestEssentialPermissions(this)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Some features will be limited", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun requestBackgroundLocationLater() {
        // Request background location after a delay to improve UX
        // This follows Android best practices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AlertDialog.Builder(this)
                .setTitle("Background Location")
                .setMessage("To track your trips continuously, please allow background location access by selecting 'Allow all the time' in the next screen.")
                .setPositiveButton("Continue") { _, _ ->
                    PermissionHelper.requestBackgroundLocationPermission(this)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun requestActivityRecognitionLater() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AlertDialog.Builder(this)
                .setTitle("Activity Recognition")
                .setMessage("Allow the app to recognize your activities (walking, driving, etc.) to suggest when to start tracking trips automatically.")
                .setPositiveButton("Allow") { _, _ ->
                    PermissionHelper.requestActivityRecognitionPermission(this)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun initializeServicesWithPermissions() {
        // Avvia Activity Recognition se il permesso è concesso
        if (PermissionHelper.hasActivityRecognitionPermission(this)) {
            ActivityRecognitionManager.startActivityRecognition(this)
            Log.d(TAG, "Activity Recognition initialized")
        }

        // Richiedi background location se non ancora concesso
        if (!PermissionHelper.hasBackgroundLocationPermission(this)) {
            requestBackgroundLocationLater()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionHelper.LOCATION_PERMISSION_CODE -> {
                PermissionHelper.handlePermissionResult(
                    requestCode, permissions, grantResults,
                    onGranted = {
                        Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()

                        // Inizializza servizi dopo aver ottenuto i permessi
                        initializeServicesWithPermissions()
                    },
                    onDenied = {
                        Toast.makeText(
                            this,
                            "Some permissions denied. Trip tracking may be limited.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }

            PermissionHelper.BACKGROUND_LOCATION_PERMISSION_CODE -> {
                if (PermissionHelper.hasBackgroundLocationPermission(this)) {
                    Toast.makeText(this, "Background tracking enabled!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Background tracking limited. App must be open to track trips.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            PermissionHelper.ACTIVITY_RECOGNITION_PERMISSION_CODE -> {
                if (PermissionHelper.hasActivityRecognitionPermission(this)) {
                    Toast.makeText(this, "Activity recognition enabled!", Toast.LENGTH_SHORT).show()
                    // Avvia Activity Recognition
                    ActivityRecognitionManager.startActivityRecognition(this)
                } else {
                    Toast.makeText(
                        this,
                        "Activity recognition denied. Automatic trip detection disabled.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            PermissionHelper.NOTIFICATION_PERMISSION_CODE -> {
                if (PermissionHelper.hasNotificationPermission(this)) {
                    Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Notification permission denied. You won't receive trip alerts.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Utility functions
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun getActionBarHeight(): Int {
        return try {
            val styledAttributes = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            val height = styledAttributes.getDimension(0, 0f).toInt()
            styledAttributes.recycle()
            if (height > 0) height else dpToPx(56) // Fallback
        } catch (e: Exception) {
            Log.e(TAG, "Error getting action bar height", e)
            dpToPx(56) // Default height
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Gestione back button: torna alla home se non ci sei già
        if (currentFragmentTag != TAG_HOME) {
            loadFragment(HomeFragment(), TAG_HOME)
            bottomNav?.selectedItemId = MENU_HOME  // CORRETTO
            updateToolbarTitle("Home")
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Ferma Activity Recognition quando l'app viene chiusa
        ActivityRecognitionManager.stopActivityRecognition(this)

        bottomNav = null
        fabStartTrip = null
        fragmentContainer = null
    }
}