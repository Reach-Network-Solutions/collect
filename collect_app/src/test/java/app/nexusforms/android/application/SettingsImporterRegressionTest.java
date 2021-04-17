package app.nexusforms.android.application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.configure.SettingsImporter;
import app.nexusforms.android.injection.DaggerUtils;
import app.nexusforms.android.preferences.keys.AdminKeys;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;
import app.nexusforms.android.preferences.source.SettingsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class SettingsImporterRegressionTest {

    private SettingsImporter settingsImporter;
    private final SettingsProvider settingsProvider = TestSettingsProvider.getSettingsProvider();

    @Before
    public void setup() {
        settingsImporter = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext()).settingsImporter();
    }

    @Test
    public void cartoDarkMatter() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_cartodb_darkmatter\"},\"admin\":{}}");
        Settings prefs = settingsProvider.getGeneralSettings();
        MatcherAssert.assertThat(prefs.getString(GeneralKeys.KEY_BASEMAP_SOURCE), Matchers.is(GeneralKeys.BASEMAP_SOURCE_CARTO));
        assertThat(prefs.getString(GeneralKeys.KEY_CARTO_MAP_STYLE), is("dark_matter"));
    }

    @Test
    public void cartoPositron() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_cartodb_positron\"},\"admin\":{}}");
        Settings prefs = settingsProvider.getGeneralSettings();
        MatcherAssert.assertThat(prefs.getString(GeneralKeys.KEY_BASEMAP_SOURCE), Matchers.is(GeneralKeys.BASEMAP_SOURCE_CARTO));
        assertThat(prefs.getString(GeneralKeys.KEY_CARTO_MAP_STYLE), is("positron"));
    }

    @Test
    public void usgsHybrid() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"osmdroid\",\"map_basemap_behavior\":\"openmap_usgs_sat\"},\"admin\":{}}");
        Settings prefs = settingsProvider.getGeneralSettings();
        MatcherAssert.assertThat(prefs.getString(GeneralKeys.KEY_BASEMAP_SOURCE), Matchers.is(GeneralKeys.BASEMAP_SOURCE_USGS));
        assertThat(prefs.getString(GeneralKeys.KEY_USGS_MAP_STYLE), is("hybrid"));
    }

    @Test
    public void googleMapsSatellite() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"google_maps\",\"map_basemap_behavior\":\"satellite\"},\"admin\":{}}");
        Settings prefs = settingsProvider.getGeneralSettings();
        MatcherAssert.assertThat(prefs.getString(GeneralKeys.KEY_BASEMAP_SOURCE), Matchers.is(GeneralKeys.BASEMAP_SOURCE_GOOGLE));
        assertThat(prefs.getString(GeneralKeys.KEY_GOOGLE_MAP_STYLE), is(String.valueOf(GoogleMap.MAP_TYPE_SATELLITE)));
    }

    @Test
    public void mapboxLight() {
        settingsImporter.fromJSON("{\"general\":{\"map_sdk_behavior\":\"mapbox_maps\",\"map_basemap_behavior\":\"mapbox_light\"},\"admin\":{}}");
        Settings prefs = settingsProvider.getGeneralSettings();
        MatcherAssert.assertThat(prefs.getString(GeneralKeys.KEY_BASEMAP_SOURCE), Matchers.is(GeneralKeys.BASEMAP_SOURCE_MAPBOX));
        assertThat(prefs.getString(GeneralKeys.KEY_MAPBOX_MAP_STYLE), is(Style.LIGHT));
    }

    @Test
    public void adminPW() {
        settingsImporter.fromJSON("{\"general\":{\"periodic_form_updates_check\":\"every_fifteen_minutes\"},\"admin\":{\"admin_pw\":\"blah\"}}");
        assertThat(settingsProvider.getAdminSettings().getString(AdminKeys.KEY_ADMIN_PW), is("blah"));
    }
}
