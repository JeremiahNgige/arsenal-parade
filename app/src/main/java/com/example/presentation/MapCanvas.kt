package com.example.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.entity.ParadePinEntity
import com.example.ui.theme.*
import kotlin.math.sqrt

// Minimal simulated interactive globe canvas
@Composable
fun MapCanvas(
    pins: List<ParadePinEntity>,
    selectedPin: ParadePinEntity?,
    onPinSelected: (ParadePinEntity) -> Unit,
    onMapClickedAtCoords: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom/Pan States
    var scale by remember { mutableStateOf(1.0f) }
    var offsetX by remember { mutableStateOf(0.0f) }
    var offsetY by remember { mutableStateOf(0.0f) }

    // Glow effects pulsing animations
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulsing")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseStrength"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseFade"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArsenalCharcoal)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Boundary control for pan
                    offsetX = (offsetX + dragAmount.x).coerceIn(-1000f, 1000f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(-1000f, 1000f)
                }
            }
            .pointerInput(pins, scale, offsetX, offsetY) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        scale = if (scale > 1.8f) 1.0f else scale + 0.5f
                    },
                    onTap = { tapOffset ->
                        // Calculate where the tap is in map coordinates (centered width/height normalized to lat/lng)
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Adjust tap for screen scale & translate offsets
                        val relativeX = (tapOffset.x - w / 2 - offsetX) / scale + w / 2
                        val relativeY = (tapOffset.y - h / 2 - offsetY) / scale + h / 2

                        // Map relative canvas coordinates back to latitude & longitude projection
                        // Let's find the nearest pin first
                        var nearestPin: ParadePinEntity? = null
                        var shortestDistance = 10000000f

                        for (pin in pins) {
                            // Map LatLng projection inside 0 to width & 0 to height
                            val px = w / 2 + (pin.longitude.toFloat() / 180f) * (w / 2)
                            val py = h / 2 - (pin.latitude.toFloat() / 90f) * (h / 2)

                            val dist = sqrt((relativeX - px) * (relativeX - px) + (relativeY - py) * (relativeY - py))
                            if (dist < 40f) { // Touch clearance circle radius
                                if (dist < shortestDistance) {
                                    shortestDistance = dist
                                    nearestPin = pin
                                }
                            }
                        }

                        if (nearestPin != null) {
                            onPinSelected(nearestPin)
                        } else {
                            // Translate tap position into simulated Lat/Lng to let user add their customized local celebration pin!
                            val tappedLng = ((relativeX - w / 2) / (w / 2)) * 180.0
                            val tappedLat = -((relativeY - h / 2) / (h / 2)) * 90.0

                            onMapClickedAtCoords(
                                tappedLat.coerceIn(-85.0, 85.0),
                                tappedLng.coerceIn(-180.0, 180.0)
                            )
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            withTransform({
                translate(w / 2 + offsetX, h / 2 + offsetY)
                scale(scale, scale, pivot = Offset(0f, 0f))
            }) {
                // Draw Ambient World Grid (Slick Stadium holographic cyber map look)
                val gridStroke = Stroke(
                    width = 1.2f,
                    pathEffect = null
                )

                // Draws high-density dot matrix layout inside map canvas
                for (latIdx in -6..6) {
                    for (lngIdx in -10..10) {
                        val dy = (latIdx * 12f / 90f) * (h / 2)
                        val dx = (lngIdx * 18f / 180f) * (w / 2)
                        drawCircle(
                            color = Color.White,
                            radius = 1.2f,
                            center = Offset(dx, dy),
                            alpha = 0.15f
                        )
                    }
                }

                // Draws Latitudinal / Longitudinal stadium lines
                for (i in -4..4) {
                    val latY = (i * 18f / 90f) * (h / 2)
                    drawLine(
                        color = ArsenalSlate.copy(alpha = 0.25f),
                        start = Offset(-w / 2, latY),
                        end = Offset(w / 2, latY),
                        strokeWidth = 1f
                    )

                    val lngX = (i * 36f / 180f) * (w / 2)
                    drawLine(
                        color = ArsenalSlate.copy(alpha = 0.25f),
                        start = Offset(lngX, -h / 2),
                        end = Offset(lngX, h / 2),
                        strokeWidth = 1f
                    )
                }

                // Draw cyber-minimalist continent shapes
                // Africa
                val pathAfrica = Path().apply {
                    moveTo(-50f, -40f)
                    lineTo(-30f, -20f)
                    lineTo(-10f, 10f)
                    lineTo(-15f, 50f)
                    lineTo(-30f, 95f)
                    lineTo(-45f, 60f)
                    lineTo(-70f, 35f)
                    lineTo(-95f, 10f)
                    lineTo(-100f, -20f)
                    close()
                }
                drawPath(pathAfrica, color = ArsenalSlate.copy(alpha = 0.15f))
                drawPath(pathAfrica, color = ArsenalSlate.copy(alpha = 0.3f), style = Stroke(2f))

                // Eurasia
                val pathEurasia = Path().apply {
                    moveTo(-120f, -120f)
                    lineTo(-70f, -140f)
                    lineTo(0f, -135f)
                    lineTo(120f, -145f)
                    lineTo(220f, -125f)
                    lineTo(250f, -60f)
                    lineTo(180f, -10f)
                    lineTo(140f, 20f)
                    lineTo(120f, -20f)
                    lineTo(80f, 0f)
                    lineTo(50f, 50f)
                    lineTo(10f, -10f)
                    lineTo(-40f, -40f)
                    lineTo(-80f, -80f)
                    close()
                }
                drawPath(pathEurasia, color = ArsenalSlate.copy(alpha = 0.15f))
                drawPath(pathEurasia, color = ArsenalSlate.copy(alpha = 0.3f), style = Stroke(2f))

                // North America
                val pathNA = Path().apply {
                    moveTo(-290f, -140f)
                    lineTo(-210f, -150f)
                    lineTo(-150f, -120f)
                    lineTo(-160f, -60f)
                    lineTo(-190f, -20f)
                    lineTo(-240f, -25f)
                    lineTo(-270f, -110f)
                    close()
                }
                drawPath(pathNA, color = ArsenalSlate.copy(alpha = 0.15f))
                drawPath(pathNA, color = ArsenalSlate.copy(alpha = 0.3f), style = Stroke(2f))

                // South America
                val pathSA = Path().apply {
                    moveTo(-190f, -15f)
                    lineTo(-155f, 10f)
                    moveTo(-155f, 10f)
                    lineTo(-135f, 55f)
                    lineTo(-160f, 120f)
                    lineTo(-180f, 160f)
                    lineTo(-190f, 100f)
                    lineTo(-220f, 40f)
                    close()
                }
                drawPath(pathSA, color = ArsenalSlate.copy(alpha = 0.15f))
                drawPath(pathSA, color = ArsenalSlate.copy(alpha = 0.3f), style = Stroke(2f))

                // Australia
                val pathOz = Path().apply {
                    moveTo(180f, 60f)
                    lineTo(240f, 45f)
                    lineTo(280f, 80f)
                    lineTo(230f, 110f)
                    lineTo(170f, 90f)
                    close()
                }
                drawPath(pathOz, color = ArsenalSlate.copy(alpha = 0.15f))
                drawPath(pathOz, color = ArsenalSlate.copy(alpha = 0.3f), style = Stroke(2f))

                // Draw Prime Meridians & Equator
                drawLine(
                    color = ArsenalGold.copy(alpha = 0.35f),
                    start = Offset(-w / 2, 0f),
                    end = Offset(w / 2, 0f),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = ArsenalGold.copy(alpha = 0.35f),
                    start = Offset(0f, -h / 2),
                    end = Offset(0f, h / 2),
                    strokeWidth = 1.5f
                )

                // DRAW THE SYSTEM EVENT PINS ON THE GLOBE
                for (pin in pins) {
                    val px = (pin.longitude.toFloat() / 180f) * (w / 2)
                    val py = -(pin.latitude.toFloat() / 90f) * (h / 2)

                    val isHighlighted = selectedPin?.id == pin.id

                    // Radar pulse waves emitting outward
                    drawCircle(
                        color = if (isHighlighted) ArsenalGold else ArsenalRed,
                        radius = pulseRadius,
                        center = Offset(px, py),
                        alpha = pulseAlpha
                    )

                    // Pin core solid dot
                    drawCircle(
                        color = if (isHighlighted) ArsenalGold else ArsenalRed,
                        radius = if (isHighlighted) 12f else 8f,
                        center = Offset(px, py)
                    )

                    // Pin crown ring (luxurious representation)
                    drawCircle(
                        color = ArsenalWhite,
                        radius = if (isHighlighted) 14f else 9f,
                        center = Offset(px, py),
                        style = Stroke(width = 2.5f)
                    )

                    // Dynamic text banner showing headcount on large cluster pins
                    if (pin.headcount > 500) {
                        val simpleText = if (pin.headcount >= 1000) "${pin.headcount / 1000}k Gooners" else "${pin.headcount} Gooners"
                        // Glow aura around coordinates
                        drawCircle(
                            color = ArsenalRedLight,
                            radius = if (isHighlighted) 40f else 25f,
                            center = Offset(px, py),
                            alpha = 0.12f
                        )
                    }
                }
            }
        }
    }
}
