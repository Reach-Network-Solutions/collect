package app.nexusforms.android.configure;

import android.content.Context;

import androidx.annotation.NonNull;

import app.nexusforms.android.preferences.FormUpdateMode;
import app.nexusforms.android.preferences.Protocol;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;

import app.nexusforms.android.preferences.source.Settings;

public class SettingsUtils {

    private SettingsUtils() {

    }

    @NonNull
    public static FormUpdateMode getFormUpdateMode(Context context, Settings generalSettings) {
        String protocol = generalSettings.getString(GeneralKeys.KEY_PROTOCOL);

        if (Protocol.parse(context, protocol) == Protocol.GOOGLE) {
            return FormUpdateMode.MANUAL;
        } else {
            String mode = generalSettings.getString(GeneralKeys.KEY_FORM_UPDATE_MODE);
            return FormUpdateMode.parse(context, mode);
        }
    }
}
