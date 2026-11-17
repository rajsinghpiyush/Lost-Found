package com.android_ca3.lostandfoundv2.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// 1b. IMAGE HELPERS (unchanged logic)
// ============================================================

@Composable
fun rememberDecodedImage(base64: String?): Bitmap? {
    return remember(base64) {
        if (base64.isNullOrBlank()) null
        else try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) { null }
    }
}

sealed interface ImageEncodeResult {
    data class Success(val base64: String) : ImageEncodeResult
    data class Failure(val message: String) : ImageEncodeResult
}

suspend fun encodeImageToBase64(context: Context, uri: Uri): ImageEncodeResult =
    withContext(Dispatchers.IO) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val boundsOk = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
                true
            } ?: false
            if (!boundsOk) {
                return@withContext ImageEncodeResult.Failure("Couldn't read the selected file. Try a different photo.")
            }

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 900) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }

            if (bitmap == null) {
                val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                return@withContext if (raw != null && raw.size <= 350_000) {
                    ImageEncodeResult.Success(Base64.encodeToString(raw, Base64.NO_WRAP))
                } else {
                    ImageEncodeResult.Failure("Unsupported image format or file too large. Try a JPG/PNG photo.")
                }
            }

            val scaled = if (bitmap.width > 900 || bitmap.height > 900) {
                val scale = 900f / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else bitmap

            var quality = 70
            var bytes: ByteArray
            do {
                val bos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, bos)
                bytes = bos.toByteArray()
                quality -= 15
            } while (bytes.size > 300_000 && quality > 20)

            if (bytes.size > 400_000) {
                return@withContext ImageEncodeResult.Failure("Photo is too large to store. Try a smaller photo.")
            }

            ImageEncodeResult.Success(Base64.encodeToString(bytes, Base64.NO_WRAP))
        } catch (e: Exception) {
            ImageEncodeResult.Failure("Photo error: ${e.message ?: "unknown"}")
        }
    }

// ============================================================
