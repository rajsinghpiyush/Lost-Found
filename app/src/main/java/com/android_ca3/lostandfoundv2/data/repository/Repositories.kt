package com.android_ca3.lostandfoundv2.data.repository

import com.android_ca3.lostandfoundv2.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// 2. REPOSITORIES (unchanged)
// ============================================================

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun currentUid(): String? = auth.currentUser?.uid

    fun authState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun observeUserProfile(uid: String): Flow<UserData?> = callbackFlow {
        val reg = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObject(UserData::class.java)?.copy(uid = uid))
            }
        awaitClose { reg.remove() }
    }

    suspend fun ensureProfile(user: FirebaseUser) {
        try {
            val ref = db.collection("users").document(user.uid)
            val snap = ref.get().await()
            if (!snap.exists()) {
                val fallback = UserData(
                    uid = user.uid,
                    name = user.displayName
                        ?: user.email?.substringBefore("@")
                        ?: "User",
                    email = user.email ?: ""
                )
                ref.set(fallback).await()
            }
        } catch (_: Exception) { }
    }

    suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signUp(user: UserData, password: String): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(user.email, password).await()
        val uid = result.user?.uid ?: error("User creation failed")
        db.collection("users").document(uid).set(user.copy(uid = uid)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun logout() = auth.signOut()
}

class PostRepository {
    private val db = FirebaseFirestore.getInstance()

    fun observePosts(): Flow<Result<List<LostFoundPost>>> = callbackFlow {
        val reg = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LostFoundPost::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(posts))
            }
        awaitClose { reg.remove() }
    }

    suspend fun fetchPostsOnce(): Result<List<LostFoundPost>> = try {
        val snap = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Result.success(snap.documents.mapNotNull {
            it.toObject(LostFoundPost::class.java)?.copy(id = it.id)
        })
    } catch (e: Exception) { Result.failure(e) }

    suspend fun createPost(post: LostFoundPost): Result<Unit> = try {
        db.collection("posts").add(post).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun deletePost(postId: String): Result<Unit> = try {
        db.collection("posts").document(postId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun markPostGiven(postId: String, claimedByName: String): Result<Unit> = try {
        db.collection("posts").document(postId).update(
            mapOf(
                "status" to "Given",
                "claimedByName" to claimedByName
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun toggleLike(postId: String, uid: String, currentlyLiked: Boolean): Result<Unit> = try {
        val update = if (currentlyLiked)
            mapOf("likes.$uid" to FieldValue.delete())
        else
            mapOf("likes.$uid" to true)
        db.collection("posts").document(postId).update(update).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun observeComments(postId: String): Flow<Result<List<Comment>>> = callbackFlow {
        val reg = db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(list))
            }
        awaitClose { reg.remove() }
    }

    suspend fun addComment(postId: String, comment: Comment): Result<Unit> = try {
        val postRef = db.collection("posts").document(postId)
        val batch = db.batch()
        batch.set(
            postRef.collection("comments").document(),
            mapOf(
                "authorId" to comment.authorId,
                "authorName" to comment.authorName,
                "text" to comment.text,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(postRef, "commentCount", FieldValue.increment(1))
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun chatIdFor(a: String, b: String) = if (a < b) "${a}_$b" else "${b}_$a"

    fun observeConversations(uid: String): Flow<Result<List<Conversation>>> = callbackFlow {
        val reg = db.collection("chats")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(list.sortedByDescending { it.lastMessageTime?.seconds ?: 0L }))
            }
        awaitClose { reg.remove() }
    }

    suspend fun getOrCreateChat(
        myUid: String, myName: String, otherUid: String, otherName: String
    ): Result<String> = try {
        val chatId = chatIdFor(myUid, otherUid)
        val ref = db.collection("chats").document(chatId)
        ref.set(
            mapOf(
                "participants" to listOf(myUid, otherUid),
                "participantNames" to mapOf(myUid to myName, otherUid to otherName)
            ),
            SetOptions.merge()
        ).await()
        Result.success(chatId)
    } catch (e: Exception) { Result.failure(e) }

    fun observeMessages(chatId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        val reg = db.collection("chats").document(chatId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val msgs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(msgs.sortedBy { it.createdAt?.seconds ?: 0L }))
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendMessage(chatId: String, senderId: String, receiverId: String, text: String): Result<Unit> = try {
        val chatRef = db.collection("chats").document(chatId)
        val batch = db.batch()
        batch.set(
            chatRef.collection("messages").document(),
            mapOf("senderId" to senderId, "text" to text, "createdAt" to FieldValue.serverTimestamp())
        )
        batch.update(
            chatRef,
            mapOf(
                "lastMessage" to text,
                "lastMessageTime" to FieldValue.serverTimestamp(),
                "unreadCounts.$receiverId" to FieldValue.increment(1)
            )
        )
        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun markChatRead(chatId: String, myUid: String) {
        try { db.collection("chats").document(chatId).update("unreadCounts.$myUid", 0).await() }
        catch (_: Exception) { }
    }
}

// ============================================================
