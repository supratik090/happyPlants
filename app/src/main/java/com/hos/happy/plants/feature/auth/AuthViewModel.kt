package com.hos.happy.plants.feature.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hos.happy.plants.data.local.AppDatabase
import com.hos.happy.plants.data.local.entity.UserProfileEntity
import com.hos.happy.plants.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class AuthUiState(
    val activeProfile: UserProfileEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val profileDao = db.userProfileDao()
    private val prefs = UserPreferencesRepository(application)

    val allProfiles: StateFlow<List<UserProfileEntity>> = profileDao.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkPersistedSession()
    }

    private fun checkPersistedSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Initialize hardware light sensor check in preferences on startup
            prefs.getOrInitializeLightMeterSettings()

            // Check if there are profiles
            val profiles = profileDao.getAllProfiles()
            if (profiles.isNotEmpty()) {
                // Read last active profile if available
                prefs.activeProfileIdFlow.collect { activeId ->
                    val matched = profiles.find { it.id == activeId }
                    _uiState.value = _uiState.value.copy(
                        activeProfile = matched,
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun register(name: String, email: String, password: String, onComplete: () -> Unit) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val existing = profileDao.getProfileByEmail(email.trim().lowercase())
            if (existing != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "A profile with this email already exists"
                )
                return@launch
            }

            val passwordHash = hashPassword(password)
            val newProfile = UserProfileEntity(
                name = name.trim(),
                email = email.trim().lowercase(),
                passwordHash = passwordHash
            )

            profileDao.insertProfile(newProfile)
            prefs.setActiveProfileId(newProfile.id)
            _uiState.value = _uiState.value.copy(
                activeProfile = newProfile,
                isLoading = false,
                isLoginSuccessful = true
            )
            onComplete()
        }
    }

    fun loginWithProfile(profile: UserProfileEntity, passwordInput: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val inputHash = hashPassword(passwordInput)
            if (profile.passwordHash == inputHash) {
                profileDao.updateLastLogin(profile.id)
                prefs.setActiveProfileId(profile.id)
                _uiState.value = _uiState.value.copy(
                    activeProfile = profile,
                    errorMessage = null,
                    isLoginSuccessful = true
                )
                onComplete()
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Incorrect password")
            }
        }
    }

    fun switchProfile() {
        viewModelScope.launch {
            prefs.setActiveProfileId(null)
            _uiState.value = _uiState.value.copy(
                activeProfile = null,
                isLoginSuccessful = false,
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
