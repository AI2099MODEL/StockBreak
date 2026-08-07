package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthManager private constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    // Web client ID from google-services.json (client_type 3)
    val webClientId: String = "777263370542-6f3cqhlm2f7jdu0a6v1nr3f0kd1sllmf.apps.googleusercontent.com"

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        Log.d("AuthManager", "signInWithGoogle requested with context: ${context.javaClass.simpleName}")
        return try {
            val activity = context.findActivity() ?: run {
                Log.w("AuthManager", "context.findActivity() returned null, falling back to passed context")
                if (context is Activity) context else null
            }

            if (activity == null) {
                Log.e("AuthManager", "Activity context could not be resolved for CredentialManager")
                return Result.failure(Exception("Activity context is required for Credential Manager."))
            }

            Log.i("AuthManager", "Initializing CredentialManager for activity: ${activity.javaClass.simpleName}")
            val credentialManager = CredentialManager.create(activity)

            Log.d("AuthManager", "Building GetGoogleIdOption with serverClientId: $webClientId")
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.i("AuthManager", "Launching CredentialManager.getCredential prompt...")
            val result = credentialManager.getCredential(
                context = activity,
                request = request
            )

            Log.i("AuthManager", "CredentialManager returned credential of type: ${result.credential.type}")
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d("AuthManager", "Google ID Token received successfully. Authenticating with Firebase...")

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                
                val user = authResult.user
                if (user != null) {
                    Log.i("AuthManager", "Firebase Google Sign-In succeeded for UID: ${user.uid}")
                    Result.success(user)
                } else {
                    Log.e("AuthManager", "Firebase user is null after sign in with credential")
                    Result.failure(Exception("Firebase user sign in failed."))
                }
            } else {
                Log.e("AuthManager", "Received invalid/unexpected credential type: ${credential.type}")
                Result.failure(Exception("Invalid credential format received from Google Credential Manager."))
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "Credential Manager Error: [${e.javaClass.simpleName}] ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In Exception: [${e.javaClass.simpleName}] ${e.message}", e)
            Result.failure(e)
        }
    }

    fun sendPhoneVerificationCode(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhoneCode(verificationId: String, code: String): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Phone sign-in failed."))
        } catch (e: Exception) {
            Log.e("AuthManager", "Phone OTP Sign-In failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Phone sign-in failed."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("Anonymous login failed."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("AuthManager", "Sign out error: ${e.message}", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(): AuthManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthManager()
                INSTANCE = instance
                instance
            }
        }
    }
}
