package com.example

import android.content.Context
import android.util.Log
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class OtpSent(val verificationId: String, val phoneNumber: String) : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authManager: AuthManager = AuthManager.getInstance()
) : ViewModel() {

    private val TAG = "AuthViewModel"

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser

    private var currentVerificationId: String? = null
    private var currentPhoneNumber: String? = null

    init {
        Log.d(TAG, "AuthViewModel initialized - listening to Firebase Auth state")
    }

    fun onGoogleSignInClicked(context: Context) {
        Log.i(TAG, "Sign-in intent captured from UI -> Triggering Google Sign-In via Credential Manager")
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            Log.d(TAG, "Requesting AuthManager.signInWithGoogle with Context: ${context.javaClass.simpleName}")
            val result = authManager.signInWithGoogle(context)
            result.fold(
                onSuccess = { user ->
                    Log.i(TAG, "Google Sign-In SUCCESS captured in AuthViewModel for user UID: ${user.uid}, email: ${user.email}")
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = { error ->
                    val userFriendlyMessage = parseAuthError(error)
                    Log.e(TAG, "Google Sign-In ERROR captured in AuthViewModel: $userFriendlyMessage (raw: ${error.message})", error)
                    _uiState.value = AuthUiState.Error(userFriendlyMessage)
                }
            )
        }
    }

    fun onSendPhoneOtpClicked(phoneNumber: String, activity: Activity) {
        val formattedPhone = if (phoneNumber.startsWith("+")) phoneNumber.trim() else "+91${phoneNumber.trim()}"
        if (formattedPhone.length < 10) {
            _uiState.value = AuthUiState.Error("Please enter a valid phone number with area/country code.")
            return
        }

        _uiState.value = AuthUiState.Loading
        currentPhoneNumber = formattedPhone

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    val result = authManager.signInWithPhoneCredential(credential)
                    result.fold(
                        onSuccess = { user -> _uiState.value = AuthUiState.Success(user) },
                        onFailure = { err -> _uiState.value = AuthUiState.Error(parseAuthError(err)) }
                    )
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Phone Verification Failed: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Phone verification failed. Check phone format or internet.")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                currentVerificationId = verificationId
                _uiState.value = AuthUiState.OtpSent(verificationId, formattedPhone)
            }
        }

        authManager.sendPhoneVerificationCode(formattedPhone, activity, callbacks)
    }

    fun onVerifyPhoneOtpClicked(code: String) {
        val verificationId = currentVerificationId
        if (verificationId.isNullOrBlank()) {
            _uiState.value = AuthUiState.Error("OTP verification session expired. Please resend OTP.")
            return
        }
        if (code.isBlank() || code.length < 4) {
            _uiState.value = AuthUiState.Error("Please enter the 6-digit OTP code sent to your phone.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authManager.signInWithPhoneCode(verificationId, code.trim())
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(parseAuthError(error))
                }
            )
        }
    }

    fun resetPhoneAuth() {
        currentVerificationId = null
        currentPhoneNumber = null
        _uiState.value = AuthUiState.Idle
    }

    fun dismissError() {
        _uiState.value = AuthUiState.Idle
    }

    fun signOut() {
        Log.i(TAG, "Sign-out intent captured from UI -> Signing out user")
        authManager.signOut()
        _uiState.value = AuthUiState.Idle
    }

    private fun parseAuthError(e: Throwable): String {
        return when (e) {
            is FirebaseNetworkException -> "Network Error: Unable to reach Firebase servers. Please verify your internet connection."
            is FirebaseAuthInvalidCredentialsException -> "Authentication Failed: Invalid credentials or token expired. Ensure SHA-1 fingerprint is registered in Firebase Console."
            is FirebaseAuthInvalidUserException -> "Account Issue: User account disabled or removed."
            is FirebaseAuthUserCollisionException -> "Account Conflict: An account already exists with a different sign-in method."
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network Error: Check internet connection."
                    "ERROR_INVALID_CREDENTIAL" -> "Invalid Credential: Enable Google provider in Firebase Authentication settings."
                    "ERROR_USER_DISABLED" -> "This account has been disabled."
                    "ERROR_USER_NOT_FOUND" -> "No user account found matching these credentials."
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Try again later."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Google Sign-In is disabled in Firebase Console -> Authentication -> Sign-in method."
                    else -> e.localizedMessage ?: "Firebase Auth Error (${e.errorCode})."
                }
            }
            is GetCredentialCancellationException -> "Sign-in request was cancelled."
            is NoCredentialException -> "No Google Accounts on Device: Google Credential Manager requires a Google account added in Android Settings (Settings -> Accounts -> Google). Additionally, verify Firebase Console -> Authentication -> Google Sign-in is enabled and Web Client ID matches."
            is GetCredentialException -> {
                val rawMsg = e.localizedMessage ?: e.message ?: ""
                if (rawMsg.contains("No credentials available", ignoreCase = true)) {
                    "Google Sign-In Error: No active Google accounts found on this device/emulator. Add a Google account under Android Settings, or check that SHA-1 fingerprint and Google Sign-In provider are enabled in Firebase Console."
                } else {
                    "Google Credential Manager Error: $rawMsg"
                }
            }
            is IOException -> "Network Connection Error: Please verify internet access."
            else -> e.localizedMessage ?: e.message ?: "An unexpected authentication error occurred."
        }
    }
}

