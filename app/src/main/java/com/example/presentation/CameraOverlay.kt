package com.example.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CameraOverlay(
    onCaptureComplete: (imageName: String, labelUri: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Interactive choices for customized celebrations
    val filters = listOf(
        FilterConfig("Red Smoke Flare 🔴", "red_flare", Color(0x7FDB0007)),
        FilterConfig("Emirates Lights 🎆", "emirates_spark", Color(0x5F06143F)),
        FilterConfig("Invincibles Gold Trophy 🥇", "gold_trophy", Color(0x6FD2AE6D)),
        FilterConfig("Standard Stadium Light 🏟️", "stadium_sun", Color.Transparent)
    )

    var selectedFilterIndex by remember { mutableStateOf(0) }
    var captionText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Hardcoded high-resolution premium celebration visuals
    val celebrationVisuals = listOf(
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=600",
        "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?auto=format&fit=crop&q=80&w=600",
        "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&q=80&w=600",
        "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&q=80&w=600",
        "https://images.unsplash.com/photo-1504609773096-104ff2c73ba4?auto=format&fit=crop&q=80&w=600"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // VIEW FINDER CAMERA WORKSPACE CANVAS OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        ) {
            // Live scanning grids
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                    Box(modifier = Modifier.weight(1f).border(0.5.dp, Color.White.copy(alpha = 0.15f)))
                }
            }

            // Apply selected creative camera lens tint overlay (simulating direct flare actions)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(filters[selectedFilterIndex].tintColor)
            )

            // Dynamic Focal Circle bounds
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.Center)
                    .border(1.5.dp, ArsenalGold.copy(alpha = 0.7f), CircleShape)
            )

            // Header Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Camera Viewfinder",
                        tint = ArsenalWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Simulated Flash Indicator",
                    tint = ArsenalGold,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = "CAM RESOLUTION: AUTOMATIC",
                    color = ArsenalGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Camera Capture Panels (Bottom Section)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, ArsenalSlate)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Filter selection slider (Slick overlay selection)
                    Text(
                        text = "SELECT EVENT CAMERA FILTER OVERLAY",
                        color = ArsenalGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEachIndexed { i, config ->
                            val isSelected = i == selectedFilterIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ArsenalRed else ArsenalSlate.copy(alpha = 0.4f))
                                    .border(1.dp, if (isSelected) ArsenalGold else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { selectedFilterIndex = i }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = config.emojiLabel,
                                    fontSize = 11.sp,
                                    color = ArsenalWhite,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Input Celebration Caption text field
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("What are you shouting right now?", color = SoftGray) },
                        placeholder = { Text("e.g. Flare lit! Emirates is rock solid today! 🔥", color = SoftGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ArsenalWhite,
                            unfocusedTextColor = ArsenalWhite,
                            focusedBorderColor = ArsenalGold,
                            unfocusedBorderColor = ArsenalSlate,
                            cursorColor = ArsenalGold
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("caption_field")
                    )

                    // Large Shutter capture triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Main physical shutter bubble button with progress overlay
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(ArsenalWhite)
                                .border(5.dp, ArsenalRed, CircleShape)
                                .clickable {
                                    if (!isSaving) {
                                        isSaving = true
                                        // Auto map image URLs based on selected index to provide beautiful visuals
                                        val pickedImage = celebrationVisuals[selectedFilterIndex % celebrationVisuals.size]
                                        val finalCaption = captionText.ifEmpty { "Champions of England! Today is the parade! COYG!" }
                                        onCaptureComplete(finalCaption, pickedImage)
                                    }
                                }
                                .testTag("shutter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = ArsenalRed, modifier = Modifier.size(36.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Trigger Camera Capture Shutter",
                                    tint = ArsenalMidnight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Click to instantly freeze local celebration & coordinate-pin via Firebase Real-time",
                        fontSize = 10.sp,
                        color = SoftGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

data class FilterConfig(
    val emojiLabel: String,
    val filterId: String,
    val tintColor: Color
)
