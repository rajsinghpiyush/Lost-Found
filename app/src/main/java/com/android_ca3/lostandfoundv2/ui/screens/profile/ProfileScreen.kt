package com.android_ca3.lostandfoundv2.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android_ca3.lostandfoundv2.data.model.*
import com.android_ca3.lostandfoundv2.ui.components.*

// 16. PROFILE SCREEN (pink animated gradient header + tinted stats)
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: UserData?,
    myPosts: List<LostFoundPost>,
    onMarkGiven: (LostFoundPost, String) -> Unit,
    onDeletePost: (LostFoundPost) -> Unit,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var postToMark by remember { mutableStateOf<LostFoundPost?>(null) }
    var claimName by remember { mutableStateOf("") }
    var postToDelete by remember { mutableStateOf<LostFoundPost?>(null) }

    val lostCount = myPosts.count { it.status.equals("Lost", true) }
    val foundCount = myPosts.count { it.status.equals("Found", true) }
    val givenCount = myPosts.count { it.status.equals("Given", true) }

    // "Mark as Given" popup
    postToMark?.let { post ->
        AlertDialog(
            onDismissRequest = { postToMark = null; claimName = "" },
            title = { Text("Mark as Given", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Who was the item given to? Their name will be shown on the post.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = claimName,
                        onValueChange = { claimName = it },
                        label = { Text("Person's name") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onMarkGiven(post, claimName.trim())
                        postToMark = null
                        claimName = ""
                    },
                    enabled = claimName.isNotBlank()
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { postToMark = null; claimName = "" }) { Text("Cancel") }
            }
        )
    }

    // Delete popup
    postToDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text("Delete Post", fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${post.title}\" permanently? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePost(post)
                        postToDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Animated gradient header ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedBrandGradient(vertical = true))
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home", tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(5.dp)
                ) {
                    GradientAvatar(user?.name ?: "U", 86.dp, MaterialTheme.typography.headlineMedium)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = user?.name ?: "Guest User",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // ---- Tinted stats row ----
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(lostCount, "Lost", statusColor("Lost"), Modifier.weight(1f))
                StatCard(foundCount, "Found", statusColor("Found"), Modifier.weight(1f))
                StatCard(givenCount, "Given", statusColor("Given"), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- Student info card ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileRow(icon = Icons.Default.Badge, label = "Student ID", value = user?.studentId)
                    ProfileRow(icon = Icons.Default.Numbers, label = "Roll Number", value = user?.roll)
                    ProfileRow(icon = Icons.Default.DateRange, label = "Semester", value = user?.semester)
                    ProfileRow(icon = Icons.Default.School, label = "Batch", value = user?.batch)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "My Posts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${myPosts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (myPosts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.AutoAwesome,
                    title = "No posts yet",
                    subtitle = "Tap + on the Home screen to create your first post!"
                )
            } else {
                myPosts.forEach { post ->
                    MyPostItem(
                        post = post,
                        onMarkGivenClick = {
                            postToMark = post
                            claimName = ""
                        },
                        onDeleteClick = { postToDelete = post }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Lost → Delete | Found → Mark as Given | Given → no buttons
@Composable
fun MyPostItem(
    post: LostFoundPost,
    onMarkGivenClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val status = post.status.lowercase()
    val badgeColor = statusColor(post.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(post.status, fontSize = 10)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${post.createdAt.timeAgo()} • ${post.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status == "given" && post.claimedByName.isNotBlank()) {
                    Text(
                        text = "✅ Given to ${post.claimedByName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = statusColor("Given")
                    )
                }
            }

            when (status) {
                "lost" -> {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.10f))
                            .clickable { onDeleteClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Post",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                "found" -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor("Given"))
                            .clickable { onMarkGivenClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Mark as Given",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // "given" → nothing
            }
        }
    }
}

@Composable
fun ProfileRow(icon: ImageVector, label: String, value: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value?.ifBlank { "—" } ?: "—",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
