package com.agrishield.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()
            location ?: fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPlaceNameFromCoordinates(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext "Field (${String.format(Locale.US, "%.2f", latitude)}, ${String.format(Locale.US, "%.2f", longitude)})"

            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            val address = addresses?.firstOrNull()
            if (address != null) {
                val locality = address.locality
                val subLocality = address.subLocality
                val subAdmin = address.subAdminArea
                val adminArea = address.adminArea

                when {
                    !locality.isNullOrBlank() && !subAdmin.isNullOrBlank() && locality != subAdmin -> "$locality, $subAdmin"
                    !locality.isNullOrBlank() -> locality
                    !subLocality.isNullOrBlank() -> subLocality
                    !subAdmin.isNullOrBlank() -> subAdmin
                    !adminArea.isNullOrBlank() -> adminArea
                    else -> "Field (${String.format(Locale.US, "%.2f", latitude)}, ${String.format(Locale.US, "%.2f", longitude)})"
                }
            } else {
                "Field (${String.format(Locale.US, "%.2f", latitude)}, ${String.format(Locale.US, "%.2f", longitude)})"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Field (${String.format(Locale.US, "%.2f", latitude)}, ${String.format(Locale.US, "%.2f", longitude)})"
        }
    }

    suspend fun getCurrentGpsLocationWithPlace(): Pair<Location, String>? {
        val location = getCurrentLocation() ?: return null
        val placeName = getPlaceNameFromCoordinates(location.latitude, location.longitude)
        return Pair(location, placeName)
    }
}
