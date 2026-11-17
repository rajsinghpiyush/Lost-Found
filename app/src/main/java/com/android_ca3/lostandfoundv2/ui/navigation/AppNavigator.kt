package com.android_ca3.lostandfoundv2.ui.navigation

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android_ca3.lostandfoundv2.R
import com.android_ca3.lostandfoundv2.data.model.*
import com.android_ca3.lostandfoundv2.ui.components.*
import com.android_ca3.lostandfoundv2.ui.screens.auth.ForgotPasswordScreen
import com.android_ca3.lostandfoundv2.ui.screens.auth.LoginScreen
import com.android_ca3.lostandfoundv2.ui.screens.auth.SignUpScreen
import com.android_ca3.lostandfoundv2.ui.screens.chat.ChatScreen
import com.android_ca3.lostandfoundv2.ui.screens.chat.MessageScreen
import com.android_ca3.lostandfoundv2.ui.screens.comments.CommentsScreen
import com.android_ca3.lostandfoundv2.ui.screens.drawer.RightDrawerUI
import com.android_ca3.lostandfoundv2.ui.screens.home.HomeScreen
import com.android_ca3.lostandfoundv2.ui.screens.post.CreatePostScreen
import com.android_ca3.lostandfoundv2.ui.screens.profile.ProfileScreen
import com.android_ca3.lostandfoundv2.ui.viewmodel.AuthViewModel
import com.android_ca3.lostandfoundv2.ui.viewmodel.ChatViewModel
import com.android_ca3.lostandfoundv2.ui.viewmodel.CommentsViewModel
import com.android_ca3.lostandfoundv2.ui.viewmodel.FeedViewModel
import com.android_ca3.lostandfoundv2.ui.viewmodel.MessagesViewModel

