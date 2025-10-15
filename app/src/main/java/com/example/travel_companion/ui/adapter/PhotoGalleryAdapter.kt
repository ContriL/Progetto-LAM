package com.example.travel_companion.ui.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_companion.data.entity.TripPhoto
import java.io.File

class PhotoGalleryAdapter(
    private val onPhotoClick: (TripPhoto) -> Unit,
    private val onPhotoLongClick: (TripPhoto) -> Unit
) : ListAdapter<TripPhoto, PhotoGalleryAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val context = parent.context
        val density = context.resources.displayMetrics.density
        val size = (context.resources.displayMetrics.widthPixels / 3) - (16 * density).toInt()

        val imageView = ImageView(context).apply {
            layoutParams = RecyclerView.LayoutParams(size, size).apply {
                val margin = (4 * density).toInt()
                setMargins(margin, margin, margin, margin)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        return PhotoViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo, onPhotoClick, onPhotoLongClick)
    }

    class PhotoViewHolder(
        private val imageView: ImageView
    ) : RecyclerView.ViewHolder(imageView) {

        fun bind(
            photo: TripPhoto,
            onPhotoClick: (TripPhoto) -> Unit,
            onPhotoLongClick: (TripPhoto) -> Unit
        ) {
            // Load image from file
            try {
                val file = File(Uri.parse(photo.photoUri).path ?: "")
                if (file.exists()) {
                    // Load scaled bitmap to avoid OutOfMemoryError
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, options)

                    // Calculate sample size
                    val reqSize = 300 // pixels
                    options.inSampleSize = calculateInSampleSize(options, reqSize, reqSize)
                    options.inJustDecodeBounds = false

                    val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Click listeners
            imageView.setOnClickListener {
                onPhotoClick(photo)
            }

            imageView.setOnLongClickListener {
                onPhotoLongClick(photo)
                true
            }
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }

    class PhotoDiffCallback : DiffUtil.ItemCallback<TripPhoto>() {
        override fun areItemsTheSame(oldItem: TripPhoto, newItem: TripPhoto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TripPhoto, newItem: TripPhoto): Boolean {
            return oldItem == newItem
        }
    }
}