package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "rhajesh_match_progress")

class ProgressRepository(private val context: Context) {
    private object Keys {
        val XP = intPreferencesKey("xp")
        val STARS = intPreferencesKey("stars")
        val COMPLETED_KEYS = stringSetPreferencesKey("completed_keys")
    }

    val progressFlow: Flow<UserProgress> = context.progressDataStore.data.map { prefs ->
        UserProgress(
            xp = prefs[Keys.XP] ?: 0,
            stars = prefs[Keys.STARS] ?: 0,
            completedKeys = prefs[Keys.COMPLETED_KEYS] ?: emptySet()
        )
    }

    suspend fun recordCorrectAnswer(bankKey: String?): Boolean {
        var awarded = false
        context.progressDataStore.edit { prefs ->
            val currentCompleted = prefs[Keys.COMPLETED_KEYS] ?: emptySet()
            val shouldAward = bankKey == null || !currentCompleted.contains(bankKey)
            if (shouldAward) {
                prefs[Keys.XP] = (prefs[Keys.XP] ?: 0) + 100
                prefs[Keys.STARS] = (prefs[Keys.STARS] ?: 0) + 1
                if (bankKey != null) {
                    prefs[Keys.COMPLETED_KEYS] = currentCompleted + bankKey
                }
                awarded = true
            }
        }
        return awarded
    }

    suspend fun resetProgress() {
        context.progressDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
