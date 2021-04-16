package app.nexusforms.android.application.initialization;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import app.nexusforms.android.application.initialization.migration.KeyRenamer;
import app.nexusforms.android.application.initialization.migration.KeyTranslator;
import app.nexusforms.android.application.initialization.migration.Migration;
import app.nexusforms.android.application.initialization.migration.MigrationUtils;
import app.nexusforms.android.preferences.keys.GeneralKeys;
import app.nexusforms.android.preferences.source.Settings;

import app.nexusforms.android.preferences.source.Settings;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Migrates old preference keys and values to new ones.
 */
public class CollectSettingsPreferenceMigrator implements SettingsPreferenceMigrator {

    private final Settings metaPrefs;

    public CollectSettingsPreferenceMigrator(Settings metaPrefs) {
        this.metaPrefs = metaPrefs;
    }

    @Override
    public void migrate(Settings generalSettings, Settings adminSettings) {
        for (Migration migration : getGeneralMigrations()) {
            migration.apply(generalSettings);
        }

        for (Migration migration : getAdminMigrations()) {
            migration.apply(adminSettings);
        }

        for (Migration migration : getMetaMigrations()) {
            migration.apply(metaPrefs);
        }
    }

    private List<Migration> getGeneralMigrations() {
        return asList(
                MigrationUtils.translateKey("map_sdk_behavior").toKey(GeneralKeys.KEY_BASEMAP_SOURCE)
                        .fromValue("google_maps").toValue("google")
                        .fromValue("mapbox_maps").toValue("mapbox"),

                // ListPreferences can only handle string values, so we use string values here.
                // Note that unfortunately there was a hidden U+200E character in the preference
                // value for "terrain" in previous versions of ODK Collect, so we need to
                // include that character to match that value correctly.
                MigrationUtils.translateKey("map_basemap_behavior").toKey(GeneralKeys.KEY_GOOGLE_MAP_STYLE)
                        .fromValue("streets").toValue(Integer.toString(GoogleMap.MAP_TYPE_NORMAL))
                        .fromValue("terrain\u200e").toValue(Integer.toString(GoogleMap.MAP_TYPE_TERRAIN))
                        .fromValue("terrain").toValue(Integer.toString(GoogleMap.MAP_TYPE_TERRAIN))
                        .fromValue("hybrid").toValue(Integer.toString(GoogleMap.MAP_TYPE_HYBRID))
                        .fromValue("satellite").toValue(Integer.toString(GoogleMap.MAP_TYPE_SATELLITE)),

                MigrationUtils.translateKey("map_basemap_behavior").toKey(GeneralKeys.KEY_MAPBOX_MAP_STYLE)
                        .fromValue("mapbox_streets").toValue(Style.MAPBOX_STREETS)
                        .fromValue("mapbox_light").toValue(Style.LIGHT)
                        .fromValue("mapbox_dark").toValue(Style.DARK)
                        .fromValue("mapbox_satellite").toValue(Style.SATELLITE)
                        .fromValue("mapbox_satellite_streets").toValue(Style.SATELLITE_STREETS)
                        .fromValue("mapbox_outdoors").toValue(Style.OUTDOORS),

                // When the map_sdk_behavior is "osmdroid", we have to also examine the
                // map_basemap_behavior key to determine the new basemap source.
                MigrationUtils.combineKeys("map_sdk_behavior", "map_basemap_behavior")
                        .withValues("osmdroid", "openmap_streets")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_OSM)

                        .withValues("osmdroid", "openmap_usgs_topo")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_USGS, GeneralKeys.KEY_USGS_MAP_STYLE, "topographic")
                        .withValues("osmdroid", "openmap_usgs_sat")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_USGS, GeneralKeys.KEY_USGS_MAP_STYLE, "hybrid")
                        .withValues("osmdroid", "openmap_usgs_img")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_USGS, GeneralKeys.KEY_USGS_MAP_STYLE, "satellite")

                        .withValues("osmdroid", "openmap_stamen_terrain")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_STAMEN)

                        .withValues("osmdroid", "openmap_cartodb_positron")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_CARTO, GeneralKeys.KEY_CARTO_MAP_STYLE, "positron")
                        .withValues("osmdroid", "openmap_cartodb_darkmatter")
                        .toPairs(GeneralKeys.KEY_BASEMAP_SOURCE, GeneralKeys.BASEMAP_SOURCE_CARTO, GeneralKeys.KEY_CARTO_MAP_STYLE, "dark_matter"),

                MigrationUtils.translateValue("other_protocol").toValue("odk_default").forKey("protocol"),

                MigrationUtils.removeKey("firstRun"),
                MigrationUtils.removeKey("lastVersion"),
                MigrationUtils.moveKey("scoped_storage_used").toPreferences(metaPrefs),
                MigrationUtils.removeKey("metadata_migrated"),
                MigrationUtils.moveKey("mapbox_initialized").toPreferences(metaPrefs),

                MigrationUtils.combineKeys("autosend_wifi", "autosend_network")
                        .withValues(false, false).toPairs("autosend", "off")
                        .withValues(false, true).toPairs("autosend", "cellular_only")
                        .withValues(true, false).toPairs("autosend", "wifi_only")
                        .withValues(true, true).toPairs("autosend", "wifi_and_cellular"),

                MigrationUtils.extractNewKey("form_update_mode").fromKey("protocol")
                        .fromValue("google_sheets").toValue("manual"),

                MigrationUtils.extractNewKey("form_update_mode").fromKey("periodic_form_updates_check")
                        .fromValue("never").toValue("manual")
                        .fromValue("every_fifteen_minutes").toValue("previously_downloaded")
                        .fromValue("every_one_hour").toValue("previously_downloaded")
                        .fromValue("every_six_hours").toValue("previously_downloaded")
                        .fromValue("every_24_hours").toValue("previously_downloaded"),

                MigrationUtils.translateValue("never").toValue("every_fifteen_minutes").forKey("periodic_form_updates_check"),

                MigrationUtils.moveKey("knownUrlList").toPreferences(metaPrefs)
        );
    }

    public List<KeyRenamer> getMetaMigrations() {
        return asList(
                MigrationUtils.renameKey("firstRun").toKey("first_run"),
                MigrationUtils.renameKey("lastVersion").toKey("last_version"),

                MigrationUtils.renameKey("knownUrlList").toKey("server_list")
        );
    }

    public List<KeyTranslator> getAdminMigrations() {
        return asList(
                // When either the map SDK or the basemap selection were previously
                // hidden, we want to hide the entire Maps preference screen.
                MigrationUtils.translateKey("show_map_sdk").toKey("maps")
                        .fromValue(false).toValue(false),
                MigrationUtils.translateKey("show_map_basemap").toKey("maps")
                        .fromValue(false).toValue(false)
        );
    }
}
