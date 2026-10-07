package com.example.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraError
import com.example.ui.theme.CameraGold
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.CameraTextSecondary
import com.example.ui.theme.CameraWhite

@Composable
fun EnhancedResultScreen(
    originalBitmap: Bitmap,
    enhancedBitmap: Bitmap?,
    errorMessage: String?,
    isNetworkError: Boolean,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit,
    onUseOriginal: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraBlack)
            .testTag("result_screen")
    ) {
        // Full screen presentation
        if (enhancedBitmap != null) {
            // Smooth before/after comparison slider using exact original and enhanced files
            BeforeAfterSlider(
                originalBitmap = originalBitmap,
                enhancedBitmap = enhancedBitmap,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                bitmap = originalBitmap.asImageBitmap(),
                contentDescription = "Original photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // TOP BAR: Back & "Enhanced Photo"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .align(Alignment.TopCenter),
            color = CameraBlack.copy(alpha = 0.55f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CameraWhite
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (enhancedBitmap != null) "Enhanced Photo" else "Captured Photo",
                    color = CameraWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                if (enhancedBitmap != null) {
                    Surface(
                        color = CameraGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "ORIGINAL ↔ ENHANCED",
                            color = CameraGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM BAR (Success): Save, Share, Retake
        if (enhancedBitmap != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                color = CameraBlack.copy(alpha = 0.85f),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Retake
                    OutlinedButton(
                        onClick = onRetake,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("retake_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CameraWhite),
                        border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    // Share
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("share_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CameraWhite),
                        border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    // Save
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(50.dp)
                            .testTag("save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CameraGold),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = CameraBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", color = CameraBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ERROR STATE OVERLAY
        if (enhancedBitmap == null && errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                color = CameraBlack.copy(alpha = 0.94f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CameraError.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isNetworkError) Icons.Default.WifiOff else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (isNetworkError) CameraGold else CameraError,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isNetworkError) "Internet connection required for AI enhancement." else "Enhancement failed",
                        color = CameraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = errorMessage,
                        color = CameraTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onUseOriginal,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("use_original_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CameraWhite),
                            border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Use Original", fontSize = 13.sp)
                        }

                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("retry_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CameraGold),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = CameraBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry", color = CameraBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
