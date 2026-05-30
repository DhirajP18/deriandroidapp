package com.example.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.DashboardAll
import com.example.data.api.Dairy
import com.example.data.api.NetworkClient
import com.example.data.local.DataStoreManager
import com.example.data.local.UserSession
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardAll) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = NetworkClient.getDataStore(application)
    private val apiService = NetworkClient.getApiService(application)

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    val sessionFlow = dataStore.sessionFlow
    val languageFlow = dataStore.languageFlow
    val themeFlow = dataStore.themeFlow

    // SuperAdmin parameters
    private val _dairies = MutableStateFlow<List<Dairy>>(emptyList())
    val dairies: StateFlow<List<Dairy>> = _dairies

    val superSelectedDairy = dataStore.superSelectedDairyFlow

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val session = sessionFlow.firstOrNull() ?: return@launch
                
                // If superadmin, fetch extra dairies and filter stats
                if (session.isSuperAdmin) {
                    val dairiesResponse = apiService.getAllDairies()
                    if (dairiesResponse.isSuccessful && dairiesResponse.body() != null) {
                        _dairies.value = dairiesResponse.body()!!.finalData ?: emptyList()
                    }
                }

                val targetDairyId = if (session.isSuperAdmin) {
                    val selectedPair = superSelectedDairy.firstOrNull()
                    selectedPair?.first // Can be null (then overall stats)
                } else {
                    session.dairyId
                }

                val response = apiService.getDashboardAll(dairyId = targetDairyId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.finalSuccess && body.finalData != null) {
                        _uiState.value = DashboardUiState.Success(body.finalData!!)
                    } else {
                        // Fallback attempt: request individual items or set empty stats if API returned failed
                        _uiState.value = DashboardUiState.Error(body.finalMessage.ifEmpty { "Failed to retrieve dashboard details" })
                    }
                } else {
                    _uiState.value = DashboardUiState.Error("Server error dashboard lookup: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error("Offline or connection error. Presenting cached local dashboard.")
            }
        }
    }

    fun setSuperSelectedDairy(dairyId: Int, name: String) {
        viewModelScope.launch {
            dataStore.saveSuperSelectedDairy(dairyId, name)
            loadDashboardData()
        }
    }

    fun clearSuperSelectedDairy() {
        viewModelScope.launch {
            dataStore.clearSuperSelectedDairy()
            loadDashboardData()
        }
    }

    fun toggleLanguage(current: String) {
        viewModelScope.launch {
            val newLang = if (current == "English") "Marathi" else "English"
            dataStore.saveLanguage(newLang)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                apiService.logout()
            } catch (e: Exception) {
                // Ignore API logout failures during local wipe
            } finally {
                dataStore.clearSession()
            }
        }
    }
}
