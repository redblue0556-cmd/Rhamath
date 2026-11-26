package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.QuestionBank
import com.example.ui.LabUiState
import com.example.ui.components.MangaButton
import com.example.ui.components.MangaPanel
import com.example.ui.components.MathRichText
import com.example.ui.components.mangaHalftoneBackground
import com.example.ui.theme.LocalMangaPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LabScreen(
    labState: LabUiState,
    onRadiusChange: (String) -> Unit,
    onAngleChange: (String) -> Unit,
    onPiToggle: (Boolean) -> Unit,
    onBackToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToLobby)
    val palette = LocalMangaPalette.current

    val r = labState.radius
    val theta = labState.angleDeg
    val pi = labState.piValue
    val piTex = if (labState.usePiFraction) "\\frac{22}{7}" else "3{,}14"

    val circumference = 2.0 * pi * r
    val area = pi * r * r
    val arcLength = (theta / 360.0) * circumference
    val sectorArea = (theta / 360.0) * area

    Box(
        modifier = modifier
            .fillMaxSize()
            .mangaHalftoneBackground(palette.paper, palette.halftoneDot)
            .safeDrawingPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 760.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 980.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MangaButton(
                    onClick = onBackToLobby,
                    modifier = Modifier.testTag("lab_back_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = palette.ink
                        )
                        Text(
                            text = "🏠 ${stringResource(R.string.back_to_lobby)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.ink
                        )
                    }
                }

                MangaPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "🧪 LAB RUMUS & KALKULATOR BUSUR",
                        style = MaterialTheme.typography.displayMedium,
                        color = palette.ink
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                LabControlsSection(
                                    labState = labState,
                                    onRadiusChange = onRadiusChange,
                                    onAngleChange = onAngleChange,
                                    onPiToggle = onPiToggle
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircleDiagramBox(radius = r, angleDeg = theta)
                                LabResultsSection(
                                    r = r,
                                    theta = theta,
                                    piTex = piTex,
                                    circumference = circumference,
                                    area = area,
                                    arcLength = arcLength,
                                    sectorArea = sectorArea
                                )
                            }
                        }
                    } else {
                        LabControlsSection(
                            labState = labState,
                            onRadiusChange = onRadiusChange,
                            onAngleChange = onAngleChange,
                            onPiToggle = onPiToggle
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircleDiagramBox(radius = r, angleDeg = theta)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LabResultsSection(
                            r = r,
                            theta = theta,
                            piTex = piTex,
                            circumference = circumference,
                            area = area,
                            arcLength = arcLength,
                            sectorArea = sectorArea
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabControlsSection(
    labState: LabUiState,
    onRadiusChange: (String) -> Unit,
    onAngleChange: (String) -> Unit,
    onPiToggle: (Boolean) -> Unit
) {
    val palette = LocalMangaPalette.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = labState.radiusInput,
                onValueChange = onRadiusChange,
                label = { Text("Jari-jari (r)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.ink,
                    unfocusedBorderColor = palette.ink,
                    focusedLabelColor = palette.ink,
                    unfocusedLabelColor = palette.muted,
                    focusedTextColor = palette.ink,
                    unfocusedTextColor = palette.ink
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("lab_radius_input")
            )

            OutlinedTextField(
                value = labState.angleInput,
                onValueChange = onAngleChange,
                label = { Text("Sudut (θ°)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.ink,
                    unfocusedBorderColor = palette.ink,
                    focusedLabelColor = palette.ink,
                    unfocusedLabelColor = palette.muted,
                    focusedTextColor = palette.ink,
                    unfocusedTextColor = palette.ink
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("lab_angle_input")
            )
        }

        // Interactive angle slider
        Column {
            Text(
                text = "Geser Sudut: ${QuestionBank.fmtNum(labState.angleDeg)}°",
                style = MaterialTheme.typography.labelLarge,
                color = palette.ink
            )
            Slider(
                value = labState.angleDeg.toFloat().coerceIn(1f, 360f),
                onValueChange = { v -> onAngleChange(v.roundToInt().toString()) },
                valueRange = 1f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = palette.crimson,
                    activeTrackColor = palette.crimson,
                    inactiveTrackColor = palette.panelSecondary
                ),
                modifier = Modifier.testTag("lab_angle_slider")
            )
        }

        // Quick preset chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(7, 14, 21).forEach { presetR ->
                FilterChip(
                    selected = labState.radius == presetR.toDouble(),
                    onClick = { onRadiusChange(presetR.toString()) },
                    label = { Text("r=$presetR") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.ink,
                        selectedLabelColor = palette.onInk
                    )
                )
            }
            listOf(45, 60, 90, 120, 180, 270).forEach { presetAngle ->
                FilterChip(
                    selected = labState.angleDeg == presetAngle.toDouble(),
                    onClick = { onAngleChange(presetAngle.toString()) },
                    label = { Text("${presetAngle}°") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.crimson,
                        selectedLabelColor = palette.onCrimson
                    )
                )
            }
        }

        // Pi Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MangaButton(
                onClick = { onPiToggle(true) },
                backgroundColor = if (labState.usePiFraction) palette.ink else palette.panelSurface,
                contentColor = if (labState.usePiFraction) palette.onInk else palette.ink,
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pi_fraction_button")
            ) {
                Text(
                    text = "π = 22/7",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            MangaButton(
                onClick = { onPiToggle(false) },
                backgroundColor = if (!labState.usePiFraction) palette.ink else palette.panelSurface,
                contentColor = if (!labState.usePiFraction) palette.onInk else palette.ink,
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("pi_decimal_button")
            ) {
                Text(
                    text = "π = 3,14",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CircleDiagramBox(
    radius: Double,
    angleDeg: Double,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val circleRadius = size.minDimension * 0.42f
            val center = Offset(size.width / 2f, size.height / 2f)
            val topLeft = Offset(center.x - circleRadius, center.y - circleRadius)
            val arcSize = Size(circleRadius * 2f, circleRadius * 2f)

            // Base circle
            drawCircle(
                color = palette.panelSurface,
                radius = circleRadius,
                center = center
            )

            // Shaded sector (counter-clockwise from 0 deg, matching standard math & HTML SVG)
            val sweep = angleDeg.toFloat().coerceIn(0f, 360f)
            if (sweep > 0f) {
                drawArc(
                    color = palette.ink.copy(alpha = 0.82f),
                    startAngle = 0f,
                    sweepAngle = -sweep,
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
                // Crimson highlight on outer arc
                drawArc(
                    color = palette.crimson,
                    startAngle = 0f,
                    sweepAngle = -sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Outer circle outline
            drawCircle(
                color = palette.ink,
                radius = circleRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Radius lines
            val endRad = sweep * PI / 180.0
            val endPt = Offset(
                center.x + (circleRadius * cos(endRad)).toFloat(),
                center.y - (circleRadius * sin(endRad)).toFloat()
            )
            drawLine(
                color = palette.ink,
                start = center,
                end = Offset(center.x + circleRadius, center.y),
                strokeWidth = 2.5.dp.toPx()
            )
            drawLine(
                color = palette.ink,
                start = center,
                end = endPt,
                strokeWidth = 2.5.dp.toPx()
            )

            // Inner angle arc
            val innerR = circleRadius * 0.28f
            drawArc(
                color = palette.yellowHighlight,
                startAngle = 0f,
                sweepAngle = -sweep,
                useCenter = false,
                topLeft = Offset(center.x - innerR, center.y - innerR),
                size = Size(innerR * 2f, innerR * 2f),
                style = Stroke(width = 2.5.dp.toPx())
            )

            drawCircle(
                color = palette.crimson,
                radius = 4.dp.toPx(),
                center = center
            )
        }
        Text(
            text = "θ = ${QuestionBank.fmtNum(angleDeg)}°   ·   r = ${QuestionBank.fmtNum(radius)}",
            style = MaterialTheme.typography.titleMedium,
            color = palette.ink
        )
    }
}

@Composable
private fun LabResultsSection(
    r: Double,
    theta: Double,
    piTex: String,
    circumference: Double,
    area: Double,
    arcLength: Double,
    sectorArea: Double
) {
    val rStr = QuestionBank.fmtNum(r)
    val tStr = QuestionBank.fmtNum(theta)
    val items = listOf(
        "Keliling: \$K=2\\pi r=2\\times$piTex\\times$rStr=${QuestionBank.fmtNum(circumference)}\$",
        "Luas: \$L=\\pi r^2=${QuestionBank.fmtNum(area)}\$",
        "Panjang busur: \$\\frac{$tStr}{360}\\times2\\times$piTex\\times$rStr=\\mathbf{${QuestionBank.fmtNum(arcLength)}}\$",
        "Luas juring: \$\\frac{$tStr}{360}\\times\\pi r^2=${QuestionBank.fmtNum(sectorArea)}\$"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (item in items) {
            LabStepCard(text = item)
        }
    }
}

@Composable
private fun LabStepCard(text: String) {
    val palette = LocalMangaPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.panelSurface)
            .border(2.dp, palette.ink)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        MathRichText(
            text = text,
            color = palette.ink,
            fontSize = 15.sp
        )
    }
}
