package com.example.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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

object OpenAiEnhancer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    const val DEFAULT_MODEL = "dall-e-2"

    const val PROFESSIONAL_ENHANCEMENT_PROMPT = """Edit this exact photograph as a high-end professional photographer would.

The input photograph is the source of truth.

Improve only photographic quality.

Return the SAME photograph, not a newly invented scene.

Preserve the exact:
- person
- identity
- face
- facial structure
- facial proportions
- expression
- skin tone
- skin texture
- hair
- clothing
- objects
- environment
- background
- composition
- perspective
- photographic content

Do not add anything.

Do not remove anything.

Do not replace anything.

Do not invent anything.

Do not regenerate unclear details.

Do not change facial features.

Do not reshape the face.

Do not change the person's identity.

Do not change age.

Do not change body shape.

Do not whiten skin.

Do not apply beauty filters.

Do not smooth skin excessively.

Preserve realistic pores and natural skin texture.

Correct exposure, highlights, shadows, white balance, contrast, color, noise, sharpness and clarity according to the original photograph.

For low light, naturally improve exposure and reduce noise while preserving the original atmosphere.

For harsh light, balance highlights and shadows.

For flat lighting, add subtle professional tonal depth.

Use realistic environmental color correction.

Outdoor photographs should remain naturally colored.

Indoor photographs should have accurate white balance.

Night photographs should remain nighttime photographs.

Only improve background separation when it is naturally supported by the photograph.

Do not create artificial background blur if it would look like a cutout.

Do not create fake details.

Do not create artificial HDR.

Do not oversaturate colors.

Do not oversharpen.

Do not make the image look AI-generated.

The final image must look like the SAME photograph captured with a significantly better professional camera and edited naturally by a human photographer.

REAL.
NATURAL.
PHOTOGRAPHIC.
SUBTLE.
PROFESSIONAL.

The image must remain faithful to the original photograph."""

    suspend fun enhanceWithOpenAi(
        source: Bitmap,
        apiKey: String,
        model: String = DEFAULT_MODEL,
        onProgressUpdate: (String) -> Unit = {}
    ): Bitmap = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("OpenAI API key is not configured. Please add OPENAI_API_KEY in AI Studio Secrets or Settings.")
        }

        onProgressUpdate("Preparing high-resolution photo for OpenAI…")

        // OpenAI Image Edits API requires a PNG image (usually square 1024x1024 or 512x512)
        val squareBitmap = prepareSquarePngBitmap(source, 1024)
        val pngBytes = bitmapToPngBytes(squareBitmap)

        onProgressUpdate("Sending photo to OpenAI Image Editing API…")

        val mediaTypePng = "image/png".toMediaType()
        val imageRequestBody = pngBytes.toRequestBody(mediaTypePng)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "photo.png", imageRequestBody)
            .addFormDataPart("prompt", PROFESSIONAL_ENHANCEMENT_PROMPT)
            .addFormDataPart("model", if (model.isNotBlank()) model else DEFAULT_MODEL)
            .addFormDataPart("n", "1")
            .addFormDataPart("size", "1024x1024")
            .addFormDataPart("response_format", "b64_json")
            .build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/edits")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        onProgressUpdate("Preserving natural detail & applying photographic corrections…")

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}: ${response.message}"
            }
            throw IOException("OpenAI API Error: $errorMsg")
        }

        onProgressUpdate("Finalizing enhanced photograph…")

        val json = JSONObject(responseBody)
        val dataArray = json.optJSONArray("data")
        if (dataArray == null || dataArray.length() == 0) {
            throw IOException("OpenAI returned no image data.")
        }

        val firstItem = dataArray.getJSONObject(0)
        val b64Json = firstItem.optString("b64_json", "")
        if (b64Json.isNotBlank()) {
            val decodedBytes = Base64.decode(b64Json, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (decodedBitmap != null) {
                // If original had a different aspect ratio, adapt to match or return high-res
                return@withContext decodedBitmap
            }
        }

        val url = firstItem.optString("url", "")
        if (url.isNotBlank()) {
            val imageReq = Request.Builder().url(url).build()
            val imgResp = client.newCall(imageReq).execute()
            val stream = imgResp.body?.byteStream()
            if (stream != null) {
                val downloaded = BitmapFactory.decodeStream(stream)
                if (downloaded != null) {
                    return@withContext downloaded
                }
            }
        }

        throw IOException("Failed to decode enhanced image from OpenAI response.")
    }

    private fun prepareSquarePngBitmap(source: Bitmap, targetSize: Int): Bitmap {
        val srcWidth = source.width
        val srcHeight = source.height

        val scaled = Bitmap.createScaledBitmap(source, targetSize, targetSize, true)
        return scaled
    }

    private fun bitmapToPngBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
