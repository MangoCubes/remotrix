package ch.skew.remotrix.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun String.toOnSend(): RemotrixSettings.OnSend {
    return when (this) {
        "Thread" -> RemotrixSettings.OnSend.Thread
        "React" -> RemotrixSettings.OnSend.React
        "Reply" -> RemotrixSettings.OnSend.Reply
        else -> RemotrixSettings.OnSend.None
    }
}

fun onSendToString(onSend: RemotrixSettings.OnSend): String {
    return when (onSend) {
        RemotrixSettings.OnSend.Thread -> "Thread"
        RemotrixSettings.OnSend.React -> "React"
        RemotrixSettings.OnSend.Reply -> "Reply"
        else -> "None"
    }
}

class RemotrixSettings(
    private val context: Context
) {
    enum class OnSend {
        Thread,
        React,
        Reply,
        None
    }
    companion object {
        private val Context.dataStore by preferencesDataStore(name = "settings")
        val managerId = stringPreferencesKey("managerId")
        val managementSpaceId = stringPreferencesKey("managementSpaceId")
        val openedBefore = stringPreferencesKey("openedBefore")
        val defaultForwarder = stringPreferencesKey("defaultForwarder")
        val logging = stringPreferencesKey("logging")
        val enableOnBootMessage = stringPreferencesKey("enableOnBootMessage")
        val debugAlivePing = stringPreferencesKey("debugAlivePing")
        val onSendSuccess = stringPreferencesKey("onSendSuccess")
        val onSendFailure = stringPreferencesKey("onSendFailure")
        val onErrorDebugLog = stringPreferencesKey("onSendFailure")
    }

    val getDebugAlivePing: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[debugAlivePing] == "1"
    }

    val getOnSendSuccess: Flow<OnSend> = context.dataStore.data.map { preferences ->
        (preferences[onSendSuccess]?.toOnSend() ?: OnSend.None)
    }

    val getOnSendFailure: Flow<OnSend> = context.dataStore.data.map { preferences ->
        (preferences[onSendFailure]?.toOnSend() ?: OnSend.None)
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

    val getDefaultForwarder: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[defaultForwarder]?.toInt() ?: -1
    }

    val getLogging: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[logging] == "1"
    }

    val getEnableOnBootMessage: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[enableOnBootMessage] === null || preferences[enableOnBootMessage] != "0"
    }

    val getOnErrorDebugLog: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[onErrorDebugLog] == "1"
    }

    suspend fun saveOnSendSuccess(set: OnSend) {
        context.dataStore.edit { preferences ->
            preferences[onSendSuccess] = onSendToString(set)
        }
    }

    suspend fun saveOnSendFailure(set: OnSend) {
        context.dataStore.edit { preferences ->
            preferences[onSendFailure] = onSendToString(set)
        }
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

    suspend fun saveDefaultForwarder(id: Int?){
        context.dataStore.edit { preferences ->
             if(id === null) preferences.remove(defaultForwarder)
             else preferences[defaultForwarder] = id.toString()
        }
    }

    suspend fun saveLogging(set: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[logging] = if(set) "1" else "0"
        }
    }

    suspend fun saveEnableOnBootMessage(set: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[enableOnBootMessage] = if (set) "1" else "0"
        }
    }

    suspend fun saveDebugAlivePing(set: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[debugAlivePing] = if (set) "1" else "0"
        }
    }

    suspend fun saveOnErrorDebugLog(set: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[onErrorDebugLog] = if(set) "1" else "0"
        }
    }
}