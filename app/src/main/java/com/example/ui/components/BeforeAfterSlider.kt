package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraGold
import com.example.ui.theme.CameraWhite

@Composable
fun BeforeAfterSlider(
    originalBitmap: Bitmap,
    enhancedBitmap: Bitmap,
    modifier: Modifier = Modifier,
    isForceOriginal: Boolean = false
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }

    val origImageBitmap = remember(originalBitmap) { originalBitmap.asImageBitmap() }
    val enhImageBitmap = remember(enhancedBitmap) { enhancedBitmap.asImageBitmap() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CameraBlack)
            .testTag("before_after_slider_container")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val splitX = if (isForceOriginal) widthPx else (sliderPosition * widthPx).coerceIn(0f, widthPx)

        // Draw Canvas with clipRects for before/after comparison
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newPos = (sliderPosition + dragAmount.x / widthPx).coerceIn(0.05f, 0.95f)
                        sliderPosition = newPos
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        sliderPosition = (tapOffset.x / widthPx).coerceIn(0.05f, 0.95f)
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate aspect-fit dst size for the bitmaps
            val imgAspect = origImageBitmap.width.toFloat() / origImageBitmap.height.toFloat()
            val canvasAspect = canvasWidth / canvasHeight

            val dstWidth: Float
            val dstHeight: Float
            val dstLeft: Float
            val dstTop: Float

            if (canvasAspect > imgAspect) {
                dstHeight = canvasHeight
                dstWidth = canvasHeight * imgAspect
                dstLeft = (canvasWidth - dstWidth) / 2f
                dstTop = 0f
            } else {
                dstWidth = canvasWidth
                dstHeight = canvasWidth / imgAspect
                dstLeft = 0f
                dstTop = (canvasHeight - dstHeight) / 2f
            }

            val dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt())
            val dstSize = IntSize(dstWidth.toInt(), dstHeight.toInt())

            // 1. Right side: Enhanced Photo
            if (!isForceOriginal && splitX < canvasWidth) {
                clipRect(left = splitX, top = 0f, right = canvasWidth, bottom = canvasHeight) {
                    drawImage(
                        image = enhImageBitmap,
                        dstOffset = dstOffset,
                        dstSize = dstSize
                    )
                }
            }

            // 2. Left side: Original Photo
            if (splitX > 0f) {
                clipRect(left = 0f, top = 0f, right = splitX, bottom = canvasHeight) {
                    drawImage(
                        image = origImageBitmap,
                        dstOffset = dstOffset,
                        dstSize = dstSize
                    )
                }
            }

            // 3. Divider Line
            if (!isForceOriginal) {
                drawLine(
                    color = Color.White.copy(alpha = 0.9f),
                    start = Offset(splitX, 0f),
                    end = Offset(splitX, canvasHeight),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        // Handle Badge on the Divider Line
        if (!isForceOriginal) {
            val density = LocalDensity.current
            val handleSizeDp = 44.dp
            val handleSizePx = with(density) { handleSizeDp.toPx() }
            val handleOffsetXPx = splitX - (handleSizePx / 2f)

            Surface(
                modifier = Modifier
                    .offset { IntOffset(handleOffsetXPx.toInt(), (heightPx / 2f - (handleSizePx / 2f)).toInt()) }
                    .size(handleSizeDp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .testTag("slider_handle"),
                color = CameraBlack.copy(alpha = 0.85f),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = "Slide to compare",
                        tint = CameraGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Side Labels
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Surface(
                color = CameraBlack.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isForceOriginal) "ORIGINAL (100%)" else "ORIGINAL",
                    color = CameraWhite,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (!isForceOriginal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    color = CameraGold.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "REALSHOT AI",
                        color = CameraBlack,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
