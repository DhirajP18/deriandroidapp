package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "deriset_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val DAIRY_ID = intPreferencesKey("dairy_id")
        val DAIRY_NAME = stringPreferencesKey("dairy_name")
        val DAIRY_NAME_MR = stringPreferencesKey("dairy_name_mr")
        val ADDRESS = stringPreferencesKey("address")
        val FULL_NAME = stringPreferencesKey("full_name")
        val FULL_NAME_MR = stringPreferencesKey("full_name_mr")
        val IS_ADMIN = booleanPreferencesKey("is_admin")
        val IS_SUPER_ADMIN = booleanPreferencesKey("is_super_admin")
        val ROLE_ID = intPreferencesKey("role_id")
        val ROLE_NAME = stringPreferencesKey("role_name")
        val SIDEBAR_CACHE = stringPreferencesKey("sidebar_cache")
        val MY_WIDGETS = stringPreferencesKey("my_widgets")
        val TOKEN_EXPIRY = stringPreferencesKey("token_expiry")
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language") // "English" or "Marathi"
        val SELECTED_THEME = stringPreferencesKey("selected_theme") // "Light", "Dark", "System"
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")

        // SuperAdmin selected dairy cache
        val SUPER_SELECTED_DAIRY_ID = intPreferencesKey("super_selected_dairy_id")
        val SUPER_SELECTED_DAIRY_NAME = stringPreferencesKey("super_selected_dairy_name")
    }

    val sessionFlow: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        try {
            val token = prefs[ACCESS_TOKEN]
            if (token.isNullOrEmpty()) null
            else {
                UserSession(
                    accessToken = token,
                    dairyId = prefs[DAIRY_ID] ?: 0,
                    dairyName = prefs[DAIRY_NAME] ?: "",
                    dairyNameMr = prefs[DAIRY_NAME_MR] ?: "",
                    address = prefs[ADDRESS] ?: "",
                    fullName = prefs[FULL_NAME] ?: "",
                    fullNameMr = prefs[FULL_NAME_MR] ?: "",
                    isAdmin = prefs[IS_ADMIN] ?: false,
                    isSuperAdmin = prefs[IS_SUPER_ADMIN] ?: false,
                    roleId = prefs[ROLE_ID] ?: 0,
                    roleName = prefs[ROLE_NAME] ?: "",
                    sidebarJson = prefs[SIDEBAR_CACHE] ?: "[]",
                    myWidgetsJson = prefs[MY_WIDGETS] ?: "[]",
                    tokenExpiry = prefs[TOKEN_EXPIRY] ?: "",
                    userId = prefs[USER_ID] ?: 0,
                    userName = prefs[USER_NAME] ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        try {
            normalizeLanguage(prefs[SELECTED_LANGUAGE])
        } catch (e: Exception) {
            "English"
        }
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        try {
            normalizeTheme(prefs[SELECTED_THEME])
        } catch (e: Exception) {
            "System"
        }
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        try {
            prefs[IS_ONBOARDING_COMPLETED] ?: false
        } catch (e: Exception) {
            false
        }
    }

    val superSelectedDairyFlow: Flow<Pair<Int, String>?> = context.dataStore.data.map { prefs ->
        try {
            val id = prefs[SUPER_SELECTED_DAIRY_ID]
            val name = prefs[SUPER_SELECTED_DAIRY_NAME]
            if (id != null && name != null) {
                Pair(id, name)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveSession(session: UserSession) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = session.accessToken
            prefs[DAIRY_ID] = session.dairyId
            prefs[DAIRY_NAME] = session.dairyName
            prefs[DAIRY_NAME_MR] = session.dairyNameMr
            prefs[ADDRESS] = session.address
            prefs[FULL_NAME] = session.fullName
            prefs[FULL_NAME_MR] = session.fullNameMr
            prefs[IS_ADMIN] = session.isAdmin
            prefs[IS_SUPER_ADMIN] = session.isSuperAdmin
            prefs[ROLE_ID] = session.roleId
            prefs[ROLE_NAME] = session.roleName
            prefs[SIDEBAR_CACHE] = session.sidebarJson
            prefs[MY_WIDGETS] = session.myWidgetsJson
            prefs[TOKEN_EXPIRY] = session.tokenExpiry
            prefs[USER_ID] = session.userId
            prefs[USER_NAME] = session.userName
        }
    }

    suspend fun updateSidebarCache(sidebarJson: String) {
        context.dataStore.edit { prefs ->
            prefs[SIDEBAR_CACHE] = sidebarJson
        }
    }

    suspend fun updateMyWidgets(myWidgetsJson: String) {
        context.dataStore.edit { prefs ->
            prefs[MY_WIDGETS] = myWidgetsJson
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_LANGUAGE] = normalizeLanguage(language)
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_THEME] = normalizeTheme(theme)
        }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveSuperSelectedDairy(id: Int, name: String) {
        context.dataStore.edit { prefs ->
            prefs[SUPER_SELECTED_DAIRY_ID] = id
            prefs[SUPER_SELECTED_DAIRY_NAME] = name
        }
    }

    suspend fun clearSuperSelectedDairy() {
        context.dataStore.edit { prefs ->
            prefs.remove(SUPER_SELECTED_DAIRY_ID)
            prefs.remove(SUPER_SELECTED_DAIRY_NAME)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(DAIRY_ID)
            prefs.remove(DAIRY_NAME)
            prefs.remove(DAIRY_NAME_MR)
            prefs.remove(ADDRESS)
            prefs.remove(FULL_NAME)
            prefs.remove(FULL_NAME_MR)
            prefs.remove(IS_ADMIN)
            prefs.remove(IS_SUPER_ADMIN)
            prefs.remove(ROLE_ID)
            prefs.remove(ROLE_NAME)
            prefs.remove(SIDEBAR_CACHE)
            prefs.remove(MY_WIDGETS)
            prefs.remove(TOKEN_EXPIRY)
            prefs.remove(USER_ID)
            prefs.remove(USER_NAME)
            prefs.remove(SUPER_SELECTED_DAIRY_ID)
            prefs.remove(SUPER_SELECTED_DAIRY_NAME)
        }
    }

    private fun normalizeLanguage(language: String?): String {
        return when (language?.trim()?.lowercase()) {
            "mr", "marathi" -> "Marathi"
            "en", "english" -> "English"
            else -> "English"
        }
    }

    private fun normalizeTheme(theme: String?): String {
        return when (theme?.trim()?.lowercase()) {
            "dark" -> "Dark"
            "light" -> "Light"
            "system" -> "System"
            else -> "System"
        }
    }
}

data class UserSession(
    val accessToken: String,
    val dairyId: Int,
    val dairyName: String,
    val dairyNameMr: String,
    val address: String,
    val fullName: String,
    val fullNameMr: String,
    val isAdmin: Boolean,
    val isSuperAdmin: Boolean,
    val roleId: Int,
    val roleName: String,
    val sidebarJson: String,
    val myWidgetsJson: String,
    val tokenExpiry: String,
    val userId: Int,
    val userName: String
)
