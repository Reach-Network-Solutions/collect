package app.nexusforms.android.utilities;


import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.source.Settings;

public class AdminPasswordProvider {
    private final Settings adminSettings;

    public AdminPasswordProvider(Settings adminSettings) {
        this.adminSettings = adminSettings;
    }

    public boolean isAdminPasswordSet() {
        String adminPassword = getAdminPassword();
        return adminPassword != null && !adminPassword.isEmpty();
    }

    public String getAdminPassword() {
        return adminSettings.getString(AdminKeys.KEY_ADMIN_PW);
    }
}
