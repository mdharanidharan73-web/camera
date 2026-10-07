package com.example

import android.graphics.Bitmap
import com.example.ai.DslrPhotoEngine
import com.example.ai.OpenAiEnhancer
import com.example.data.CameraMode
import com.example.data.EnhancementStrength
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DslrPhotoEngineTest {

    @Test
    fun enhancePhoto_preservesDimensionsAndReturnsAnalysisReport() = runBlocking {
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val (enhanced, report) = DslrPhotoEngine.enhancePhoto(
            source = testBitmap,
            strength = EnhancementStrength.BALANCED,
            mode = CameraMode.PHOTO
        )

        assertNotNull(enhanced)
        assertEquals(100, enhanced.width)
        assertEquals(100, enhanced.height)
        assertNotNull(report.exposureAdjustment)
        assertNotNull(report.shadowRecovery)
        assertNotNull(report.skinTexturePreservation)
    }

    @Test
    fun openAiInstruction_containsStrictPreservationMandates() {
        val prompt = OpenAiEnhancer.PROFESSIONAL_ENHANCEMENT_PROMPT
        assertTrue(prompt.contains("The input photograph is the source of truth"))
        assertTrue(prompt.contains("Return the SAME photograph"))
        assertTrue(prompt.contains("Do not add anything"))
        assertTrue(prompt.contains("Do not remove anything"))
        assertTrue(prompt.contains("Preserve realistic pores and natural skin texture"))
    }
}
