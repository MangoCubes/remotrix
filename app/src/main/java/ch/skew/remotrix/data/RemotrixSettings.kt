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
        val managementSpaceId = stringPreferencesKey("managementSpaceId")
        val openedBefore = stringPreferencesKey("openedBefore")
        val defaultSend = stringPreferencesKey("defaultSend")
    }

    val getManagerId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[managerId] ?: ""
    }

    val getManagementSpaceId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[managementSpaceId]
    }

    val getOpenedBefore: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[openedBefore] == "1"
    }

    val getDefaultSend: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[defaultSend]?.toInt()
    }

    suspend fun saveManagerId(name: String) {
        context.dataStore.edit { preferences ->
            preferences[managerId] = name
        }
    }

    suspend fun saveManagementSpaceId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id === null) preferences.remove(managementSpaceId)
            else preferences[managementSpaceId] = id
        }
    }
    suspend fun saveOpenedBefore() {
        context.dataStore.edit { preferences ->
            preferences[openedBefore] = "1"
        }
    }

    suspend fun saveDefaultSend(id: Int?){
        context.dataStore.edit { preferences ->
             if(id === null) preferences.remove(defaultSend)
             else preferences[defaultSend] = id.toString()
        }
    }
}