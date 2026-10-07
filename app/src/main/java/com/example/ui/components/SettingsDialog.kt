package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.EnhancementStrength
import com.example.data.ImageQuality
import com.example.data.SettingsRepository
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraGold
import com.example.ui.theme.CameraGoldLight
import com.example.ui.theme.CameraSurfaceDark
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.CameraTextMuted
import com.example.ui.theme.CameraTextSecondary
import com.example.ui.theme.CameraWhite

@Composable
fun SettingsDialog(
    settingsRepository: SettingsRepository,
    isFrontCamera: Boolean,
    onToggleCameraLens: () -> Unit,
    onDismiss: () -> Unit
) {
    var aiEnabled by remember { mutableStateOf(settingsRepository.aiEnhancementEnabled) }
    var autoSave by remember { mutableStateOf(settingsRepository.autoSaveToGallery) }
    var saveBackup by remember { mutableStateOf(settingsRepository.saveOriginalBackup) }
    var strength by remember { mutableStateOf(settingsRepository.enhancementStrength) }
    var quality by remember { mutableStateOf(settingsRepository.imageQuality) }
    var openAiModel by remember { mutableStateOf(settingsRepository.openAiModel) }
    var customKey by remember { mutableStateOf(settingsRepository.customOpenAiKey) }
    var showKey by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(28.dp))
                .testTag("settings_dialog"),
            color = CameraSurfaceDark,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = CameraGold.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = CameraGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Settings",
                                color = CameraWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "RealShot AI Camera",
                                color = CameraGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CameraWhite.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    color = CameraWhite.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 1. AI ENHANCEMENT (ON / OFF)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CameraSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AI Enhancement", color = CameraWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Send photo to OpenAI image editing for natural RAW enhancement", color = CameraTextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = aiEnabled,
                                onCheckedChange = {
                                    aiEnabled = it
                                    settingsRepository.aiEnhancementEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CameraBlack,
                                    checkedTrackColor = CameraGold
                                )
                            )
                        }

                        HorizontalDivider(color = CameraWhite.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 10.dp))

                        // 2. AUTO SAVE (ON / OFF)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Save", color = CameraWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Save enhanced photos automatically to Gallery album 'RealShot AI'", color = CameraTextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = autoSave,
                                onCheckedChange = {
                                    autoSave = it
                                    settingsRepository.autoSaveToGallery = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CameraBlack,
                                    checkedTrackColor = CameraGold
                                )
                            )
                        }

                        HorizontalDivider(color = CameraWhite.copy(alpha = 0.06f), modifier = Modifier.padding(vertical = 10.dp))

                        // 3. SAVE ORIGINAL (ON / OFF)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Save Original", color = CameraWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("Preserve unedited original photo as RealShot_Original backup", color = CameraTextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = saveBackup,
                                onCheckedChange = {
                                    saveBackup = it
                                    settingsRepository.saveOriginalBackup = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CameraBlack,
                                    checkedTrackColor = CameraGold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. ENHANCEMENT STRENGTH (Natural, Balanced, Professional)
                Text("ENHANCEMENT STRENGTH", color = CameraGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancementStrength.entries.forEach { s ->
                        val isSelected = (strength == s)
                        Surface(
                            color = if (isSelected) CameraGold else CameraSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) CameraGold else CameraWhite.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    strength = s
                                    settingsRepository.enhancementStrength = s
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = s.label,
                                    color = if (isSelected) CameraBlack else CameraWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. IMAGE QUALITY (High, Maximum)
                Text("IMAGE QUALITY", color = CameraGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageQuality.entries.forEach { q ->
                        val isSelected = (quality == q)
                        Surface(
                            color = if (isSelected) CameraGold else CameraSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) CameraGold else CameraWhite.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    quality = q
                                    settingsRepository.imageQuality = q
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = q.label,
                                    color = if (isSelected) CameraBlack else CameraWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. CAMERA (Rear / Front)
                Text("CAMERA LENS", color = CameraGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Rear", "Front").forEach { lens ->
                        val isSelected = (if (lens == "Front") isFrontCamera else !isFrontCamera)
                        Surface(
                            color = if (isSelected) CameraGold else CameraSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) CameraGold else CameraWhite.copy(alpha = 0.1f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if ((lens == "Front" && !isFrontCamera) || (lens == "Rear" && isFrontCamera)) {
                                        onToggleCameraLens()
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = lens,
                                    color = if (isSelected) CameraBlack else CameraWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. OPENAI API & MODEL
                Text("OPENAI IMAGE MODEL & API KEY", color = CameraGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CameraSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current AI Model:", color = CameraTextSecondary, fontSize = 12.sp)
                            Surface(
                                color = CameraBlack,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CameraWhite.copy(alpha = 0.15f))
                            ) {
                                Text(
                                    text = openAiModel,
                                    color = CameraGoldLight,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("API Key Status:", color = CameraTextSecondary, fontSize = 12.sp)
                            val isConfigured = settingsRepository.isOpenAiKeyConfigured()
                            Surface(
                                color = if (isConfigured) Color(0x334CAF50) else Color(0x33FF9800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isConfigured) "Configured (${settingsRepository.getMaskedApiKey()})" else "Not configured",
                                    color = if (isConfigured) Color(0xFF81C784) else Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Set OPENAI_API_KEY in AI Studio Secrets panel. The key is securely passed server-side and never exposed.",
                            color = CameraTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Optional custom API key override field
                        OutlinedTextField(
                            value = customKey,
                            onValueChange = { customKey = it },
                            label = { Text("OPENAI_API_KEY override (Optional)", fontSize = 11.sp) },
                            singleLine = true,
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = CameraWhite.copy(alpha = 0.6f)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CameraGold,
                                unfocusedBorderColor = CameraWhite.copy(alpha = 0.2f),
                                focusedTextColor = CameraWhite,
                                unfocusedTextColor = CameraWhite,
                                focusedLabelColor = CameraGold,
                                unfocusedLabelColor = CameraTextSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (customKey != settingsRepository.customOpenAiKey) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { settingsRepository.customOpenAiKey = customKey },
                                colors = ButtonDefaults.buttonColors(containerColor = CameraGold),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Save Key", color = CameraBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 8. ABOUT REALSHOT AI CAMERA
                Card(
                    colors = CardDefaults.cardColors(containerColor = CameraSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CameraGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CameraGold, modifier = Modifier.size(18.dp))
                            Text("About RealShot AI Camera", color = CameraWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "RealShot AI Camera is a professional photographic enhancement tool. Photographs are sent exclusively to the configured OpenAI service. The AI preserves the exact same person, face, identity, clothing, and scene, enhancing lighting, exposure, and optical detail to mirror a high-end DSLR camera.",
                            color = CameraTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CameraGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Done", color = CameraBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
