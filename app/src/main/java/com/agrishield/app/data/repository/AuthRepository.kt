package com.agrishield.app.data.repository

import com.agrishield.app.data.firebase.FirebaseAuthManager
import com.agrishield.app.data.firebase.FirestoreManager
import com.agrishield.app.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val authManager: FirebaseAuthManager,
    private val firestoreManager: FirestoreManager
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun signIn(email: String, pass: String): Result<User> {
        val authResult = authManager.signIn(email, pass)
        return if (authResult.isSuccess) {
            val firebaseUser = authResult.getOrNull()!!
            val profileResult = firestoreManager.getUserProfile(firebaseUser.uid)
            val user = profileResult.getOrNull() ?: User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                displayName = firebaseUser.displayName ?: "Farmer"
            )
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(authResult.exceptionOrNull() ?: Exception("Sign-in failed"))
        }
    }

    suspend fun signUp(
        email: String,
        pass: String,
        displayName: String,
        location: String,
        crop: String
    ): Result<User> {
        val authResult = authManager.signUp(email, pass)
        return if (authResult.isSuccess) {
            val firebaseUser = authResult.getOrNull()!!
            val newUser = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName.ifBlank { "Farmer" },
                location = location.ifBlank { "Tamil Nadu" },
                primaryCrop = crop.ifBlank { "Tomato" }
            )
            firestoreManager.saveUserProfile(newUser)
            _currentUser.value = newUser
            Result.success(newUser)
        } else {
            Result.failure(authResult.exceptionOrNull() ?: Exception("Registration failed"))
        }
    }

    fun signOut() {
        authManager.signOut()
        _currentUser.value = null
    }

    suspend fun refreshUserProfile() {
        val uid = authManager.currentUserId
        if (authManager.isUserLoggedIn) {
            val profileResult = firestoreManager.getUserProfile(uid)
            _currentUser.value = profileResult.getOrNull() ?: User(uid = uid)
        }
    }
}
