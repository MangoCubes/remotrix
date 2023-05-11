package ch.skew.remotrix.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RemotrixSettings(
    private val context: Context
) {
    companion object {
        private val Context.dataStore by preferencesDataStore(name = "settings")
        val managerId = stringPreferencesKey("managerId")
    }

    val getId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[managerId] ?: ""
    }


    suspend fun saveId(name: String) {
        context.dataStore.edit { preferences ->
            preferences[managerId] = name
        }
    }
}