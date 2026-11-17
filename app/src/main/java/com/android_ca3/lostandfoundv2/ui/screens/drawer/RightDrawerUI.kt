package com.android_ca3.lostandfoundv2.ui.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android_ca3.lostandfoundv2.data.model.*
import com.android_ca3.lostandfoundv2.ui.components.*

// 5. DRAWER (animated pink gradient header)
// ============================================================

@Composable
fun RightDrawerUI(
    user: UserData?,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(animatedBrandGradient(vertical = true))
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(5.dp)
                    ) {
                        GradientAvatar(user?.name ?: "G", 46.dp, MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = user?.name ?: "Guest User",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = user?.email ?: "Not logged in",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DrawerItem(icon = Icons.Default.Home, text = "Home", onClick = onHomeClick)
            DrawerItem(icon = Icons.Default.Person, text = "Profile", onClick = onProfileClick)

            Spacer(modifier = Modifier.weight(1f))

            DrawerItem(
                icon = Icons.Default.Logout,
                text = "Logout",
                tint = MaterialTheme.colorScheme.error,
                containerTint = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                onClick = onLogoutClick
            )
            Text(
                "Findly • v2.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    text: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = text, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(text, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
    }
}

// ============================================================
