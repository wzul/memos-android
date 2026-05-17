package com.example.memos.data.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val INSTANCE_URL = stringPreferencesKey("instance_url")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val CURRENT_USER = stringPreferencesKey("current_user")
    }

    val instanceUrl: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.INSTANCE_URL] }

    val accessToken: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.ACCESS_TOKEN] }

    val currentUserJson: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[Keys.CURRENT_USER] }

    suspend fun saveCredentials(instanceUrl: String, accessToken: String) {
        dataStore.edit {
            it[Keys.INSTANCE_URL] = instanceUrl.removeSuffix("/")
            it[Keys.ACCESS_TOKEN] = accessToken
        }
    }

    suspend fun saveCurrentUser(userJson: String) {
        dataStore.edit { it[Keys.CURRENT_USER] = userJson }
    }

    suspend fun clearCredentials() {
        dataStore.edit {
            it.remove(Keys.INSTANCE_URL)
            it.remove(Keys.ACCESS_TOKEN)
            it.remove(Keys.CURRENT_USER)
        }
    }
}
