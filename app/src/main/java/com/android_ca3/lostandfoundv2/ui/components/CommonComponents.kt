package com.android_ca3.lostandfoundv2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android_ca3.lostandfoundv2.data.model.statusColor

// 1c. FINDORA THEME (logo pink palette) + SHARED UI COMPONENTS
// ============================================================

// ---- Brand palette, matched to the Findly logo ----
val BrandPinkDeep = Color(0xFFE0246E)   // deep hot pink (logo shadow pink)
val BrandPink = Color(0xFFF43F7F)       // the logo's main pink
val BrandPinkLight = Color(0xFFFF7FAE)  // soft pink highlight
val BrandPinkSoft = Color(0xFFFFD9E6)   // very light pink for containers

val BrandGradient = Brush.linearGradient(
    listOf(BrandPinkDeep, BrandPinkLight)
)

// Slowly shifting brand gradient — used on splash, login hero, profile, drawer
@Composable
fun animatedBrandGradient(vertical: Boolean = false): Brush {
    val transition = rememberInfiniteTransition(label = "brandBg")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )
    val c1 = lerp(BrandPinkDeep, BrandPinkLight, shift)
    val c2 = lerp(BrandPinkLight, BrandPinkDeep, shift)
    return if (vertical) Brush.verticalGradient(listOf(c1, c2))
    else Brush.linearGradient(listOf(c1, c2))
}

private val FindlyLight = lightColorScheme(
    primary = BrandPinkDeep,
    onPrimary = Color.White,
    primaryContainer = BrandPinkSoft,
    onPrimaryContainer = Color(0xFF5C0A2E),
    secondary = Color(0xFF745560),
    secondaryContainer = Color(0xFFFFDCE8),
    onSecondaryContainer = Color(0xFF2B1219),
    tertiary = Color(0xFF1F9D55),
    tertiaryContainer = Color(0xFFD3F5E2),
    background = Color(0xFFFDF4F7),          // pink-tinted off-white
    onBackground = Color(0xFF221218),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF221218),
    surfaceVariant = Color(0xFFF9E2EB),
    onSurfaceVariant = Color(0xFF745560),
    outline = Color(0xFFE5B8C8),
    error = Color(0xFFE04B4B),
    errorContainer = Color(0xFFFFE4E4)
)

private val FindlyDark = darkColorScheme(
    primary = Color(0xFFFF8AB4),
    onPrimary = Color(0xFF5C0A2E),
    primaryContainer = Color(0xFF8C1B4B),
    onPrimaryContainer = BrandPinkSoft,
    secondary = Color(0xFFE0BDC9),
    secondaryContainer = Color(0xFF4A2B36),
    onSecondaryContainer = Color(0xFFFFDCE8),
    tertiary = Color(0xFF6FE0A0),
    tertiaryContainer = Color(0xFF14532D),
    background = Color(0xFF190D12),          // deep pink-black
    onBackground = Color(0xFFF3DEE6),
    surface = Color(0xFF24141B),
    onSurface = Color(0xFFF3DEE6),
    surfaceVariant = Color(0xFF3A222C),
    onSurfaceVariant = Color(0xFFD3C0C8),
    outline = Color(0xFF5F404C),
    error = Color(0xFFFF7B7B),
    errorContainer = Color(0xFF4A1D1D)
)

@Composable
fun FindlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) FindlyDark else FindlyLight,
        content = content
    )
}

// Gradient pill button
@Composable
fun GradientButton(
    text: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (enabled) BrandGradient
    else Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(if (enabled) 10.dp else 0.dp, RoundedCornerShape(18.dp), spotColor = BrandPinkDeep)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = Color.White
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Reusable gradient circle action button (chat send, comment send)
@Composable
fun GradientCircleButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bg = if (enabled) BrandGradient
    else Brush.linearGradient(
        listOf(
            BrandPinkDeep.copy(alpha = 0.35f),
            BrandPinkLight.copy(alpha = 0.35f)
        )
    )
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

// Gradient circle avatar with initial
@Composable
fun GradientAvatar(name: String, size: Dp, textStyle: androidx.compose.ui.text.TextStyle? = null) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(BrandPinkDeep, Color(0xFFFF9EC0)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().take(1).uppercase().ifBlank { "?" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = textStyle ?: MaterialTheme.typography.titleMedium
        )
    }
}

// Colored status pill with icon
@Composable
fun StatusBadge(status: String, fontSize: Int = 11) {
    val color = statusColor(status)
    val icon = when (status.lowercase()) {
        "lost" -> Icons.Default.SearchOff
        "found" -> Icons.Default.CheckCircle
        "given" -> Icons.Default.TaskAlt
        else -> Icons.Default.AutoAwesome
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp
            )
        }
    }
}

// Modern empty state
@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Error banner chip
@Composable
fun ErrorBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

// Pulsing "online" dot
@Composable
fun PulsingDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// ============================================================
