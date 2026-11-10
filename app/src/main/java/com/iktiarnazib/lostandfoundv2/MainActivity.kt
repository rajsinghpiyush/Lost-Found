package com.iktiarnazib.lostandfoundv2

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

// ============================================================
// 1. DATA MODELS & HELPERS  (unchanged logic)
// ============================================================

data class UserData(
    val uid: String = "",
    val name: String = "",
    val studentId: String = "",
    val roll: String = "",
    val semester: String = "",
    val batch: String = "",
    val email: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class LostFoundPost(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val status: String = "",       // "Lost", "Found" or "Given"
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val authorStudentId: String = "",
    val authorRoll: String = "",
    val authorSemester: String = "",
    val authorBatch: String = "",
    val claimedByName: String = "",
    val imageData: String = "",
    val likes: Map<String, Boolean> = emptyMap(),
    val commentCount: Long = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Comment(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val unreadCounts: Map<String, Long> = emptyMap()
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)

data class ChatDestination(
    val chatId: String,
    val otherUserId: String,
    val otherUserName: String
)

enum class Screen { Login, SignUp, ForgotPassword, Home, Messages, Chat, CreatePost, Comments, Profile }

fun statusColor(status: String): Color = when (status.lowercase()) {
    "lost" -> Color(0xFFEF4444)
    "found" -> Color(0xFF3B82F6)
    "given" -> Color(0xFF22C55E)
    else -> Color(0xFF8E8EA3)
}

fun Conversation.otherUserName(myUid: String): String =
    participantNames.entries.firstOrNull { it.key != myUid }?.value ?: "Unknown"

fun Conversation.otherUserId(myUid: String): String =
    participants.firstOrNull { it != myUid } ?: ""

fun Conversation.unreadFor(myUid: String): Int = (unreadCounts[myUid] ?: 0L).toInt()

fun Timestamp?.timeAgo(): String {
    if (this == null) return ""
    val minutes = (System.currentTimeMillis() - toDate().time) / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(toDate())
    }
}

fun Timestamp?.toChatTime(): String =
    this?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it.toDate()) } ?: ""

// ============================================================
// 1b. IMAGE HELPERS (unchanged logic)
// ============================================================

@Composable
fun rememberDecodedImage(base64: String?): Bitmap? {
    return remember(base64) {
        if (base64.isNullOrBlank()) null
        else try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) { null }
    }
}

sealed interface ImageEncodeResult {
    data class Success(val base64: String) : ImageEncodeResult
    data class Failure(val message: String) : ImageEncodeResult
}

suspend fun encodeImageToBase64(context: Context, uri: Uri): ImageEncodeResult =
    withContext(Dispatchers.IO) {
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val boundsOk = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
                true
            } ?: false
            if (!boundsOk) {
                return@withContext ImageEncodeResult.Failure("Couldn't read the selected file. Try a different photo.")
            }

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 900) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }

            if (bitmap == null) {
                val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                return@withContext if (raw != null && raw.size <= 350_000) {
                    ImageEncodeResult.Success(Base64.encodeToString(raw, Base64.NO_WRAP))
                } else {
                    ImageEncodeResult.Failure("Unsupported image format or file too large. Try a JPG/PNG photo.")
                }
            }

            val scaled = if (bitmap.width > 900 || bitmap.height > 900) {
                val scale = 900f / maxOf(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else bitmap

            var quality = 70
            var bytes: ByteArray
            do {
                val bos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, bos)
                bytes = bos.toByteArray()
                quality -= 15
            } while (bytes.size > 300_000 && quality > 20)

            if (bytes.size > 400_000) {
                return@withContext ImageEncodeResult.Failure("Photo is too large to store. Try a smaller photo.")
            }

            ImageEncodeResult.Success(Base64.encodeToString(bytes, Base64.NO_WRAP))
        } catch (e: Exception) {
            ImageEncodeResult.Failure("Photo error: ${e.message ?: "unknown"}")
        }
    }

// ============================================================
// 1c. FINDORA THEME (logo pink palette) + SHARED UI COMPONENTS
// ============================================================

// ---- Brand palette, matched to the Findora logo ----
val BrandPinkDeep = Color(0xFFE0246E)   // deep hot pink (logo shadow pink)
val BrandPink = Color(0xFFF43F7F)       // the logo's main pink
val BrandPinkLight = Color(0xFFFF7FAE)  // soft pink highlight
val BrandPinkSoft = Color(0xFFFFD9E6)   // very light pink for containers

private val BrandGradient = Brush.linearGradient(
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

private val FindoraLight = lightColorScheme(
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

private val FindoraDark = darkColorScheme(
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
fun FindoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) FindoraDark else FindoraLight,
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

