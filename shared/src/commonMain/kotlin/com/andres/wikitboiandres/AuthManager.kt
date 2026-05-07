package com.andres.wikitboiandres

import kotlinx.coroutines.flow.StateFlow

interface AuthManager {
    val currentUser: StateFlow<AuthUser?>
    suspend fun signInWithGoogle()
    suspend fun signOut()
}
