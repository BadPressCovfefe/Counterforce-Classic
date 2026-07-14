package com.apps.fast.launch.components;

import android.content.Context;
import android.content.SharedPreferences;

import com.apps.fast.launch.activities.MainActivity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import launch.game.Defs;

public class ClientDefs
{
    public static final String SETTINGS = "CounterforceSettings";     //The sharedpreferences file name for settings and settings folder name.

    //Debugging.
    public static final int NOTIFICATION_DEBUG_LOG_SIZE = 500;
    public static final String DEBUG_INDEX = "dbg_index";
    public static final String DEBUG_PREFIX = "dbg_";

    //Major settings.
    public static final String SETTINGS_SERVER_URL = "ServerAddress";
    public static final String SETTINGS_SERVER_PORT = "ServerPort";
    public static final String SETTINGS_NOTIFICATION_MINUTES = "NotificationMinutes";
    public static final String SETTINGS_NOTIFICATION_DEBUG = "NotifDebug";
    public static final String SETTINGS_SHORT_UNITS = "ShortUnits";
    public static final String SETTINGS_LONG_UNITS = "LongUnits";
    public static final String SETTINGS_SPEEDS = "Speeds";
    public static final String SETTINGS_USE_LBS = "UseLbs";
    public static final String SETTINGS_CURRENCY = "CurrencyUnit";
    public static final String SETTINGS_DISCLAIMER_ACCEPTED = "DisclaimerAccepted00001"; /* Increment trailing digits as required to force a new acceptance. */
    public static final String SETTINGS_IDENTITY_STORED = "StoredIdentity0"; //To prevent IMEI spoofing.
    public static final String SETTINGS_IDENTITY_GENERATED = "IdentityGenerated"; //Device ID has been generated.
    public static final String SETTINGS_GOOGLE_ID_STORED = "StoredGoogleID0";
    public static final String SETTINGS_DISABLE_AUDIO = "DisableAudio";
    public static final String SETTINGS_SHOW_HINTS = "ShowHints";
    public static final String SETTINGS_BLAST_RADII_CLICKABLE = "BlastRadiiClickable";
    public static final String SETTINGS_DYNAMIC_MARKERS = "DynamicShipMarkers";
    public static final String SETTINGS_INITIAL_ZOOM = "InitialZoom";
    public static final String SETTINGS_CLUSTERING = "Clustering";
    public static final String SETTINGS_THEME = "Theme";
    public static final String SETTINGS_MAP_SATELLITE = "Satellite";
    public static final String SETTINGS_ZOOM_LEVEL = "ZoomLevel";
    public static final String SETTINGS_MAX_ZOOM = "MaxZoom";
    public static final String SETTINGS_TICK_RATE = "TickRate";
    public static final String HAS_MIGRATED_TO_GOOGLE = "HasMigratedToGoogle";
    public static final int DEFAULT_TICK_RATE = 33;

    //Visibility overrides.
    public static final String SETTINGS_VISIBILITY_OVERRIDES = "VisibilityOverrides";

    //Defaults.

    /* Live */
    public static final String SETTINGS_SERVER_URL_DEFAULT = "career.CounterforceGame.com";
    public static int GetDefaultServerPort() { return 30070; }

    /* Debug */
    /*public static final String SETTINGS_SERVER_URL_DEFAULT = "71.72.100.217";
    public static int GetDefaultServerPort() { return 30069; }*/

    public static final int SETTINGS_NOTIFICATION_MINUTES_DEFAULT = 15;
    public static final boolean SETTINGS_NOTIFICATION_DEBUG_DEFAULT = false;
    public static final boolean SETTINGS_USE_LBS_DEFAULT = false;
    public static final int SETTINGS_UNITS_DEFAULT = 0;
    public static final String SETTINGS_CURRENCY_DEFAULT = "£";
    public static final boolean SETTINGS_DISCLAIMER_ACCEPTED_DEFAULT = false;
    public static final boolean SETTINGS_SHOW_HINTS_DEFAULT = true;
    public static final boolean SETTINGS_BLAST_RADII_CLICKABLE_DEFAULT = false;
    public static final boolean SETTINGS_DISABLE_AUDIO_DEFAULT = false;
    public static final boolean SETTINGS_INITIAL_ZOOM_DEFAULT = true;
    public static final boolean SETTINGS_DYNAMIC_MARKERS_DEFAULT = true;
    public static final int SETTINGS_CLUSTERING_DEFAULT = 8;
    public static final int SETTINGS_THEME_DEFAULT = 0;
    public static final boolean SETTINGS_MAP_SATELLITE_DEFAULT = false;
    public static final float SETTINGS_ZOOM_LEVEL_DEFAULT = 15.0f;
    public static final float SETTINGS_MAX_ZOOM_DEFAULT = 16.0f;

    //Filenames etc.
    public static final String CONFIG_FILENAME = "config";
    public static final String PRIVACY_FILENAME = "privacy";

    public static final String AVATAR_FOLDER = "avatars";
    public static final String IMGASSETS_FOLDER = "imgassets";
    public static final String IMAGE_FORMAT = ".png";

    //Themes.
    public static final int THEME_LAUNCH = 0;
    public static final int THEME_BORING = 1;
    public static final int THEME_CONTAMINATED = 2;
    public static final int THEME_CLASSIC = 3;

    public static final String[] Themes = new String[]
    {
        "Counterforce",
        "Boring",
        "Contamination",
        "Classic"
    };

    //External applications and links
    public static final String DISCORD_URL = "https://discord.gg/KK4q5nHcHn";
    public static final String WIKI_URL = "http://www.counterforcegame.com";
    public static final String PLAY_STORE_URL = "market://details?id=com.apps.fast.counterforceclassic";

    //The rest.
    public static final int NEAREST_ENTITY_COUNT = 12;

    public static final float PRIVACY_ZONE_DEFAULT_RADIUS = 100.0f;
    public static final int PRIVACY_ZONE_MAX_RADIUS = 1000;

    public static final float TRACK_THRESHOLD = 0.01f;  //Auto track players with player-tracking missiles if selection distance is less than this.

    public static final long EVENT_MAIN_SCREEN_PERSISTENCE = 7000; //Show latest events for up to 30 seconds. Made it 5 seconds instead of 30. -Corbin

    public static final int ACTIVITY_REQUEST_CODE_AVATAR_IMAGE = 0;
    public static final int ACTIVITY_REQUEST_CODE_AVATAR_CAMERA = 1;
    public static final int RC_SIGN_IN = 9001;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public static final int DEFAULT_AVATAR_ID = Defs.THE_GREAT_BIG_NOTHING;

    public static int CLUSTERING_SIZE = 8; //Read/write.

    /**
     * The maximum number of types to be considered for SAM/Missile site map range ring thicknesses.
     */
    public static final int MAX_RANGE_RING_THICKNESS = 10;

    public static void StoreMoreVolatileSettings(MainActivity activity)
    {
        SharedPreferences.Editor editor = activity.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();

        editor.putBoolean(SETTINGS_MAP_SATELLITE, activity.GetMapSatellite());

        editor.commit();
    }
}
