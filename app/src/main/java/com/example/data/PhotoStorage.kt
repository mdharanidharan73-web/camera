package com.example.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoStorage(private val context: Context) {

    suspend fun saveTempImage(bitmap: Bitmap, prefix: String = "realshot_orig"): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "captures").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(cacheDir, "${prefix}_$timeStamp.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        file
    }

    suspend fun saveToGallery(bitmap: Bitmap, isEnhanced: Boolean = true): Uri? = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val displayName = if (isEnhanced) "RealShot_$timeStamp.jpg" else "RealShot_Original_$timeStamp.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RealShot AI")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 96, stream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                return@withContext uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
            }
        }

        // Fallback to internal files if MediaStore failed
        val picturesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "RealShot AI").apply { mkdirs() }
        val fallbackFile = File(picturesDir, displayName)
        FileOutputStream(fallbackFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
        }
        Uri.fromFile(fallbackFile)
    }

    fun loadBitmapFromFile(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a realistic high-definition camera test scene for testing on
     * emulator devices or when a physical camera feed is unavailable.
     */
    fun createSampleScene(type: SampleSceneType): Bitmap {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            SampleSceneType.PORTRAIT_LOWLIGHT -> {
                // Dim twilight backdrop
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(Color.rgb(18, 22, 34), Color.rgb(38, 28, 38), Color.rgb(15, 12, 18)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Warm ambient light source on the left
                paint.shader = RadialGradient(
                    200f, 400f, 600f,
                    Color.argb(90, 240, 160, 80),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                canvas.drawCircle(200f, 400f, 600f, paint)

                // Subject shoulders & face contour (natural portrait silhouette)
                paint.shader = null
                paint.color = Color.rgb(45, 38, 42)
                canvas.drawOval(width / 2f - 220f, 850f, width / 2f + 220f, height.toFloat() + 200f, paint) // body

                // Neck
                paint.color = Color.rgb(160, 115, 95)
                canvas.drawRect(width / 2f - 75f, 650f, width / 2f + 75f, 900f, paint)

                // Face oval with natural skin tone
                paint.shader = RadialGradient(
                    width / 2f - 30f, 500f, 220f,
                    Color.rgb(205, 150, 125), // lit side
                    Color.rgb(140, 95, 75),   // shadow side
                    Shader.TileMode.CLAMP
                )
                canvas.drawOval(width / 2f - 160f, 320f, width / 2f + 160f, 740f, paint)

                // Hair volume
                paint.shader = null
                paint.color = Color.rgb(30, 22, 18)
                canvas.drawOval(width / 2f - 180f, 260f, width / 2f + 180f, 520f, paint)

                // Features (eyes, nose bridge, lips in natural proportions)
                paint.color = Color.rgb(65, 45, 38)
                canvas.drawOval(width / 2f - 85f, 480f, width / 2f - 35f, 510f, paint) // Left eye
                canvas.drawOval(width / 2f + 35f, 480f, width / 2f + 85f, 510f, paint) // Right eye
                canvas.drawRoundRect(width / 2f - 15f, 520f, width / 2f + 15f, 590f, 8f, 8f, paint) // Nose
                paint.color = Color.rgb(155, 80, 80)
                canvas.drawRoundRect(width / 2f - 45f, 620f, width / 2f + 45f, 650f, 12f, 12f, paint) // Lips
            }

            SampleSceneType.GOLDEN_HOUR_LANDSCAPE -> {
                // Sky with golden horizon
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height * 0.65f,
                    intArrayOf(Color.rgb(40, 80, 140), Color.rgb(230, 140, 70), Color.rgb(255, 200, 120)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height * 0.65f, paint)

                // Golden sun disc
                paint.shader = null
                paint.color = Color.rgb(255, 245, 200)
                canvas.drawCircle(width * 0.7f, height * 0.45f, 90f, paint)

                // Mountain ridges in foreground
                paint.color = Color.rgb(75, 45, 35)
                val mountainPath = android.graphics.Path().apply {
                    moveTo(0f, height * 0.55f)
                    lineTo(width * 0.35f, height * 0.40f)
                    lineTo(width * 0.65f, height * 0.52f)
                    lineTo(width.toFloat(), height * 0.38f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(mountainPath, paint)

                // Foreground meadow & trees
                paint.color = Color.rgb(40, 55, 25)
                canvas.drawRect(0f, height * 0.68f, width.toFloat(), height.toFloat(), paint)
            }

            SampleSceneType.INDOOR_STUDIO -> {
                // Neutral studio backdrop
                paint.shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(Color.rgb(70, 75, 85), Color.rgb(35, 40, 50)),
                    null, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Architectural interior element (window frame / lamp)
                paint.shader = null
                paint.color = Color.rgb(240, 220, 180)
                paint.alpha = 180
                canvas.drawRect(width * 0.65f, height * 0.15f, width * 0.90f, height * 0.60f, paint)

                // Architectural table / still life
                paint.alpha = 255
                paint.color = Color.rgb(90, 60, 45)
                canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), paint)
            }
        }

        return bitmap
    }

    fun exportPhotosCsv(photos: List<PhotoRecord>): File {
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val csvFile = File(cacheDir, "realshot_captured_photos.csv")
        csvFile.printWriter().use { out ->
            out.println("ID,Timestamp,Date,Mode,Engine,OriginalFile,EnhancedFile,Status")
            photos.forEach { p ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(p.timestamp))
                out.println("${p.id},${p.timestamp},$dateStr,${p.cameraMode.label},${p.engineUsed.name},${p.originalPath},${p.enhancedPath ?: "N/A"},${if (p.enhancedPath != null) "Enhanced" else "Original"}")
            }
        }
        return csvFile
    }
}

enum class SampleSceneType(val label: String) {
    PORTRAIT_LOWLIGHT("Low-Light Portrait"),
    GOLDEN_HOUR_LANDSCAPE("Golden Hour Landscape"),
    INDOOR_STUDIO("Indoor Studio")
}
