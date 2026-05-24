package com.example.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.CelebrationPostEntity
import com.example.data.entity.MusicTrackEntity
import com.example.data.entity.ParadePinEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: ParadeViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val pins by viewModel.cachedPins.collectAsState()
    val posts by viewModel.cachedPosts.collectAsState()
    val tracks by viewModel.cachedTracks.collectAsState()
    val mapUiState by viewModel.mapUiState.collectAsState()
    
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Interactive camera views overlay triggered
    var showCameraView by remember { mutableStateOf(false) }
    var cameFromPostFeed by remember { mutableStateOf(false) }

    // Clicked Map location temporary creation variables
    var pendingLatitude by remember { mutableStateOf(0.0) }
    var pendingLongitude by remember { mutableStateOf(0.0) }
    var showCreatePinDialog by remember { mutableStateOf(false) }

    // Instant toast announcements
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect background firebase sync events
    LaunchedEffect(Unit) {
        viewModel.lastSimulatedPinEvent.collectLatest { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (currentUser == null) {
        // Enforce authentic personalized Google login onboarding first
        GoogleSignInView(
            onGoogleSignIn = { email, name ->
                viewModel.handleSimulateGoogleSignIn(email, name)
            },
            onGuestLogin = {
                viewModel.handleGuestAccess()
            }
        )
    } else {
        val user = currentUser!!

        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // High Density Title Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ArsenalRed)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Brand Shield Shield Badge: rounded-full white/20 border-white/30 italic "AFC"
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AFC",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "TROPHY PARADE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "GLOBAL LIVE FEED",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // High Density Mono live ticker card: ● 142.5K LIVE
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(0.7.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    val greenBlink = remember { Animatable(1f) }
                                    LaunchedEffect(Unit) {
                                        greenBlink.animateTo(
                                            targetValue = 0.3f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1100, easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse
                                            )
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4ADE80).copy(alpha = greenBlink.value))
                                    )
                                    Text(
                                        text = "142.5K LIVE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Sim sync trigger button
                            IconButton(
                                onClick = {
                                    viewModel.simulateIncomingCelebration()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Simulate Live Sync Event",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Audio bar + Standard material bottom tabs
                Column(
                    modifier = Modifier.background(ArsenalMidnight)
                ) {
                    // Floating vinyl music track player to hear celebration shouts
                    currentTrack?.let { track ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                ArsenalMidnightLight,
                                                ArsenalMidnight
                                            )
                                        )
                                    )
                                    .border(BorderStroke(0.5.dp, ArsenalSlate))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Animated Spinning Vinyl records representing stadium hymns
                                    val rotation = remember { Animatable(0f) }
                                    LaunchedEffect(isPlaying) {
                                        if (isPlaying) {
                                            rotation.animateTo(
                                                targetValue = rotation.value + 360f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(4000, easing = LinearEasing),
                                                    repeatMode = RepeatMode.Restart
                                                )
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .rotate(rotation.value)
                                            .border(1.5.dp, ArsenalGold, CircleShape)
                                            .background(Color.Black, CircleShape)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(1.dp, ArsenalRed, CircleShape)
                                                .background(Color.DarkGray, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎵", fontSize = 14.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = track.title,
                                            color = ArsenalWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artist,
                                            color = ArsenalGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Controls on soundtracks matching setup
                                    IconButton(
                                        onClick = { viewModel.togglePlayback() },
                                        modifier = Modifier.testTag("play_pause_bottom")
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause background sound" else "Play background sound",
                                            tint = ArsenalWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.playNext() },
                                        modifier = Modifier.testTag("skip_next_bottom")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Skip to next anthem",
                                            tint = ArsenalWhite,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Primary Material 3 bottom tabs
                    NavigationBar(
                        containerColor = ArsenalMidnight,
                        tonalElevation = 8.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { viewModel.setTab(0) },
                            icon = { Icon(Icons.Default.Public, contentDescription = "Celebrations Map") },
                            label = { Text("Map", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArsenalWhite,
                                unselectedIconColor = SoftGray,
                                selectedTextColor = ArsenalGold,
                                unselectedTextColor = SoftGray,
                                indicatorColor = ArsenalRed
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { viewModel.setTab(1) },
                            icon = { Icon(Icons.Default.MusicNote, contentDescription = "Stadia Anthems") },
                            label = { Text("Songs", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArsenalWhite,
                                unselectedIconColor = SoftGray,
                                selectedTextColor = ArsenalGold,
                                unselectedTextColor = SoftGray,
                                indicatorColor = ArsenalRed
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { viewModel.setTab(2) },
                            icon = { Icon(Icons.Default.Feed, contentDescription = "Global Live Feed") },
                            label = { Text("Feed", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArsenalWhite,
                                unselectedIconColor = SoftGray,
                                selectedTextColor = ArsenalGold,
                                unselectedTextColor = SoftGray,
                                indicatorColor = ArsenalRed
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { viewModel.setTab(3) },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "My Gooner Settings") },
                            label = { Text("Settings", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArsenalWhite,
                                unselectedIconColor = SoftGray,
                                selectedTextColor = ArsenalGold,
                                unselectedTextColor = SoftGray,
                                indicatorColor = ArsenalRed
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> {
                        // MAP TAB
                        Box(modifier = Modifier.fillMaxSize()) {
                            val activeSelectedPin = if (mapUiState is MapUiState.SelectedPin) {
                                (mapUiState as MapUiState.SelectedPin).pin
                            } else null

                            // Draw structural canvas map
                            MapCanvas(
                                pins = pins,
                                selectedPin = activeSelectedPin,
                                onPinSelected = { viewModel.selectPin(it) },
                                onMapClickedAtCoords = { lat, lng ->
                                    // Trigger dialog overlay asking to place new celebration pinpoint at coordinate intersection!
                                    pendingLatitude = lat
                                    pendingLongitude = lng
                                    showCreatePinDialog = true
                                }
                            )

                            // Helper mini-tip overlay
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = ArsenalMidnight.copy(alpha = 0.85f))
                            ) {
                                Text(
                                    text = "💡 Tap empty map space to pin local celebration, or click a point!",
                                    color = ArsenalGold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // ----------------- HIGH DENSITY DESIGN TOOLBAR & TELEMETRY CARD OVERLAYS -----------------
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 70.dp, start = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // A. MUSIC SYNC PANEL (Real-time animating equalizer)
                                Box(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "MUSIC SYNC",
                                            color = Color.LightGray,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = currentTrack?.title ?: "No Anthem",
                                            color = ArsenalRedLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Animated musical telemetry bar indicators mimicking stadium soundwave dynamics
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.Bottom,
                                            modifier = Modifier.height(14.dp)
                                        ) {
                                            val bar1Height = remember { Animatable(6f) }
                                            val bar2Height = remember { Animatable(12f) }
                                            val bar3Height = remember { Animatable(8f) }
                                            LaunchedEffect(isPlaying) {
                                                if (isPlaying) {
                                                    launch {
                                                        while (true) {
                                                            bar1Height.animateTo(3f + (0..10).random().toFloat(), tween(250))
                                                        }
                                                    }
                                                    launch {
                                                        while (true) {
                                                            bar2Height.animateTo(4f + (0..12).random().toFloat(), tween(300))
                                                        }
                                                    }
                                                    launch {
                                                        while (true) {
                                                            bar3Height.animateTo(2f + (0..10).random().toFloat(), tween(200))
                                                        }
                                                    }
                                                } else {
                                                    bar1Height.snapTo(2f)
                                                    bar2Height.snapTo(4f)
                                                    bar3Height.snapTo(3f)
                                                }
                                            }
                                            Box(modifier = Modifier.size(width = 4.dp, height = bar1Height.value.dp).clip(RoundedCornerShape(1.dp)).background(ArsenalRed))
                                            Box(modifier = Modifier.size(width = 4.dp, height = bar2Height.value.dp).clip(RoundedCornerShape(1.dp)).background(ArsenalRedLight))
                                            Box(modifier = Modifier.size(width = 4.dp, height = bar3Height.value.dp).clip(RoundedCornerShape(1.dp)).background(ArsenalGold))
                                        }
                                    }
                                }

                                // B. REAL-TIME PINS RATE DISPLAY PANEL
                                Box(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "REAL-TIME PINS",
                                            color = Color.LightGray,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+4.2/min",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            // C. FLOATING CAMERA ACTION TRIGGER (Right margin overlay)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        cameFromPostFeed = true
                                        showCameraView = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArsenalRed),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .size(54.dp)
                                        .border(1.dp, ArsenalRedLight, RoundedCornerShape(16.dp)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Quick Live Broadcast Snap",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // D. HIGH-DENSITY PERSISTENT BOTTOM HUB SUMMARY (Visual preview card loaded when no pin selected)
                            AnimatedVisibility(
                                visible = activeSelectedPin == null,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(14.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ArsenalSlate)
                                        ) {
                                            AsyncImage(
                                                model = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=150",
                                                contentDescription = "Hub visual feed preview",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                        )
                                                    )
                                            )
                                            Text(
                                                text = "LIVE",
                                                color = Color.White,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Islington Red Hub Celebration",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Track: ${currentTrack?.title ?: "North London Forever"} • Louis",
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Avatars stacking row indicators
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy((-6).dp)
                                                ) {
                                                    val borderMod = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .border(1.dp, Color(0xFF0F172A), CircleShape)
                                                    Box(modifier = borderMod.background(Color(0xFFEF4444)))
                                                    Box(modifier = borderMod.background(Color(0xFFFBBF24)))
                                                    Box(
                                                        modifier = borderMod.background(Color(0xFF3B82F6)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("+9k", fontSize = 6.sp, color = Color.White, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "joined locally",
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                cameFromPostFeed = false
                                                showCameraView = true
                                            },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(ArsenalRed)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Capture celebration photo post",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Event Detail overlay on click pin (Popup sheet Card layout)
                            AnimatedVisibility(
                                visible = activeSelectedPin != null,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                activeSelectedPin?.let { pin ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                                        border = BorderStroke(1.dp, ArsenalGold)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(ArsenalRed),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("📢", fontSize = 16.sp)
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = pin.title,
                                                    color = ArsenalWhite,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.clearSelection() }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Collapse detail dialog",
                                                        tint = SoftGray
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = pin.description,
                                                color = ArsenalWhite,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            // Attachments: Real Photos synced
                                            pin.celebrateImageUrl?.let { url ->
                                                AsyncImage(
                                                    model = url,
                                                    contentDescription = "Fan Photo share",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(130.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(ArsenalCharcoal),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "🔥 Joined Gooners Headcount",
                                                        color = ArsenalGold,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${pin.headcount} shouting players",
                                                        color = ArsenalWhite,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        // Instantly join headcount ticker
                                                        viewModel.selectPin(pin.copy(headcount = pin.headcount + 1))
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                "Successfully checked-in. Shouting Gooners headcount recalculated!"
                                                            )
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ArsenalRed),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Text("Join Cheer !", fontSize = 12.sp, color = ArsenalWhite)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // STADIA SONGS PLAYLIST
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ArsenalCharcoal)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "Stadium Chants & Anthems",
                                    color = ArsenalWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "Choose a song to sync automatically with map markers!",
                                    color = ArsenalGold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            items(tracks) { track ->
                                val isActive = currentTrack?.id == track.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.playTrack(track) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) ArsenalMidnightLight else CardBackgroundDark
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActive) ArsenalGold else ArsenalSlate
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isActive) ArsenalRed else ArsenalSlate),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isActive && isPlaying) Icons.Filled.VolumeUp else Icons.Filled.MusicNote,
                                                contentDescription = "Track status",
                                                tint = ArsenalWhite
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = track.title,
                                                color = ArsenalWhite,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = track.artist,
                                                color = SoftGray,
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (isActive) {
                                            Text(
                                                text = "Playing",
                                                color = ArsenalGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // GLOBAL BROADCAST FEED AND CHAT
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ArsenalCharcoal)
                        ) {
                            // Small composer section allowing instant photo posts of fan celebrations
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ArsenalMidnight)
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(ArsenalRed)
                                            .clickable {
                                                cameFromPostFeed = true
                                                showCameraView = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Capture celebration photo post",
                                            tint = ArsenalWhite,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                cameFromPostFeed = true
                                                showCameraView = true
                                            }
                                    ) {
                                        Text(
                                            text = "CAPTION A LOCAL CELEBRATION MOMENT",
                                            color = ArsenalGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Take a photo & broadcast to Gooners!",
                                            color = SoftGray,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(posts) { post ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(ArsenalRed),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("🤖", fontSize = 16.sp)
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Text(
                                                        text = post.userDisplayName,
                                                        color = ArsenalWhite,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "Synced via Firebase Database • Live",
                                                        color = ArsenalGold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            // Actual attachment
                                            AsyncImage(
                                                model = post.imageUrl,
                                                contentDescription = "Celebration Broadcast image",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .background(ArsenalCharcoal),
                                                contentScale = ContentScale.Crop
                                            )

                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = post.description,
                                                    color = ArsenalWhite,
                                                    fontSize = 14.sp
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = "Like reaction count icon",
                                                        tint = ArsenalRed,
                                                        modifier = Modifier.clickable {
                                                            // Local reactivity like trigger
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("You liked this global celebration!")
                                                            }
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${post.likesCount + 1} likes",
                                                        color = SoftGray,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // PERSONALIZED ACCOUNT SETTINGS & BADGING
                        var tempName by remember { mutableStateOf(user.displayName) }
                        var tempQuote by remember { mutableStateOf(user.quote) }
                        var selectedBadge by remember { mutableStateOf(user.badge) }

                        val badgesList = listOf("Emirates Faithful 🔴", "Invincible King 👑", "Legendary Striker ⚽", "Golden Gunner ⚡")

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ArsenalCharcoal)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "Personalize Gooner Account",
                                    color = ArsenalWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Google Sign-In integration token matches active identity details.",
                                    color = ArsenalGold,
                                    fontSize = 12.sp
                                )
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                                    border = BorderStroke(1.dp, ArsenalSlate),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        OutlinedTextField(
                                            value = tempName,
                                            onValueChange = { tempName = it },
                                            label = { Text("Display Name", color = SoftGray) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = ArsenalWhite,
                                                unfocusedTextColor = ArsenalWhite,
                                                focusedBorderColor = ArsenalGold,
                                                unfocusedBorderColor = ArsenalSlate,
                                                cursorColor = ArsenalGold
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = tempQuote,
                                            onValueChange = { tempQuote = it },
                                            label = { Text("Shoutout / Bio Quote", color = SoftGray) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = ArsenalWhite,
                                                unfocusedTextColor = ArsenalWhite,
                                                focusedBorderColor = ArsenalGold,
                                                unfocusedBorderColor = ArsenalSlate,
                                                cursorColor = ArsenalGold
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Gooner Badge Champion Status",
                                            color = ArsenalGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        // Badge picker Chips
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            badgesList.forEach { badgeLabel ->
                                                val isBadgeSel = badgeLabel == selectedBadge
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isBadgeSel) ArsenalRed else ArsenalSlate.copy(alpha = 0.5f))
                                                        .clickable { selectedBadge = badgeLabel }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = badgeLabel.split(" ").first(),
                                                        color = ArsenalWhite,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = {
                                                viewModel.updatePersonalSettings(tempName, tempQuote, selectedBadge, user.avatar)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Gooner profile configuration synced!")
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ArsenalGold),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Save Changes", color = ArsenalMidnight, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            item {
                                Button(
                                    onClick = { viewModel.handleLogout() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArsenalRed),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Logout icon indicator",
                                        tint = ArsenalWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Logout Fan Account", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // DIALOG: Overlay Dialog to place & map celebration pins
                if (showCreatePinDialog) {
                    var inputPinTitle by remember { mutableStateOf("") }
                    var inputPinDesc by remember { mutableStateOf("") }
                    var linkedTrackId by remember { mutableStateOf<String?>(null) }

                    AlertDialog(
                        onDismissRequest = { showCreatePinDialog = false },
                        containerColor = CardBackgroundDark,
                        title = {
                            Text(
                                text = "Setup New Parade Center",
                                color = ArsenalGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "LatLng coordinates chosen:\nLat: ${"%.4f".format(pendingLatitude)} , Lng: ${"%.4f".format(pendingLongitude)}",
                                    color = SoftGray,
                                    fontSize = 11.sp
                                )

                                OutlinedTextField(
                                    value = inputPinTitle,
                                    onValueChange = { inputPinTitle = it },
                                    label = { Text("Zone / Club Title", color = SoftGray) },
                                    placeholder = { Text("e.g. Manchester Supporters Club", color = SoftGray.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = ArsenalWhite,
                                        unfocusedTextColor = ArsenalWhite,
                                        focusedBorderColor = ArsenalGold,
                                        unfocusedBorderColor = ArsenalSlate
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = inputPinDesc,
                                    onValueChange = { inputPinDesc = it },
                                    label = { Text("Action Description", color = SoftGray) },
                                    placeholder = { Text("e.g. Gathering 40 people with flags!", color = SoftGray.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = ArsenalWhite,
                                        unfocusedTextColor = ArsenalWhite,
                                        focusedBorderColor = ArsenalGold,
                                        unfocusedBorderColor = ArsenalSlate
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Song linking picker options
                                Text(
                                    text = "Link Soundtrack Anthem 🎵",
                                    color = ArsenalGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    tracks.take(3).forEach { tr ->
                                        val isSel = linkedTrackId == tr.id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) ArsenalRed else ArsenalSlate.copy(alpha = 0.5f))
                                                .clickable { linkedTrackId = if (isSel) null else tr.id }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tr.title.split(" ").first(),
                                                color = ArsenalWhite,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Interactive camera action trigger inside pin dialogue
                                Button(
                                    onClick = {
                                        cameFromPostFeed = false
                                        viewModel.updateFormFields(
                                            title = inputPinTitle,
                                            desc = inputPinDesc,
                                            trackId = linkedTrackId,
                                            imageUri = null
                                        )
                                        showCreatePinDialog = false
                                        showCameraView = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArsenalSlate),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Capture live photo with Camera overlay",
                                        tint = ArsenalWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Snap Celebration Photo", color = ArsenalWhite)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.updateFormFields(
                                        title = inputPinTitle,
                                        desc = inputPinDesc,
                                        trackId = linkedTrackId,
                                        imageUri = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&q=80&w=600"
                                    )
                                    viewModel.createLocalCelebrationPin(pendingLatitude, pendingLongitude)
                                    showCreatePinDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Celebration coordinates pinned & synced live via Firebase!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ArsenalGold)
                            ) {
                                Text("Map Pin !", color = ArsenalMidnight, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCreatePinDialog = false }) {
                                Text("Cancel", color = ArsenalRedLight)
                            }
                        }
                    )
                }

                // FULL CAMERA OVERLAY COMPONENT WINDOW VIEW
                if (showCameraView) {
                    CameraOverlay(
                        onCaptureComplete = { caption, imgUri ->
                            showCameraView = false
                            if (cameFromPostFeed) {
                                // Broadcaster feed action
                                viewModel.postCelebrationFeed(caption, imgUri)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Celebration post published worldwide!")
                                }
                            } else {
                                // Pin creation attachment action
                                viewModel.updateFormFields(
                                    title = viewModel.pinTitle.value,
                                    desc = caption,
                                    trackId = viewModel.selectedTrackId.value,
                                    imageUri = imgUri
                                )
                                viewModel.createLocalCelebrationPin(pendingLatitude, pendingLongitude)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Captured image coordinates mapped & synced live on globe!")
                                }
                            }
                        },
                        onClose = {
                            showCameraView = false
                        }
                    )
                }
            }
        }
    }
}
