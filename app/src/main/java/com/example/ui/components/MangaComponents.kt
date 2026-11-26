package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalMangaPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun Modifier.mangaHalftoneBackground(
    paperColor: Color,
    dotColor: Color
): Modifier = this.drawBehind {
    drawRect(color = paperColor)
    val spacing = 8.dp.toPx()
    val radius = 1.1.dp.toPx()
    var y = spacing / 2f
    while (y < size.height) {
        var x = spacing / 2f
        while (x < size.width) {
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(x, y)
            )
            x += spacing
        }
        y += spacing
    }
}

fun Modifier.mangaSpeedLines(lineColor: Color): Modifier = this.drawBehind {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val maxR = maxOf(size.width, size.height) * 0.95f
    val rayCount = 60
    val stepDeg = 360f / rayCount
    val wedgeHalfRad = (1.1f * PI / 180.0)
    for (i in 0 until rayCount) {
        val angleRad = i * stepDeg * PI / 180.0
        val innerR = maxR * 0.22f
        val x1 = cx + (innerR * cos(angleRad)).toFloat()
        val y1 = cy + (innerR * sin(angleRad)).toFloat()
        val x2 = cx + (maxR * cos(angleRad - wedgeHalfRad)).toFloat()
        val y2 = cy + (maxR * sin(angleRad - wedgeHalfRad)).toFloat()
        val x3 = cx + (maxR * cos(angleRad + wedgeHalfRad)).toFloat()
        val y3 = cy + (maxR * sin(angleRad + wedgeHalfRad)).toFloat()
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
            lineTo(x3, y3)
            close()
        }
        drawPath(path = path, color = lineColor)
    }
}

@Composable
fun MangaPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalMangaPalette.current.panelSurface,
    contentColor: Color = LocalMangaPalette.current.ink,
    borderColor: Color = LocalMangaPalette.current.ink,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMangaPalette.current
    Box(modifier = modifier) {
        // Hard comic offset shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(palette.shadow, shape)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor,
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun MangaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = LocalMangaPalette.current.panelSurface,
    contentColor: Color = LocalMangaPalette.current.ink,
    borderColor: Color = LocalMangaPalette.current.ink,
    shadowOffset: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalMangaPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) shadowOffset else 0.dp,
        animationSpec = spring(stiffness = 700f),
        label = "btnPressOffset"
    )

    val shadowColor = if (enabled) palette.shadow else palette.muted.copy(alpha = 0.4f)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .drawBehind {
                val s = shadowOffset.toPx()
                val cr = 4.dp.toPx()
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(s, s),
                    size = size,
                    cornerRadius = CornerRadius(cr, cr)
                )
            }
            .offset {
                val px = currentOffset.roundToPx()
                IntOffset(px, px)
            }
            .clip(shape)
            .background(backgroundColor, shape)
            .border(2.5.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun MangaClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = LocalMangaPalette.current.panelSurface,
    contentColor: Color = LocalMangaPalette.current.ink,
    borderColor: Color = LocalMangaPalette.current.ink,
    shadowOffset: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMangaPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) shadowOffset else 0.dp,
        animationSpec = spring(stiffness = 700f),
        label = "cardPressOffset"
    )
    val shadowColor = if (enabled) palette.shadow else palette.muted.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .drawBehind {
                val s = shadowOffset.toPx()
                val cr = 4.dp.toPx()
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(s, s),
                    size = size,
                    cornerRadius = CornerRadius(cr, cr)
                )
            }
            .offset {
                val px = currentOffset.roundToPx()
                IntOffset(px, px)
            }
            .clip(shape)
            .background(backgroundColor, shape)
            .border(2.5.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun MangaProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 18.dp
) {
    val palette = LocalMangaPalette.current
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(palette.panelSurface)
            .border(2.5.dp, palette.ink)
    ) {
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .fillMaxHeight()
                    .drawBehind {
                        drawRect(color = palette.ink)
                        val stripeW = 6.dp.toPx()
                        val accentColor = palette.muted.copy(alpha = 0.55f)
                        clipRect {
                            var x = -size.height
                            while (x < size.width + size.height) {
                                val path = Path().apply {
                                    moveTo(x, size.height)
                                    lineTo(x + stripeW, size.height)
                                    lineTo(x + size.height + stripeW, 0f)
                                    lineTo(x + size.height, 0f)
                                    close()
                                }
                                drawPath(path, color = accentColor)
                                x += stripeW * 2f
                            }
                        }
                    }
            )
        }
    }
}

@Composable
fun MangaSpeechBubble(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalMangaPalette.current.panelSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMangaPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        contentColor = palette.ink,
        border = BorderStroke(2.5.dp, palette.ink)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

@Composable
fun MangaSectionBox(
    title: String,
    modifier: Modifier = Modifier,
    headerColor: Color = LocalMangaPalette.current.ink,
    headerTextColor: Color = LocalMangaPalette.current.onInk,
    borderColor: Color = LocalMangaPalette.current.ink,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMangaPalette.current
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(palette.shadow)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.panelSurface)
                .border(2.5.dp, borderColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = headerTextColor
                )
            }
            Column(
                modifier = Modifier.padding(10.dp),
                content = content
            )
        }
    }
}
