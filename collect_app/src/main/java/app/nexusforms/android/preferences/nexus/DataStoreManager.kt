package app.nexusforms.android.preferences.nexus

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(context: Context) {

    private val settingsDataStore = context.dataStore

    suspend fun saveLaunchState(alreadyLaunched: Boolean) {
        settingsDataStore.edit { preference ->
            preference[PREF_LAUNCH_STATE] = alreadyLaunched
        }
    }

    val isFirstLaunch = settingsDataStore.data.map { preference ->
        preference[PREF_LAUNCH_STATE]
    }


    companion object {
        val PREF_LAUNCH_STATE = booleanPreferencesKey("is-first-launch")
    }

}