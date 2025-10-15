package com.example.travel_companion.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val LOCATION_PERMISSION_CODE = 1001
    const val CAMERA_PERMISSION_CODE = 1002
    const val BACKGROUND_LOCATION_PERMISSION_CODE = 1003
    const val NOTIFICATION_PERMISSION_CODE = 1004
    const val ACTIVITY_RECOGNITION_PERMISSION_CODE = 1005

    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val BACKGROUND_LOCATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyArray()
    }

    private val CAMERA_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    private val NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    private val ACTIVITY_RECOGNITION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
    } else {
        emptyArray()
    }

    // Check if location permission is granted
    fun hasLocationPermission(context: Context): Boolean {
        return LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Check if background location permission is granted
    fun hasBackgroundLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check if camera permission is granted
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check if notification permission is granted
    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Check if activity recognition permission is granted
    fun hasActivityRecognitionPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permissions
    fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            LOCATION_PERMISSION_CODE
        )
    }

    // Request background location permission (must be done separately from foreground)
    fun requestBackgroundLocationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                BACKGROUND_LOCATION_PERMISSIONS,
                BACKGROUND_LOCATION_PERMISSION_CODE
            )
        }
    }

    // Request camera permission
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            CAMERA_PERMISSIONS,
            CAMERA_PERMISSION_CODE
        )
    }

    // Request notification permission
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                NOTIFICATION_PERMISSIONS,
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }

    // Request activity recognition permission
    fun requestActivityRecognitionPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                ACTIVITY_RECOGNITION_PERMISSIONS,
                ACTIVITY_RECOGNITION_PERMISSION_CODE
            )
        }
    }

    // Request all essential permissions at once
    fun requestEssentialPermissions(activity: Activity) {
        val permissions = mutableListOf<String>()

        if (!hasLocationPermission(activity)) {
            permissions.addAll(LOCATION_PERMISSIONS)
        }

        if (!hasCameraPermission(activity)) {
            permissions.addAll(CAMERA_PERMISSIONS)
        }

        if (!hasNotificationPermission(activity)) {
            permissions.addAll(NOTIFICATION_PERMISSIONS)
        }

        if (!hasActivityRecognitionPermission(activity)) {
            permissions.addAll(ACTIVITY_RECOGNITION_PERMISSIONS)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissions.toTypedArray(),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    // Check if we should show rationale for permission
    fun shouldShowLocationRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun shouldShowCameraRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.CAMERA
        )
    }

    fun shouldShowActivityRecognitionRationale(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
    }

    // Handle permission result
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (grantResults.isEmpty()) {
            onDenied()
            return
        }

        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (allGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }
}