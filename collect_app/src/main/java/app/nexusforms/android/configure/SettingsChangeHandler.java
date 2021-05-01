package app.nexusforms.android.configure;

public interface SettingsChangeHandler {
    void onSettingChanged(String changedKey, Object newValue);
}