@Composable
fun AppNavigator() {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isCheckingAuth by authViewModel.isCheckingAuth.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.Login) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var chatDestination by remember { mutableStateOf<ChatDestination?>(null) }
    var selectedPost by remember { mutableStateOf<LostFoundPost?>(null) }

    val context = LocalContext.current

    LaunchedEffect(currentUser?.uid) {
        chatDestination = null
        selectedPost = null
        currentScreen = if (currentUser != null) Screen.Home else Screen.Login
        isDrawerOpen = false
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isCheckingAuth) {
            // ---- Branded animated splash ----
            val pulse = rememberInfiniteTransition(label = "splashPulse")
            val logoScale by pulse.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "logoScale"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(animatedBrandGradient(vertical = true)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(logoScale)
                            .shadow(24.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.findlylogo),
                            contentDescription = "Findly",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(18.dp))
                    Text("Findly", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }
        } else {
            val user = currentUser
            if (user == null) {
                // ---------- AUTH SCREENS ----------
                Crossfade(targetState = currentScreen, animationSpec = tween(250), label = "auth") { screen ->
                    when (screen) {
                        Screen.SignUp -> SignUpScreen(
                            authViewModel = authViewModel,
                            onBackClick = { currentScreen = Screen.Login }
                        )
                        Screen.ForgotPassword -> ForgotPasswordScreen(
                            authViewModel = authViewModel,
                            onBackClick = { currentScreen = Screen.Login }
                        )
                        else -> LoginScreen(
                            authViewModel = authViewModel,
                            onSignUpClick = { currentScreen = Screen.SignUp },
                            onForgotPasswordClick = { currentScreen = Screen.ForgotPassword }
                        )
                    }
                }
            } else {
                // ---------- LOGGED-IN ----------
                val feedViewModel: FeedViewModel = viewModel(key = "feed_${user.uid}")
                val messagesViewModel: MessagesViewModel = viewModel(key = "messages_${user.uid}")
                val chatViewModel: ChatViewModel = viewModel(key = "chat_${user.uid}")
                val commentsViewModel: CommentsViewModel = viewModel(key = "comments_${user.uid}")

                val posts by feedViewModel.posts.collectAsState()
                val conversations by messagesViewModel.conversations.collectAsState()
                val feedError by feedViewModel.feedError.collectAsState()
                val isRefreshing by feedViewModel.isRefreshing.collectAsState()
                val totalUnread = conversations.sumOf { it.unreadFor(user.uid) }

                val myPosts = posts.filter { it.userId == user.uid }
                val showBottomBar = currentScreen == Screen.Home ||
                        currentScreen == Screen.Messages || currentScreen == Screen.Profile

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        Crossfade(targetState = currentScreen, animationSpec = tween(260), label = "screens") { screen ->
                            when (screen) {
                                Screen.Messages -> MessageScreen(
                                    viewModel = messagesViewModel,
                                    onBackClick = { currentScreen = Screen.Home },
                                    onOpenChat = { conversation ->
                                        messagesViewModel.markChatRead(conversation.id)
                                        chatDestination = ChatDestination(
                                            chatId = conversation.id,
                                            otherUserId = conversation.otherUserId(user.uid),
                                            otherUserName = conversation.otherUserName(user.uid)
                                        )
                                        currentScreen = Screen.Chat
                                    }
                                )
                                Screen.Chat -> ChatScreen(
                                    destination = chatDestination,
                                    viewModel = chatViewModel,
                                    onBackClick = { currentScreen = Screen.Messages }
                                )
                                Screen.CreatePost -> CreatePostScreen(
                                    viewModel = feedViewModel,
                                    currentUser = user,
                                    onBackClick = { currentScreen = Screen.Home },
                                    onPostCreated = { currentScreen = Screen.Home }
                                )
                                Screen.Comments -> CommentsScreen(
                                    post = selectedPost,
                                    viewModel = commentsViewModel,
                                    user = user,
                                    onBackClick = { currentScreen = Screen.Home }
                                )
                                Screen.Profile -> ProfileScreen(
                                    user = user,
                                    myPosts = myPosts,
                                    onMarkGiven = { post, claimName ->
                                        feedViewModel.markPostGiven(post, claimName)
                                    },
                                    onDeletePost = { post -> feedViewModel.deletePost(post) },
                                    onMenuClick = { isDrawerOpen = true },
                                    onBackClick = { currentScreen = Screen.Home }
                                )
                                else -> HomeScreen(
                                    posts = posts,
                                    userName = user.name,
                                    myUid = user.uid,
                                    feedError = feedError,
                                    isRefreshing = isRefreshing,
                                    onRefresh = { feedViewModel.refresh() },
                                    onUserMessageClick = { post ->
                                        messagesViewModel.startChatWith(
                                            otherUid = post.userId,
                                            otherName = post.username,
                                            myName = user.name,
                                            onReady = { chatId, otherName ->
                                                chatDestination = ChatDestination(chatId, post.userId, otherName)
                                                currentScreen = Screen.Chat
                                            },
                                            onError = { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    onLikeClick = { post -> feedViewModel.toggleLike(post, user.uid) },
                                    onCommentClick = { post ->
                                        selectedPost = post
                                        currentScreen = Screen.Comments
                                    },
                                    onDeletePost = { post -> feedViewModel.deletePost(post) },
                                    onAddPostClick = { currentScreen = Screen.CreatePost },
                                    onMenuClick = { isDrawerOpen = true }
                                )
                            }
                        }
                    }

                    // ---- Floating pill bottom navigation ----
                    AnimatedVisibility(
                        visible = showBottomBar,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        FindlyBottomBar(
                            current = currentScreen,
                            unreadCount = totalUnread,
                            onHome = { currentScreen = Screen.Home },
                            onMessages = { currentScreen = Screen.Messages },
                            onProfile = { currentScreen = Screen.Profile }
                        )
                    }
                }
            }
        }

        // ---------- Drawer scrim ----------
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { isDrawerOpen = false }
            )
        }

        // ---------- Drawer ----------
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            RightDrawerUI(
                user = currentUser,
                onHomeClick = { isDrawerOpen = false; currentScreen = Screen.Home },
                onProfileClick = { isDrawerOpen = false; currentScreen = Screen.Profile },
                onLogoutClick = { isDrawerOpen = false; authViewModel.logout() }
            )
        }
    }
}

// Floating pill bottom bar with animated selection
@Composable
fun FindlyBottomBar(
    current: Screen,
    unreadCount: Int,
    onHome: () -> Unit,
    onMessages: () -> Unit,
    onProfile: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Home, "Home", current == Screen.Home, onClick = onHome)
            BottomNavItem(Icons.Default.Email, "Chats", current == Screen.Messages, badge = unreadCount, onClick = onMessages)
            BottomNavItem(Icons.Default.Person, "Profile", current == Screen.Profile, onClick = onProfile)
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "navTint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "navScale"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(25.dp).scale(scale))
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = (-3).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(BrandPinkDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        badge.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(
            label,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ============================================================
