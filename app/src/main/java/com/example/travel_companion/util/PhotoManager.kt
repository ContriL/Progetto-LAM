package com.example.travel_companion.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoManager(private val context: Context) {

    companion object {
        private const val PHOTO_DIRECTORY = "TripPhotos"
        private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun createImageFile(): File {
        val timeStamp = dateFormat.format(Date())
        val imageFileName = "TRIP_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Create directory if not exists
        val photoDir = File(storageDir, PHOTO_DIRECTORY)
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        return File.createTempFile(
            imageFileName,
            ".jpg",
            photoDir
        )
    }

    fun getImageUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun deletePhoto(uri: String) {
        try {
            val file = File(Uri.parse(uri).path ?: return)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPhotoSize(uri: String): Long {
        return try {
            val file = File(Uri.parse(uri).path ?: return 0L)
            file.length()
        } catch (e: Exception) {
            0L
        }
    }

    fun getAllTripPhotos(): List<File> {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoDir = File(storageDir, PHOTO_DIRECTORY)

        return if (photoDir.exists()) {
            photoDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getTotalPhotoSize(): Long {
        return getAllTripPhotos().sumOf { it.length() }
    }
}