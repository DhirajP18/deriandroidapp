package com.example.presentation.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiJsonParser
import com.example.data.api.LoginRequest
import com.example.data.api.NetworkClient
import com.example.data.local.UserSession
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = NetworkClient.getDataStore(application)
    private val apiService = NetworkClient.getApiService(application)

    val languageFlow = dataStore.languageFlow
    val themeFlow = dataStore.themeFlow

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableSharedFlow<String>()
    val errorMsg: SharedFlow<String> = _errorMsg

    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess: SharedFlow<Unit> = _loginSuccess

    fun onUsernameChange(value: String) {
        _username.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun toggleLanguage(current: String) {
        viewModelScope.launch {
            val newLang = if (current.equals("Marathi", ignoreCase = true)) "English" else "Marathi"
            dataStore.saveLanguage(newLang)
        }
    }

    fun toggleTheme(current: String) {
        viewModelScope.launch {
            val nextTheme = when (current.trim().lowercase()) {
                "system" -> "Dark"
                "dark" -> "Light"
                else -> "System"
            }
            dataStore.saveTheme(nextTheme)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            dataStore.saveTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            dataStore.saveLanguage(language)
        }
    }

    fun toggleThemePreference() {
        viewModelScope.launch {
            val currentTheme = dataStore.themeFlow.firstOrNull() ?: "System"
            val nextTheme = when (currentTheme.trim().lowercase()) {
                "system" -> "Dark"
                "dark" -> "Light"
                else -> "System"
            }
            dataStore.saveTheme(nextTheme)
        }
    }

    fun performLogin() {
        val user = _username.value.trim()
        val pw = _password.value

        if (user.isEmpty() || pw.isEmpty()) {
            viewModelScope.launch {
                _errorMsg.emit("Please enter both Username and Password")
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.login(LoginRequest(userName = user, password = pw))
                if (response.isSuccessful && response.body() != null) {
                    val raw = response.body()!!.string()
                    val payload = ApiJsonParser.parseObject(raw)
                    val success = ApiJsonParser.readBoolean(payload, "Success", "success", "IsSuccess", "isSuccess") == true
                    if (!success) {
                        _errorMsg.emit(
                            ApiJsonParser.readString(payload, "Message", "message", "ResMsg", "resMsg")
                                ?: "Invalid credentials or unauthorized"
                        )
                        return@launch
                    }

                    val data = ApiJsonParser.readNestedObject(payload, "Data", "data", "Result", "result") ?: payload
                    val accessToken = ApiJsonParser.readString(data, "AccessToken", "accessToken")
                    if (accessToken.isNullOrBlank()) {
                        _errorMsg.emit("Login response is missing token data.")
                        return@launch
                    }

                    val sidebarJson = ApiJsonParser.readArray(data, "Sidebar", "sidebar").toString()
                    val widgetsJson = ApiJsonParser.readArray(data, "MyWidgets", "myWidgets").toString()

                    val userSession = UserSession(
                        accessToken = accessToken,
                        dairyId = ApiJsonParser.readInt(data, "DairyId", "dairyId") ?: 0,
                        dairyName = ApiJsonParser.readString(data, "DairyName", "dairyName") ?: "",
                        dairyNameMr = ApiJsonParser.readString(data, "DairyNameMr", "dairyNameMr") ?: "",
                        address = ApiJsonParser.readString(data, "Address", "address") ?: "",
                        fullName = ApiJsonParser.readString(data, "FullName", "fullName") ?: "",
                        fullNameMr = ApiJsonParser.readString(data, "FullNameMr", "fullNameMr") ?: "",
                        isAdmin = ApiJsonParser.readBoolean(data, "IsAdmin", "isAdmin") ?: false,
                        isSuperAdmin = ApiJsonParser.readBoolean(data, "IsSuperAdmin", "isSuperAdmin") ?: false,
                        roleId = ApiJsonParser.readInt(data, "RoleId", "roleId") ?: 0,
                        roleName = ApiJsonParser.readString(data, "RoleName", "roleName") ?: "",
                        sidebarJson = sidebarJson,
                        myWidgetsJson = widgetsJson,
                        tokenExpiry = ApiJsonParser.readString(data, "TokenExpiry", "tokenExpiry") ?: "",
                        userId = ApiJsonParser.readInt(data, "UserId", "userId") ?: 0,
                        userName = ApiJsonParser.readString(data, "UserName", "userName") ?: user
                    )

                    dataStore.saveSession(userSession)
                    _loginSuccess.emit(Unit)
                } else {
                    _errorMsg.emit("Network login failed: Code ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorDetails = e.localizedMessage ?: e.message ?: e.javaClass.simpleName
                _errorMsg.emit("Unable to connect to the server ($errorDetails). Please check your network connection.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
