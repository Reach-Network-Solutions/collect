package app.nexusforms.collect.android.utilities;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import app.nexusforms.android.configure.ServerRepository;
import app.nexusforms.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.source.SettingsProvider;

import app.nexusforms.android.utilities.ApplicationResetter;
import app.nexusforms.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import app.nexusforms.android.preferences.source.SettingsProvider;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ApplicationResetterTest {

    private final ServerRepository serverRepository = mock(ServerRepository.class);

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public ServerRepository providesServerRepository(Context context, SettingsProvider settingsProvider) {
                return serverRepository;
            }
        });
    }

    @Test
    public void resettingPreferences_alsoResetsServers() {
        ApplicationResetter applicationResetter = new ApplicationResetter();
        applicationResetter.reset(asList(ApplicationResetter.ResetAction.RESET_PREFERENCES));
        verify(serverRepository).clear();
    }
}