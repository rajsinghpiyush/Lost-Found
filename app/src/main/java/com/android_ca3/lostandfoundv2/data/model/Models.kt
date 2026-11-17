package com.android_ca3.lostandfoundv2.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Locale

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
