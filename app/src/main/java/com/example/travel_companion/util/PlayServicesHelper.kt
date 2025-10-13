package com.example.travel_companion.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object PlayServicesHelper {

    private const val TAG = "PlayServicesHelper"
    private const val PLAY_SERVICES_REQUEST_CODE = 9000

    /**
     * Verifica se Google Play Services è disponibile
     */
    fun isPlayServicesAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)

        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "Google Play Services is available")
                true
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.e(TAG, "Google Play Services is missing")
                false
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.e(TAG, "Google Play Services needs to be updated")
                false
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.e(TAG, "Google Play Services is disabled")
                false
            }
            ConnectionResult.SERVICE_INVALID -> {
                Log.e(TAG, "Google Play Services is invalid")
                false
            }
            else -> {
                Log.e(TAG, "Google Play Services error: $resultCode")
                false
            }
        }
    }

    /**
     * Verifica e mostra dialog per risolvere il problema
     */
    fun checkPlayServices(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(
                    activity,
                    resultCode,
                    PLAY_SERVICES_REQUEST_CODE
                )?.show()
            } else {
                Log.e(TAG, "This device is not supported by Google Play Services")
            }
            return false
        }
        return true
    }

    /**
     * Ottiene messaggio di errore leggibile
     */
    fun getErrorMessage(context: Context): String? {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)

        return if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorString(resultCode)
        } else {
            null
        }
    }
}