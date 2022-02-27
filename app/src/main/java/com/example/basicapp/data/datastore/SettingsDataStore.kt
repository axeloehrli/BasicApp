package com.example.basicapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "layout_preferences"
)

class SettingsDataStore(context: Context) {
    private val IS_LINEAR_LAYOUT_MANAGER = booleanPreferencesKey("is_linear_layout_manager")
    private val IS_BACKGROUND_BLACK = booleanPreferencesKey("is_background_black")

    val preferenceFlow: Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // On the first run of the app, we will use LinearLayoutManager by default

            preferences[IS_LINEAR_LAYOUT_MANAGER] ?: true

        }

    val backgroundFlow : Flow<Boolean> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            preferences ->
            preferences[IS_BACKGROUND_BLACK] ?: false
        }



    suspend fun saveLayoutToPreferencesStore(isLinearLayoutManager: Boolean,context: Context) {
        context.dataStore.edit { preferences ->
            preferences[IS_LINEAR_LAYOUT_MANAGER] = isLinearLayoutManager
        }
    }

    suspend fun saveBackgroundToPreferencesStore(isBackgroundBlack: Boolean,context: Context) {
        context.dataStore.edit { preferences ->
            preferences[IS_BACKGROUND_BLACK] = isBackgroundBlack
        }
    }
}