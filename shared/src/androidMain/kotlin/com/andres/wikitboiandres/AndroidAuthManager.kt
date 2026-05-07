package com.andres.wikitboiandres

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class AndroidAuthManager(private val context: Context) : AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            _currentUser.value = firebaseUser?.let {
                AuthUser(
                    uid = it.uid,
                    name = it.displayName,
                    email = it.email,
                    photoUrl = it.photoUrl?.toString()
                )
            }
            Log.d("AuthManager", "User status changed: ${firebaseUser?.email ?: "Logged out"}")
        }
    }

    override suspend fun signInWithGoogle() {
        Log.d("AuthManager", "Starting Google Sign-In...")
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("381449892791-1nkoiibk9qgcsbspdecnkiu2gdrb2nk1.apps.googleusercontent.com")
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            Log.d("AuthManager", "Credential received: ${credential.type}")

            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                Log.d("AuthManager", "Firebase Sign-In successful")
            } else {
                Log.e("AuthManager", "Received unexpected credential type: ${credential.type}")
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "Credential Manager error: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Unknown error during sign-in: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error inesperado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d("AuthManager", "Signed out successfully")
        } catch (e: Exception) {
            Log.e("AuthManager", "Error during sign out", e)
        }
    }
}
