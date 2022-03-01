package com.example.basicapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "layout_preferences"
)


class SettingsDataStore(context: Context) {
    private val SORT_BY = stringPreferencesKey("sort_order")

    val sortOrderFlow : Flow<String> = context.dataStore.data
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
            preferences[SORT_BY] ?: "sort_by_none"
        }


    suspend fun saveSortByToPreferences(sortOrder : String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[SORT_BY] = sortOrder
        }
    }
}