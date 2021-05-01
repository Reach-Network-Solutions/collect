package app.nexusforms.android.application.initialization;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import app.nexusforms.android.TestSettingsProvider;
import app.nexusforms.android.application.initialization.migration.SharedPreferenceUtils;
import app.nexusforms.android.preferences.source.Settings;

import static java.util.Arrays.asList;

@RunWith(RobolectricTestRunner.class)
public class CollectSettingsPreferenceMigratorTest {

    private final Settings generalSettings = TestSettingsProvider.getGeneralSettings();
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();
    private final Settings metaSettings = TestSettingsProvider.getMetaSettings();

    @Before
    public void setUp() throws Exception {
        generalSettings.clear();
        adminSettings.clear();
        metaSettings.clear();
    }

    @Test
    public void shouldMigrateGoogleMapSettings() {
        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "streets");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_NORMAL));

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "satellite");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_SATELLITE));

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "terrain\u200e");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_TERRAIN));

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "hybrid");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_HYBRID));
    }

    @Test
    public void shouldMigrateMapboxMapSettings() {
        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_streets");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.MAPBOX_STREETS);

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_light");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.LIGHT);

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_dark");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.DARK);

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE);

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite_streets");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE_STREETS);

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_outdoors");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "mapbox", "mapbox_map_style", Style.OUTDOORS);
    }

    @Test
    public void shouldMigrateOsmMapSettings() {
        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_streets");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "osm");

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_topo");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "usgs", "usgs_map_style", "topographic");

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_sat");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "usgs", "usgs_map_style", "hybrid");

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_img");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "usgs", "usgs_map_style", "satellite");

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_stamen_terrain");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "stamen");

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_positron");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "carto", "carto_map_style", "positron");

        SharedPreferenceUtils.initPrefs(generalSettings, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_darkmatter");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "basemap_source", "carto", "carto_map_style", "dark_matter");
    }

    @Test
    public void shouldMigrateAdminSettings() {
        SharedPreferenceUtils.initPrefs(adminSettings, "unrelated", "value");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(adminSettings, "unrelated", "value");

        SharedPreferenceUtils.initPrefs(adminSettings, "show_map_sdk", true);
        runMigrations();
        SharedPreferenceUtils.assertPrefs(adminSettings, "show_map_sdk", true);

        SharedPreferenceUtils.initPrefs(adminSettings, "show_map_sdk", false);
        runMigrations();
        SharedPreferenceUtils.assertPrefs(adminSettings, "maps", false);

        SharedPreferenceUtils.initPrefs(adminSettings, "show_map_basemap", true);
        runMigrations();
        SharedPreferenceUtils.assertPrefs(adminSettings, "show_map_basemap", true);

        SharedPreferenceUtils.initPrefs(adminSettings, "show_map_basemap", false);
        runMigrations();
        SharedPreferenceUtils.assertPrefs(adminSettings, "maps", false);
    }

    @Test
    public void migratesMetaKeysToMetaPrefs() {
        SharedPreferenceUtils.initPrefs(generalSettings,
                "firstRun", true,
                "lastVersion", 1L,
                "scoped_storage_used", true,
                "metadata_migrated", true,
                "mapbox_initialized", true
        );

        runMigrations();

        SharedPreferenceUtils.assertPrefsEmpty(generalSettings);
        SharedPreferenceUtils.assertPrefs(metaSettings,
                "scoped_storage_used", true,
                "mapbox_initialized", true
        );
    }

    @Test
    public void migratesServerType() {
        SharedPreferenceUtils.initPrefs(generalSettings, "protocol", "other_protocol");
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings, "protocol", "odk_default");
    }

    @Test
    public void migratesAutosendSettings() {
        SharedPreferenceUtils.initPrefs(generalSettings,
                "autosend_wifi", false,
                "autosend_network", false
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "autosend", "off"
        );

        SharedPreferenceUtils.initPrefs(generalSettings,
                "autosend_wifi", true,
                "autosend_network", false
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "autosend", "wifi_only"
        );

        SharedPreferenceUtils.initPrefs(generalSettings,
                "autosend_wifi", false,
                "autosend_network", true
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "autosend", "cellular_only"
        );

        SharedPreferenceUtils.initPrefs(generalSettings,
                "autosend_wifi", true,
                "autosend_network", true
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "autosend", "wifi_and_cellular"
        );
    }

    @Test
    public void migratesFormUpdateModeSettings() {
        SharedPreferenceUtils.initPrefs(generalSettings,
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );

        List<String> periods = asList("every_fifteen_minutes", "every_one_hour", "every_six_hours", "every_24_hours");
        for (String period : periods) {
            SharedPreferenceUtils.initPrefs(generalSettings,
                    "periodic_form_updates_check", period
            );
            runMigrations();
            SharedPreferenceUtils.assertPrefs(generalSettings,
                    "periodic_form_updates_check", period,
                    "form_update_mode", "previously_downloaded"
            );
        }

        SharedPreferenceUtils.initPrefs(generalSettings,
                "protocol", "google_sheets"
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual"
        );

        SharedPreferenceUtils.initPrefs(generalSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "every_24_hours"
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_24_hours"
        );

        SharedPreferenceUtils.initPrefs(generalSettings,
                "protocol", "google_sheets",
                "periodic_form_updates_check", "never"
        );
        runMigrations();
        SharedPreferenceUtils.assertPrefs(generalSettings,
                "protocol", "google_sheets",
                "form_update_mode", "manual",
                "periodic_form_updates_check", "every_fifteen_minutes"
        );
    }

    @Test
    public void migratesServerList() {
        SharedPreferenceUtils.initPrefs(generalSettings,
                "knownUrlList", "[\"http://blah.com\"]"
        );

        runMigrations();
        SharedPreferenceUtils.assertPrefsEmpty(generalSettings);
        SharedPreferenceUtils.assertPrefs(metaSettings,
                "server_list", "[\"http://blah.com\"]"
        );
    }

    private void runMigrations() {
        new CollectSettingsPreferenceMigrator(metaSettings).migrate(generalSettings, adminSettings);
    }
}
