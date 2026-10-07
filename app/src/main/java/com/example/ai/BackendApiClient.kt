package com.example.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

object BackendApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    const val DEFAULT_BACKEND_URL = "http://10.0.2.2:3000"

    suspend fun enhanceViaBackend(
        source: Bitmap,
        backendUrl: String,
        onProgressUpdate: (String) -> Unit = {}
    ): Bitmap = withContext(Dispatchers.IO) {
        val baseUrl = if (backendUrl.isNotBlank()) backendUrl.trimEnd('/') else DEFAULT_BACKEND_URL
        val endpoint = "$baseUrl/api/enhance"

        onProgressUpdate("Connecting to secure RealShot AI backend…")

        val stream = ByteArrayOutputStream()
        source.compress(Bitmap.CompressFormat.JPEG, 94, stream)
        val jpegBytes = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "capture.jpg",
                jpegBytes.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        onProgressUpdate("Backend processing with OpenAI image editing…")

        val response = client.newCall(request).execute()
        val code = response.code

        if (!response.isSuccessful) {
            val errString = response.body?.string() ?: "HTTP $code"
            val errMsg = try {
                val json = JSONObject(errString)
                json.optString("error", errString)
            } catch (e: Exception) {
                errString
            }
            throw IOException("Backend error ($code): $errMsg")
        }

        onProgressUpdate("Receiving enhanced photograph…")

        val bytes = response.body?.bytes()
            ?: throw IOException("Empty response from backend service.")

        val enhancedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IOException("Could not decode enhanced image received from backend.")

        return@withContext enhancedBitmap
    }
}
