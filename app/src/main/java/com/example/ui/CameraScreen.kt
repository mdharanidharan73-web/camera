package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.CameraMode
import com.example.data.SampleSceneType
import com.example.ui.components.CameraBottomControls
import com.example.ui.components.CameraPreviewView
import com.example.ui.components.CameraTopBar
import com.example.ui.components.FocusReticle
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraGold
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.CameraTextSecondary
import com.example.ui.theme.CameraWhite
import com.example.viewmodel.CameraUiState
import com.example.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    uiState: CameraUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var focusPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraBlack)
            .testTag("camera_screen")
    ) {
        if (hasCameraPermission) {
            // Live CameraX Edge-to-Edge Preview
            CameraPreviewView(
                isFrontCamera = uiState.isFrontCamera,
                flashMode = uiState.flashMode,
                gridEnabled = uiState.gridEnabled,
                zoomRatio = uiState.zoomRatio,
                imageCaptureHolder = { capture -> viewModel.setImageCapture(capture) },
                onTapFocus = { offset -> focusPosition = offset },
                modifier = Modifier.fillMaxSize()
            )

            // Animated Focus Reticle
            focusPosition?.let { pos ->
                FocusReticle(
                    position = pos,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // TOP BAR: Flash (Left) | REALSHOT AI (Center) | Settings (Right)
            CameraTopBar(
                flashMode = uiState.flashMode,
                onFlashToggle = { viewModel.toggleFlash() },
                onSettingsClick = { viewModel.openSettings() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // BOTTOM CONTROLS: Mode, Zoom, Gallery, Large Circular Shutter, Switch Camera
            CameraBottomControls(
                isCapturing = uiState.isCapturing,
                currentMode = uiState.currentMode,
                onModeSelect = { mode -> viewModel.setCameraMode(mode) },
                zoomRatio = uiState.zoomRatio,
                onZoomSelect = { zoom -> viewModel.setZoomRatio(zoom) },
                onShutterClick = { viewModel.capturePhoto() },
                onSwitchCamera = { viewModel.toggleCameraLens() },
                onGalleryClick = { viewModel.openGallery() },
                onSampleSceneSelect = { sceneType -> viewModel.captureSampleScene(sceneType) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            // Clean Permission Request Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = CameraSurfaceElevated,
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = CameraGold,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "REALSHOT AI CAMERA",
                    color = CameraWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Capture real. Enhance naturally.",
                    color = CameraGold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Camera access is needed to capture photographs for professional photographic enhancement.",
                    color = CameraTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("grant_permission_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CameraGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Grant Camera Access", color = CameraBlack, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { viewModel.captureSampleScene(SampleSceneType.PORTRAIT_LOWLIGHT) },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Or Try Sample Scene", color = CameraWhite)
                }
            }
        }
    }
}
