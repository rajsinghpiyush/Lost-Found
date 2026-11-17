package com.android_ca3.lostandfoundv2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android_ca3.lostandfoundv2.data.model.*
import com.android_ca3.lostandfoundv2.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 3. VIEWMODELS (unchanged)
// ============================================================

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth: StateFlow<Boolean> = _isCheckingAuth.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    private var profileJob: Job? = null

    init {
        viewModelScope.launch {
            repo.authState().collect { firebaseUser ->
                _isCheckingAuth.value = false
                profileJob?.cancel()
                if (firebaseUser == null) {
                    _currentUser.value = null
                } else {
                    var ensured = false
                    profileJob = viewModelScope.launch {
                        repo.observeUserProfile(firebaseUser.uid).collect { profile ->
                            if (profile == null) {
                                if (!ensured) {
                                    ensured = true
                                    repo.ensureProfile(firebaseUser)
                                }
                            } else {
                                _currentUser.value = profile
                            }
                        }
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true; _authError.value = null
            repo.login(email.trim(), password).onFailure { _authError.value = friendlyError(it) }
            _isLoading.value = false
        }
    }

    fun signUp(user: UserData, password: String) {
        viewModelScope.launch {
            _isLoading.value = true; _authError.value = null
            repo.signUp(user, password).onFailure { _authError.value = friendlyError(it) }
            _isLoading.value = false
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _isLoading.value = true; _authError.value = null
            repo.sendPasswordReset(email.trim())
                .onSuccess { _infoMessage.value = "Reset email sent to ${email.trim()}" }
                .onFailure { _authError.value = friendlyError(it) }
            _isLoading.value = false
        }
    }

    fun logout() = repo.logout()

    fun clearMessages() { _authError.value = null; _infoMessage.value = null }

    private fun friendlyError(e: Throwable): String = when ((e as? FirebaseAuthException)?.errorCode) {
        "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
        "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
        "ERROR_USER_NOT_FOUND" -> "No account found with this email."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
        "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection."
        else -> e.message ?: "Something went wrong. Please try again."
    }
}

class FeedViewModel : ViewModel() {
    private val postRepo = PostRepository()

    private val _posts = MutableStateFlow<List<LostFoundPost>>(emptyList())
    val posts: StateFlow<List<LostFoundPost>> = _posts.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _feedError = MutableStateFlow<String?>(null)
    val feedError: StateFlow<String?> = _feedError.asStateFlow()

    private var feedJob: Job? = null

    init { startFeedListener() }

    private fun startFeedListener() {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            postRepo.observePosts().collect { result ->
                result
                    .onSuccess { _posts.value = it; _feedError.value = null }
                    .onFailure { _feedError.value = "Feed error: ${it.message}" }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            postRepo.fetchPostsOnce()
                .onSuccess { _posts.value = it; _feedError.value = null }
                .onFailure { _feedError.value = "Refresh failed: ${it.message}" }
            _isRefreshing.value = false
            startFeedListener()
        }
    }

    fun createPost(
        status: String, title: String, description: String,
        location: String, imageData: String, author: UserData?,
        onDone: (Boolean) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _isPosting.value = true
            val result = postRepo.createPost(
                LostFoundPost(
                    userId = uid,
                    username = author?.name ?: "Anonymous",
                    authorStudentId = author?.studentId ?: "",
                    authorRoll = author?.roll ?: "",
                    authorSemester = author?.semester ?: "",
                    authorBatch = author?.batch ?: "",
                    status = status, title = title,
                    description = description, location = location,
                    imageData = imageData
                )
            )
            _isPosting.value = false
            result
                .onSuccess { onDone(true) }
                .onFailure {
                    _feedError.value = "Could not save post: ${it.message}"
                    onDone(false)
                }
        }
    }

    fun deletePost(post: LostFoundPost) {
        val original = _posts.value
        _posts.value = original.filterNot { it.id == post.id }
        viewModelScope.launch {
            postRepo.deletePost(post.id).onFailure {
                _posts.value = original
                _feedError.value = "Could not delete post: ${it.message}"
            }
        }
    }

    fun markPostGiven(post: LostFoundPost, claimedByName: String) {
        if (claimedByName.isBlank()) return
        viewModelScope.launch {
            postRepo.markPostGiven(post.id, claimedByName)
                .onFailure { _feedError.value = "Could not update post: ${it.message}" }
        }
    }

    fun toggleLike(post: LostFoundPost, myUid: String) {
        if (myUid.isBlank()) return
        val currentlyLiked = post.likes[myUid] == true
        _posts.value = _posts.value.map {
            if (it.id == post.id) {
                val newLikes = it.likes.toMutableMap()
                if (currentlyLiked) newLikes.remove(myUid) else newLikes[myUid] = true
                it.copy(likes = newLikes)
            } else it
        }
        viewModelScope.launch { postRepo.toggleLike(post.id, myUid, currentlyLiked) }
    }
}

class MessagesViewModel : ViewModel() {
    private val chatRepo = ChatRepository()
    private val authRepo = AuthRepository()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val myUid: String get() = authRepo.currentUid() ?: ""

    private var convJob: Job? = null

    init { startListening() }

    private fun startListening() {
        val uid = authRepo.currentUid() ?: return
        convJob?.cancel()
        convJob = viewModelScope.launch {
            chatRepo.observeConversations(uid).collect { result ->
                result
                    .onSuccess { _conversations.value = it; _error.value = null }
                    .onFailure { _error.value = "Messages error: ${it.message}" }
            }
        }
    }

    fun retryIfNeeded() { if (_error.value != null) startListening() }
    fun retry() { _error.value = null; startListening() }

    fun startChatWith(
        otherUid: String, otherName: String, myName: String,
        onReady: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = authRepo.currentUid()
        if (uid == null) { onError("Not logged in."); return }
        if (otherUid.isBlank()) { onError("This post has no author info."); return }
        if (otherUid == uid) { onError("This is your own post. Use a second account to test chat."); return }
        viewModelScope.launch {
            chatRepo.getOrCreateChat(uid, myName, otherUid, otherName)
                .onSuccess { onReady(it, otherName) }
                .onFailure { onError("Could not open chat: ${it.message}") }
        }
    }

    fun markChatRead(chatId: String) {
        val uid = authRepo.currentUid() ?: return
        viewModelScope.launch { chatRepo.markChatRead(chatId, uid) }
    }
}

class ChatViewModel : ViewModel() {
    private val chatRepo = ChatRepository()
    private val authRepo = AuthRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    val myUid: String get() = authRepo.currentUid() ?: ""

    private var currentChatId: String? = null
    private var otherUserId: String = ""
    private var listenerJob: Job? = null
    private var lastAttachFailed = false

    fun initChat(chatId: String, otherUid: String) {
        if (currentChatId == chatId && !lastAttachFailed) return
        currentChatId = chatId
        otherUserId = otherUid
        _messages.value = emptyList()
        _sendError.value = null
        _loadError.value = null
        listenerJob?.cancel()
        listenerJob = viewModelScope.launch {
            chatRepo.observeMessages(chatId).collect { result ->
                result
                    .onSuccess {
                        _messages.value = it
                        lastAttachFailed = false
                        _loadError.value = null
                    }
                    .onFailure {
                        lastAttachFailed = true
                        _loadError.value = "Can't load messages: ${it.message}"
                    }
            }
        }
    }

    fun retryLoad() {
        val chatId = currentChatId ?: return
        initChat(chatId, otherUserId)
    }

    fun sendMessage(text: String) {
        val chatId = currentChatId ?: return
        val uid = authRepo.currentUid() ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            chatRepo.sendMessage(chatId, uid, otherUserId, text.trim())
                .onFailure { _sendError.value = "Message failed to send: ${it.message}" }
        }
    }

    fun clearError() { _sendError.value = null }
}

class CommentsViewModel : ViewModel() {
    private val postRepo = PostRepository()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentPostId: String? = null
    private var job: Job? = null

    fun initPost(postId: String) {
        if (currentPostId == postId) return
        currentPostId = postId
        job?.cancel()
        job = viewModelScope.launch {
            postRepo.observeComments(postId).collect { result ->
                result
                    .onSuccess { _comments.value = it; _error.value = null }
                    .onFailure { _error.value = "Comments error: ${it.message}" }
            }
        }
    }

    fun addComment(postId: String, text: String, author: UserData?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            postRepo.addComment(
                postId,
                Comment(authorId = uid, authorName = author?.name ?: "Anonymous", text = text.trim())
            ).onFailure { _error.value = "Could not comment: ${it.message}" }
        }
    }
}

// ============================================================
