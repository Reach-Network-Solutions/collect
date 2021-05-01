package app.nexusforms.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.nexusforms.android.application.Collect
import app.nexusforms.android.injection.DaggerUtils
import app.nexusforms.android.preferences.source.Settings
import app.nexusforms.android.preferences.source.SettingsProvider
import app.nexusforms.android.preferences.source.SharedPreferencesSettings


// Use just for testing
object TestSettingsProvider {
    @JvmStatic
    fun getSettingsProvider(): SettingsProvider {
        return DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).preferencesRepository()
    }

    @JvmStatic
    fun getGeneralSettings(): Settings {
        return getSettingsProvider().getGeneralSettings()
    }

    @JvmStatic
    fun getAdminSettings(): Settings {
        return getSettingsProvider().getAdminSettings()
    }

    @JvmStatic
    fun getMetaSettings(): Settings {
        return getSettingsProvider().getMetaSettings()
    }

    @JvmStatic
    fun getTestSettings(name: String?): Settings {
        return SharedPreferencesSettings(ApplicationProvider.getApplicationContext<Collect>().getSharedPreferences(name, Context.MODE_PRIVATE))
    }
}
