package com.android_ca3.lostandfoundv2.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android_ca3.lostandfoundv2.R
import com.android_ca3.lostandfoundv2.data.model.*
import com.android_ca3.lostandfoundv2.ui.components.*

// 9. HOME FEED (hero header, filter chips, modern cards)
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: List<LostFoundPost>,
    userName: String,
    myUid: String,
    feedError: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onUserMessageClick: (LostFoundPost) -> Unit,
    onLikeClick: (LostFoundPost) -> Unit,
    onCommentClick: (LostFoundPost) -> Unit,
    onDeletePost: (LostFoundPost) -> Unit,
    onAddPostClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Lost, Found, Given

    val lostCount = posts.count { it.status.equals("Lost", true) }
    val foundCount = posts.count { it.status.equals("Found", true) }
    val givenCount = posts.count { it.status.equals("Given", true) }

    val filteredPosts = remember(searchQuery, posts, selectedFilter) {
        posts.filter { post ->
            (selectedFilter == "All" || post.status.equals(selectedFilter, ignoreCase = true)) &&
                    (searchQuery.isBlank() ||
                            post.title.contains(searchQuery, ignoreCase = true) ||
                            post.description.contains(searchQuery, ignoreCase = true) ||
                            post.location.contains(searchQuery, ignoreCase = true) ||
                            post.username.contains(searchQuery, ignoreCase = true) ||
                            post.status.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.findlylogo),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(6.dp, CircleShape, spotColor = BrandPinkDeep)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Findly",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Hi, ${userName.split(" ").firstOrNull() ?: "there"} 👋",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(10.dp, CircleShape, spotColor = BrandPinkDeep)
                    .clip(CircleShape)
                    .background(BrandGradient)
                    .clickable(onClick = onAddPostClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Post", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search lost & found items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill("All", Icons.Default.Apps, MaterialTheme.colorScheme.primary, posts.size, selectedFilter) { selectedFilter = "All" }
                FilterPill("Lost", Icons.Default.SearchOff, statusColor("Lost"), lostCount, selectedFilter) { selectedFilter = "Lost" }
                FilterPill("Found", Icons.Default.CheckCircle, statusColor("Found"), foundCount, selectedFilter) { selectedFilter = "Found" }
                FilterPill("Given", Icons.Default.TaskAlt, statusColor("Given"), givenCount, selectedFilter) { selectedFilter = "Given" }
            }

            feedError?.let { ErrorBanner(it) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (filteredPosts.isEmpty()) {
                    item {
                        EmptyState(
                            icon = if (searchQuery.isBlank()) Icons.Default.AutoAwesome else Icons.Default.Search,
                            title = if (searchQuery.isBlank()) "Nothing here yet" else "No results",
                            subtitle = if (searchQuery.isBlank())
                                "Be the first to post a lost or found item. Tap the + button!"
                            else "Try a different keyword or switch the filter."
                        )
                    }
                } else {
                    items(filteredPosts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            myUid = myUid,
                            onMessageUser = onUserMessageClick,
                            onLikeClick = onLikeClick,
                            onCommentClick = onCommentClick,
                            onDeletePost = onDeletePost
                        )
                    }
                }
            }
        }
    }
}

// Version-safe FilterChip colors (works with all M3 versions)
@Composable
fun FilterPill(
    label: String,
    icon: ImageVector,
    color: Color,
    count: Int,
    selected: String,
    onSelect: () -> Unit
) {
    val isSelected = selected == label
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
        label = {
            Text(
                "$label ($count)",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = color,
            selectedContainerColor = color,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}

// ============================================================
