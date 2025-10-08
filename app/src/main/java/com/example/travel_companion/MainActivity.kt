package com.example.travel_companion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var bottomNav: BottomNavigationView? = null
    private var fabStartTrip: FloatingActionButton? = null
    private var fragmentContainer: FrameLayout? = null
    private var currentFragmentTag: String = "HOME"

    // ✅ ID corretti per i menu item (evita Expected resource of type id)
    private val ID_HOME = ViewCompat.generateViewId()
    private val ID_TRIPS = ViewCompat.generateViewId()
    private val ID_STATS = ViewCompat.generateViewId()
    private val ID_PROFILE = ViewCompat.generateViewId()

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val TAG_HOME = "HOME"
        private const val TAG_TRIPS = "TRIPS"
        private const val TAG_STATS = "STATS"
        private const val TAG_PROFILE = "PROFILE"

        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "onCreate started")

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
                bottomNav?.selectedItemId = ID_HOME   // ✅ uso dell'ID corretto
            } else {
                currentFragmentTag = savedInstanceState.getString("CURRENT_FRAGMENT", TAG_HOME)
                Log.d(TAG, "Restored fragment tag: $currentFragmentTag")
            }

            // Richiedi permessi
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

            // ✅ Crea menu con ID validi
            menu.add(0, ID_HOME, 0, "Home").setIcon(android.R.drawable.ic_menu_compass)
            menu.add(0, ID_TRIPS, 0, "Trips").setIcon(android.R.drawable.ic_menu_mapmode)
            menu.add(0, ID_STATS, 0, "Stats").setIcon(android.R.drawable.ic_menu_sort_by_size)
            menu.add(0, ID_PROFILE, 0, "Profile").setIcon(android.R.drawable.ic_menu_myplaces)
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
                    ID_HOME -> {
                        loadFragment(HomeFragment(), TAG_HOME)
                        updateToolbarTitle("Home")
                        true
                    }
                    ID_TRIPS -> {
                        loadFragment(TripsFragment(), TAG_TRIPS)
                        updateToolbarTitle("My Trips")
                        true
                    }
                    ID_STATS -> {
                        loadFragment(StatsFragment(), TAG_STATS)
                        updateToolbarTitle("Statistics")
                        true
                    }
                    ID_PROFILE -> {
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
        try {
            val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults.getOrNull(index) != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isNotEmpty()) {
                Log.w(TAG, "Some permissions denied: $deniedPermissions")
                Toast.makeText(
                    this,
                    "Some permissions were denied. App functionality may be limited.",
                    Toast.LENGTH_LONG
                ).show()
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
            if (height > 0) height else dpToPx(56)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting action bar height", e)
            dpToPx(56)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentFragmentTag != TAG_HOME) {
            loadFragment(HomeFragment(), TAG_HOME)
            bottomNav?.selectedItemId = ID_HOME    // ✅ uso corretto dell'ID
            updateToolbarTitle("Home")
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomNav = null
        fabStartTrip = null
        fragmentContainer = null
    }
}
