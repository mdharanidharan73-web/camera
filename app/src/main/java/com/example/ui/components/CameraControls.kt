package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CameraMode
import com.example.data.FlashMode
import com.example.data.SampleSceneType
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraGold
import com.example.ui.theme.CameraGoldLight
import com.example.ui.theme.CameraWhite

@Composable
fun CameraTopBar(
    flashMode: FlashMode,
    onFlashToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = CameraBlack.copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Flash
            IconButton(
                onClick = onFlashToggle,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("flash_button")
            ) {
                Icon(
                    imageVector = when (flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                        FlashMode.ON -> Icons.Default.FlashOn
                    },
                    contentDescription = "Flash mode: ${flashMode.name}",
                    tint = if (flashMode == FlashMode.OFF) CameraWhite.copy(alpha = 0.7f) else CameraGold
                )
            }

            // CENTER: REALSHOT AI
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REALSHOT AI",
                    color = CameraWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Capture real. Enhance naturally.",
                    color = CameraGold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // RIGHT: Settings
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = CameraWhite
                )
            }
        }
    }
}

@Composable
fun FocusReticle(
    position: Offset,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1.5f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(position) {
        scale.snapTo(1.4f)
        alpha.snapTo(1f)
        scale.animateTo(1f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        alpha.animateTo(0.6f, animationSpec = tween(500, delayMillis = 350))
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .scale(scale.value)
            .border(1.5.dp, CameraGold.copy(alpha = alpha.value), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(CameraGold.copy(alpha = alpha.value), CircleShape)
        )
    }
}

@Composable
fun CameraBottomControls(
    isCapturing: Boolean,
    currentMode: CameraMode,
    onModeSelect: (CameraMode) -> Unit,
    zoomRatio: Float,
    onZoomSelect: (Float) -> Unit,
    onShutterClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    onGalleryClick: () -> Unit,
    onSampleSceneSelect: (SampleSceneType) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick scene shortcut for emulator test
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCENE PRESET:",
                color = CameraWhite.copy(alpha = 0.45f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 6.dp)
            )
            SampleSceneType.entries.forEach { scene ->
                Surface(
                    color = CameraBlack.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clickable { onSampleSceneSelect(scene) }
                ) {
                    Text(
                        text = scene.label,
                        color = CameraWhite.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ABOVE SHUTTER 1: ZOOM SELECTOR (0.5x, 1x, 2x)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CameraBlack.copy(alpha = 0.6f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(0.5f, 1.0f, 2.0f).forEach { zoom ->
                val isSelected = (zoomRatio == zoom)
                Surface(
                    color = if (isSelected) CameraGold else Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onZoomSelect(zoom) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${if (zoom == 0.5f) "0.5" else zoom.toInt()}x",
                            color = if (isSelected) CameraBlack else CameraWhite.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ABOVE SHUTTER 2: CAMERA MODES (PHOTO, PORTRAIT, NIGHT)
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                Text(
                    text = mode.label,
                    color = if (isSelected) CameraGold else CameraWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onModeSelect(mode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MAIN SHUTTER ROW (Gallery, Shutter, Switch Camera)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Gallery thumbnail
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier
                    .size(54.dp)
                    .background(CameraBlack.copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, CameraWhite.copy(alpha = 0.25f), CircleShape)
                    .testTag("gallery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = CameraWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // CENTER: Large circular shutter
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(4.dp, CameraWhite, CircleShape)
                    .padding(5.dp)
                    .background(if (isPressed) CameraGoldLight else CameraWhite, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !isCapturing,
                        onClick = onShutterClick
                    )
                    .testTag("shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) CameraGold else CameraWhite)
                        .border(1.dp, CameraBlack.copy(alpha = 0.2f), CircleShape)
                )
            }

            // RIGHT: Front/rear camera switch
            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .size(54.dp)
                    .background(CameraBlack.copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, CameraWhite.copy(alpha = 0.25f), CircleShape)
                    .testTag("switch_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = CameraWhite,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
