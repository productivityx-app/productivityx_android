package com.oussama_chatri.productivityx.features.notes.presentation.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.FileNotFoundException

class PdfImageHandler(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    data class LoadedImage(
        val bitmap: Bitmap,
        val widthPx: Int,
        val heightPx: Int,
        val aspectRatio: Float
    )

    fun loadImage(uriString: String): LoadedImage? {
        return try {
            val uri = Uri.parse(uriString)
            loadFromUri(uri)
        } catch (e: Exception) {
            tryLoadFromLocalPath(uriString)
        }
    }

    private fun loadFromUri(uri: Uri): LoadedImage? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val orientation = getExifOrientation(uri)
            val isRotated = orientation == 90 || orientation == 270
            val (origW, origH) = if (isRotated) options.outHeight to options.outWidth
            else options.outWidth to options.outHeight

            val targetW = 2048f
            val targetH = 2048f
            val scale = minOf(
                if (origW > 0) targetW / origW else 1f,
                if (origH > 0) targetH / origH else 1f
            )

            val sampleSize = if (scale < 1f) {
                (1f / scale).toInt().coerceIn(1, 8)
            } else 1

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val rawBitmap = contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val rotatedBitmap = if (orientation != 0) {
                val matrix = Matrix().apply { postRotate(orientation.toFloat()) }
                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                    .also { if (it != rawBitmap) rawBitmap.recycle() }
            } else rawBitmap

            LoadedImage(
                bitmap = rotatedBitmap,
                widthPx = rotatedBitmap.width,
                heightPx = rotatedBitmap.height,
                aspectRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height.toFloat()
            )
        } catch (e: FileNotFoundException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    private fun tryLoadFromLocalPath(path: String): LoadedImage? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            val targetW = 2048f
            val targetH = 2048f
            val scale = minOf(
                if (options.outWidth > 0) targetW / options.outWidth else 1f,
                if (options.outHeight > 0) targetH / options.outHeight else 1f
            )
            val sampleSize = if (scale < 1f) (1f / scale).toInt().coerceIn(1, 8) else 1

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = BitmapFactory.decodeFile(path, decodeOptions) ?: return null

            val exifOrientation = try {
                val exif = ExifInterface(path)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } catch (_: Exception) { 0 }

            val rotatedBitmap = if (exifOrientation != 0) {
                val matrix = Matrix().apply { postRotate(exifOrientation.toFloat()) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    .also { if (it != bitmap) bitmap.recycle() }
            } else bitmap

            LoadedImage(
                bitmap = rotatedBitmap,
                widthPx = rotatedBitmap.width,
                heightPx = rotatedBitmap.height,
                aspectRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height.toFloat()
            )
        } catch (_: Exception) { null }
    }

    private fun getExifOrientation(uri: Uri): Int {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) { 0 }
    }

    fun recycle(loaded: LoadedImage) {
        loaded.bitmap.recycle()
    }
}
