package com.apps.fast.launch.activities;

import static com.apps.fast.launch.components.ClientDefs.LOCATION_PERMISSION_REQUEST_CODE;
import static launch.utilities.LaunchLog.LogType.APPLICATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.UI.StructureIconBitmaps;
import com.apps.fast.launch.UI.map.LaunchClusterItem;
import com.apps.fast.launch.UI.map.SelectableMapFragment;
import com.apps.fast.launch.components.BlastEffect;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.Locatifier;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.TutorialController;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.BannedView;
import com.apps.fast.launch.launchviews.BottomBuildStructure;
import com.apps.fast.launch.launchviews.BottomElectronicWarfare;
import com.apps.fast.launch.launchviews.BottomInterceptorTarget;
import com.apps.fast.launch.launchviews.BottomMissileTarget;
import com.apps.fast.launch.launchviews.BottomMoveOrder;
import com.apps.fast.launch.launchviews.BottomNewHomebase;
import com.apps.fast.launch.launchviews.BottomRangeFinder;
import com.apps.fast.launch.launchviews.BottomTorpedoTarget;
import com.apps.fast.launch.launchviews.BottomTransferAccount;
import com.apps.fast.launch.launchviews.BottomUnitCommand;
import com.apps.fast.launch.launchviews.BuildViewNew;
import com.apps.fast.launch.launchviews.CreateAllianceView;
import com.apps.fast.launch.launchviews.DiplomacyActionView;
import com.apps.fast.launch.launchviews.DiplomacyView;
import com.apps.fast.launch.launchviews.DisclaimerView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.MainNormalView;
import com.apps.fast.launch.launchviews.MapClickView;
import com.apps.fast.launch.launchviews.MapSelectView;
import com.apps.fast.launch.launchviews.NoIMEIView;
import com.apps.fast.launch.launchviews.PermissionsView;
import com.apps.fast.launch.launchviews.PlayersView;
import com.apps.fast.launch.launchviews.PrivacyZonesView;
import com.apps.fast.launch.launchviews.RegisterView;
import com.apps.fast.launch.launchviews.ReportsView;
import com.apps.fast.launch.launchviews.SelectInterceptorView;
import com.apps.fast.launch.launchviews.SelectMissileView;
import com.apps.fast.launch.launchviews.QuickSelectActionView;
import com.apps.fast.launch.launchviews.SettingsView;
import com.apps.fast.launch.launchviews.SplashView;
import com.apps.fast.launch.launchviews.UploadAvatarView;
import com.apps.fast.launch.launchviews.WarningsView;
import com.apps.fast.launch.launchviews.controls.AllianceControl;
import com.apps.fast.launch.launchviews.entities.ABMSiteView;
import com.apps.fast.launch.launchviews.entities.AirbaseView;
import com.apps.fast.launch.launchviews.entities.AirplaneView;
import com.apps.fast.launch.launchviews.entities.ArmoryView;
import com.apps.fast.launch.launchviews.entities.CommandPostView;
import com.apps.fast.launch.launchviews.entities.DotncarryMemorialView;
import com.apps.fast.launch.launchviews.entities.InterceptorView;
import com.apps.fast.launch.launchviews.entities.ShipView;
import com.apps.fast.launch.launchviews.entities.ShipyardView;
import com.apps.fast.launch.launchviews.entities.SubmarineView;
import com.apps.fast.launch.launchviews.entities.Thomas99MemorialView;
import com.apps.fast.launch.launchviews.entities.TorpedoView;
import com.apps.fast.launch.launchviews.entities.WarehouseView;
import com.apps.fast.launch.launchviews.entities.LootView;
import com.apps.fast.launch.launchviews.entities.RubbleView;
import com.apps.fast.launch.launchviews.entities.MissileSiteView;
import com.apps.fast.launch.launchviews.entities.MissileView;
import com.apps.fast.launch.launchviews.entities.NukeSiteView;
import com.apps.fast.launch.launchviews.entities.PlayerView;
import com.apps.fast.launch.launchviews.entities.SAMSiteView;
import com.apps.fast.launch.launchviews.entities.SentryGunView;
import com.apps.fast.launch.launchviews.entities.TankView;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import launch.comm.clienttasks.Task.TaskMessage;
import launch.game.Alliance;
import launch.game.Config;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientAppInterface;
import launch.game.LaunchClientGame;
import launch.game.User;
import launch.game.entities.*;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.*;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.EntityPointer.EntityType;
import launch.game.systems.CargoSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.systems.MissileSystem;
import launch.game.treaties.Treaty;
import launch.game.types.*;
import launch.utilities.LaunchClientLocation;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchLog;
import launch.utilities.LaunchReport;
import launch.utilities.LaunchUtilities;
import launch.utilities.MissileStats;
import launch.utilities.PrivacyZone;
import launch.utilities.Security;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnGroundOverlayClickListener, GoogleMap.OnMapClickListener, ClusterManager.OnClusterClickListener, ClusterManager.OnClusterItemClickListener, GoogleMap.OnMapLongClickListener, LaunchClientAppInterface, SelectableMapFragment.SelectableMapListener, GoogleMap.OnCircleClickListener
{
    private static final String LOG_NAME = "MainActivity";
    private static final int PLAYER_MAP_ICON_SIZE = 64;
    private static boolean bRunning = false;    //For notification service.
    private String strGoogleID = null;
    private GoogleSignInClient mGoogleSignInClient;

    public enum InteractionMode
    {
        MAP_UPDATE,             //Google Play Services update required screen.
        PERMISSIONS,            //Permissions screen. Checking and waiting for approval.
        DISCLAIMER,             //Disclaimer screen. No comms yet.
        IDENTITY_WARNING,       //Invalid device identity. No comms yet either.
        SPLASH,                 //Splash screen, communicating with server.
        STANDARD,               //Standard game display.
        DIPLOMACY,              //Diplomacy screens.
        PRIVACY_ZONES,          //Editing privacy zones.
        TARGET_MISSILE,         //Assigning a missile target.
        TARGET_INTERCEPTOR,     //Specifying a missile to shoot down.
        TARGET_TORPEDO,         //Assigning a torpedo target.
        REGISTRATION,           //Player needs to register a username to start.
        CANT_LOG_IN,            //Version is invalid or user is banned.
        UNIT_COMMAND,           //Give a mobile unit a command.
        MOVE_ORDER,             //Assign a position for a unit to move to.
        RANGE_FINDER,           //Find the distance between two points.
        SELECT_NEW_HOMEBASE,    //Change an aircraft's homebase.
        PLACE_BLUEPRINT,        //Place a blueprint on the map.
        BUILD_STRUCTURE,        //Build a structure remotely using a command post.
        SET_TARGET,             //Assign a target to an entity.
        SEEK_FUEL,              //Select a tanker for refueling.
        PROVIDE_FUEL,           //Select a naval vessel or aircraft to provide fuel to.
        CAPTURE_TARGET,         //Select a target to capture with infantry.
        LIBERATE_TARGET,         //Select a target to capture with infantry.
        TURN_INFANTRY,          //Turn infantry to face a new direction.
        ELECTRONIC_WARFARE,     //Select a target structure or ship to disable it with electronic warfare.
        LOAD_LOOT,              //Select a loot object to pick up.
        TRANSFER_CARGO,         //Select a unit to transfer cargo to.
        SET_ARTILLERY_TARGET,   //Set a target for artillery auto-fire.
        TRANSFER_ACCOUNT,       //Transfer a player's account..
    }

    private InteractionMode interactionMode = InteractionMode.MAP_UPDATE;

    public enum ReportsStatus
    {
        NONE,   //No reports have occurred in the marker_player's absense.
        MINOR,  //Reports have occurred, but none that reference the marker_player.
        MAJOR   //Reports have occurred, including one(s) that refence(s) the marker_player.
    }

    private ReportsStatus reportStatus = ReportsStatus.NONE;

    private LaunchClientGame game;
    private Locatifier locatifier = null;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private UploadAvatarView pendingAvatarView;
    private FrameLayout lytMain;
    private LaunchView CurrentView = null;
    private long uiUpdateAccumulator = 0;
    private LaunchDialog commsDialog;
    private static final double CHUNK_SIZE_DEG = 0.25;
    private Map<MapChunkKey, List<Marker>> MarkerChunks = new ConcurrentHashMap<>();
    private Map<MapChunkKey, List<GroundOverlay>> OverlayChunks = new ConcurrentHashMap<>();
    private Map<MapChunkKey, List<EntityPointer>> ChunkEntities = new ConcurrentHashMap<>();
    private Map<MapChunkKey, Integer> ChunkZoomLevelGenerated = new ConcurrentHashMap<>(); // highest zoom level at which chunk was generated

    private MapEntity selectedEntity = null;
    private LatLng selectedLocation = null;
    private PrivacyZone selectedPrivacyZone = null;
    public List<MapEntity> multiSelectedEntities = new ArrayList<>();
    private boolean bMapIsSatellite = false;
    private boolean bMapModeZoom = true;
    public boolean bShowBlastRadii = true;
    public boolean bShowMissileTrails = true;
    private boolean bShowFirstLocation = true;
    private boolean bRebuildMap = false;

    private Map<Marker, PrivacyZone> PrivacyZoneMarkers = new ConcurrentHashMap<>();
    public Map<Integer, Polyline> MissileTrails = new ConcurrentHashMap<>();
    private Map<Integer, Polyline> TorpedoTrajectories = new ConcurrentHashMap<>();
    private Map<Integer, Polyline> InterceptorTrajectories = new ConcurrentHashMap<>();
    private List<Polyline> MovableTrajectories = new ArrayList<>();
    private Map<EntityPointer, Polyline> MassCommandTrajectories = new ConcurrentHashMap<>();
    private List<Marker> MoveMarkers = new ArrayList<>();
    private List<Polyline> PolyLines = new ArrayList<>();
    private Map<Integer, Circle> RadiationMarkers = new ConcurrentHashMap<>();
    private Map<Integer, Circle> BlastRadii = new ConcurrentHashMap<>();
    private Map<Integer, Circle> EMPMarkers = new ConcurrentHashMap<>();
    private Map<Integer, Circle> ABMEMPMarkers = new ConcurrentHashMap<>();
    private Map<Integer, Marker> ThreatMarkers = new ConcurrentHashMap<>();
    private List<BlastEffect> BlastEffects = new ArrayList<>();
    private Map<Integer, Marker> DepositMarkers = new ConcurrentHashMap<>();
    private Map<EntityType, Map<Integer, Marker>> AllMarkers = new ConcurrentHashMap<>();
    private Map<EntityType, Map<Integer, GroundOverlay>> AllOverlays = new ConcurrentHashMap<>();
    private Map<Integer, Marker> InterceptorMarkers = new ConcurrentHashMap<>();
    private Map<Integer, Marker> TorpedoMarkers = new ConcurrentHashMap<>();
    private List<Circle> SelectedEntityCircles = new ArrayList<>();
    public float fltZoomLevel = 1;
    private static final float ZOOMED_IN_THRESHOLD = 10;
    private static final float CITY_RENDER_THRESHOLD = 5;
    private float fltZoomToLevel;
    private boolean bVariableTickRate = true;
    private Marker DotncarryMemorial;
    private Marker Thomas99Memorial;

    //---------------------------------------------------------------------------------------------------------------------------------
    // Targeting related variables.
    //---------------------------------------------------------------------------------------------------------------------------------

    private EntityPointer targeter;
    private EntityType blueprintType;
    private ResourceType blueprintResourceType;
    private boolean bUseSubstitutes;
    private AirplaneInterface homebaseChangeAircraft;
    private Player transferAccountFromPlayer;
    private Polyline movableTrajectory;
    private Marker movableTarget;
    private Polyline targetTrajectory;
    private List<Circle> targetBlastRadii = new ArrayList<>();
    private List<Circle> CircleOverlays = new ArrayList<>();
    private List<Circle> multiCircleOverlays = new ArrayList<>();
    private List<Marker> ContactBearings = new ArrayList<>();
    private Circle targetRange;
    //private boolean bPlayerTargetting;      //The targetting system is attached to the marker_player (as opposed to a structure).
    private int lTargettingSiteID;          //The site ID of whatever is targeting, if not attached to the marker_player.
    private int lTargettingSlotNo;         //The slot number of the selected missile or marker_interceptor that is being used for targetting.
    private Polyline selectionRect;
    private SystemType targetSystemType;
    private GeoCoord geoTargetingOrigin;
    private GeoCoord geoTarget;
    public boolean bAirburst;
    private MissileType targetingMissileType;
    private InterceptorType targetingInterceptorType;
    private TorpedoType targetingTorpedoType;
    private List<Circle> AirspaceOverlays = new ArrayList<>();
    private static final float TRAJECTORY_LINE_WIDTH = 0.5f;
    private static final float TRAIL_LINE_WIDTH = 2.0f;
    private static final float INTERCEPTOR_TRAJ_WIDTH = 1.0f;
    private static final float TORPEDO_TRAJ_WIDTH = 1.0f;
    private static final int BLAST_RADII_STAGES = 10;

    //---------------------------------------------------------------------------------------------------------------------------------
    // Rangefinding related variables.
    //---------------------------------------------------------------------------------------------------------------------------------

    private GeoCoord geoRangeFind1;

    //---------------------------------------------------------------------------------------------------------------------------------
    // Cargo transfer related variables.
    //---------------------------------------------------------------------------------------------------------------------------------

    private LaunchEntity cargoTransferEntity;
    private Haulable cargoToTransfer;
    private boolean bFromCargo;

    //---------------------------------------------------------------------------------------------------------------------------------
    // Movable-entity control related variables.
    //---------------------------------------------------------------------------------------------------------------------------------

    private EntityPointer movableEntity;
    private List<EntityPointer> MovableEntities;

    //Self reference for views started in threads.
    private final MainActivity me = this;

    //Map reference.
    private GoogleMap map;
    private GeoJsonLayer layer = null;

    /** The maximum level the player can zoom to. Max is 21, minimum is 0. 0 is a larger view. **/
    private float fltMaxZoom;

    //Tutorial.
    private TutorialController tutorial = new TutorialController();

    //If the game is experiencing a slow-down, this will pace the UI activity by preventing stacking UI activity.
    private boolean bRendering = false;

    //Maximum distance to render ores, for optimisation.
    private float fltLootMaxDistance = 100.0f;

    private float p = 360 / 360;
    private float d = 0;

    Random random = new Random();

    private Task<String> tokenTask;
    private String strFirebaseToken;
    private int lMapTheme;

    int screenHeight;
    int screenWidth;

    //Player stuff visibility. To control the amount of Google maps-drawn stuff.
    //private Map<Integer, Boolean> PlayerVisibilities = new ConcurrentHashMap();
    //private Map<Integer, Boolean> CustomVisibilities = new ConcurrentHashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        //Utilities.StartHackilyLoggingExceptions();

        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        bShowFirstLocation = sharedPreferences.getBoolean(ClientDefs.SETTINGS_INITIAL_ZOOM, ClientDefs.SETTINGS_INITIAL_ZOOM_DEFAULT);

        switch (sharedPreferences.getInt(ClientDefs.SETTINGS_THEME, ClientDefs.SETTINGS_THEME_DEFAULT))
        {
            case ClientDefs.THEME_LAUNCH:
            {
                setTheme(R.style.LaunchTheme);
                lMapTheme = ClientDefs.THEME_LAUNCH;
            }
            break;

            case ClientDefs.THEME_BORING:
            {
                setTheme(R.style.AppTheme);
                lMapTheme = ClientDefs.THEME_BORING;
            }
            break;

            case ClientDefs.THEME_CONTAMINATED:
            {
                setTheme(R.style.ContaminatedTheme);
                lMapTheme = ClientDefs.THEME_CONTAMINATED;
            }
            break;

            case ClientDefs.THEME_CLASSIC:
            {
                setTheme(R.style.ClassicTheme);
                lMapTheme = ClientDefs.THEME_CLASSIC;
            }
        }

        //VisibleLatLng = map.getProjection().getVisibleRegion().latLngBounds;

        fltZoomToLevel = sharedPreferences.getFloat(ClientDefs.SETTINGS_ZOOM_LEVEL, ClientDefs.SETTINGS_ZOOM_LEVEL_DEFAULT);

        fltMaxZoom = sharedPreferences.getFloat(ClientDefs.SETTINGS_MAX_ZOOM, ClientDefs.SETTINGS_MAX_ZOOM_DEFAULT);

        ClientDefs.CLUSTERING_SIZE = sharedPreferences.getInt(ClientDefs.SETTINGS_CLUSTERING, ClientDefs.SETTINGS_CLUSTERING_DEFAULT);

        super.onCreate(savedInstanceState);

        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.SESSION, true);
        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.COMMS, true);
        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.APPLICATION, true);
        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.GAME, true);
        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.TASKS, true);
        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.SERVICES, true);
        LaunchLog.SetConsoleLoggingEnabled(LaunchLog.LogType.LOCATIONS, true);

        TextUtilities.Initialise(this);
        Sounds.Init(this);

        bRunning = true;

        //Get and set main UI elements.
        setContentView(R.layout.activity_main);
        lytMain = findViewById(R.id.lytMain);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            getWindow().setDecorFitsSystemWindows(false);

            // Hide system bars
            final WindowInsetsController insetsController = getWindow().getInsetsController();

            if(insetsController != null)
            {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());

                // Keep them hidden until user swipes
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        //Load config.
        File fileDir = getDir(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        File file = new File(fileDir, ClientDefs.CONFIG_FILENAME);

        Config config = null;

        if(file.exists())
        {
            RandomAccessFile rafConfig = null;

            try
            {
                rafConfig = new RandomAccessFile(file, "r");
                byte[] cConfig = new byte[(int) rafConfig.length()];
                rafConfig.read(cConfig);

                config = new Config(cConfig);
            }
            catch (Exception ex)
            { /* Don't care; if the stored one is broken or outdated we'll simply download another automatically. */ }
            finally
            {
                if (rafConfig != null)
                {
                    try
                    {
                        rafConfig.close();
                    }
                    catch (Exception ex)
                    { /* Don't care. */ }
                }
            }
        }

        //Load privacy zones.
        List<PrivacyZone> PrivacyZones = new ArrayList<>();

        file = new File(fileDir, ClientDefs.PRIVACY_FILENAME);

        if(file.exists())
        {
            try
            {
                RandomAccessFile rafConfig = new RandomAccessFile(file, "r");
                byte[] cConfig = new byte[(int) rafConfig.length()];
                rafConfig.read(cConfig);

                ByteBuffer bb = ByteBuffer.wrap(cConfig);

                while (bb.hasRemaining())
                {
                    PrivacyZones.add(new PrivacyZone(bb));
                }

                rafConfig.close();
            }
            catch (Exception ex)
            {

            }
        }

        //Load visibility overrides.
        //TODO: Player visibility
        /*for(String strOverride : sharedPreferences.getStringSet(ClientDefs.SETTINGS_VISIBILITY_OVERRIDES, new HashSet<String>()))
        {
            PlayerVisibilities.put(Integer.parseInt(strOverride), true);
            CustomVisibilities.put(Integer.parseInt(strOverride), true);
        }*/

        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->
                {
                    if(pendingAvatarView != null && uri != null)
                    {
                        pendingAvatarView.HandlePickedImage(uri);
                    }

                    pendingAvatarView = null;
                }
        );

        //Fire up the workings.
        String strURL = sharedPreferences.getString(ClientDefs.SETTINGS_SERVER_URL, ClientDefs.SETTINGS_SERVER_URL_DEFAULT);
        int lPort = sharedPreferences.getInt(ClientDefs.SETTINGS_SERVER_PORT, ClientDefs.GetDefaultServerPort());

        //Save the default port now that we've read it (it's random first time).
        SharedPreferences.Editor editor = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putInt(ClientDefs.SETTINGS_SERVER_PORT, lPort);
        editor.commit();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Launch sign-in intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, ClientDefs.RC_SIGN_IN);

        int lTickRate = sharedPreferences.getInt(ClientDefs.SETTINGS_TICK_RATE, ClientDefs.DEFAULT_TICK_RATE);

        game = new LaunchClientGame(config, this, PrivacyZones, strURL, lPort, Defs.SERVER_TICK_RATE/*lTickRate*/);

        locatifier = new Locatifier(this, game, this);

        CheckMap();

        ReturnToMainView();
    }

    /**
     * Call when persistent game settings change, and the main activity should refetch them.
     */
    public void SettingsChanged()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        fltZoomToLevel = sharedPreferences.getFloat(ClientDefs.SETTINGS_ZOOM_LEVEL, ClientDefs.SETTINGS_ZOOM_LEVEL_DEFAULT);
    }

    /**
     * A recursive directory delete implementation.
     * @param file File reference of directory to delete.
     */
    private void DeleteEntireDirectoryRecursively(File file)
    {
        if (file.isDirectory())
        {
            File[] entries = file.listFiles();
            if (entries != null)
            {
                for(File entry : entries)
                {
                    DeleteEntireDirectoryRecursively(entry);
                }
            }
        }
        else
            file.delete();
    }

    /**
     * Purge all avatars from the client. Used to manage changes to the avatar feature so that any redundancies can be brutally wiped.
     */
    public void PurgeAvatars()
    {
        try
        {
            File fileDir = getDir(ClientDefs.AVATAR_FOLDER, Context.MODE_PRIVATE);
            DeleteEntireDirectoryRecursively(fileDir);
        }
        catch(Exception ex) { /* Don't care. We tried. */ }
    }

    /**
     * Purge all avatars, assets and the config file from the client. SharedPreferences are not affected.
     */
    public void PurgeClient()
    {
        try
        {
            File fileDir = getDir(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
            DeleteEntireDirectoryRecursively(fileDir);
        }
        catch(Exception ex) { /* Don't care. We tried. */ }

        try
        {
            File fileDir = getDir(ClientDefs.IMGASSETS_FOLDER, Context.MODE_PRIVATE);
            DeleteEntireDirectoryRecursively(fileDir);
        }
        catch(Exception ex) { /* Don't care. We tried. */ }

        PurgeAvatars();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setMinZoomPreference(2);
        map.setMaxZoomPreference(fltMaxZoom);

        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        switch(sharedPreferences.getInt(ClientDefs.SETTINGS_THEME, ClientDefs.SETTINGS_THEME_DEFAULT))
        {
            case ClientDefs.THEME_LAUNCH:
            {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstylelaunch));
            }
            break;

            case ClientDefs.THEME_BORING:
            {
                /* Leave default map theme */
            }
            break;

            case ClientDefs.THEME_CONTAMINATED:
            {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstylecontaminated));
            }
            break;

            case ClientDefs.THEME_CLASSIC:
            {
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyleclassic));
            }
            break;
        }

        SetMapSatellite(sharedPreferences.getBoolean(ClientDefs.SETTINGS_MAP_SATELLITE, ClientDefs.SETTINGS_MAP_SATELLITE_DEFAULT));

        /*try
        {
            layer = new GeoJsonLayer(googleMap, R.raw.ne_50m_coastline, getApplicationContext());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }

        for(GeoJsonFeature feature : layer.getFeatures())
        {
            GeoJsonLineStringStyle lineStyle = new GeoJsonLineStringStyle();
            lineStyle.setColor(Color.parseColor("#82ff82"));
            lineStyle.setWidth(2f);

            feature.setLineStringStyle(lineStyle);
        }*/

        googleMap.setMyLocationEnabled(false);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnCircleClickListener(this);
        googleMap.setOnGroundOverlayClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener()
        {
            @Override
            public void onCameraIdle()
            {
                HighLevelUIRefresh();
            }
        });

        //layer.addLayerToMap();
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem)
    {
        LaunchClusterItem launchClusterItem = (LaunchClusterItem)clusterItem;
        EntityClicked(launchClusterItem.GetEntity());
        return false;
    }

    @Override
    public boolean onClusterClick(Cluster cluster)
    {
        //TO DO: Show cluster information.

        return false;
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        locatifier.Suspend();
        game.Suspend();
        ClientDefs.StoreMoreVolatileSettings(this);

        //Save custom player visibilities.
        HashSet<String> VisibilityOverridesSet = new HashSet<>();

        //TODO: Player visibility
        /*for(Map.Entry<Integer, Boolean> entry : CustomVisibilities.entrySet())
        {
            if(entry.getValue())
                VisibilityOverridesSet.add(Integer.toString(entry.getKey()));
        }*/

        SharedPreferences.Editor editor = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putStringSet(ClientDefs.SETTINGS_VISIBILITY_OVERRIDES, VisibilityOverridesSet);
        editor.commit();

        bRunning = false;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        CheckAndRequestLocationPermission();

        bRunning = true;

        switch (interactionMode)
        {
            case MAP_UPDATE:
            {
                CheckMap();
            }

            case PERMISSIONS:
            case DISCLAIMER:
            case IDENTITY_WARNING:
            case CANT_LOG_IN:
            {
                /* Don't resume anything in these modes. */
            }
            break;

            default:
            {
                locatifier.Resume();
                game.Resume();
            }
        }
    }

    private void CheckMap()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int lApiAvailabilityResult = apiAvailability.isGooglePlayServicesAvailable(this);

        if (lApiAvailabilityResult == ConnectionResult.SUCCESS)
        {
            //Get and configure the map.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            ((SelectableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).SetListener(this);

            CheckPermissions();
        }
        else
        {
            apiAvailability.getErrorDialog(this, lApiAvailabilityResult, 0).show();
        }
    }

    private void CheckPermissions()
    {
        if(Utilities.CheckPermissions(this))
        {
            CheckDisclaimer();
        }
        else
        {
            interactionMode = InteractionMode.PERMISSIONS;
            ReturnToMainView();
        }
    }

    private void CheckDisclaimer()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, MODE_PRIVATE);
        locatifier.Resume();

        if(sharedPreferences.getBoolean(ClientDefs.SETTINGS_DISCLAIMER_ACCEPTED, ClientDefs.SETTINGS_DISCLAIMER_ACCEPTED_DEFAULT))
        {
            CheckIdentity();
        }
        else
        {
            //Present disclaimer.
            interactionMode = InteractionMode.DISCLAIMER;
            ReturnToMainView();
        }
    }

    private void CheckIdentity()
    {
        if(Utilities.DeviceHasValidID(this))
        {
            GoToGame();
        }
        else
        {
            // No UUID or identity at all – block access
            interactionMode = InteractionMode.IDENTITY_WARNING;
            ReturnToMainView();
        }
    }

    public void GoToGame()
    {
        //Present login/register view.
        interactionMode = InteractionMode.SPLASH;
        game.SetDeviceID(Utilities.GetEncryptedDeviceID(this), strGoogleID, Utilities.GetDeviceName(), Utilities.GetProcessName(this));
        game.Resume();
        ReturnToMainView();
    }

    public void SetView(final LaunchView view)
    {
        //MUST BE CALLED FROM A UI THREAD! runOnUiThread not done here, because the LaunchView itself should be created from within a UI thread before this is called.
        //Remove the current view, if applicable.
        if(CurrentView != null)
        {
            lytMain.removeView(CurrentView);
        }

        //Assign the current view and add it to the activity.
        CurrentView = view;
        lytMain.addView(CurrentView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(interactionMode == InteractionMode.PERMISSIONS)
        {
            ((PermissionsView)CurrentView).PermissionsUpdated();

            if(Utilities.CheckPermissions(this))
            {
                CheckDisclaimer();
            }
        }
        else if(requestCode == 1001)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                LaunchLog.ConsoleMessage("Notifications enabled!");
            }
            else
            {
                LaunchLog.ConsoleMessage("Notifications denied.");
            }
        }
    }

    /**
     * Signature for Android buttons, which pass a view parameter that we don't care about.
     * @param view The button that invoked this function, which we don't care about but have to match a function signature.
     */
    public void ReturnToMainView(View view)
    {
        ReturnToMainView();
    }

    /**
     * Returns to the "main view" from menus etc.
     */
    public void ReturnToMainView()
    {
        Utilities.DismissKeyboard(this, getCurrentFocus());

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                switch (interactionMode)
                {
                    case DISCLAIMER:
                    {
                        SetView(new DisclaimerView(game, me));
                    }
                    break;

                    case PERMISSIONS:
                    {
                        SetView(new PermissionsView(game, me));
                    }
                    break;

                    case IDENTITY_WARNING:
                    {
                        SetView(new NoIMEIView(game, me));
                    }
                    break;

                    case SPLASH:
                    {
                        if (CurrentView instanceof SettingsView)
                        {
                            //Save settings when leaving settings view.
                            ((SettingsView) CurrentView).SaveSettings();
                        }

                        SetView(new SplashView(game, me));
                    }
                    break;

                    case STANDARD:
                    {
                        if(CurrentView instanceof SettingsView)
                        {
                            //Save settings when leaving settings view.
                            ((SettingsView) CurrentView).SaveSettings();
                            SetView(new MainNormalView(game, me));
                        }
                        else if(CurrentView instanceof AirplaneView)
                        {
                            AirplaneView view = ((AirplaneView)CurrentView);

                            AirplaneInterface aircraft = (AirplaneInterface)((AirplaneView)CurrentView).GetCurrentAircraft();

                            if(aircraft != null && (aircraft.Flying() && (!((Airplane)aircraft.GetAirplane()).Orphaned()) || (!aircraft.Flying() && aircraft.GetHomeBase() != null)) && ((AirplaneView)CurrentView).bFromAirbase)
                            {
                                SetView(new MainNormalView(game, me));
                                MainNormalView mainView = ((MainNormalView)CurrentView);

                                MapEntity host = aircraft.GetHomeBase().GetMapEntity(game);

                                if(host instanceof Ship)
                                {
                                    mainView.BottomLayoutShowView(new ShipView(game, me, (Ship)host));
                                }
                                else if(host instanceof Airbase)
                                {
                                    mainView.BottomLayoutShowView(new AirbaseView(game, me, (Airbase)host));
                                }
                            }
                            else
                                SetView(new MainNormalView(game, me));
                        }
                        else if (CurrentView instanceof UploadAvatarView)
                        {
                            //Return to settings view from various views.
                            SetView(new SettingsView(game, me));
                        }
                        else
                        {
                            SetView(new MainNormalView(game, me));

                            if(selectedEntity != null)
                            {
                                ApplySelectedEntityView();
                            }
                            else if(geoTarget != null)
                            {
                                onMapClick(Utilities.GetLatLng(geoTarget));
                            }
                        }
                    }
                    break;

                    case DIPLOMACY:
                    {
                        if (CurrentView instanceof UploadAvatarView)
                        {
                            if (game.GetOurPlayer().GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                            {
                                //Cancelled avatar upload from alliance control.
                                SetView(new AllianceControl(game, me, game.GetAlliance(game.GetOurPlayer().GetAllianceMemberID())));
                            }
                            else
                            {
                                //Cancelled avatar upload from new alliance view.
                                SetView(new CreateAllianceView(game, me, ClientDefs.DEFAULT_AVATAR_ID));
                            }
                        }
                        else if (CurrentView instanceof AllianceControl)
                        {
                            SetView(new DiplomacyView(game, me));
                        }
                        else
                        {
                            interactionMode = InteractionMode.STANDARD;
                            SetView(new MainNormalView(game, me));
                        }
                    }
                    break;

                    case PRIVACY_ZONES:
                    {
                        SetView(new PrivacyZonesView(game, me));
                    }
                    break;

                    case SELECT_NEW_HOMEBASE:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomNewHomebase(game, me, homebaseChangeAircraft));
                    }
                    break;

                    case TURN_INFANTRY:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.TURN, targeter));
                    }
                    break;

                    case ELECTRONIC_WARFARE:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomElectronicWarfare(game, me, (Airplane)homebaseChangeAircraft));
                    }
                    break;

                    case UNIT_COMMAND:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.MOVE, movableEntity));
                    }
                    break;

                    case MOVE_ORDER:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);

                        if(movableEntity != null)
                            normalView.BottomLayoutShowView(new BottomMoveOrder(game, me, MoveOrders.MOVE, movableEntity));
                        else if(MovableEntities != null && !MovableEntities.isEmpty())
                            normalView.BottomLayoutShowView(new BottomMoveOrder(game, me, MoveOrders.MOVE, MovableEntities));
                    }
                    break;

                    case RANGE_FINDER:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomRangeFinder(game, me, geoRangeFind1));
                    }
                    break;

                    case TARGET_MISSILE:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomMissileTarget(game, me, lTargettingSiteID, lTargettingSlotNo, targetSystemType));
                    }
                    break;

                    case TARGET_TORPEDO:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomTorpedoTarget(game, me, lTargettingSiteID, lTargettingSlotNo, targetSystemType));
                    }
                    break;

                    case SET_TARGET:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);

                        if(targeter != null)
                            normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.ATTACK, targeter));
                        else if(MovableEntities != null && !MovableEntities.isEmpty())
                            normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.ATTACK, MovableEntities));
                    }
                    break;

                    case SEEK_FUEL:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.SEEK_FUEL, targeter));
                    }
                    break;

                    case PROVIDE_FUEL:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.PROVIDE_FUEL, targeter));
                    }
                    break;

                    case CAPTURE_TARGET:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.CAPTURE, targeter));
                    }
                    break;

                    case LIBERATE_TARGET:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomUnitCommand(game, me, MoveOrders.LIBERATE, targeter));
                    }
                    break;

                    case BUILD_STRUCTURE:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomBuildStructure(game, me, lTargettingSiteID, blueprintType, blueprintResourceType, bUseSubstitutes));
                    }
                    break;

                    case TARGET_INTERCEPTOR:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);
                        normalView.BottomLayoutShowView(new BottomInterceptorTarget(game, me, lTargettingSiteID, lTargettingSlotNo, targetSystemType));
                    }
                    break;

                    case TRANSFER_ACCOUNT:
                    {
                        MainNormalView normalView = new MainNormalView(game, me);
                        SetView(normalView);

                        if(transferAccountFromPlayer != null)
                        {
                            normalView.BottomLayoutShowView(new BottomTransferAccount(game, me, transferAccountFromPlayer));
                        }
                    }
                    break;

                    case REGISTRATION:
                    {
                        if(strGoogleID == null)
                        {
                            // Handle error - force user to sign in first
                            LaunchLog.ConsoleMessage("Google ID is null. Cannot proceed with registration.");

                            SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, MODE_PRIVATE);
                            strGoogleID = sharedPreferences.getString(ClientDefs.SETTINGS_GOOGLE_ID_STORED, "");

                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestId()
                                    .requestEmail()
                                    .build();

                            if(strGoogleID.isEmpty())
                            {
                                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(me, gso); // assuming `gso` is accessible
                                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                me.startActivityForResult(signInIntent, ClientDefs.RC_SIGN_IN); // define RC_SIGN_IN
                            }

                            break;
                        }

                        SetView(new RegisterView(game, me, ClientDefs.DEFAULT_AVATAR_ID, strGoogleID, Utilities.GetEncryptedDeviceID(me)));
                    }
                    break;

                    case CANT_LOG_IN:
                    {
                        //Nothing to do.
                    }
                    break;
                }

                GameTicked(0); //Faster UI update.
            }
        });
    }

    public void ExpandView()
    {
        ((MainNormalView) CurrentView).ExpandBottomView();
    }

    public void ContractView()
    {
        ((MainNormalView) CurrentView).ContractBottomView();
    }

    @Override
    public void onBackPressed()
    {
        switch (interactionMode)
        {
            case TARGET_MISSILE:
            case SET_ARTILLERY_TARGET:
            case SET_TARGET:
            case SEEK_FUEL:
            case PROVIDE_FUEL:
            case CAPTURE_TARGET:
            case LIBERATE_TARGET:
            case TARGET_INTERCEPTOR:
            {
                ResetInteractionMode();

                if(selectedEntity != null)
                    SelectEntity(selectedEntity);
            }
            break;

            case UNIT_COMMAND:
            {
                if(movableEntity != null)
                {
                    LaunchEntity entity = movableEntity.GetEntity(game);

                    if(entity instanceof MapEntity)
                    {
                        ResetInteractionMode();
                        SelectEntity((MapEntity)entity);
                    }
                    else if(entity instanceof StoredEntity)
                    {
                        ResetInteractionMode();
                        SelectStoredEntity((StoredEntity)entity);
                    }
                }
            }
            break;

            case LOAD_LOOT:
            {
                if(targeter != null)
                {
                    LaunchEntity entity = targeter.GetEntity(game);

                    if(entity instanceof MapEntity)
                    {
                        ResetInteractionMode();
                        SelectEntity((MapEntity)entity);
                    }
                    else if(entity instanceof StoredEntity)
                    {
                        ResetInteractionMode();
                        SelectStoredEntity((StoredEntity)entity);
                    }
                }
                else
                {
                    InformationMode(false);
                }
            }
            break;

            case TRANSFER_CARGO:
            {
                if(cargoTransferEntity != null)
                {
                    if(cargoTransferEntity instanceof MapEntity)
                    {
                        ResetInteractionMode();
                        SelectEntity((MapEntity)cargoTransferEntity);
                    }
                    else if(cargoTransferEntity instanceof StoredEntity)
                    {
                        ResetInteractionMode();
                        SelectStoredEntity((StoredEntity)cargoTransferEntity);
                    }
                }
                else if(cargoToTransfer != null)
                {
                    if(cargoToTransfer instanceof LaunchEntity)
                    {
                        if(cargoToTransfer instanceof MapEntity)
                        {
                            ResetInteractionMode();
                            SelectEntity((MapEntity)cargoToTransfer);
                        }
                        else if(cargoToTransfer instanceof StoredEntity)
                        {
                            ResetInteractionMode();
                            SelectStoredEntity((StoredEntity)cargoToTransfer);
                        }
                    }
                }
                else
                {
                    InformationMode(false);
                }
            }
            break;

            case STANDARD:
            case RANGE_FINDER:
            case DIPLOMACY:
            {
                geoTarget = null;
                InformationMode(false);
            }
            break;

            default:
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderLaunch();
                launchDialog.SetMessage(getString(R.string.quit));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        finish();
                    }
                });
                launchDialog.SetOnClickNo(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                    }
                });
                launchDialog.show(getFragmentManager(), "");
            }
            break;
        }
    }

    public void ExitPrivacyZones()
    {
        if(game.GetAuthenticated())
        {
            interactionMode = InteractionMode.STANDARD;
        }
        else
        {
            interactionMode = InteractionMode.REGISTRATION;
        }

        RebuildMap();
        ReturnToMainView();
    }

    @Override
    public void ShowBasicOKDialog(final String strMessage)
    {
        final Context context = this;

        //Show an 'ok' dialog that merely displays a message.
        if(!isFinishing())
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(strMessage);
                    launchDialog.SetOnClickOk(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(getFragmentManager(), "");
                }
            });
        }
    }

    public void DisclaimerAgreed()
    {
        CheckIdentity();
    }

    public void LocationsUpdated()
    {
        if(bShowFirstLocation)
        {
            GoTo(locatifier.GetLocation());
            bShowFirstLocation = false;
        }
    }

    public void SetSelectedPrivacyZone(PrivacyZone privacyZone)
    {
        selectedPrivacyZone = privacyZone;
    }

    /**
     * Remove targetting UI elements from the map, when leaving a target control view.
     */
    public void RemoveTargettingMapUI()
    {
        if(!MovableTrajectories.isEmpty())
        {
            for(Polyline line : MovableTrajectories)
                line.remove();

            MovableTrajectories.clear();
        }

        if(!MoveMarkers.isEmpty())
        {
            for(Marker marker : MoveMarkers)
                marker.remove();

            MoveMarkers.clear();
        }

        if(!CircleOverlays.isEmpty())
        {
            for(Circle circle : CircleOverlays)
            {
                circle.remove();
            }
            CircleOverlays.clear();
        }

        if(!MassCommandTrajectories.isEmpty())
        {
            for(Polyline line : MassCommandTrajectories.values())
            {
                if(line != null)
                {
                    line.remove();
                    line = null;
                }
            }

            MassCommandTrajectories.clear();
        }

        if(!AirspaceOverlays.isEmpty())
        {
            for(Circle circle : AirspaceOverlays)
            {
                circle.remove();
            }
            AirspaceOverlays.clear();
        }

        if(targetRange != null)
        {
            targetRange.remove();
        }

        for(Circle targetBlastRadius : targetBlastRadii)
        {
            if (targetBlastRadius != null)
            {
                targetBlastRadius.remove();
            }
        }

        targetBlastRadii.clear();

        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
            movableTrajectory = null;
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
            movableTarget = null;
        }
    }

    public void ClearGeoTarget()
    {
        geoTarget = null;
    }

    public void ClearSelectedEntity()
    {
        selectedEntity = null;
        selectedLocation = null;
        ClearEntityOverlays();
    }

    public void ClearEntityOverlays()
    {
        if(!PolyLines.isEmpty())
        {
            for(Polyline line : PolyLines)
                line.remove();

            PolyLines.clear();
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
            movableTarget = null;
        }

        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
            movableTrajectory = null;
        }

        if(multiSelectedEntities.size() > 0)
            multiSelectedEntities.clear();

        for(Circle circle : multiCircleOverlays)
        {
            circle.remove();
        }
        multiCircleOverlays.clear();

        for(Circle circle : CircleOverlays)
        {
            circle.remove();
        }
        CircleOverlays.clear();

        //Used for range rings in standard mode.
        if(targetRange != null)
        {
            targetRange.remove();
            targetRange = null;
        }
    }

    /**
     * Select an entity, setting the selected entity to this and dealing with map/info screen UI etc.
     * @param entity The entity to select.
     */

    public void SelectLaunchEntity(LaunchEntity entity)
    {
        if(entity instanceof StoredEntity)
        {
            //Select the host.
            StoredEntity storedEntity = (StoredEntity)entity;
            EntityPointer pointer = storedEntity.GetHost();

            if(pointer != null)
            {
                MapEntity mapEntity = pointer.GetMapEntity(game);

                if(mapEntity != null)
                {
                    SelectEntity(mapEntity);
                }
            }
        }
        else if(entity instanceof MapEntity)
        {
            //Select the entity normally.
            SelectEntity((MapEntity)entity);
        }
    }

    public void SelectEntity(MapEntity entity)
    {
        ClearSelectedEntity();
        ReturnToMainView();

        DrawEntityOverlay(entity);

        selectedEntity = entity;

        ApplySelectedEntityView();
    }

    public void SelectStoredEntity(StoredEntity entity)
    {
        MainNormalView mainView = new MainNormalView(game, me);
        SetView(mainView);

        if(entity instanceof StoredAirplane)
            mainView.BottomLayoutShowView(new AirplaneView(game, me, (StoredAirplane)entity, true));

        //TODO: When stored trains are added, it will be needed here.
    }

    public void MultiSelectEntities(List<MapEntity> selectedEntities)
    {
        DrawMultipleEntityOverlays(selectedEntities);
    }

    public void DrawMultipleEntityOverlays(List<MapEntity> selectedEntities)
    {
        Context context = this;

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(!multiCircleOverlays.isEmpty())
                {
                    for(Circle circle : multiCircleOverlays)
                    {
                        circle.remove();
                    }
                    multiCircleOverlays.clear();
                }

                if(!ContactBearings.isEmpty())
                {
                    for(Marker marker : ContactBearings)
                        marker.remove();

                    ContactBearings.clear();
                }

                for(MapEntity entity : selectedEntities)
                {
                    if(entity instanceof Infantry)
                    {
                        Infantry infantry = (Infantry)entity;

                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.INFANTRY_COMBAT_RANGE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, infantry.GetMoveOrders() == MoveOrders.ATTACK ? R.attr.MissilePathColour : R.attr.InterceptorPathColour))
                                .strokeWidth(3.0f)));
                    }

                    if(entity instanceof Structure && ZoomedIn())
                    {
                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(entity.GetEntityType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(1.0f)));
                    }

                    if(entity instanceof Rubble && ZoomedIn())
                    {
                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(((Rubble)entity).GetStructureType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(1.0f)));
                    }

                    if(entity instanceof Blueprint && ZoomedIn())
                    {
                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(((Blueprint)entity).GetType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(1.0f)));

                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.BLUEPRINT_CONSTRUCT_DISTANCE * Defs.METRES_PER_KM)
                                .fillColor(Utilities.ColourFromAttr(context, R.attr.LootRadiusColour))
                                .strokeWidth(0.0f)));
                    }

                    if(entity instanceof Loot && ZoomedIn())
                    {
                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetOreCollectRadius() * Defs.METRES_PER_KM)
                                .fillColor(Utilities.ColourFromAttr(context, R.attr.LootRadiusColour))
                                .strokeWidth(0.0f)));
                    }

                    if(entity instanceof SentryGun && ZoomedIn())
                    {
                        multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.SENTRY_RANGE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                .strokeWidth(5.0f)));
                    }

                    if(entity.GetOwnedBy(game.GetOurPlayerID()))
                    {
                        if(entity instanceof CommandPost && ZoomedIn())
                        {
                            multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(game.GetConfig().GetCommandPostShelterRadius() * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                    .strokeWidth(5.0f)));
                        }

                        if(entity instanceof RadarStation)
                        {
                            RadarStation radarStation = game.GetRadarStation(entity.GetID());

                            multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(Defs.RADAR_STATION_SCANNER_RANGE * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.RadarColor))
                                    .strokeWidth(3.0f)));
                        }

                        if(entity instanceof SAMSite)
                        {
                            SAMSite samSite = ((SAMSite)entity);

                            MissileSystem missileSystem = samSite.GetInterceptorSystem();

                            for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                            {
                                InterceptorType type = game.GetConfig().GetInterceptorType(TypeCount.getKey());
                                float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                        .strokeWidth(fltRangeRadius)));
                            }
                        }

                        if(entity instanceof MissileSite)
                        {
                            MissileSystem missileSystem = ((MissileSite)entity).GetMissileSystem();

                            for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                            {
                                MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                                float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                        .strokeWidth(fltRangeRadius)));
                            }
                        }

                        if(entity instanceof Warehouse)
                        {
                            multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(game.GetConfig().GetWarehouseLinkRange() * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                    .strokeWidth(3.0f)));

                            multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(game.GetConfig().GetWarehouseCollectRadius() * Defs.METRES_PER_KM)
                                    .fillColor(Utilities.ColourFromAttr(context, R.attr.OreRadiusColour))
                                    .strokeWidth(0.0f)));

                            multiCircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(game.GetConfig().GetOreCollectRadius() * Defs.METRES_PER_KM)
                                    .fillColor(Utilities.ColourFromAttr(context, R.attr.LootRadiusColour))
                                    .strokeWidth(0.0f)));
                        }

                        if(entity instanceof NavalVessel)
                        {
                            NavalVessel vessel = (NavalVessel)entity;

                            if(game.EntityIsFriendly(vessel, game.GetOurPlayer()))
                            {
                                PatternItem dash = new Dash(10);
                                PatternItem gap = new Gap(20);

                                List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                                if(vessel.GetMoveOrders() == Movable.MoveOrders.MOVE)
                                {
                                    movableTrajectory = map.addPolyline(new PolylineOptions()
                                            .add(Utilities.GetLatLng(vessel.GetPosition()))
                                            .add(Utilities.GetLatLng(vessel.GetGeoTarget()))
                                            .pattern(trajectoryPattern)
                                            .geodesic(true)
                                            .width(3.0f)
                                            .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));
                                }

                                if(vessel.HasPassiveContact())
                                {
                                    for(float fltBearing : vessel.GetContactBearings())
                                    {
                                        ContactBearings.add(map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(vessel.GetPosition()))
                                                .anchor(0.5f, 1.0f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.contact_bearing_overlay))
                                                .rotation((float)GeoCoord.ToDegrees(fltBearing))));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void DrawEntityOverlay(MapEntity entity)
    {
        Context context = this;
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(entity != null)
                {
                    if(movableTarget != null)
                    {
                        movableTarget.remove();
                        movableTarget = null;
                    }

                    if(!PolyLines.isEmpty())
                    {
                        for(Polyline line : PolyLines)
                            line.remove();

                        PolyLines.clear();
                    }

                    if(!ContactBearings.isEmpty())
                    {
                        for(Marker marker : ContactBearings)
                            marker.remove();

                        ContactBearings.clear();
                    }

                    if(!MoveMarkers.isEmpty())
                    {
                        for(Marker marker : MoveMarkers)
                            marker.remove();

                        MoveMarkers.clear();
                    }

                    if(!CircleOverlays.isEmpty())
                    {
                        for(Circle circle : CircleOverlays)
                        {
                            circle.remove();
                        }
                        CircleOverlays.clear();
                    }

                    if(movableTrajectory != null)
                    {
                        movableTrajectory.remove();
                    }

                    if(entity instanceof Capturable)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.SHIPYARD_REPAIR_DISTANCE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathTrackingColour))
                                .strokeWidth(5.0f)));
                    }

                    if(entity instanceof Loot)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetOreCollectRadius() * Defs.METRES_PER_KM)
                                .fillColor(Utilities.ColourFromAttr(context, R.attr.LootRadiusColour))
                                .strokeWidth(0.0f)));
                    }

                    if(entity instanceof ResourceDeposit)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.DEPOSIT_RADIUS * Defs.METRES_PER_KM)
                                .fillColor(Utilities.ColourFromAttr(context, R.attr.LootRadiusColour))
                                .strokeWidth(0.0f)));
                    }

                    if(entity instanceof SentryGun)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.SENTRY_RANGE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                .strokeWidth(5.0f)));
                    }

                    if(game.EntityIsFriendly(entity, game.GetOurPlayer()))
                    {
                        if(entity instanceof ArtilleryGun || (entity instanceof Ship && ((Ship)entity).HasArtillery()))
                        {
                            ArtilleryInterface artillery = (ArtilleryInterface)entity;

                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(Defs.ARTILLERY_RANGE * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                    .strokeWidth(5.0f)));

                            if(artillery.HasFireOrder())
                            {
                                PolyLines.add(map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(entity.GetPosition()))
                                        .add(Utilities.GetLatLng(artillery.GetFireOrder().GetGeoTarget()))
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))));

                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(artillery.GetFireOrder().GetGeoTarget()))
                                        .radius(artillery.GetFireOrder().GetRadius() * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                        .strokeWidth(5.0f)));
                            }
                        }

                        if(entity instanceof CommandPost)
                        {
                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(Defs.COMMAND_POST_RADIUS * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                    .strokeWidth(5.0f)));
                        }

                        if(entity instanceof SAMSite)
                        {
                            SAMSite samSite = ((SAMSite)entity);

                            MissileSystem missileSystem = samSite.GetInterceptorSystem();

                            for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                            {
                                InterceptorType type = game.GetConfig().GetInterceptorType(TypeCount.getKey());
                                float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                        .strokeWidth(fltRangeRadius)));
                            }
                        }

                        if(entity instanceof MissileSite)
                        {
                            MissileSystem missileSystem = ((MissileSite)entity).GetMissileSystem();

                            for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                            {
                                MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                                float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                        .strokeWidth(fltRangeRadius)));
                            }
                        }

                        if(entity instanceof RadarStation)
                        {
                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(Defs.RADAR_STATION_SCANNER_RANGE * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.RadarColor))
                                    .strokeWidth(3.0f)));
                        }
                    }

                    if(entity instanceof Structure)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(entity.GetEntityType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(3.0f)));
                    }

                    if(entity instanceof Blueprint)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(((Blueprint)entity).GetType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(3.0f)));

                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.BLUEPRINT_CONSTRUCT_DISTANCE * Defs.METRES_PER_KM)
                                .fillColor(Utilities.ColourFromAttr(context, R.attr.LootRadiusColour))
                                .strokeWidth(0.0f)));
                    }

                    if(entity instanceof Rubble)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(game.GetConfig().GetStructureSeparation(((Rubble)entity).GetStructureType()) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.StructureSeparationRadiusColour))
                                .strokeWidth(3.0f)));
                    }

                    if(entity instanceof Missile)
                    {
                        Missile missile = ((Missile)entity);
                        MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                        if(!bShowBlastRadii)
                        {
                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(missile.GetTarget()))
                                    .radius(MissileStats.GetBlastRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                    .fillColor(Utilities.ColourFromAttr(context, R.attr.BlastRadiusColour))
                                    .strokeWidth(0.0f)));

                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(missile.GetTarget()))
                                    .radius(MissileStats.GetMissileEMPRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                    .fillColor(Utilities.ColourFromAttr(context, R.attr.EMPColour))
                                    .strokeWidth(0.0f)));

                            if(type.GetStealth())
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(Defs.STEALTH_ENGAGEMENT_DISTANCE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                        .strokeWidth(3.0f)));
                            }
                        }

                        GeoCoord geoTarget = game.GetMissileTarget(missile);

                        if(geoTarget != null)
                        {
                            PolyLines.add(map.addPolyline(new PolylineOptions()
                                    .add(Utilities.GetLatLng(missile.GetPosition()))
                                    .add(Utilities.GetLatLng(geoTarget))
                                    .width(4.0f)
                                    .geodesic(true)
                                    .color(Utilities.ColourFromAttr(context, missile.GetTracking()? R.attr.MissilePathTrackingColour : R.attr.MissilePathColour))));
                        }
                    }

                    if(entity instanceof Torpedo)
                    {
                        if(!bShowBlastRadii && ((Torpedo)entity).GetState() != Torpedo.TorpedoState.SEEKING)
                        {
                            Torpedo torpedo = ((Torpedo)entity);
                            TorpedoType type = game.GetConfig().GetTorpedoType(torpedo.GetType());

                            GeoCoord geoTarget = null;

                            if(torpedo.HasTarget())
                            {
                                MapEntity target = torpedo.GetTarget().GetMapEntity(game);

                                if(target != null)
                                    geoTarget = target.GetPosition();
                            }
                            else if(torpedo.GetGeoTarget() != null)
                            {
                                geoTarget = torpedo.GetGeoTarget();
                            }

                            if(geoTarget != null)
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(geoTarget))
                                        .radius(type.GetBlastRadius() * Defs.METRES_PER_KM)
                                        .fillColor(Utilities.ColourFromAttr(context, R.attr.BlastRadiusColour))
                                        .strokeWidth(0.0f)));
                            }

                            if(!torpedo.HasTarget() && torpedo.GetHoming())
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(Defs.TORPEDO_HOMING_RANGE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                        .strokeWidth(3.0f)));
                            }
                        }
                    }

                    if(entity instanceof Infantry)
                    {
                        Infantry infantry = (Infantry)entity;

                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.INFANTRY_COMBAT_RANGE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, infantry.GetMoveOrders() == MoveOrders.ATTACK ? R.attr.MissilePathColour : R.attr.InterceptorPathColour))
                                .strokeWidth(3.0f)));

                        if(game.EntityIsFriendly(infantry, game.GetOurPlayer()))
                        {
                            PatternItem dash = new Dash(10);
                            PatternItem gap = new Gap(20);

                            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                            if(infantry.GetMoveOrders() == Movable.MoveOrders.MOVE)
                            {
                                movableTrajectory = map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(infantry.GetPosition()))
                                        .add(Utilities.GetLatLng(infantry.GetGeoTarget()))
                                        .pattern(trajectoryPattern)
                                        .geodesic(true)
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));

                                MarkerOptions options = new MarkerOptions()
                                        .position(Utilities.GetLatLng(infantry.GetGeoTarget()))
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint));

                                if(infantry.HasGeoCoordChain())
                                {
                                    Map<Integer, GeoCoord> Coordinates = infantry.GetCoordinates();
                                    List<LatLng> points = new ArrayList<LatLng>();

                                    points.add(Utilities.GetLatLng(infantry.GetPosition()));

                                    for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                    {
                                        GeoCoord geoCoord = Coordinates.get(i);

                                        if(geoCoord != null)
                                        {
                                            points.add(Utilities.GetLatLng(geoCoord));
                                        }
                                    }

                                    if(movableTrajectory != null)
                                    {
                                        movableTrajectory.setPoints(points);
                                    }

                                    options.position(Utilities.GetLatLng(infantry.GetLastCoordinate()));
                                }

                                movableTarget = map.addMarker(options);
                            }
                            else if(infantry.GetMoveOrders() == Movable.MoveOrders.CAPTURE)
                            {
                                if(infantry.GetTarget() != null || infantry.GetGeoTarget() != null)
                                {
                                    GeoCoord geoTarget = null;

                                    if(infantry.GetTarget() != null)
                                    {
                                        MapEntity target = infantry.GetTarget().GetMapEntity(game);

                                        if(target != null)
                                        {
                                            geoTarget = target.GetPosition();
                                        }
                                    }

                                    if(geoTarget != null)
                                    {
                                        movableTrajectory = map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(infantry.GetPosition()))
                                                .add(Utilities.GetLatLng(geoTarget))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.MissilePathColour)));

                                        movableTarget = map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(geoTarget))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_attack_waypoint)));
                                    }
                                }
                            }
                            else if(infantry.GetMoveOrders() == Movable.MoveOrders.LIBERATE)
                            {
                                if(infantry.GetTarget() != null || infantry.GetGeoTarget() != null)
                                {
                                    GeoCoord geoTarget = null;

                                    if(infantry.GetTarget() != null)
                                    {
                                        MapEntity target = infantry.GetTarget().GetMapEntity(game);

                                        if(target != null)
                                        {
                                            geoTarget = target.GetPosition();
                                        }
                                    }

                                    if(geoTarget != null)
                                    {
                                        movableTrajectory = map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(infantry.GetPosition()))
                                                .add(Utilities.GetLatLng(geoTarget))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Color.MAGENTA));

                                        movableTarget = map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(geoTarget))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_liberate_waypoint)));
                                    }
                                }
                            }
                            else if(infantry.GetMoveOrders() == MoveOrders.ATTACK)
                            {
                                if(infantry.GetTarget() != null)
                                {
                                    if(infantry.GetTarget() != null)
                                    {
                                        MapEntity target = infantry.GetTarget().GetMapEntity(game);

                                        if(target != null)
                                        {
                                            geoTarget = target.GetPosition();
                                        }
                                    }

                                    if(geoTarget != null)
                                    {
                                        movableTrajectory = map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(infantry.GetPosition()))
                                                .add(Utilities.GetLatLng(geoTarget))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.MissilePathColour)));

                                        movableTarget = map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(geoTarget))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_attack_waypoint)));
                                    }
                                }
                            }
                        }
                    }

                    if(entity instanceof Tank)
                    {
                        Tank tank = (Tank)entity;

                        if(game.EntityIsFriendly(tank, game.GetOurPlayer()))
                        {
                            PatternItem dash = new Dash(10);
                            PatternItem gap = new Gap(20);

                            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                            if(tank.IsAnMBT())
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(Defs.BATTLE_TANK_FIRING_RANGE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                        .strokeWidth(5.0f)));
                            }
                            else if(tank.IsASPAAG())
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(Defs.SPAAG_FIRING_RANGE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                        .strokeWidth(5.0f)));
                            }

                            if(tank.GetMoveOrders() == Movable.MoveOrders.MOVE)
                            {
                                movableTrajectory = map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(tank.GetPosition()))
                                        .add(Utilities.GetLatLng(tank.GetGeoTarget()))
                                        .pattern(trajectoryPattern)
                                        .geodesic(true)
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));

                                MarkerOptions options = new MarkerOptions()
                                        .position(Utilities.GetLatLng(tank.GetGeoTarget()))
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint));

                                if(tank.HasGeoCoordChain())
                                {
                                    Map<Integer, GeoCoord> Coordinates = tank.GetCoordinates();
                                    List<LatLng> points = new ArrayList<LatLng>();

                                    points.add(Utilities.GetLatLng(tank.GetPosition()));

                                    for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                    {
                                        GeoCoord geoCoord = Coordinates.get(i);

                                        if(geoCoord != null)
                                        {
                                            points.add(Utilities.GetLatLng(geoCoord));
                                        }
                                    }

                                    if(movableTrajectory != null)
                                    {
                                        movableTrajectory.setPoints(points);
                                    }

                                    options.position(Utilities.GetLatLng(tank.GetLastCoordinate()));
                                }

                                movableTarget = map.addMarker(options);
                            }
                            else if(tank.GetMoveOrders() == Movable.MoveOrders.ATTACK)
                            {
                                if(tank.GetTarget() != null)
                                {
                                    MapEntity target = tank.GetTarget().GetMapEntity(game);

                                    if(target != null)
                                    {
                                        GeoCoord geoTarget = target.GetPosition();

                                        if(geoTarget != null)
                                        {
                                            movableTrajectory = map.addPolyline(new PolylineOptions()
                                                    .add(Utilities.GetLatLng(tank.GetPosition()))
                                                    .add(Utilities.GetLatLng(geoTarget))
                                                    .pattern(trajectoryPattern)
                                                    .geodesic(true)
                                                    .width(3.0f)
                                                    .color(Utilities.ColourFromAttr(me, R.attr.MissilePathColour)));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if(entity instanceof CargoTruck)
                    {
                        CargoTruck truck = (CargoTruck)entity;

                        if(game.EntityIsFriendly(truck, game.GetOurPlayer()))
                        {
                            PatternItem dash = new Dash(10);
                            PatternItem gap = new Gap(20);

                            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                            if(truck.GetMoveOrders() == Movable.MoveOrders.MOVE)
                            {
                                movableTrajectory = map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(truck.GetPosition()))
                                        .add(Utilities.GetLatLng(truck.GetGeoTarget()))
                                        .pattern(trajectoryPattern)
                                        .geodesic(true)
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));

                                MarkerOptions options = new MarkerOptions()
                                        .position(Utilities.GetLatLng(truck.GetGeoTarget()))
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint));

                                if(truck.HasGeoCoordChain())
                                {
                                    Map<Integer, GeoCoord> Coordinates = truck.GetCoordinates();
                                    List<LatLng> points = new ArrayList<LatLng>();

                                    points.add(Utilities.GetLatLng(truck.GetPosition()));

                                    for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                    {
                                        GeoCoord geoCoord = Coordinates.get(i);

                                        if(geoCoord != null)
                                        {
                                            points.add(Utilities.GetLatLng(geoCoord));
                                        }
                                    }

                                    if(movableTrajectory != null)
                                    {
                                        movableTrajectory.setPoints(points);
                                    }

                                    options.position(Utilities.GetLatLng(truck.GetLastCoordinate()));
                                }

                                movableTarget = map.addMarker(options);
                            }
                        }
                    }

                    if(entity instanceof NavalVessel)
                    {
                        NavalVessel vessel = (NavalVessel)entity;

                        if(vessel.HasSonar())
                        {
                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(Defs.SONAR_RANGE * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.SonarColour))
                                    .strokeWidth(3.0f)));
                        }

                        if(vessel instanceof Ship)
                        {
                            Ship ship = (Ship)vessel;

                            if(ship.HasSentries())
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(Defs.CIWS_RANGE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                        .strokeWidth(5.0f)));
                            }

                            if(ship.HasSupport())
                            {
                                CircleOverlays.add(map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(entity.GetPosition()))
                                        .radius(Defs.SHIPYARD_REPAIR_DISTANCE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(context, R.attr.InfoColour))
                                        .strokeWidth(5.0f)));
                            }

                            if(game.EntityIsFriendly(entity, game.GetOurPlayer()))
                            {
                                if(ship.HasInterceptors())
                                {
                                    MissileSystem missileSystem = ship.GetInterceptorSystem();

                                    for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                                    {
                                        InterceptorType type = game.GetConfig().GetInterceptorType(TypeCount.getKey());
                                        float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                                .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                                .strokeWidth(fltRangeRadius)));
                                    }
                                }

                                if(ship.HasMissiles())
                                {
                                    MissileSystem missileSystem = ship.GetMissileSystem();

                                    for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                                    {
                                        MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                                        float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                                .center(Utilities.GetLatLng(ship.GetPosition()))
                                                .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                                .strokeWidth(fltRangeRadius)));
                                    }
                                }
                            }
                        }
                        else if(vessel instanceof Submarine)
                        {
                            Submarine submarine = (Submarine)vessel;

                            if(game.EntityIsFriendly(entity, game.GetOurPlayer()))
                            {
                                if(submarine.HasMissiles())
                                {
                                    MissileSystem missileSystem = submarine.GetMissileSystem();

                                    for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                                    {
                                        MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                                        float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                                .center(Utilities.GetLatLng(submarine.GetPosition()))
                                                .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                                .strokeWidth(fltRangeRadius)));
                                    }
                                }

                                if(submarine.HasICBMs())
                                {
                                    MissileSystem missileSystem = submarine.GetICBMSystem();

                                    for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                                    {
                                        MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                                        float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                                .center(Utilities.GetLatLng(submarine.GetPosition()))
                                                .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                                .strokeWidth(fltRangeRadius)));
                                    }
                                }
                            }
                        }

                        if(game.EntityIsFriendly(vessel, game.GetOurPlayer()))
                        {
                            PatternItem dash = new Dash(10);
                            PatternItem gap = new Gap(20);

                            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                            if(vessel.GetMoveOrders() == Movable.MoveOrders.MOVE)
                            {
                                movableTrajectory = map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(vessel.GetPosition()))
                                        .add(Utilities.GetLatLng(vessel.GetGeoTarget()))
                                        .pattern(trajectoryPattern)
                                        .geodesic(true)
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));

                                MarkerOptions options = new MarkerOptions()
                                        .position(Utilities.GetLatLng(vessel.GetGeoTarget()))
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint));

                                if(vessel.HasGeoCoordChain())
                                {
                                    Map<Integer, GeoCoord> Coordinates = vessel.GetCoordinates();
                                    List<LatLng> points = new ArrayList<LatLng>();

                                    points.add(Utilities.GetLatLng(vessel.GetPosition()));

                                    for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                    {
                                        GeoCoord geoCoord = Coordinates.get(i);

                                        if(geoCoord != null)
                                        {
                                            points.add(Utilities.GetLatLng(geoCoord));
                                        }
                                    }

                                    if(movableTrajectory != null)
                                    {
                                        movableTrajectory.setPoints(points);
                                    }

                                    options.position(Utilities.GetLatLng(vessel.GetLastCoordinate()));
                                }

                                movableTarget = map.addMarker(options);
                            }
                            else if(vessel.GetMoveOrders() == MoveOrders.SEEK_FUEL || vessel.GetMoveOrders() == MoveOrders.PROVIDE_FUEL)
                            {
                                if(vessel.GetTarget() != null)
                                {
                                    MapEntity target = vessel.GetTarget().GetMapEntity(game);

                                    if(target != null)
                                    {
                                        movableTrajectory = map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(vessel.GetPosition()))
                                                .add(Utilities.GetLatLng(target.GetPosition()))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.FuelColour)));
                                    }
                                }
                            }

                            if(vessel.HasPassiveContact())
                            {
                                for(float fltBearing : vessel.GetContactBearings())
                                {
                                    ContactBearings.add(map.addMarker(new MarkerOptions()
                                            .position(Utilities.GetLatLng(vessel.GetPosition()))
                                            .anchor(0.5f, 1.0f)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.contact_bearing_overlay))
                                            .rotation((float)GeoCoord.ToDegrees(fltBearing))));
                                }
                            }
                        }
                    }

                    if(entity instanceof Airplane)
                    {
                        Airplane aircraft = ((Airplane)entity);

                        if(aircraft.HasCannon())
                        {
                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(Defs.AIRCRAFT_CANNON_RANGE * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                    .strokeWidth(3.0f)));
                        }

                        if(game.EntityIsFriendly(aircraft, game.GetOurPlayer()))
                        {
                            if(aircraft.HasInterceptors())
                            {
                                MissileSystem missileSystem = aircraft.GetInterceptorSystem();

                                for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                                {
                                    InterceptorType type = game.GetConfig().GetInterceptorType(TypeCount.getKey());
                                    float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                    CircleOverlays.add(map.addCircle(new CircleOptions()
                                            .center(Utilities.GetLatLng(entity.GetPosition()))
                                            .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                                            .strokeColor(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))
                                            .strokeWidth(fltRangeRadius)));
                                }
                            }

                            if(aircraft.HasMissiles())
                            {
                                MissileSystem missileSystem = aircraft.GetMissileSystem();

                                for(Map.Entry<Integer, Integer> TypeCount : missileSystem.GetTypeCounts().entrySet())
                                {
                                    MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                                    float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                                    CircleOverlays.add(map.addCircle(new CircleOptions()
                                            .center(Utilities.GetLatLng(aircraft.GetPosition()))
                                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                            .strokeColor(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))
                                            .strokeWidth(fltRangeRadius)));
                                }
                            }

                            CircleOverlays.add(map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(entity.GetPosition()))
                                    .radius(game.GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetAircraftType())) * Defs.METRES_PER_KM)
                                    .strokeColor(Utilities.ColourFromAttr(context, R.attr.FuelColour))
                                    .strokeWidth(3.0f)));

                            PatternItem dash = new Dash(10);
                            PatternItem gap = new Gap(20);

                            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                            if(aircraft.GetMoveOrders() == Airplane.MoveOrders.MOVE && (aircraft.HasGeoTarget() || aircraft.HasGeoCoordChain()))
                            {
                                movableTrajectory = map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(aircraft.GetPosition()))
                                        .add(Utilities.GetLatLng(aircraft.GetGeoTarget()))
                                        .pattern(trajectoryPattern)
                                        .geodesic(true)
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));

                                MarkerOptions options = new MarkerOptions()
                                        .position(Utilities.GetLatLng(aircraft.GetGeoTarget()))
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint));

                                if(aircraft.HasGeoCoordChain())
                                {
                                    Map<Integer, GeoCoord> Coordinates = aircraft.GetCoordinates();
                                    List<LatLng> points = new ArrayList<LatLng>();

                                    points.add(Utilities.GetLatLng(aircraft.GetPosition()));

                                    for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                    {
                                        GeoCoord geoCoord = Coordinates.get(i);

                                        if(geoCoord != null)
                                        {
                                            points.add(Utilities.GetLatLng(geoCoord));
                                        }
                                    }

                                    if(movableTrajectory != null)
                                    {
                                        movableTrajectory.setPoints(points);
                                    }

                                    options.position(Utilities.GetLatLng(aircraft.GetLastCoordinate()));
                                }

                                movableTarget = map.addMarker(options);
                            }
                            else if(aircraft.GetMoveOrders() == Airplane.MoveOrders.ATTACK)
                            {
                                if(aircraft.GetTarget() != null || aircraft.GetGeoTarget() != null)
                                {
                                    GeoCoord geoTarget = null;
                                    MapEntity target = null;

                                    if(aircraft.GetTarget() != null && aircraft.GetTarget().GetMapEntity(game) != null)
                                    {
                                        target = aircraft.GetTarget().GetMapEntity(game);
                                        geoTarget = aircraft.GetTarget().GetMapEntity(game).GetPosition().GetCopy();
                                    }
                                    else if(aircraft.GetGeoTarget() != null)
                                    {
                                        geoTarget = aircraft.GetGeoTarget();
                                    }

                                    if(geoTarget != null)
                                    {
                                        movableTrajectory = map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(aircraft.GetPosition()))
                                                .add(Utilities.GetLatLng(geoTarget))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.MissilePathColour)));

                                        MarkerOptions options = new MarkerOptions()
                                                .position(Utilities.GetLatLng(geoTarget))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_attack_waypoint));

                                        movableTarget = map.addMarker(options);
                                    }
                                }
                            }
                            else if(aircraft.GetMoveOrders() == Airplane.MoveOrders.SEEK_FUEL || aircraft.GetMoveOrders() == Airplane.MoveOrders.PROVIDE_FUEL)
                            {
                                if(aircraft.GetTarget() != null)
                                {
                                    MapEntity target = aircraft.GetTarget().GetMapEntity(game);

                                    if(target != null)
                                    {
                                        movableTrajectory = map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(aircraft.GetPosition()))
                                                .add(Utilities.GetLatLng(target.GetPosition()))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.FuelColour)));
                                    }
                                }
                            }
                            else if(aircraft.GetMoveOrders() == Airplane.MoveOrders.RETURN)
                            {
                                MapEntity host = aircraft.GetHomeBase().GetMapEntity(game);

                                if(host != null && host.GetPosition() != null)
                                {
                                    movableTrajectory = map.addPolyline(new PolylineOptions()
                                            .add(Utilities.GetLatLng(aircraft.GetPosition()))
                                            .add(Utilities.GetLatLng(host.GetPosition()))
                                            .pattern(trajectoryPattern)
                                            .geodesic(true)
                                            .width(3.0f)
                                            .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));
                                }
                            }
                        }
                    }

                    if((entity instanceof Haulable || entity instanceof HaulerInterface) && !(entity instanceof Shipyard))
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.LOAD_DISTANCE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.InfoColour))
                                .strokeWidth(5.0f)));
                    }

                    if(entity instanceof Distributor)
                    {
                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(entity.GetPosition()))
                                .radius(Defs.DISTRIBUTOR_ACTION_DISTANCE * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.InfoColour))
                                .strokeWidth(5.0f)));
                    }
                }
            }
        });
    }

    public void ApplySelectedEntityView()
    {
        if(CurrentView instanceof MainNormalView)
        {
            MainNormalView mainView = (MainNormalView)CurrentView;

            if(selectedEntity instanceof Player)
            {
                mainView.BottomLayoutShowView(new PlayerView(game, this, selectedEntity.GetID()));
            }
            else if(selectedEntity instanceof Missile)
            {
                mainView.BottomLayoutShowView(new MissileView(game, this, selectedEntity.GetID()));
            }
            else if(selectedEntity instanceof Interceptor)
            {
                mainView.BottomLayoutShowView(new InterceptorView(game, this, selectedEntity.GetID()));
            }
            else if(selectedEntity instanceof Torpedo)
            {
                mainView.BottomLayoutShowView(new TorpedoView(game, this, selectedEntity.GetID()));
            }
            else if(selectedEntity instanceof MissileSite)
            {
                if(((MissileSite)selectedEntity).CanTakeICBM())
                {
                    mainView.BottomLayoutShowView(new NukeSiteView(game, this, selectedEntity));
                }
                else
                {
                    mainView.BottomLayoutShowView(new MissileSiteView(game, this, selectedEntity));
                }
            }
            else if(selectedEntity instanceof SAMSite)
            {
                if(((SAMSite)selectedEntity).GetIsABMSilo())
                {
                    mainView.BottomLayoutShowView(new ABMSiteView(game, this, selectedEntity));
                }
                else
                {
                    mainView.BottomLayoutShowView(new SAMSiteView(game, this, selectedEntity));
                }
            }
            else if(selectedEntity instanceof SentryGun)
            {
                mainView.BottomLayoutShowView(new SentryGunView(game, this, selectedEntity));
            }
            else if(selectedEntity instanceof CommandPost)
            {
                mainView.BottomLayoutShowView(new CommandPostView(game, this, selectedEntity));
            }
            else if(selectedEntity instanceof Warehouse)
            {
                mainView.BottomLayoutShowView(new WarehouseView(game, this, selectedEntity));
            }
            else if(selectedEntity instanceof Loot)
            {
                mainView.BottomLayoutShowView(new LootView(game, this, selectedEntity.GetID()));
            }
            else if(selectedEntity instanceof Rubble)
            {
                mainView.BottomLayoutShowView(new RubbleView(game, this, selectedEntity.GetID()));
            }
            else if(selectedEntity instanceof Airbase)
            {
                mainView.BottomLayoutShowView(new AirbaseView(game, this, selectedEntity));
            }
            else if(selectedEntity instanceof Armory)
            {
                mainView.BottomLayoutShowView(new ArmoryView(game, this, selectedEntity));
            }
            else if(selectedEntity instanceof Airplane)
            {
                mainView.BottomLayoutShowView(new AirplaneView(game, this, ((Airplane)selectedEntity), false));
            }
            else if(selectedEntity instanceof Tank)
            {
                mainView.BottomLayoutShowView(new TankView(game, this, ((Tank)selectedEntity)));
            }
            else if(selectedEntity instanceof Shipyard)
            {
                mainView.BottomLayoutShowView(new ShipyardView(game, this, ((Shipyard)selectedEntity)));
            }
            else if(selectedEntity instanceof Ship)
            {
                mainView.BottomLayoutShowView(new ShipView(game, this, ((Ship)selectedEntity)));
            }
            else if(selectedEntity instanceof Submarine)
            {
                mainView.BottomLayoutShowView(new SubmarineView(game, this, ((Submarine)selectedEntity)));
            }
        }
    }

    public void GoTo(final LaunchClientLocation location)
    {
        if (map != null)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (location != null)
                    {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.GetLatitude(), location.GetLongitude()), fltZoomToLevel));
                    }
                }
            });
        }
    }

    public void SelectedEntityUpdated()
    {
        if(selectedEntity instanceof MissileSite)
            selectedEntity = game.GetMissileSite(selectedEntity.GetID());
        else if(selectedEntity instanceof SAMSite)
            selectedEntity = game.GetSAMSite(selectedEntity.GetID());
        else if(selectedEntity instanceof SentryGun)
            selectedEntity = game.GetSentryGun(selectedEntity.GetID());
        else if(selectedEntity instanceof OreMine)
            selectedEntity = game.GetOreMine(selectedEntity.GetID());
        else if(selectedEntity instanceof Airbase)
            selectedEntity = game.GetAirbase(selectedEntity.GetID());
        else if(selectedEntity instanceof Armory)
            selectedEntity = game.GetArmory(selectedEntity.GetID());
        else if(selectedEntity instanceof Airplane)
            selectedEntity = game.GetAirplane(selectedEntity.GetID());
        else if(selectedEntity instanceof Bank)
            selectedEntity = game.GetBank(selectedEntity.GetID());
        else if(selectedEntity instanceof CommandPost)
            selectedEntity = game.GetCommandPost(selectedEntity.GetID());
        else if(selectedEntity instanceof Warehouse)
            selectedEntity = game.GetWarehouse(selectedEntity.GetID());
        else if(selectedEntity instanceof Processor)
            selectedEntity = game.GetProcessor(selectedEntity.GetID());
        else if(selectedEntity instanceof Distributor)
            selectedEntity = game.GetDistributor(selectedEntity.GetID());
        else if(selectedEntity instanceof ScrapYard)
            selectedEntity = game.GetScrapYard(selectedEntity.GetID());
        else if(selectedEntity instanceof Missile)
            selectedEntity = game.GetMissile(selectedEntity.GetID());
        else if(selectedEntity instanceof Interceptor)
            selectedEntity = game.GetInterceptor(selectedEntity.GetID());
        else if(selectedEntity instanceof Torpedo)
            selectedEntity = game.GetTorpedo(selectedEntity.GetID());
        else if(selectedEntity instanceof RadarStation)
            selectedEntity = game.GetRadarStation(selectedEntity.GetID());
        else if(selectedEntity instanceof Player)
            selectedEntity = game.GetPlayer(selectedEntity.GetID());
        else if(selectedEntity instanceof Infantry)
            selectedEntity = game.GetInfantry(selectedEntity.GetID());
        else if(selectedEntity instanceof Tank)
            selectedEntity = game.GetTank(selectedEntity.GetID());
        else if(selectedEntity instanceof ResourceDeposit)
            selectedEntity = game.GetResourceDeposit(selectedEntity.GetID());
        else if(selectedEntity instanceof CargoTruck)
            selectedEntity = game.GetCargoTruck(selectedEntity.GetID());
        else if(selectedEntity instanceof Shipyard)
            selectedEntity = game.GetShipyard(selectedEntity.GetID());
        else if(selectedEntity instanceof Ship)
            selectedEntity = game.GetShip(selectedEntity.GetID());
        else if(selectedEntity instanceof Submarine)
            selectedEntity = game.GetShip(selectedEntity.GetID());

        if(CurrentView != null)
            CurrentView.EntityUpdated(selectedEntity);

        if(selectedEntity != null)
            DrawEntityOverlay(selectedEntity);
    }

    public void SelectOwner(View view)
    {
        if(game.GetOwner(selectedEntity) != null)
        {
            ShowBasicOKDialog(getString(R.string.no_owner));
        }
        else if(!(selectedEntity instanceof Player))
        {
            SelectEntity(selectedEntity);
        }
    }

    public MapEntity GetSelectedEntity()
    {
        return selectedEntity;
    }

    public void GoToSelectedEntity(final boolean bZoomIn)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                LatLng location = selectedLocation;

                if(selectedEntity != null)
                {
                    if((!(selectedEntity instanceof Player) || selectedEntity.GetID() == game.GetOurPlayerID()))
                        location = Utilities.GetLatLng(selectedEntity.GetPosition());
                }

                if(location != null)
                {
                    if (bZoomIn)
                    {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, fltZoomToLevel));
                    }
                    else
                    {
                        map.animateCamera(CameraUpdateFactory.newLatLng(location));
                    }
                }
            }
        });
    }

    public ReportsStatus GetReportsStatus() { return reportStatus; }

    public InteractionMode GetInteractionMode()
    {
        return interactionMode;
    }

    public void ResetInteractionMode()
    {
        interactionMode = InteractionMode.STANDARD;
    }

    public void InformationMode(boolean bRebuildMap)
    {
        interactionMode = InteractionMode.STANDARD;
        RemoveTargettingMapUI();
        ReturnToMainView();

        if(bRebuildMap)
            RebuildMap();
    }

    public void PrivacyZoneMode()
    {
        interactionMode = InteractionMode.PRIVACY_ZONES;
        RebuildMap();
        ReturnToMainView();
    }

    public void ChooseNewHomebaseMode(AirplaneInterface aircraft)
    {
        interactionMode = InteractionMode.SELECT_NEW_HOMEBASE;
        homebaseChangeAircraft = aircraft;
        RebuildMap();
        ReturnToMainView();
    }

    public void ElectronicWarfareMode(Airplane aircraft)
    {
        interactionMode = InteractionMode.ELECTRONIC_WARFARE;
        homebaseChangeAircraft = aircraft;
        geoTargetingOrigin = aircraft.GetPosition();
        RebuildMap();
        ReturnToMainView();
    }

    public void UnitCommandMode(EntityPointer movable)
    {
        interactionMode = InteractionMode.UNIT_COMMAND;
        movableEntity = movable;
        ClearSelectedEntity();
        RebuildMap();

        /*if(movable.GetType() == EntityType.AIRPLANE || movable.GetType() == EntityType.STORED_AIRPLANE)
            DrawAirspaceCircles();*/

        ReturnToMainView();
    }

    public void MoveOrderMode(EntityPointer movable, List<EntityPointer> Movables)
    {
        interactionMode = InteractionMode.MOVE_ORDER;
        movableEntity = movable;
        MovableEntities = Movables;
        RemoveTargettingMapUI();
        DrawAllMovableUI();

        //TODO: This is where some code will need to go for radars and whatnot as well as airspace.

        /*if(movable.GetType() == EntityType.AIRPLANE || movable.GetType() == EntityType.STORED_AIRPLANE)
            DrawAirspaceCircles();*/

        ReturnToMainView();
    }

    public void TransferCargoMode(LaunchEntity hauler, Haulable cargo, boolean bDropFromCargo)
    {
        interactionMode = InteractionMode.TRANSFER_CARGO;

        if(hauler != null)
        {
            cargoTransferEntity = hauler;
            cargoToTransfer = cargo;
            bFromCargo = bDropFromCargo;

            if(hauler instanceof StoredAirplane)
            {
                MapEntity host = ((StoredAirplane)hauler).GetHomeBase().GetMapEntity(game);

                if(host != null)
                    geoTargetingOrigin = host.GetPosition();
            }
            else if(hauler instanceof MapEntity)
            {
                geoTargetingOrigin = ((MapEntity)hauler).GetPosition();
            }

            ClearSelectedEntity();
            ReturnToMainView();
        }
        else if(cargo instanceof LandUnit)
        {
            cargoTransferEntity = null;
            cargoToTransfer = cargo;
            geoTargetingOrigin = ((LandUnit)cargo).GetPointer().GetMapEntity(game).GetPosition();
            ClearSelectedEntity();
            ReturnToMainView();
        }
    }

    public void LoadLootMode(LaunchEntity lootReceiver)
    {
        interactionMode = InteractionMode.LOAD_LOOT;
        targeter = lootReceiver.GetPointer();

        if(lootReceiver instanceof StoredAirplane)
        {
            MapEntity host = ((StoredAirplane)lootReceiver).GetHomeBase().GetMapEntity(game);

            if(host != null)
                geoTargetingOrigin = host.GetPosition();
        }
        else if(lootReceiver instanceof MapEntity)
        {
            geoTargetingOrigin = ((MapEntity)lootReceiver).GetPosition();
        }

        ClearSelectedEntity();

        ReturnToMainView();
    }

    public void SetTargetMode(EntityPointer targeterPointer, List<EntityPointer> Commandables)
    {
        interactionMode = InteractionMode.SET_TARGET;
        ClearEntityOverlays();
        targeter = targeterPointer;
        MovableEntities = Commandables;
        movableEntity = targeterPointer;

        ReturnToMainView();
    }

    public void TurnInfantryMode(int lInfantryID)
    {
        Infantry infantry = game.GetInfantry(lInfantryID);

        if(infantry != null)
        {
            geoTargetingOrigin = infantry.GetPosition();
        }

        if(geoTargetingOrigin != null)
        {
            interactionMode = InteractionMode.TURN_INFANTRY;
            targeter = infantry.GetPointer();
        }

        ReturnToMainView();
    }

    public void SeekFuelMode(EntityPointer refueleePointer)
    {
        switch(refueleePointer.GetType())
        {
            case AIRPLANE:
            {
                Airplane refueleeAircraft = game.GetAirplane(refueleePointer.GetID());
                geoTargetingOrigin = refueleeAircraft.GetPosition();

                if(geoTargetingOrigin != null)
                {
                    interactionMode = InteractionMode.SEEK_FUEL;
                    targeter = refueleePointer;
                    RebuildMap();
                }
            }
            break;

            case SHIP:
            {
                Ship refueleeShip = game.GetShip(refueleePointer.GetID());
                geoTargetingOrigin = refueleeShip.GetPosition();

                if(geoTargetingOrigin != null)
                {
                    interactionMode = InteractionMode.SEEK_FUEL;
                    targeter = refueleePointer;
                    RebuildMap();
                }
            }
            break;

            case SUBMARINE:
            {
                Submarine refueleeSubmarine = game.GetSubmarine(refueleePointer.GetID());
                geoTargetingOrigin = refueleeSubmarine.GetPosition();

                if(geoTargetingOrigin != null)
                {
                    interactionMode = InteractionMode.SEEK_FUEL;
                    targeter = refueleePointer;
                    RebuildMap();
                }
            }
            break;
        }

        ReturnToMainView();
    }

    public void ProvideFuelMode(EntityPointer refuelerPointer)
    {
        switch(refuelerPointer.GetType())
        {
            case AIRPLANE:
            {
                Airplane aircraft = game.GetAirplane(refuelerPointer.GetID());

                if(aircraft != null)
                {
                    geoTargetingOrigin = aircraft.GetPosition();

                    if(geoTargetingOrigin != null)
                    {
                        interactionMode = InteractionMode.PROVIDE_FUEL;
                        targeter = refuelerPointer;
                        RebuildMap();
                    }
                }
            }
            break;

            case SHIP:
            {
                Ship ship = game.GetShip(refuelerPointer.GetID());

                if(ship != null)
                {
                    geoTargetingOrigin = ship.GetPosition();

                    if(geoTargetingOrigin != null)
                    {
                        interactionMode = InteractionMode.PROVIDE_FUEL;
                        targeter = refuelerPointer;
                        RebuildMap();
                    }
                }
            }
            break;

            case SUBMARINE:
            {
                Submarine submarine = game.GetSubmarine(refuelerPointer.GetID());

                if(submarine != null)
                {
                    geoTargetingOrigin = submarine.GetPosition();

                    if(geoTargetingOrigin != null)
                    {
                        interactionMode = InteractionMode.PROVIDE_FUEL;
                        targeter = refuelerPointer;
                        RebuildMap();
                    }
                }
            }
            break;
        }

        ReturnToMainView();
    }

    public void CaptureTargetMode(EntityPointer infantry)
    {
        Infantry capturer = (Infantry)infantry.GetEntity(game);
        geoTargetingOrigin = capturer.GetPosition();

        if(geoTargetingOrigin != null)
        {
            interactionMode = InteractionMode.CAPTURE_TARGET;
            targeter = infantry;
            movableEntity = infantry;
            //RebuildMap();
        }

        ReturnToMainView();
    }

    public void LiberateTargetMode(EntityPointer infantry)
    {
        Infantry capturer = (Infantry)infantry.GetEntity(game);
        geoTargetingOrigin = capturer.GetPosition();

        if(geoTargetingOrigin != null)
        {
            interactionMode = InteractionMode.LIBERATE_TARGET;
            targeter = infantry;
            movableEntity = infantry;
            //RebuildMap();
        }

        ReturnToMainView();
    }

    public void TransferAccountMode(Player fromPlayer)
    {
        geoTargetingOrigin = fromPlayer.GetPosition();

        if(geoTargetingOrigin != null)
        {
            interactionMode = InteractionMode.TRANSFER_ACCOUNT;
            transferAccountFromPlayer = fromPlayer;
            //RebuildMap();
        }

        ReturnToMainView();
    }

    public void MissileTargetMode(int lSiteID, int lSlotNo, SystemType systemType, boolean bAirburstMode)
    {
        MissileType type = null;

        switch(systemType)
        {
            case MISSILE_SITE:
            {
                MissileSite site = game.GetMissileSite(lSiteID);
                type = game.GetConfig().GetMissileType(site.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = site.GetPosition();
                targetingMissileType = type;

                if(type != null && type.GetMissileRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(site.GetPosition()))
                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case ARTILLERY_GUN:
            {
                ArtilleryGun artillery = game.GetArtilleryGun(lSiteID);
                type = game.GetConfig().GetMissileType(artillery.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = artillery.GetPosition();
                targetingMissileType = type;

                if(type != null && type.GetMissileRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(artillery.GetPosition()))
                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case SHIP_ARTILLERY:
            {
                Ship ship = game.GetShip(lSiteID);
                type = game.GetConfig().GetMissileType(ship.GetArtillerySystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = ship.GetPosition();
                targetingMissileType = type;

                if(type != null && type.GetMissileRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(ship.GetPosition()))
                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case SHIP_MISSILES:
            {
                Ship ship = game.GetShip(lSiteID);
                type = game.GetConfig().GetMissileType(ship.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = ship.GetPosition();
                targetingMissileType = type;

                if(type != null && type.GetMissileRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(ship.GetPosition()))
                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case SUBMARINE_MISSILES:
            {
                Submarine submarine = game.GetSubmarine(lSiteID);
                type = game.GetConfig().GetMissileType(submarine.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = submarine.GetPosition();
                targetingMissileType = type;

                if(type != null && type.GetMissileRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(submarine.GetPosition()))
                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case SUBMARINE_ICBM:
            {
                Submarine submarine = game.GetSubmarine(lSiteID);
                type = game.GetConfig().GetMissileType(submarine.GetICBMSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = submarine.GetPosition();
                targetingMissileType = type;

                if(type != null && type.GetMissileRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(submarine.GetPosition()))
                            .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case AIRCRAFT_MISSILES:
            {
                Airplane aircraft = game.GetAirplane(lSiteID);
                type = game.GetConfig().GetMissileType(aircraft.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = aircraft.GetPosition();
                targetingMissileType = type;

                if(type != null)
                {
                    if(type.GetMissileRange() < 20038)
                    {
                        targetRange = map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(aircraft.GetPosition()))
                                .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                                .strokeWidth(5.0f));
                    }
                }
            }
            break;

            case TANK_ARTILLERY:
            case TANK_MISSILES:
            {
                Tank tank = game.GetTank(lSiteID);
                type = game.GetConfig().GetMissileType(tank.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = tank.GetPosition();
                targetingMissileType = type;

                if(type != null)
                {
                    if(type.GetMissileRange() < 20038)
                    {
                        targetRange = map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(tank.GetPosition()))
                                .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                                .strokeWidth(5.0f));
                    }
                }
            }
            break;
        }

        if(targetingMissileType != null)
        {
            interactionMode = InteractionMode.TARGET_MISSILE;
            targetSystemType = systemType;
            lTargettingSiteID = lSiteID;
            lTargettingSlotNo = lSlotNo;
            bAirburst = bAirburstMode;
        }

        ReturnToMainView();
    }

    public void SetArtilleryTargetMode(int lSiteID, SystemType systemType)
    {
        switch(systemType)
        {
            case MISSILE_SITE:
            {
                MissileSite site = game.GetMissileSite(lSiteID);
                geoTargetingOrigin = site.GetPosition();
            }
            break;

            case ARTILLERY_GUN:
            {
                ArtilleryGun artillery = game.GetArtilleryGun(lSiteID);
                geoTargetingOrigin = artillery.GetPosition();
            }
            break;

            case TANK_ARTILLERY:
            case TANK_MISSILES:
            {
                Tank tank = game.GetTank(lSiteID);
                geoTargetingOrigin = tank.GetPosition();
            }
            break;

            case SHIP_ARTILLERY:
            {
                Ship ship = game.GetShip(lSiteID);
                geoTargetingOrigin = ship.GetPosition();
            }
            break;
        }

        if(geoTargetingOrigin != null)
        {
            interactionMode = InteractionMode.SET_ARTILLERY_TARGET;
            targetSystemType = systemType;
            lTargettingSiteID = lSiteID;
        }

        ReturnToMainView();
    }

    public void TorpedoTargetMode(int lSiteID, int lSlotNo, SystemType systemType)
    {
        TorpedoType type = null;

        switch(systemType)
        {
            case SHIP_TORPEDOES:
            {
                Ship ship = game.GetShip(lSiteID);
                type = game.GetConfig().GetTorpedoType(ship.GetTorpedoSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = ship.GetPosition();
                targetingTorpedoType = type;

                targetRange = map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(ship.GetPosition()))
                        .radius(type.GetTorpedoRange() * Defs.METRES_PER_KM)
                        .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                        .strokeWidth(5.0f));
            }
            break;

            case SUBMARINE_TORPEDO:
            {
                Submarine submarine = game.GetSubmarine(lSiteID);
                type = game.GetConfig().GetTorpedoType(submarine.GetTorpedoSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = submarine.GetPosition();
                targetingTorpedoType = type;

                targetRange = map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(submarine.GetPosition()))
                        .radius(type.GetTorpedoRange() * Defs.METRES_PER_KM)
                        .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                        .strokeWidth(5.0f));
            }
            break;
        }

        if(targetingTorpedoType != null)
        {
            interactionMode = InteractionMode.TARGET_TORPEDO;
            targetSystemType = systemType;
            lTargettingSiteID = lSiteID;
            lTargettingSlotNo = lSlotNo;
        }

        ReturnToMainView();
    }

    public void PlaceBlueprintMode(EntityType structureType, ResourceType resourceType)
    {
        interactionMode = InteractionMode.PLACE_BLUEPRINT;
        blueprintType = structureType;
        blueprintResourceType = resourceType;

        ReturnToMainView();
    }

    public void BuildStructureMode(int lCommandCenterID, EntityType structureType, ResourceType resourceType, boolean bUseSubstitutes)
    {
        interactionMode = InteractionMode.BUILD_STRUCTURE;
        lTargettingSiteID = lCommandCenterID;
        blueprintType = structureType;
        blueprintResourceType = resourceType;
        bUseSubstitutes = bUseSubstitutes;

        ReturnToMainView();
    }

    public void BuildStructure(GeoCoord geoLocation)
    {
        ((BottomBuildStructure)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, map);
    }

    public void MissileSelectForTarget(final GeoCoord geoTarget, MapEntity entityTarget, final String strTargetName)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SetView(new SelectMissileView(game, me, geoTarget, entityTarget, strTargetName));
            }
        });
    }

    public void InterceptorSelectForTarget(final int lTargetID, final String strTargetName, final MapEntity targetEntity)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SetView(new SelectInterceptorView(game, me, lTargetID, strTargetName, targetEntity));
            }
        });
    }

    public void SelectForAction(GeoCoord geoCoord, MapEntity entity, EntityType type, MoveOrders order)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                switch(type)
                {
                    case AIRPLANE: SetView(new QuickSelectActionView(game, me, geoCoord, entity, EntityType.AIRPLANE, order)); break;
                    case INFANTRY: SetView(new QuickSelectActionView(game, me, geoCoord, entity, EntityType.INFANTRY, order)); break;
                    case TANK: SetView(new QuickSelectActionView(game, me, geoCoord, entity, EntityType.TANK, order)); break;
                    case CARGO_TRUCK: SetView(new QuickSelectActionView(game, me, geoCoord, entity, EntityType.CARGO_TRUCK, order)); break;
                    case ARTILLERY_GUN: SetView(new QuickSelectActionView(game, me, geoCoord, entity, EntityType.ARTILLERY_GUN, order)); break;
                }
            }
        });
    }

    public void DesignateMissileTarget(int lSiteID, int lSlotNo, GeoCoord geoTarget, MapEntity entity, SystemType systemType)
    {
        MissileTargetMode(lSiteID, lSlotNo, systemType, false);
        TargetMissile(geoTarget, entity, bAirburst);
    }

    public void DesignateTorpedoTarget(int lSiteID, int lSlotNo, GeoCoord geoTarget, MapEntity entity, SystemType systemType)
    {
        TorpedoTargetMode(lSiteID, lSlotNo, systemType);
        TargetTorpedo(geoTarget, entity);
    }

    public void DesignateMoveToPosition(LaunchEntity entity, GeoCoord geoTarget, MapEntity targetEntity)
    {
        UnitCommandMode(entity.GetPointer());
        PerformUnitCommand(entity.GetPointer(), geoTarget, targetEntity);
    }

    public void DesignateAttackTarget(MapEntity targeter, MapEntity target, boolean bKamikaze)
    {
        SetTargetMode(targeter.GetPointer(), MovableEntities);
        SetTarget(movableEntity, null, null, target);
    }

    public void DesignateCaptureTarget(InfantryInterface capturer)
    {
        CaptureTargetMode(capturer.GetInfantry().GetPointer());
    }

    public void DesignateInterceptorTarget(int lSiteID, int lSlotNo, MapEntity target, SystemType systemType)
    {
        InterceptorTargetMode(lSiteID, lSlotNo, systemType);
        TargetInterceptor(target);
    }

    public void InterceptorTargetMode(int lSiteID, int lSlotNo, SystemType systemType)
    {
        switch(systemType)
        {
            case SAM_SITE:
            {
                SAMSite site = game.GetSAMSite(lSiteID);
                InterceptorType type = game.GetConfig().GetInterceptorType(site.GetInterceptorSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = site.GetPosition();
                targetingInterceptorType = type;

                if(type.GetInterceptorRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoTargetingOrigin))
                            .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case SHIP_INTERCEPTORS:
            {
                Ship ship = game.GetShip(lSiteID);
                InterceptorType type = game.GetConfig().GetInterceptorType(ship.GetInterceptorSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = ship.GetPosition();
                targetingInterceptorType = type;

                if(type.GetInterceptorRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoTargetingOrigin))
                            .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case AIRCRAFT_INTERCEPTORS:
            {
                Airplane aircraft = game.GetAirplane(lSiteID);
                InterceptorType type = game.GetConfig().GetInterceptorType(aircraft.GetInterceptorSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = aircraft.GetPosition();
                targetingInterceptorType = type;

                if(type.GetInterceptorRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoTargetingOrigin))
                            .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;

            case TANK_INTERCEPTORS:
            {
                Tank tank = game.GetTank(lSiteID);
                InterceptorType type = game.GetConfig().GetInterceptorType(tank.GetMissileSystem().GetSlotMissileType(lSlotNo));
                geoTargetingOrigin = tank.GetPosition();
                targetingInterceptorType = type;

                if(type.GetInterceptorRange() < 20038)
                {
                    targetRange = map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoTargetingOrigin))
                            .radius(game.GetConfig().GetInterceptorRange(type) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                            .strokeWidth(5.0f));
                }
            }
            break;
        }

        interactionMode = InteractionMode.TARGET_INTERCEPTOR;
        lTargettingSiteID = lSiteID;
        lTargettingSlotNo = lSlotNo;
        targetSystemType = systemType;

        ReturnToMainView();
    }

    private void FindRangeTo(GeoCoord geoRangeFind2)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        targetTrajectory = map.addPolyline(new PolylineOptions()
                .add(Utilities.GetLatLng(geoRangeFind1))
                .add(Utilities.GetLatLng(geoRangeFind2))
                .geodesic(true)
                .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));

        ((BottomRangeFinder)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoRangeFind2, targetTrajectory, map);
    }

    public void SetTarget(EntityPointer movableEntity, GeoCoord geoTarget, List<EntityPointer> Targeters, MapEntity entity)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
        }

        if(!CircleOverlays.isEmpty())
        {
            for(Circle circle : CircleOverlays)
            {
                circle.remove();
            }

            CircleOverlays.clear();
        }

        if(!MassCommandTrajectories.isEmpty())
        {
            for(Polyline line : MassCommandTrajectories.values())
            {
                if(line != null)
                {
                    line.remove();
                    line = null;
                }
            }

            MassCommandTrajectories.clear();
        }

        PatternItem dash = new Dash(10);
        PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        if(movableEntity != null && (geoTarget != null || entity != null))
        {
            GeoCoord geoSource = game.GetPosition(movableEntity.GetEntity(game));

            targetTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoSource))
                    .add(Utilities.GetLatLng(geoTarget != null && entity == null ? geoTarget : entity.GetPosition()))
                    .pattern(trajectoryPattern)
                    .geodesic(true)
                    .width(3.0f)
                    .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));
        }
        else if(Targeters != null)
        {
            for(EntityPointer pointer : Targeters)
            {
                GeoCoord geoSource = game.GetPosition(pointer.GetEntity(game));

                if(geoSource != null && !MassCommandTrajectories.containsKey(pointer))
                {
                    MassCommandTrajectories.put(pointer, map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoTarget != null && entity == null ? geoTarget : entity.GetPosition()))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))));
                }

                /*if(pointer.GetMapEntity(game) instanceof LauncherInterface)
                {
                    MapEntity mapEntity = pointer.GetMapEntity(game);
                    MissileSystem system = ((LauncherInterface)mapEntity).GetMissileSystem();

                    for(Map.Entry<Integer, Integer> TypeCount : system.GetTypeCounts().entrySet())
                    {
                        MissileType type = game.GetConfig().GetMissileType(TypeCount.getKey());
                        float fltRangeRadius = Math.min(ClientDefs.MAX_RANGE_RING_THICKNESS, TypeCount.getValue());

                        CircleOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(geoTarget != null ? geoTarget : entity.GetPosition()))
                                .radius(game.GetConfig().GetMissileRange(type) * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))
                                .strokeWidth(fltRangeRadius)));
                    }
                }*/
            }
        }

        movableTarget = map.addMarker(new MarkerOptions()
            .position(Utilities.GetLatLng(geoTarget != null && entity == null ? geoTarget : entity.GetPosition()))
            .anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_attack_waypoint)));

        if(geoTarget != null)
        {
            ((BottomUnitCommand)((MainNormalView) CurrentView).GetBottomView()).LocationSelected(geoTarget, targetTrajectory, MassCommandTrajectories, movableTarget, map);
        }
        else
        {
            ((BottomUnitCommand)((MainNormalView) CurrentView).GetBottomView()).TargetSelected(entity, targetTrajectory, MassCommandTrajectories, movableTarget, map);
        }
    }

    public void LiberateTarget(EntityPointer liberator, List<EntityPointer> Targeters, MapEntity entity)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
        }

        if(!CircleOverlays.isEmpty())
        {
            for(Circle circle : CircleOverlays)
            {
                circle.remove();
            }

            CircleOverlays.clear();
        }

        if(!MassCommandTrajectories.isEmpty())
        {
            for(Polyline line : MassCommandTrajectories.values())
            {
                if(line != null)
                {
                    line.remove();
                    line = null;
                }
            }

            MassCommandTrajectories.clear();
        }

        PatternItem dash = new Dash(10);
        PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        if(liberator != null && entity != null)
        {
            if(movableEntity != null)
            {
                GeoCoord geoSource = game.GetPosition(liberator.GetEntity(game));

                targetTrajectory = map.addPolyline(new PolylineOptions()
                        .add(Utilities.GetLatLng(geoSource))
                        .add(Utilities.GetLatLng(entity.GetPosition()))
                        .pattern(trajectoryPattern)
                        .geodesic(true)
                        .width(3.0f)
                        .color(Color.MAGENTA));
            }
            else if(Targeters != null)
            {
                for(EntityPointer pointer : Targeters)
                {
                    GeoCoord geoSource = game.GetPosition(pointer.GetEntity(game));

                    if(geoSource != null && !MassCommandTrajectories.containsKey(pointer))
                    {
                        MassCommandTrajectories.put(pointer, map.addPolyline(new PolylineOptions()
                                .add(Utilities.GetLatLng(geoSource))
                                .add(Utilities.GetLatLng(entity.GetPosition()))
                                .pattern(trajectoryPattern)
                                .geodesic(true)
                                .width(3.0f)
                                .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour))));
                    }
                }
            }

            movableTarget = map.addMarker(new MarkerOptions()
                    .position(Utilities.GetLatLng(entity.GetPosition()))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_liberate_waypoint)));

            ((BottomUnitCommand)((MainNormalView) CurrentView).GetBottomView()).TargetSelected(entity, targetTrajectory, MassCommandTrajectories, movableTarget, map);
        }

    }

    public void SetElectronicWarfareTarget(MapEntity entity)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        PatternItem dash = new Dash(10);
        PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        targetTrajectory = map.addPolyline(new PolylineOptions()
                .add(Utilities.GetLatLng(geoTargetingOrigin))
                .add(Utilities.GetLatLng(entity.GetPosition()))
                .pattern(trajectoryPattern)
                .width(3.0f)
                .geodesic(true)
                .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));

        if(homebaseChangeAircraft != null)
        {
            ((BottomElectronicWarfare)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(entity, targetTrajectory, map);
        }
    }

    public void SelectRefueler(MapEntity refueler)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        PatternItem dash = new Dash(10);
        PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        targetTrajectory = map.addPolyline(new PolylineOptions()
                .add(Utilities.GetLatLng(geoTargetingOrigin))
                .add(Utilities.GetLatLng(refueler.GetPosition()))
                .pattern(trajectoryPattern)
                .width(3.0f)
                .geodesic(true)
                .color(Utilities.ColourFromAttr(this, R.attr.FuelColour)));

        if(targeter != null)
        {
            ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(refueler, targetTrajectory, null, null, map);
        }
    }

    public void SelectRefuelee(MapEntity refuelee)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        PatternItem dash = new Dash(10);
        PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        targetTrajectory = map.addPolyline(new PolylineOptions()
                .add(Utilities.GetLatLng(geoTargetingOrigin))
                .add(Utilities.GetLatLng(refuelee.GetPosition()))
                .pattern(trajectoryPattern)
                .width(3.0f)
                .geodesic(true)
                .color(Utilities.ColourFromAttr(this, R.attr.FuelColour)));

        if(targeter != null)
        {
            ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(refuelee, targetTrajectory, null, null, map);
        }
    }

    public void TargetMissile(GeoCoord geoLocation, MapEntity entity, boolean bAirburstFuze)
    {
        for(Circle targetBlastRadius : targetBlastRadii)
        {
            if (targetBlastRadius != null)
            {
                targetBlastRadius.remove();
            }
        }

        targetBlastRadii.clear();

        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        if(entity != null)
        {
            targetTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoTargetingOrigin))
                    .add(Utilities.GetLatLng(entity.GetPosition()))
                    .geodesic(true)
                    .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));

            if(targetingMissileType != null)
            {
                if(targetingMissileType.GetSonobuoy())
                {
                    targetBlastRadii.add(map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(entity.GetPosition()))
                            .radius(Defs.SONAR_RANGE * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(this, R.attr.SonarColour))
                            .strokeWidth(3.0f)));
                }

                targetBlastRadii.add(map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(entity.GetPosition()))
                        .radius(MissileStats.GetMissileEMPRadius(targetingMissileType, bAirburstFuze) * Defs.METRES_PER_KM)
                        .fillColor(Utilities.ColourFromAttr(this, R.attr.EMPColour))
                        .strokeWidth(0.0f)));

                for(float fltBlastRadius : MissileStats.GetBlastRadii(targetingMissileType, bAirburstFuze))
                {
                    targetBlastRadii.add(map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(entity.GetPosition()))
                            .radius(fltBlastRadius * Defs.METRES_PER_KM)
                            .fillColor(Utilities.ColourFromAttr(this, R.attr.BlastRadiusStageColour))
                            .strokeWidth(0.0f)));
                }

                ((BottomMissileTarget)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(entity, targetTrajectory, map);
            }
        }
        else
        {
            targetTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoTargetingOrigin))
                    .add(Utilities.GetLatLng(geoLocation))
                    .geodesic(true)
                    .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));

            if(targetingMissileType != null)
            {
                targetBlastRadii.add(map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(geoLocation))
                        .radius(MissileStats.GetMissileEMPRadius(targetingMissileType, bAirburstFuze) * Defs.METRES_PER_KM)
                        .fillColor(Utilities.ColourFromAttr(this, R.attr.EMPColour))
                        .strokeWidth(0.0f)));

                for(float fltBlastRadius : MissileStats.GetBlastRadii(targetingMissileType, bAirburstFuze))
                {
                    targetBlastRadii.add(map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoLocation))
                            .radius(fltBlastRadius * Defs.METRES_PER_KM)
                            .fillColor(Utilities.ColourFromAttr(this, R.attr.BlastRadiusStageColour))
                            .strokeWidth(0.0f)));
                }

                ((BottomMissileTarget)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, targetTrajectory, map);
            }
        }
    }

    public void TransferAccount(GeoCoord geoLocation, Player player)
    {
        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        targetTrajectory = map.addPolyline(new PolylineOptions()
                .add(Utilities.GetLatLng(geoTargetingOrigin))
                .add(Utilities.GetLatLng(geoLocation))
                .geodesic(true)
                .color(Utilities.ColourFromAttr(this, R.attr.InfoColour)));

        if(transferAccountFromPlayer != null)
        {
            ((BottomTransferAccount)((MainNormalView)CurrentView).GetBottomView()).PlayerSelected(player, targetTrajectory, map);
        }
    }

    public void TargetTorpedo(GeoCoord geoLocation, MapEntity entity)
    {
        for(Circle targetBlastRadius : targetBlastRadii)
        {
            if (targetBlastRadius != null)
            {
                targetBlastRadius.remove();
            }
        }

        targetBlastRadii.clear();

        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        if(entity instanceof NavalVessel)
        {
            targetTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoTargetingOrigin))
                    .add(Utilities.GetLatLng(entity.GetPosition()))
                    .geodesic(true)
                    .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));

            if(targetingTorpedoType != null)
            {
                targetBlastRadii.add(map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(entity.GetPosition()))
                        .radius(targetingTorpedoType.GetBlastRadius() * Defs.METRES_PER_KM)
                        .fillColor(Utilities.ColourFromAttr(this, R.attr.BlastRadiusStageColour))
                        .strokeWidth(0.0f)));

                ((BottomTorpedoTarget)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(entity, targetTrajectory, map);
            }
        }
        else
        {
            targetTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoTargetingOrigin))
                    .add(Utilities.GetLatLng(geoLocation))
                    .geodesic(true)
                    .color(Utilities.ColourFromAttr(this, R.attr.MissilePathColour)));

            if(targetingTorpedoType != null)
            {
                targetBlastRadii.add(map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(geoLocation))
                        .radius(targetingTorpedoType.GetBlastRadius() * Defs.METRES_PER_KM)
                        .fillColor(Utilities.ColourFromAttr(this, R.attr.BlastRadiusStageColour))
                        .strokeWidth(0.0f)));

                ((BottomTorpedoTarget)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, targetTrajectory, map);
            }
        }
    }

    private void NewAirbaseSelected(MapEntity homebase)
    {
        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
        }

        GeoCoord geoSource = ((MapEntity)homebaseChangeAircraft).GetPosition();


        if(geoSource != null)
        {
            PatternItem dash = new Dash(10);
            PatternItem gap = new Gap(20);

            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

            movableTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoSource))
                    .add(Utilities.GetLatLng(homebase.GetPosition()))
                    .pattern(trajectoryPattern)
                    .geodesic(true)
                    .width(3.0f)
                    .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

            CircleOverlays.add(map.addCircle(new CircleOptions()
                    .center(Utilities.GetLatLng(((MapEntity)homebaseChangeAircraft).GetPosition()))
                    .radius(game.GetFuelableRange(homebaseChangeAircraft.GetCurrentFuel(), Defs.GetAircraftRange(((LaunchEntity)homebaseChangeAircraft).GetEntityType())) * Defs.METRES_PER_KM)
                    .strokeColor(Utilities.ColourFromAttr(me, R.attr.FuelColour))
                    .strokeWidth(3.0f)));
        }

        ((BottomNewHomebase)((MainNormalView)CurrentView).GetBottomView()).AirbaseSelected(homebase, movableTrajectory, map);
    }

    private void PerformUnitCommand(EntityPointer movable, GeoCoord geoLocation, MapEntity targetEntity)
    {
        switch(movable.GetType())
        {
            case AIRPLANE:
            case STORED_AIRPLANE:
            {
                AirplaneInterface aircraft = ((AirplaneInterface)movable.GetEntity(game));

                if(movableTrajectory != null)
                {
                    movableTrajectory.remove();
                }

                if(movableTarget != null)
                {
                    movableTarget.remove();
                }

                GeoCoord geoSource = game.GetAircraftPosition(aircraft);

                if(geoSource != null)
                {
                    PatternItem dash = new Dash(10);
                    PatternItem gap = new Gap(20);

                    List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                    movableTrajectory = map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

                    CircleOverlays.add(map.addCircle(new CircleOptions()
                            .center(Utilities.GetLatLng(geoSource))
                            .radius(game.GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(((LaunchEntity)aircraft).GetEntityType())) * Defs.METRES_PER_KM)
                            .strokeColor(Utilities.ColourFromAttr(me, R.attr.FuelColour))
                            .strokeWidth(3.0f)));

                    movableTarget = map.addMarker(new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

                    try
                    {
                        if(aircraft.Flying() && targetEntity instanceof Airbase)
                            ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(targetEntity, movableTrajectory, null, null, map);
                        else
                            ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, null, movableTarget, map);
                    }
                    catch(ClassCastException ex)
                    {
                        InformationMode(true);
                    }
                }
            }
            break;

            case INFANTRY:
            {
                Infantry infantry = ((Infantry)movable.GetEntity(game));

                if(movableTrajectory != null)
                {
                    movableTrajectory.remove();
                }

                if(movableTarget != null)
                {
                    movableTarget.remove();
                }

                GeoCoord geoSource = infantry.GetPosition();

                if(geoSource != null)
                {
                    PatternItem dash = new Dash(10);
                    PatternItem gap = new Gap(20);

                    List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                    movableTrajectory = map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

                    movableTarget = map.addMarker(new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

                    ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, null, movableTarget, map);
                }
            }
            break;

            case TANK:
            {
                Tank tank = ((Tank)movable.GetEntity(game));

                if(movableTrajectory != null)
                {
                    movableTrajectory.remove();
                }

                if(movableTarget != null)
                {
                    movableTarget.remove();
                }

                GeoCoord geoSource = tank.GetPosition();

                if(geoSource != null)
                {
                    PatternItem dash = new Dash(10);
                    PatternItem gap = new Gap(20);

                    List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                    movableTrajectory = map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

                    movableTarget = map.addMarker(new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

                    ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, null, movableTarget, map);
                }
            }
            break;

            case CARGO_TRUCK:
            {
                CargoTruck truck = ((CargoTruck)movable.GetEntity(game));

                if(movableTrajectory != null)
                {
                    movableTrajectory.remove();
                }

                if(movableTarget != null)
                {
                    movableTarget.remove();
                }

                GeoCoord geoSource = truck.GetPosition();

                if(geoSource != null)
                {
                    PatternItem dash = new Dash(10);
                    PatternItem gap = new Gap(20);

                    List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                    movableTrajectory = map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

                    movableTarget = map.addMarker(new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

                    ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, null, movableTarget, map);
                }
            }
            break;

            case SHIP:
            {
                Ship ship = ((Ship)movable.GetEntity(game));

                if(movableTrajectory != null)
                {
                    movableTrajectory.remove();
                }

                if(movableTarget != null)
                {
                    movableTarget.remove();
                }

                GeoCoord geoSource = ship.GetPosition();

                if(geoSource != null)
                {
                    PatternItem dash = new Dash(10);
                    PatternItem gap = new Gap(20);

                    List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                    movableTrajectory = map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

                    movableTarget = map.addMarker(new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

                    ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, null, movableTarget, map);
                }
            }
            break;

            case SUBMARINE:
            {
                Submarine ship = ((Submarine)movable.GetEntity(game));

                if(movableTrajectory != null)
                {
                    movableTrajectory.remove();
                }

                if(movableTarget != null)
                {
                    movableTarget.remove();
                }

                GeoCoord geoSource = ship.GetPosition();

                if(geoSource != null)
                {
                    PatternItem dash = new Dash(10);
                    PatternItem gap = new Gap(20);

                    List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                    movableTrajectory = map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

                    movableTarget = map.addMarker(new MarkerOptions()
                            .position(Utilities.GetLatLng(geoLocation))
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

                    ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, null, movableTarget, map);
                }
            }
            break;
        }
    }

    private void MoveToPosition(EntityPointer movableEntity, List<EntityPointer> Movables, GeoCoord geoLocation, MapEntity targetEntity)
    {
        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
        }

        if(!MassCommandTrajectories.isEmpty())
        {
            for(Polyline line : MassCommandTrajectories.values())
            {
                if(line != null)
                {
                    line.remove();
                    line = null;
                }
            }

            MassCommandTrajectories.clear();
        }

        PatternItem dash = new Dash(10);
        PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        List<LatLng> points = new ArrayList<LatLng>();

        if(movableEntity != null)
        {
            GeoCoord geoSource = game.GetPosition(movableEntity.GetEntity(game));

            movableTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoSource))
                    .add(Utilities.GetLatLng(geoLocation))
                    .pattern(trajectoryPattern)
                    .geodesic(true)
                    .width(3.0f)
                    .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));
        }
        else if(Movables != null)
        {
            for(EntityPointer pointer : Movables)
            {
                GeoCoord geoSource = game.GetPosition(pointer.GetEntity(game));

                if(geoSource != null && !MassCommandTrajectories.containsKey(pointer))
                {
                    MassCommandTrajectories.put(pointer, map.addPolyline(new PolylineOptions()
                            .add(Utilities.GetLatLng(geoSource))
                            .add(Utilities.GetLatLng(geoLocation))
                            .pattern(trajectoryPattern)
                            .geodesic(true)
                            .width(3.0f)
                            .color(Utilities.ColourFromAttr(this, R.attr.GoodColour))));
                }
            }
        }

        movableTarget = map.addMarker(new MarkerOptions()
                .position(Utilities.GetLatLng(geoLocation))
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint)));

        ((BottomMoveOrder)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableEntity, movableTrajectory, MassCommandTrajectories, movableTarget, map);
    }

    private void MoveToAirbase(Airplane aircraft, EntityPointer airbase)
    {
        MapEntity entity = airbase.GetMapEntity(game);

        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
        }

        GeoCoord geoSource = aircraft.GetPosition();

        if(geoSource != null)
        {
            PatternItem dash = new Dash(10);
            PatternItem gap = new Gap(20);

            List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

            movableTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoSource))
                    .add(Utilities.GetLatLng(entity.GetPosition()))
                    .pattern(trajectoryPattern)
                    .geodesic(true)
                    .width(3.0f)
                    .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

            CircleOverlays.add(map.addCircle(new CircleOptions()
                    .center(Utilities.GetLatLng(geoSource))
                    .radius(game.GetFuelableRange(aircraft.GetCurrentFuel(), Defs.GetAircraftRange(aircraft.GetEntityType())) * Defs.METRES_PER_KM)
                    .strokeColor(Utilities.ColourFromAttr(me, R.attr.FuelColour))
                    .strokeWidth(3.0f)));

            ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(airbase.GetMapEntity(game), movableTrajectory, MassCommandTrajectories, null, map);
        }
    }

    private void TurnInfantry(EntityPointer movable, GeoCoord geoLocation)
    {
        Infantry infantry = ((Infantry)movable.GetEntity(game));

        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
        }

        GeoCoord geoSource = infantry.GetPosition();
            PatternItem dash = new Dash(10);
            PatternItem gap = new Gap(20);

        List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

        movableTrajectory = map.addPolyline(new PolylineOptions()
                .add(Utilities.GetLatLng(geoSource))
                .add(Utilities.GetLatLng(geoLocation))
                .pattern(trajectoryPattern)
                .geodesic(true)
                .width(3.0f)
                .color(Utilities.ColourFromAttr(this, R.attr.GoodColour)));

        ((BottomUnitCommand)((MainNormalView)CurrentView).GetBottomView()).LocationSelected(geoLocation, movableTrajectory, MassCommandTrajectories, movableTarget, map);
    }

    private void TargetInterceptor(MapEntity targetEntity)
    {
        for(Circle targetBlastRadius : targetBlastRadii)
        {
            if (targetBlastRadius != null)
            {
                targetBlastRadius.remove();
            }
        }

        targetBlastRadii.clear();

        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
        }

        if(targetingInterceptorType != null)
        {
            if(targetingInterceptorType.GetNuclear())
            {
                targetBlastRadii.add(map.addCircle(new CircleOptions()
                        .center(Utilities.GetLatLng(targetEntity.GetPosition().GetCopy()))
                        .radius(targetingInterceptorType.GetBlastRadius() * Defs.METRES_PER_KM)
                        .fillColor(Utilities.ColourFromAttr(this, R.attr.BlastRadiusStageColour))
                        .strokeWidth(0.0f)));
            }

            targetTrajectory = map.addPolyline(new PolylineOptions()
                    .add(Utilities.GetLatLng(geoTargetingOrigin))
                    .add(Utilities.GetLatLng(targetEntity.GetPosition()))
                    .geodesic(true)
                    .color(Utilities.ColourFromAttr(this, R.attr.InterceptorPathColour)));

            ((BottomInterceptorTarget)((MainNormalView)CurrentView).GetBottomView()).TargetSelected(targetEntity, targetTrajectory, map);
        }
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
            movableTrajectory = null;
        }

        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
            targetTrajectory = null;
        }

        if(!MassCommandTrajectories.isEmpty())
        {
            for(Polyline line : MassCommandTrajectories.values())
            {
                if(line != null)
                {
                    line.remove();
                    line = null;
                }
            }

            MassCommandTrajectories.clear();
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
            movableTarget = null;
        }

        switch(interactionMode)
        {
            case STANDARD:
            {
                ClearSelectedEntity();
                geoTarget = new GeoCoord(latLng.latitude, latLng.longitude, true);

                if (CurrentView instanceof MainNormalView)
                {
                    selectedLocation = latLng;
                    ((MainNormalView) CurrentView).BottomLayoutShowView(new MapClickView(game, this, latLng));
                }
            }
            break;

            case TARGET_MISSILE:
            {
                TargetMissile(new GeoCoord(latLng.latitude, latLng.longitude, true), null, bAirburst);
            }
            break;

            case SET_TARGET:
            {
                SetTarget(movableEntity, new GeoCoord(latLng.latitude, latLng.longitude, true), MovableEntities, null);
            }
            break;

            case TARGET_TORPEDO:
            {
                TargetTorpedo(new GeoCoord(latLng.latitude, latLng.longitude, true), null);
            }
            break;

            case BUILD_STRUCTURE:
            {
                BuildStructure(new GeoCoord(latLng.latitude, latLng.longitude, true));
            }
            break;

            case TURN_INFANTRY:
            {
                TurnInfantry(targeter, new GeoCoord(latLng.latitude, latLng.longitude, true));
            }
            break;

            case UNIT_COMMAND:
            {
                if(movableEntity != null)
                {
                    PerformUnitCommand(movableEntity, new GeoCoord(latLng.latitude, latLng.longitude, true), null);
                }
            }
            break;

            case MOVE_ORDER:
            {
                MoveToPosition(movableEntity, MovableEntities, new GeoCoord(latLng.latitude, latLng.longitude, true), null);
            }
            break;

            case RANGE_FINDER:
            {
                FindRangeTo(new GeoCoord(latLng.latitude, latLng.longitude, true));
            }
            break;

            case PRIVACY_ZONES:
            {
                PrivacyZone privacyZone = new PrivacyZone(new GeoCoord(latLng.latitude, latLng.longitude, true), ClientDefs.PRIVACY_ZONE_DEFAULT_RADIUS);
                game.AddPrivacyZone(privacyZone);
                selectedPrivacyZone = privacyZone;
                RebuildMap();
                ((PrivacyZonesView)CurrentView).SetPrivacyZone(privacyZone);
            }
            break;
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        geoRangeFind1 = new GeoCoord(latLng.latitude, latLng.longitude, true);

        ClearSelectedEntity();
        RemoveTargettingMapUI();

        interactionMode = InteractionMode.RANGE_FINDER;
        ReturnToMainView();
    }

    @Override
    public void onCircleClick(@NonNull Circle circle)
    {
        if(circle.getTag() != null)
        {
            if(circle.getTag() instanceof MapEntity)
            {
                EntityClicked((MapEntity)circle.getTag());
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        switch(interactionMode)
        {
            case PRIVACY_ZONES:
            {
                selectedPrivacyZone = null;

                if(PrivacyZoneMarkers.containsKey(marker))
                {
                    selectedPrivacyZone = PrivacyZoneMarkers.get(marker);
                    ((PrivacyZonesView)CurrentView).SetPrivacyZone(selectedPrivacyZone);
                }

                RebuildMap();
            }
            break;

            default:
            {
                for(Marker moveMarker : MoveMarkers)
                {
                    if(marker.equals(moveMarker))
                    {
                        onMapClick(marker.getPosition());
                        break;
                    }
                }

                if(marker.equals(movableTarget))
                {
                    onMapClick(marker.getPosition());
                }
                else if(marker.equals(DotncarryMemorial))
                {
                    ClearSelectedEntity();
                    ReturnToMainView();

                    if(CurrentView instanceof MainNormalView)
                    {
                        MainNormalView mainView = (MainNormalView) CurrentView;
                        mainView.BottomLayoutShowView(new DotncarryMemorialView(game, this));
                    }
                }
                else if(marker.equals(Thomas99Memorial))
                {
                    ClearSelectedEntity();
                    ReturnToMainView();

                    if(CurrentView instanceof MainNormalView)
                    {
                        MainNormalView mainView = (MainNormalView) CurrentView;
                        mainView.BottomLayoutShowView(new Thomas99MemorialView(game, this));
                    }
                }
                else
                {
                    MapEntity entity = GetEntityFromMarker(marker);

                    if(entity != null)
                        EntityClicked(entity);
                }
            }
            break;
        }

        return false;
    }

    @Override
    public void onGroundOverlayClick(GroundOverlay overlay)
    {
        switch(interactionMode)
        {
            default:
            {
                MapEntity entity = GetEntityFromOverlay(overlay);

                if(entity != null)
                    EntityClicked(entity);
            }
            break;
        }
    }

    public MapEntity GetEntityFromMarker(Marker marker)
    {
        if(InterceptorMarkers.containsValue(marker))
        {
            Integer lID = Utilities.GetMapKeyByValue(InterceptorMarkers, marker);

            if (lID != null)
            {
                return game.GetInterceptor(lID);
            }
        }

        if(TorpedoMarkers.containsValue(marker))
        {
            Integer lID = Utilities.GetMapKeyByValue(TorpedoMarkers, marker);

            if (lID != null)
            {
                return game.GetTorpedo(lID);
            }
        }

        if(DepositMarkers.containsValue(marker))
        {
            Integer lID = Utilities.GetMapKeyByValue(DepositMarkers, marker);

            if(lID != null)
            {
                return game.GetResourceDeposit(lID);
            }
        }

        if(ThreatMarkers.containsValue(marker))
        {
            Integer lID = Utilities.GetMapKeyByValue(ThreatMarkers, marker);

            if(lID != null)
            {
                return game.GetMissile(lID);
            }
        }

        if(marker.getTag() != null)
        {
            return ((EntityPointer)marker.getTag()).GetMapEntity(game);
        }

        return null;
    }

    public MapEntity GetEntityFromOverlay(GroundOverlay overlay)
    {
        if(overlay.getTag() != null)
        {
            return ((EntityPointer)overlay.getTag()).GetMapEntity(game);
        }

        return null;
    }

    public void EntityClicked(MapEntity entity)
    {
        geoTarget = null;

        if(targetTrajectory != null)
        {
            targetTrajectory.remove();
            targetTrajectory = null;
        }

        if(movableTrajectory != null)
        {
            movableTrajectory.remove();
            movableTrajectory = null;
        }

        if(movableTarget != null)
        {
            movableTarget.remove();
            movableTarget = null;
        }

        switch(interactionMode)
        {
            case STANDARD:
            {
                SelectEntity(entity);
            }
            break;

            case TARGET_MISSILE:
            {
                TargetMissile(entity.GetPosition(), entity, bAirburst);
            }
            break;

            case TARGET_TORPEDO:
            {
                TargetTorpedo(entity.GetPosition(), entity);
            }
            break;

            case TRANSFER_ACCOUNT:
            {
                if(entity instanceof Player)
                    TransferAccount(entity.GetPosition(), (Player)entity);
            }
            break;

            case SET_TARGET:
            case CAPTURE_TARGET:
            {
                SetTarget(movableEntity, null, MovableEntities, entity);
            }
            break;

            case LIBERATE_TARGET:
            {
                LiberateTarget(movableEntity, MovableEntities, entity);
            }
            break;

            case TURN_INFANTRY:
            {
                TurnInfantry(targeter, entity.GetPosition());
            }
            break;

            case SEEK_FUEL:
            {
                if(entity instanceof Airplane)
                {
                    if(((Airplane)entity).CanTransferFuel())
                        SelectRefueler(entity);
                }
                else if(entity instanceof Ship)
                {
                    Ship ship = (Ship)entity;

                    if(!ship.GetNuclear() || (ship.HasCargo() && ship.GetCargoSystem().ContainsResourceType(Resource.ResourceType.FUEL)))
                        SelectRefueler(entity);
                }
                else if(entity instanceof Submarine)
                {
                    if(!((Submarine)entity).GetNuclear())
                        SelectRefueler(entity);
                }
            }
            break;

            case PROVIDE_FUEL:
            {
                if(entity instanceof Airplane)
                {
                    SelectRefuelee(entity);
                }
                else if(entity instanceof NavalVessel)
                {
                    //Nuclear vessels don't need to receive fuel in any circumstance.
                    if(!((NavalVessel)entity).GetNuclear())
                        SelectRefuelee(entity);
                }
            }
            break;

            case TARGET_INTERCEPTOR:
            {
                if(entity instanceof Missile || entity instanceof Airplane)
                {
                    TargetInterceptor(entity);
                }
            }
            break;

            case SELECT_NEW_HOMEBASE:
            {
                if(entity instanceof Airbase || (entity instanceof Ship && ((Ship)entity).HasAircraft()))
                    NewAirbaseSelected(entity);
            }
            break;

            case UNIT_COMMAND:
            {
                if(movableEntity != null)
                {
                    if(movableEntity.GetEntity(game) instanceof Airplane && (entity instanceof Airbase || (entity instanceof Ship && ((Ship)entity).HasAircraft())))
                        MoveToAirbase((Airplane)movableEntity.GetMapEntity(game), entity.GetPointer());
                    else
                        PerformUnitCommand(movableEntity, entity.GetPosition(), null);
                }
            }
            break;

            case MOVE_ORDER:
            {
                MoveToPosition(movableEntity, null, entity.GetPosition(), entity);
            }
            break;

            case RANGE_FINDER:
            {
                FindRangeTo(entity.GetPosition());
            }
            break;

            case ELECTRONIC_WARFARE:
            {
                SetElectronicWarfareTarget(entity);
            }
            break;
        }
    }

    public void ButtonBuild(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(!game.GetInteractionReady())
        {
            ShowBasicOKDialog(getString(R.string.waiting_for_data));
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SetView(new BuildViewNew(game, me));
                }
            });
        }
    }

    public void ButtonDiplomacy(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(!game.GetInteractionReady())
        {
            ShowBasicOKDialog(getString(R.string.waiting_for_data));
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    interactionMode = InteractionMode.DIPLOMACY;

                    if(game.PendingDiplomacyItems())
                    {
                        SetView(new DiplomacyActionView(game, me));
                    }
                    else
                    {
                        SetView(new DiplomacyView(game, me));
                    }
                }
            });
        }
    }

    public void ButtonReports(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(!game.GetInteractionReady())
        {
            ShowBasicOKDialog(getString(R.string.waiting_for_data));
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SetView(new ReportsView(game, me));
                    reportStatus = ReportsStatus.NONE;
                }
            });
        }
    }

    public void ButtonWarnings(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(!game.GetInteractionReady())
        {
            ShowBasicOKDialog(getString(R.string.waiting_for_data));
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SetView(new WarningsView(game, me));
                }
            });
        }
    }

    public void ButtonPlayers(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(!game.GetInteractionReady())
        {
            ShowBasicOKDialog(getString(R.string.waiting_for_data));
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SetView(new PlayersView(game, me));
                }
            });
        }
    }

    public void ButtonSettings(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    SetView(new SettingsView(game, me));
                }
            });
    }

    public void ButtonRespawn(View view)
    {
        game.Respawn();
    }

    public void ButtonPlayStore(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ClientDefs.PLAY_STORE_URL));
        startActivity(intent);
    }

    public void ButtonEmailServerHelp(View view)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { Defs.SERVER_EMAIL });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Launch! Help & Support");

        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    public void ButtonDiscord(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ClientDefs.DISCORD_URL));
        startActivity(intent);
    }

    public void SetMapSatellite(boolean bSatellite)
    {
        bMapIsSatellite = bSatellite;

        map.setMapType(bSatellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
    }

    public void SetMapModeZoomOrSelect(boolean bZoom)
    {
        bMapModeZoom = bZoom;

        CurrentView.Update();

        ((SelectableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).SetZoom(bZoom);
    }

    public boolean GetMapSatellite()
    {
        return bMapIsSatellite;
    }

    public boolean GetMapModeZoom()
    {
        return bMapModeZoom;
    }

    @Override
    public void SetSelectionRectangle(float xFrom, float yFrom, float xTo, float yTo)
    {
        LatLng pt1 = map.getProjection().fromScreenLocation(new Point((int)xFrom, (int)yFrom));
        LatLng pt2 = map.getProjection().fromScreenLocation(new Point((int)xFrom, (int)yTo));
        LatLng pt3 = map.getProjection().fromScreenLocation(new Point((int)xTo, (int)yTo));
        LatLng pt4 = map.getProjection().fromScreenLocation(new Point((int)xTo, (int)yFrom));

        if(selectionRect == null)
        {
            selectionRect = map.addPolyline(new PolylineOptions()
                    .add(pt1)
                    .add(pt2)
                    .add(pt3)
                    .add(pt4)
                    .add(pt1)
                    .geodesic(true)
                    .color(Utilities.ColourFromAttr(this, R.attr.InfoColour)));
        }
        else
        {
            List<LatLng> points = new ArrayList<>();
            points.add(pt1);
            points.add(pt2);
            points.add(pt3);
            points.add(pt4);
            points.add(pt1);

            selectionRect.setPoints(points);
        }
    }

    @Override
    public void SelectEntities(float xFrom, float yFrom, float xTo, float yTo)
    {
        if(selectionRect != null)
        {
            selectionRect.remove();
            selectionRect = null;
        }

        if(CurrentView instanceof MainNormalView)
            ((MainNormalView) CurrentView).BottomLayoutShowView(new MapSelectView(game, this, map.getProjection().fromScreenLocation(new Point((int)xFrom, (int)yTo)), map.getProjection().fromScreenLocation(new Point((int)xTo, (int)yFrom)))); //Note y reversed because its magnitude is opposite to latitudinal magnitude.


        SetMapModeZoomOrSelect(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case ClientDefs.ACTIVITY_REQUEST_CODE_AVATAR_IMAGE:
            {
                if(resultCode == Activity.RESULT_OK)
                {
                    if(CurrentView instanceof UploadAvatarView)
                        ((UploadAvatarView) CurrentView).ImageActivityResult(data);
                }
            }
            break;

            case ClientDefs.ACTIVITY_REQUEST_CODE_AVATAR_CAMERA:
            {
                if(resultCode == Activity.RESULT_OK)
                {
                    if(CurrentView instanceof UploadAvatarView)
                        ((UploadAvatarView) CurrentView).CameraActivityResult(data);
                }
            }
            break;

            case ClientDefs.RC_SIGN_IN:
            {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                try
                {
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    if(account != null)
                    {
                        strGoogleID = account.getId();

                        SharedPreferences.Editor editor = getSharedPreferences(ClientDefs.SETTINGS, MODE_PRIVATE).edit();
                        editor.putString(ClientDefs.SETTINGS_GOOGLE_ID_STORED, strGoogleID);
                        editor.apply();

                        GoToGame();
                    }
                }
                catch(ApiException e)
                {
                    // Handle sign-in failure
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    public void SignOut(View view)
    {
        final LaunchDialog launchDialog = new LaunchDialog();
        launchDialog.SetHeaderLaunch();
        launchDialog.SetMessage(getString(R.string.logout_confirm));
        launchDialog.SetOnClickYes(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                launchDialog.dismiss();

                mGoogleSignInClient.signOut()
                    .addOnCompleteListener(me, new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(Task<Void> task)
                        {
                            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();

                            PurgeClient();
                            recreate();
                        }
                    });
            }
        });
        launchDialog.SetOnClickNo(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                launchDialog.dismiss();
            }
        });
        launchDialog.show(getFragmentManager(), "");


    }

    @Override
    public void AccountMigrated(boolean bDisplaySuccess)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ClientDefs.SETTINGS, MODE_PRIVATE).edit();
        editor.putBoolean(ClientDefs.HAS_MIGRATED_TO_GOOGLE, true);
        editor.apply();

        if(bDisplaySuccess)
            ShowBasicOKDialog(getString(R.string.account_migration_successful));

        GoToGame();
    }

    @Override
    public void FailedSpoofCheck()
    {
        ShowBasicOKDialog(getString(R.string.failed_spoof_check));
    }

    @Override
    public void AccountUnmigrated()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(me, gso); // assuming `gso` is accessible
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        me.startActivityForResult(signInIntent, ClientDefs.RC_SIGN_IN); // define RC_SIGN_IN

        game.MigrateAccount(Utilities.GetEncryptedDeviceID(me), strGoogleID);

        interactionMode = InteractionMode.STANDARD;
        ReturnToMainView();
    }

    public Locatifier GetLocatifier()
    {
        return locatifier;
    }

    public void SavePrivacyZones()
    {
        //Save privacy zones file on device.
        File fileDir = getDir(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        File file = new File(fileDir, ClientDefs.PRIVACY_FILENAME);

        try
        {
            RandomAccessFile rafConfig = new RandomAccessFile(file, "rw");
            rafConfig.setLength(0); //Clear the file.

            for(PrivacyZone privacyZone : game.GetPrivacyZones())
            {
                rafConfig.write(privacyZone.GetBytes());
            }

            rafConfig.close();
        }
        catch (Exception ex)
        {
            LaunchLog.Log(APPLICATION, LOG_NAME, "Could not save config: " + ex.getMessage());
        }
    }

    public void SetPlayersStuffVisibility(int lPlayerID, boolean bVisible)
    {
        /*if(bVisible)
        {
            CustomVisibilities.put(lPlayerID, true);
        }
        else
        {
            CustomVisibilities.remove(lPlayerID);
        }

        PlayerVisibilities.put(lPlayerID, bVisible);
        RebuildMap();*/

        //TODO: Player visibility
    }

    public boolean GetPlayersStuffVisibility(int lPlayerID)
    {
        //TODO: Player visibility
        /*if(!PlayerVisibilities.containsKey(lPlayerID))
            PlayerVisibilities.put(lPlayerID, false);

        return PlayerVisibilities.get(lPlayerID);*/

        return true;
    }

    @Override
    public void GameTicked(int lMS)
    {
       final Context context = this;

        //Instruct the current view to update, but only once every 1000 ms regardless of tick rate.
        /*uiUpdateAccumulator += lMS;
        long runnableUpdateAccumulator = uiUpdateAccumulator;

        if(uiUpdateAccumulator >= Defs.MS_PER_SEC)
        {
            uiUpdateAccumulator -= Defs.MS_PER_SEC;

            if(CurrentView != null)
            {
                CurrentView.Update();
            }
        }*/

        if(CurrentView != null)
        {
            CurrentView.Update();
        }

        //Update the map.
        if(!bRendering)
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    bRendering = true;

                    if(map != null)
                    {
                        //Rebuild map display if required.
                        if(bRebuildMap)
                        {
                            bRebuildMap = false;
                            boolean bDrawEverything = true;

                            if(interactionMode != InteractionMode.SET_TARGET && interactionMode != InteractionMode.MOVE_ORDER && interactionMode != InteractionMode.CAPTURE_TARGET)
                            {
                                if(movableTrajectory != null)
                                {
                                    movableTrajectory.remove();
                                }

                                if(movableTarget != null)
                                {
                                    movableTarget.remove();
                                    movableTarget = null;
                                }
                            }

                            for(Circle circle : multiCircleOverlays)
                            {
                                circle.remove();
                            }
                            multiCircleOverlays.clear();

                            for(Circle circle : CircleOverlays)
                            {
                                circle.remove();
                            }
                            CircleOverlays.clear();

                            //Populate map.
                            map.clear();

                            PrivacyZoneMarkers = new ConcurrentHashMap<>();
                            InterceptorMarkers = new ConcurrentHashMap<>();

                            for(Map<Integer, Marker> map : AllMarkers.values())
                            {
                                for(Marker marker : map.values())
                                {
                                    if(marker != null)
                                        marker.remove();

                                    marker = null;
                                }

                                map = new ConcurrentHashMap<>();
                                map.clear();
                            }

                            for(Map<Integer, GroundOverlay> map : AllOverlays.values())
                            {
                                for(GroundOverlay overlay : map.values())
                                {
                                    if(overlay != null)
                                        overlay.remove();

                                    overlay = null;
                                }

                                map = new ConcurrentHashMap<>();
                                map.clear();
                            }

                            AllMarkers = new ConcurrentHashMap<>();
                            AllOverlays = new ConcurrentHashMap<>();
                            MarkerChunks = new ConcurrentHashMap<>();
                            OverlayChunks = new ConcurrentHashMap<>();
                            ChunkEntities = new ConcurrentHashMap<>();
                            ChunkZoomLevelGenerated = new ConcurrentHashMap<>();
                            TorpedoMarkers = new ConcurrentHashMap<>();
                            RadiationMarkers = new ConcurrentHashMap<>();
                            DepositMarkers = new ConcurrentHashMap<>();
                            BlastRadii = new ConcurrentHashMap<>();
                            EMPMarkers = new ConcurrentHashMap<>();
                            ABMEMPMarkers = new ConcurrentHashMap<>();
                            ThreatMarkers = new ConcurrentHashMap<>();
                            BlastEffects = new ArrayList<>();

                            switch (interactionMode)
                            {
                                case PRIVACY_ZONES:
                                {
                                    for(PrivacyZone privacyZone : game.GetPrivacyZones())
                                    {
                                        bDrawEverything = false;

                                        MarkerOptions options = new MarkerOptions();
                                        options.position(Utilities.GetLatLng(privacyZone.GetPosition()));
                                        options.anchor(0.5f, 0.5f);
                                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.privacy));

                                        PrivacyZoneMarkers.put(map.addMarker(options), privacyZone);

                                        Circle privacyZoneCircle = map.addCircle(new CircleOptions()
                                                .center(Utilities.GetLatLng(privacyZone.GetPosition()))
                                                .radius(privacyZone.GetRadius())
                                                .fillColor(Utilities.ColourFromAttr(context, selectedPrivacyZone == privacyZone ? R.attr.PrivacyZoneSelectedColour : R.attr.PrivacyZoneColour))
                                                .strokeWidth(0.0f));

                                        //Pass circle to editor if editing this privacy zone.
                                        if (selectedPrivacyZone == privacyZone)
                                        {
                                            if (CurrentView instanceof PrivacyZonesView)
                                            {
                                                ((PrivacyZonesView) CurrentView).SetCircle(privacyZoneCircle);
                                            }
                                        }
                                    }
                                }
                                break;

                                /*case SELECT_NEW_HOMEBASE:
                                {
                                    bDrawEverything = false;

                                    for(Airbase airbase : game.GetAirbases())
                                    {
                                        if((airbase.GetOwnedBy(game.GetOurPlayerID()) || airbase.GetAircraftSystem().GetOpen()))
                                            AddEntityToChunk(airbase);
                                    }

                                    for(Ship ship : game.GetShips())
                                    {
                                        if(ship.HasAircraft() && (ship.GetOwnedBy(game.GetOurPlayerID()) || ship.GetAircraftSystem().GetOpen()))
                                            AddEntityToChunk(ship);
                                    }

                                    if(homebaseChangeAircraft != null)
                                        AddEntityToChunk(((MapEntity)homebaseChangeAircraft));
                                }
                                break;*/

                                case SEEK_FUEL:
                                {
                                    bDrawEverything = false;

                                    if(targeter != null)
                                    {
                                        switch(targeter.GetType())
                                        {
                                            case AIRPLANE:
                                            {
                                                for(Airplane aircraft : game.GetAirplanes())
                                                {
                                                    if(aircraft.CanTransferFuel())
                                                    {
                                                        AddEntityToChunk(aircraft);
                                                    }
                                                }
                                            }
                                            break;

                                            case SHIP:
                                            case SUBMARINE:
                                            {
                                                for(Ship ship : game.GetShips())
                                                {
                                                    if(!ship.GetNuclear() || (ship.HasCargo() && ship.GetCargoSystem().ContainsResourceType(Resource.ResourceType.FUEL)))
                                                    {
                                                        AddEntityToChunk(ship);
                                                    }
                                                }

                                                for(Submarine submarine : game.GetSubmarines())
                                                {
                                                    if(!submarine.GetNuclear())
                                                        AddEntityToChunk(submarine);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                break;

                                case PROVIDE_FUEL:
                                {
                                    bDrawEverything = false;

                                    if(targeter != null)
                                    {
                                        switch(targeter.GetType())
                                        {
                                            case AIRPLANE:
                                            {
                                                for(Airplane aircraft : game.GetAirplanes())
                                                {
                                                    AddEntityToChunk(aircraft);
                                                }
                                            }
                                            break;

                                            case SHIP:
                                            case SUBMARINE:
                                            {
                                                for(Ship ship : game.GetShips())
                                                {
                                                    if(!ship.GetNuclear())
                                                    {
                                                        AddEntityToChunk(ship);
                                                    }
                                                }

                                                for(Submarine submarine : game.GetSubmarines())
                                                {
                                                    if(!submarine.GetNuclear())
                                                        AddEntityToChunk(submarine);
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                break;

                                case LOAD_LOOT:
                                {
                                    bDrawEverything = false;

                                    for(Loot loot : game.GetLoots())
                                    {
                                        AddEntityToChunk(loot);
                                    }
                                }
                                break;

                                case MOVE_ORDER:
                                case UNIT_COMMAND:
                                {
                                    DrawAllMovableUI();
                                }

                                default:
                                {

                                }
                            }

                            if(bDrawEverything)
                            {
                                if(game.GetInteractionReady())
                                {
                                    //Dotncarry memorial.
                                    MarkerOptions optionDotncarry = new MarkerOptions();
                                    optionDotncarry.position(new LatLng(31.891461, -104.86071));
                                    optionDotncarry.anchor(0.5f, 0.5f);
                                    optionDotncarry.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_texas_obelisk));
                                    DotncarryMemorial = map.addMarker(optionDotncarry);

                                    //Thomas99 memorial.
                                    MarkerOptions optionThomas99 = new MarkerOptions();
                                    optionThomas99.position(new LatLng(33.200087, -97.198009));
                                    optionThomas99.anchor(0.5f, 0.5f);
                                    optionThomas99.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_texas_obelisk));
                                    Thomas99Memorial = map.addMarker(optionThomas99);

                                    for(Shipyard shipyard : game.GetShipyards())
                                    {
                                        AddEntityToChunk(shipyard);
                                    }

                                    for(Player player : game.GetPlayers())
                                    {
                                        AddEntityToChunk(player);
                                    }

                                    for(MissileSite missileSite : game.GetMissileSites())
                                    {
                                        AddEntityToChunk(missileSite);
                                    }

                                    for(ArtilleryGun artillery : game.GetArtilleryGuns())
                                    {
                                        AddEntityToChunk(artillery);
                                    }

                                    for(Processor processor : game.GetProcessors())
                                    {
                                        AddEntityToChunk(processor);
                                    }

                                    for(SAMSite samSite : game.GetSAMSites())
                                    {
                                        AddEntityToChunk(samSite);
                                    }

                                    for(SentryGun sentryGun : game.GetSentryGuns())
                                    {
                                        AddEntityToChunk(sentryGun);
                                    }

                                    for(OreMine oreMine : game.GetOreMines())
                                    {
                                        AddEntityToChunk(oreMine);
                                    }

                                    for(CommandPost bunker : game.GetCommandPosts())
                                    {
                                        AddEntityToChunk(bunker);
                                    }

                                    for(Airbase airbase : game.GetAirbases())
                                    {
                                        AddEntityToChunk(airbase);
                                    }

                                    for(Armory armory : game.GetArmories())
                                    {
                                        AddEntityToChunk(armory);
                                    }

                                    for(Bank bank : game.GetBanks())
                                    {
                                        AddEntityToChunk(bank);
                                    }

                                    for(MissileFactory factory : game.GetMissileFactorys())
                                    {
                                        AddEntityToChunk(factory);
                                    }

                                    for(Warehouse warehouse : game.GetWarehouses())
                                    {
                                        AddEntityToChunk(warehouse);
                                    }

                                    for(ScrapYard yard : game.GetScrapYards())
                                    {
                                        AddEntityToChunk(yard);
                                    }

                                    for(RadarStation radarStation : game.GetRadarStations())
                                    {
                                        AddEntityToChunk(radarStation);
                                    }

                                    for(Airplane aircraft : game.GetAirplanes())
                                    {
                                        AddEntityToChunk(aircraft);
                                    }

                                    for(Tank tank : game.GetTanks())
                                    {
                                        AddEntityToChunk(tank);
                                    }

                                    for(Ship ship : game.GetShips())
                                    {
                                        AddEntityToChunk(ship);
                                    }

                                    for(Submarine submarine : game.GetSubmarines())
                                    {
                                        AddEntityToChunk(submarine);
                                    }

                                    for(CargoTruck truck : game.GetCargoTrucks())
                                    {
                                        AddEntityToChunk(truck);
                                    }

                                    for(Infantry infantry : game.GetInfantries())
                                    {
                                        AddEntityToChunk(infantry);
                                    }

                                    for(Blueprint blueprint : game.GetBlueprints())
                                    {
                                        AddEntityToChunk(blueprint);
                                    }

                                    for(Loot loot : game.GetLoots())
                                    {
                                        AddEntityToChunk(loot);
                                    }

                                    for(Airdrop airdrop : game.GetAirdrops())
                                    {
                                        CreateEntityUI(airdrop);
                                    }

                                    for(Rubble rubble : game.GetRubbles())
                                    {
                                        AddEntityToChunk(rubble);
                                    }

                                    for(ResourceDeposit deposit : game.GetResourceDeposits())
                                    {
                                        CreateEntityUI(deposit);
                                    }

                                    for(Radiation radiation : game.GetRadiations())
                                    {
                                        CreateEntityUI(radiation);
                                    }

                                    if(selectedEntity != null)
                                    {
                                        if((movableEntity != null && movableEntity.GetMapEntity(game) != null) && !selectedEntity.ApparentlyEquals(movableEntity.GetMapEntity(game)))
                                            DrawEntityOverlay(selectedEntity);
                                    }

                                    for(Missile missile : game.GetMissiles())
                                    {
                                        AddEntityToChunk(missile);
                                    }

                                    for(Interceptor interceptor : game.GetInterceptors())
                                    {
                                        CreateEntityUI(interceptor);
                                    }

                                    for(Torpedo torpedo : game.GetTorpedoes())
                                    {
                                        CreateEntityUI(torpedo);
                                    }

                                    HighLevelUIRefresh();
                                }
                            }
                        }

                        if(interactionMode != InteractionMode.SET_TARGET && interactionMode != InteractionMode.MOVE_ORDER && interactionMode != InteractionMode.CAPTURE_TARGET)
                        {
                            if(selectedEntity instanceof Movable && selectedEntity != homebaseChangeAircraft)
                            {
                                Movable movable = (Movable)selectedEntity;

                                if(movable.GetMoveOrders() != MoveOrders.NONE && movable.GetMoveOrders() != MoveOrders.WAIT && movable.GetMoveOrders() != MoveOrders.DEFEND)
                                {
                                    DrawEntityOverlay(selectedEntity);
                                }
                            }
                        }

                        //Move entities that can move.

                        for(BlastEffect blastEffect : BlastEffects)
                        {
                            blastEffect.Tick();
                        }

                        if(AllMarkers.get(EntityType.MISSILE) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.MISSILE).entrySet())
                            {
                                Missile missile = game.GetMissile(entry.getKey());
                                Marker marker = entry.getValue();

                                if(missile != null)
                                {
                                    if(marker != null && marker.isVisible())
                                    {
                                        marker.setPosition(Utilities.GetLatLng(missile.GetPosition()));
                                        marker.setRotation((float) GeoCoord.ToDegrees(missile.GetPosition().GetLastBearing()));
                                    }

                                    Marker threatMarker = ThreatMarkers.get(missile.GetID());

                                    if(threatMarker != null && threatMarker.isVisible())
                                    {
                                        threatMarker.setPosition(Utilities.GetLatLng(missile.GetPosition()));
                                    }
                                }
                            }
                        }

                        for(Map.Entry<Integer, Marker> entry : InterceptorMarkers.entrySet())
                        {
                            Interceptor interceptor = game.GetInterceptor(entry.getKey());
                            Marker marker = entry.getValue();

                            if (marker != null && interceptor != null)
                            {
                                marker.setPosition(Utilities.GetLatLng(interceptor.GetPosition()));
                                marker.setRotation((float) GeoCoord.ToDegrees(interceptor.GetPosition().GetLastBearing()));

                                if(!marker.isVisible())
                                {
                                    marker.setVisible(true);
                                }

                                GeoCoord interceptorTarget = null;

                                switch(interceptor.GetTargetType())
                                {
                                    case MISSILE:
                                    {
                                        if(game.GetMissile(interceptor.GetTargetID()) != null && game.GetMissile(interceptor.GetTargetID()).GetPosition() != null)
                                            interceptorTarget = game.GetMissile(interceptor.GetTargetID()).GetPosition();
                                    }
                                    break;

                                    case AIRPLANE:
                                    {
                                        if(game.GetAirplane(interceptor.GetTargetID()) != null && game.GetAirplane(interceptor.GetTargetID()).GetPosition() != null)
                                            interceptorTarget = game.GetAirplane(interceptor.GetTargetID()).GetPosition();
                                    }
                                    break;
                                }

                                if (interceptorTarget != null)
                                {
                                    //Update trajectory.
                                    Polyline trajectory = InterceptorTrajectories.get(interceptor.GetID());

                                    if(trajectory != null)
                                    {
                                        //We want to update the trajectory as normal even if the interceptor is off screen, or else it will look janky.
                                        trajectory.setPoints(Arrays.asList(Utilities.GetLatLng(interceptor.GetPosition()), Utilities.GetLatLng(interceptorTarget)));
                                    }
                                }
                            }
                        }

                        for(Map.Entry<Integer, Marker> entry : TorpedoMarkers.entrySet())
                        {
                            Torpedo torpedo = game.GetTorpedo(entry.getKey());
                            Marker marker = entry.getValue();

                            if(marker != null && torpedo != null)
                            {
                                marker.setPosition(Utilities.GetLatLng(torpedo.GetPosition()));
                                marker.setRotation((float) GeoCoord.ToDegrees(torpedo.GetPosition().GetLastBearing()));

                                GeoCoord geoTarget = null;

                                if(torpedo.HasTarget())
                                {
                                    MapEntity target = torpedo.GetTarget().GetMapEntity(game);

                                    if(target != null)
                                        geoTarget = target.GetPosition();
                                }
                                else if(torpedo.GetGeoTarget() != null)
                                {
                                    geoTarget = torpedo.GetGeoTarget();
                                }

                                //Update trajectory.
                                Polyline trajectory = TorpedoTrajectories.get(torpedo.GetID());

                                if(trajectory != null)
                                {
                                    if(geoTarget != null)
                                        trajectory.setPoints(Arrays.asList(Utilities.GetLatLng(geoTarget), Utilities.GetLatLng(torpedo.GetPosition())));
                                    else
                                        CreateEntityUI(torpedo);
                                }
                            }
                        }

                        if(AllMarkers.get(EntityType.AIRPLANE) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.AIRPLANE).entrySet())
                            {
                                Airplane aircraft = game.GetAirplane(entry.getKey());
                                Marker marker = entry.getValue();

                                if(marker != null && aircraft != null)
                                {
                                    marker.setPosition(Utilities.GetLatLng(aircraft.GetPosition()));
                                    marker.setRotation((float) GeoCoord.ToDegrees(aircraft.GetPosition().GetLastBearing()));
                                }

                                if(!marker.isVisible())
                                {
                                    marker.setVisible(true);
                                }
                            }
                        }

                        if(AllMarkers.get(EntityType.TANK) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.TANK).entrySet())
                            {
                                Tank tank = game.GetTank(entry.getKey());

                                if(tank != null)
                                {
                                    Marker marker = entry.getValue();

                                    if(marker != null && tank.Moving())
                                    {
                                        marker.setPosition(Utilities.GetLatLng(tank.GetPosition()));
                                    }
                                }
                            }
                        }

                        if(AllMarkers.get(EntityType.INFANTRY) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.INFANTRY).entrySet())
                            {
                                Infantry infantry = game.GetInfantry(entry.getKey());
                                Marker marker = entry.getValue();

                                if(infantry != null && marker != null)
                                {
                                    if(infantry.Moving())
                                    {
                                        marker.setPosition(Utilities.GetLatLng(infantry.GetPosition()));
                                    }
                                }
                            }
                        }

                        if(AllMarkers.get(EntityType.CARGO_TRUCK) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.CARGO_TRUCK).entrySet())
                            {
                                CargoTruck truck = game.GetCargoTruck(entry.getKey());
                                Marker marker = entry.getValue();

                                if(truck != null && marker != null)
                                {
                                    if(truck.Moving())
                                    {
                                        marker.setPosition(Utilities.GetLatLng(truck.GetPosition()));
                                    }
                                }
                            }
                        }

                        if(AllMarkers.get(EntityType.SHIP) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.SHIP).entrySet())
                            {
                                Ship ship = game.GetShip(entry.getKey());
                                Marker marker = entry.getValue();

                                if(ship != null && marker != null)
                                {
                                    if(ship.Moving())
                                    {
                                        marker.setPosition(Utilities.GetLatLng(ship.GetPosition()));
                                    }
                                }
                            }
                        }

                        if(AllMarkers.get(EntityType.SUBMARINE) != null)
                        {
                            for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.SUBMARINE).entrySet())
                            {
                                Submarine submarine = game.GetSubmarine(entry.getKey());
                                Marker marker = entry.getValue();

                                if(submarine != null && marker != null)
                                {
                                    if(submarine.Moving())
                                    {
                                        marker.setPosition(Utilities.GetLatLng(submarine.GetPosition()));
                                    }
                                }
                            }
                        }

                        if(interactionMode == InteractionMode.TARGET_INTERCEPTOR || interactionMode == InteractionMode.TARGET_MISSILE)
                        {
                            switch(targetSystemType)
                            {
                                case AIRCRAFT_INTERCEPTORS:
                                case AIRCRAFT_MISSILES:
                                {
                                    if(targetRange != null)
                                    {
                                        targetRange.setCenter(Utilities.GetLatLng(game.GetAirplane(lTargettingSiteID).GetPosition()));
                                    }
                                }
                                break;

                                case SUBMARINE_MISSILES:
                                case SUBMARINE_TORPEDO:
                                case SUBMARINE_ICBM:
                                {
                                    if(targetRange != null)
                                    {
                                        targetRange.setCenter(Utilities.GetLatLng(game.GetSubmarine(lTargettingSiteID).GetPosition()));
                                    }
                                }
                                break;

                                case SHIP_MISSILES:
                                case SHIP_TORPEDOES:
                                case SHIP_INTERCEPTORS:
                                {
                                    if(targetRange != null)
                                    {
                                        targetRange.setCenter(Utilities.GetLatLng(game.GetShip(lTargettingSiteID).GetPosition()));
                                    }
                                }
                                break;
                            }
                        }
                    }

                    bRendering = false;
                }
            });
        }
    }

    @Override
    public void SaveConfig(Config config)
    {
        //As the config has changed, purge everything to be downloaded again (as per config "Variant" functionality, among other stuff).
        PurgeClient();

        //Save config file on device.
        File fileDir = getDir(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        File file = new File(fileDir, ClientDefs.CONFIG_FILENAME);

        try
        {
            RandomAccessFile rafConfig = new RandomAccessFile(file, "rw");
            rafConfig.setLength(0); //Clear the file.
            rafConfig.write(config.GetData());
            rafConfig.close();
        }
        catch (Exception ex)
        {
            LaunchLog.Log(APPLICATION, LOG_NAME, "Could not save config: " + ex.getMessage());
        }
    }

    @Override
    public void SaveAvatar(int lAvatarID, byte[] cData)
    {
        //Avatar downloaded.
        FileOutputStream out = null;

        try
        {
            File fileDir = getDir(ClientDefs.AVATAR_FOLDER, Context.MODE_PRIVATE);
            File file = new File(fileDir, lAvatarID + ClientDefs.IMAGE_FORMAT);
            out = new FileOutputStream(file);
            out.write(cData);
            AvatarBitmaps.InvalidateAvatar(lAvatarID);

            //Update UI.
            for(final Map.Entry<Integer, Marker> entry : Objects.requireNonNull(AllMarkers.get(EntityType.PLAYER)).entrySet())
            {
                final Player player = game.GetPlayer(entry.getKey());

                if(player.GetAvatarID() == lAvatarID)
                {
                    if(player.GetPosition().GetValid())
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Marker marker = entry.getValue();

                                if(marker != null)
                                    CreateEntityUI(player);
                            }
                        });
                    }
                }
            }

            //Update current view.
            CurrentView.AvatarSaved(lAvatarID);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch(Exception ex) { /* Don't care */ }
        }
    }

    @Override
    public void SaveImage(int lImageID, byte[] cData)
    {
        //Image downloaded.
        FileOutputStream out = null;
        boolean bSucceeded = true;

        try
        {
            File fileDir = getDir(ClientDefs.IMGASSETS_FOLDER, Context.MODE_PRIVATE);
            File file = new File(fileDir, lImageID + ClientDefs.IMAGE_FORMAT);
            out = new FileOutputStream(file);
            out.write(cData);
        }
        catch (FileNotFoundException e)
        {
            bSucceeded = false;
            e.printStackTrace();
        }
        catch (IOException e)
        {
            bSucceeded = false;
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch(Exception ex) { /* Don't care */ }
        }

        if(bSucceeded)
        {
            //Update alliance/report screens, etc.
            /*if(CurrentView instanceof DiplomacyActionView)
            {
                //TO DO.
            }

            if(CurrentView instanceof DiplomacyView)
            {
                ((DiplomacyView)CurrentView).RefreshAvatar(lAvatarID);
            }

            if(CurrentView instanceof ReportsView)
            {
                //TO DO.
            }*/

            //Update UI.
            final MainActivity self = this;

            if(AllMarkers.get(EntityType.MISSILE) != null)
            {
                for(Map.Entry<Integer, Marker> entry : AllMarkers.get(EntityType.MISSILE).entrySet())
                {
                    final Missile missile = game.GetMissile(entry.getKey());

                    if(missile != null)
                    {
                        final MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                        if (type.GetAssetID() == lImageID)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    CreateEntityUI(missile);
                                }
                            });
                        }
                    }
                }
            }

            for(final Map.Entry<Integer, Marker> entry : InterceptorMarkers.entrySet())
            {
                final Interceptor interceptor = game.GetInterceptor(entry.getKey());

                if(interceptor != null)
                {
                    final InterceptorType type = game.GetConfig().GetInterceptorType(interceptor.GetType());

                    if (type.GetAssetID() == lImageID)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                CreateEntityUI(interceptor);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void AvatarUploaded(final int lAvatarID)
    {
        //Update UI.
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                switch(interactionMode)
                {
                    case REGISTRATION:
                    {
                        //Player's avatar uploaded. Go back to registration screen.
                        SetView(new RegisterView(game, me, lAvatarID, strGoogleID, Utilities.GetEncryptedDeviceID(me)));
                    }
                    break;

                    case DIPLOMACY:
                    {
                        //Player uploaded an avatar for their alliance.
                        if(game.GetOurPlayer().GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED)
                        {
                            SetView(new CreateAllianceView(game, me, lAvatarID));
                        }
                        else
                        {
                            game.SetAvatar(lAvatarID, true);
                            SetView(new AllianceControl(game, me, game.GetAlliance(game.GetOurPlayer().GetAllianceMemberID())));
                        }
                    }
                    break;

                    default:
                    {
                        //Player uploaded their own avatar from settings.
                        game.SetAvatar(lAvatarID, false);
                        ReturnToMainView();
                    }
                    break;
                }
            }
        });
    }

    @Override
    public boolean PlayerLocationAvailable()
    {
        return locatifier.GetCurrentLocationGood();
    }

    @Override
    public LaunchClientLocation GetPlayerLocation()
    {
        return locatifier.GetLocation();
    }

    @Override
    public void TypeAdded(LaunchType type)
    {
        //Unused.
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals(selectedEntity))
        {
            SelectedEntityUpdated();
            DrawEntityOverlay(selectedEntity);
        }

        if(entity instanceof Player)
        {
            final Player player = (Player)entity;

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(!player.GetAWOL() && (!player.GetHasFullStats() || player.ApparentlyEquals(game.GetOurPlayer())))
                    {
                        if(AllMarkers.get(EntityType.PLAYER) != null)
                        {
                            Marker marker = AllMarkers.get(EntityType.PLAYER).get(player.GetID());

                            if(marker != null)
                            {
                                marker.setPosition(Utilities.GetLatLng(player.GetPosition()));
                                marker.setIcon(GetPlayerIcon(player));
                                marker.setAlpha(player.GetRespawnProtected() ? 0.5f : 1.0f);
                            }
                            else
                            {
                                AddEntityToChunk(player);
                            }

                            return;
                        }
                    }

                    //If we are this far, the player wasn't found and we need to create a new marker.
                    AddEntityToChunk(player);
                }
            });
        }
        else
        {
            if(entity instanceof Radiation)
            {
                CreateEntityUI((MapEntity)entity);
            }
            if(entity instanceof MapEntity)
            {
                EntityType typeToCheck = entity.GetEntityType();

                if(entity instanceof Airplane)
                    typeToCheck = EntityType.AIRPLANE;
                else if(entity instanceof Tank)
                    typeToCheck = EntityType.TANK;
                else if(entity instanceof Ship)
                    typeToCheck = EntityType.SHIP;
                else if(entity instanceof Submarine)
                    typeToCheck = EntityType.SUBMARINE;

                if(EntityShouldAlwaysRender(typeToCheck))
                    CreateEntityUI((MapEntity)entity);
                else
                    AddEntityToChunk((MapEntity)entity);
            }
        }

        //Update the current view with the entity.
        if(CurrentView != null)
        {
            CurrentView.EntityUpdated(entity);
        }
    }

    @Override
    public void TreatyUpdated(Treaty treaty)
    {
        //Update the current view with the treaty.
        if(CurrentView != null)
        {
            CurrentView.TreatyUpdated(treaty);
        }
    }

    public BitmapDescriptor GetPlayerIcon(Player player)
    {
        //Scale it to be smaller for the map.
        Bitmap src = AvatarBitmaps.GetPlayerAvatar(this, game, player);

        Bitmap bitmap = Bitmap.createScaledBitmap(src, PLAYER_MAP_ICON_SIZE, PLAYER_MAP_ICON_SIZE, false);

        if(player.GetIsAnAdmin())
        {
            Bitmap newMap = Bitmap.createBitmap(128,128, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newMap);
            canvas.drawBitmap(bitmap, 32, 32, null);
            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.admin_overlay), 0, 0, null);

            return BitmapDescriptorFactory.fromBitmap(newMap);
        }

        Bitmap newMap = Bitmap.createBitmap(PLAYER_MAP_ICON_SIZE,PLAYER_MAP_ICON_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newMap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        return BitmapDescriptorFactory.fromBitmap(newMap);
    }

    @Override
    public void EntityRemoved(final LaunchEntity entity)
    {
        if(entity instanceof MapEntity)
            RemoveEntityUI((MapEntity)entity, true);

        //Update the current view with the entity.
        if(CurrentView != null)
        {
            CurrentView.EntityRemoved(entity);
        }
    }

    @Override
    public void MajorChanges()
    {
        //Rebuild the map.
        RebuildMap();
    }

    @Override
    public void Authenticated()
    {
        if(interactionMode == InteractionMode.REGISTRATION || interactionMode == InteractionMode.SPLASH)
        {
            interactionMode = InteractionMode.STANDARD;
            ReturnToMainView();

            //tutorial.NotifyEntry(this);

            //Display server message if applicable.
            //TO DO: Get from server, and reinstate!
            /*String strServerMessage = "Missile & interceptor purchasing has been improved; you can now purchase multiple in one go.";
            byte cServerMessageData[] = strServerMessage.getBytes();

            CRC32 crc32 = new CRC32();
            crc32.update(cServerMessageData);

            SharedPreferences sharedPreferences = getSharedPreferences(ClientDefs.SETTINGS, MODE_PRIVATE);

            if(sharedPreferences.getLong(ClientDefs.SETTINGS_SERVER_MESSAGE_CHECKSUM, 0) != crc32.getValue())
            {
                //We haven't seen this message before. Display it.
                ShowBasicOKDialog(strServerMessage);
                SharedPreferences.Editor editor = getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                editor.putLong(ClientDefs.SETTINGS_SERVER_MESSAGE_CHECKSUM, crc32.getValue());
                editor.commit();
            }*/
        }

        //Update the notification service.
        //LaunchAlertManager.SystemCheck(this);
    }

    @Override
    public void AccountUnregistered()
    {
        //Check we're not already in registration mode (because the comms get dropped when uploading an avatar on initial registration).
        //Also check we're not in disclaimer mode, because we don't want to change from that screen.
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(interactionMode != InteractionMode.REGISTRATION)
                {
                    interactionMode = InteractionMode.REGISTRATION;
                    SetView(new RegisterView(game, me, ClientDefs.DEFAULT_AVATAR_ID, strGoogleID, Utilities.GetEncryptedDeviceID(me)));
                }
            }
        });
    }

    @Override
    public void AccountNameTaken()
    {
        ((RegisterView)CurrentView).UsernameTaken();
    }

    @Override
    public void DeviceIDUsed()
    {
        final LaunchDialog launchDialog = new LaunchDialog();
        launchDialog.SetHeaderLaunch();
        launchDialog.SetMessage(getString(R.string.device_id_taken));
        launchDialog.SetOnClickOk(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                launchDialog.dismiss();
                mGoogleSignInClient.signOut()
                    .addOnCompleteListener(me, new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(Task<Void> task)
                        {
                            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();

                            PurgeClient();
                            recreate();
                        }
                    });
            }
        });
        launchDialog.show(getFragmentManager(), "");
    }

    @Override
    public void MajorVersionMismatch()
    {
        interactionMode = InteractionMode.CANT_LOG_IN;

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                setContentView(R.layout.main_majorupdate);
            }
        });
    }

    @Override
    public void MinorVersionMismatch()
    {
        //Hack: Push an event into the client game to make it appear on the text event list.
        game.EventReceived(new LaunchEvent(getString(R.string.version_mismatch_minor)));
    }

    @Override
    public void ActionSucceeded()
    {

    }

    @Override
    public void ActionFailed()
    {

    }

    @Override
    public void ShowTaskMessage(final TaskMessage message)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String strText = "";

                switch(message)
                {
                    case REGISTERING: strText = getString(R.string.registering); break;
                    case UPLOADING_AVATAR: strText = getString(R.string.uploading_avatar); break;
                    case RESPAWNING: strText = getString(R.string.respawning); break;
                    case CONSTRUCTING: strText = getString(R.string.constructing); break;
                    case PURCHASING: strText = getString(R.string.purchasing); break;
                    case DECOMISSIONING: strText = getString(R.string.decommissioning); break;
                    case SELLING: strText = getString(R.string.selling); break;
                    case LAUNCHING_MISSILE: strText = getString(R.string.launching_missile); break;
                    case LAUNCHING_INTERCEPTOR: strText = getString(R.string.launching_interceptor); break;
                    case CLOSING_ACCOUNT: strText = getString(R.string.closing_account); break;
                    case REPAIRING: strText = getString(R.string.repairing); break;
                    case HEALING: strText = getString(R.string.healing); break;
                    case CONFIGURING: strText = getString(R.string.configuring); break;
                    case UPGRADING: strText = getString(R.string.upgrading); break;
                    case ALLIANCE_CREATE: strText = getString(R.string.creating_alliance); break;
                    case ALLIANCE_JOIN: strText = getString(R.string.joining_alliance); break;
                    case ALLIANCE_LEAVE: strText = getString(R.string.leaving_alliance); break;
                    case DECLARE_WAR: strText = getString(R.string.declaring_war); break;
                    case PROMOTING: strText = getString(R.string.promoting); break;
                    case ACCEPTING: strText = getString(R.string.accepting); break;
                    case REJECTING: strText = getString(R.string.rejecting); break;
                    case KICKING: strText = getString(R.string.kicking); break;
                    case DIPLOMACY: strText = getString(R.string.diplomacy); break;
                    case RAISING: strText = getString(R.string.raising_bounty); break;
                    case GIVING: strText = getString(R.string.giving_wealth); break;
                    case SETTING_TOKEN: strText = getString(R.string.setting_token); break;
                    case TRANSMITTING: strText = getString(R.string.transmitting); break;
                    case PINGING: strText = getString(R.string.pinging); break;
                    case SCANNING: strText = getString(R.string.scanning); break;
                    case MIGRATING: strText = getString(R.string.migrating); break;

                    default: strText = "<THE GAME IS BROKEN. PLEASE SCREENSHOT AND REPORT THIS.>"; break;
                }

                //Modify old one if possible.
                if (commsDialog != null)
                {
                    commsDialog.SetMessage(strText);
                }
                else
                {
                    //Create new one.
                    commsDialog = new LaunchDialog();
                    commsDialog.setCancelable(false);

                    commsDialog.SetHeaderComms();
                    commsDialog.SetMessage(strText);
                    commsDialog.SetEnableProgressSpinner();

                    commsDialog.show(getFragmentManager(), "");

                    //TO DO: The comms dialog should have comms status on it.
                }
            }
        });
    }

    @Override
    public void DismissTaskMessage()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (commsDialog != null)
                {
                    commsDialog.dismiss();
                    commsDialog = null;
                }
            }
        });
    }

    @Override
    public void NewEvent(LaunchEvent event)
    {
        switch(event.GetSoundEffect())
        {
            case EXPLOSION:
            {
                if(event.GetMessage().contains(game.GetOurPlayer().GetName()))
                    Sounds.PlayNearExplosion();
                else
                    Sounds.PlayFarExplosion();
            }
            break;

            case AIRDROP: Sounds.PlayAirdrop(); break;
            case MONEY: Sounds.PlayMoney(); break;
            case MISSILE_LAUNCH: Sounds.PlayMissileLaunch(); break;
            case BOMB_DROP: Sounds.PlayBombDrop(); break;
            case ICBM_LAUNCH: Sounds.PlayICBMLaunch(); break;
            case INTERCEPTOR_LAUNCH: Sounds.PlayInterceptorLaunch(); break;
            case CONSTRUCTION: Sounds.PlayConstruction(); break;
            case INTERCEPTOR_MISS: Sounds.PlayInterceptorMiss(); break;
            case INTERCEPTOR_HIT: Sounds.PlayInterceptorHit(); break;
            case SENTRY_GUN_HIT: Sounds.PlaySentryGunHit(); break;
            case SENTRY_GUN_MISS: Sounds.PlaySentryGunMiss(); break;
            case RECONFIG: Sounds.PlayReconfig(); break;
            case EQUIP: Sounds.PlayEquip(); break;
            case RESPAWN: Sounds.PlayRespawn(); break;
            case DEATH: Sounds.PlayDeath(); break;
            case REPAIR: Sounds.PlayRepair(); break;
            case HEAL: Sounds.PlayHeal(); break;
            case TRANSMIT: Sounds.PlayTransmit(); break;
            case PROSPECT: Sounds.PlayProspect(); break;
            case TORPEDO_LAUNCH: Sounds.PlayTorpedoLaunch(); break;
            case SONAR:
            case RADAR: Sounds.PlaySonar(); break;
            case ARTILLERY_EXPLOSION: Sounds.PlayArtilleryExplosion(); break;
            case ARTILLERY_FIRE: Sounds.PlayArtilleryFire(); break;
            case NUKE_EXPLOSION: Sounds.PlayNukeExplosion(); break;
            case AIRCRAFT_MOVE: Sounds.PlayAircraftMovement(); break;
            case INFANTRY_MOVE: Sounds.PlayInfantryMovement(); break;
            case HELICOPTER_MOVE: Sounds.PlayHelicopterMovement(); break;
            case TRUCK_MOVE: Sounds.PlayCargoTruckMovement(); break;
            case TANK_MOVE: Sounds.PlayTankMovement(); break;
            case NAVAL_MOVE: Sounds.PlayShipMovement(); break;
            case CARGO_TRANSFER: Sounds.PlayLoadCargo(); break;
            case SUB_DIVE: Sounds.PlaySubmarineDive(); break;
            case SUB_SURFACE: Sounds.PlaySubmarineSurface(); break;
            case CARAVAN_MOVE: Sounds.PlayCaravanMove(); break;
            case RAILGUN_FIRE: Sounds.PlayRailgunFire(); break;
            case WATER_EXPLOSION: Sounds.PlayWaterExplosion(); break;
            case INFANTRY_ATTACK: Sounds.PlayInfantryAttack(); break;
            case INFANTRY_CAPTURE: Sounds.PlayInfantryCapture(); break;
            case LIBERATE: Sounds.PlayLiberate(); break;
        }
    }

    @Override
    public void NewReport(LaunchReport report)
    {
        //Flash the reports button accordingly.
        if(report.GetMajor())
        {
            reportStatus = ReportsStatus.MAJOR;
        }
        else
        {
            reportStatus = ReportsStatus.MINOR;
        }
    }

    @Override
    public void Quit()
    {
        finish();
    }

    @Override
    public void AllianceCreated()
    {
        //Return to diplomacy screen after creating a new alliance.
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SetView(new DiplomacyView(game, me));
            }
        });
    }

    @Override
    public String GetProcessNames()
    {
        String strApps = "";

        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for(ApplicationInfo applicationInfo : packages)
        {
            strApps += applicationInfo.processName + "\n";
        }

        return strApps.substring(0, strApps.length() - 1);
    }

    @Override
    public boolean GetConnectionMobile()
    {
        //TODO: FIX THIS. It currently breaks on some player's phones.
        /*ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null)
        {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            if(networkCapabilities != null)
            {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    return false;

                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    return true;
            }
        }*/

        return false;
    }

    @Override
    public void DeviceChecksRequested()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    SafetyNet.getClient(me).attest(Security.CreateRandomHash(), "AIzaSyCzBW2Vr5AHj0RlZsmoUlWq56L_FQmC_V4")
                    .addOnSuccessListener(me,
                            new OnSuccessListener<SafetyNetApi.AttestationResponse>()
                            {
                                @Override
                                public void onSuccess(SafetyNetApi.AttestationResponse response)
                                {
                                    // Indicates communication with the service was successful.
                                    try
                                    {
                                        String strPayload = response.getJwsResult().split("\\.")[1];
                                        String strPayloadDecoded = new String(Base64.decode(strPayload, Base64.NO_WRAP));
                                        JSONObject json = new JSONObject(strPayloadDecoded);
                                        boolean bProfileMatch = json.getBoolean("ctsProfileMatch");
                                        boolean bBasicIntegrity = json.getBoolean("basicIntegrity");

                                        game.DeviceCheck(false, false, 0, bProfileMatch, bBasicIntegrity);
                                    }
                                    catch (JSONException e)
                                    {
                                        //A JSON error occurred.
                                        game.DeviceCheck(true, false, 0, false, false);
                                    }
                                    catch(Exception e)
                                    {
                                        //Anything else went wrong.
                                        game.DeviceCheck(true, false, 0, false, false);
                                    }
                                }
                            })
                    .addOnFailureListener(me, new OnFailureListener()
                    {
                        @Override
                        public void onFailure(Exception e)
                        {
                            // An error occurred while communicating with the service.
                            if (e instanceof ApiException)
                            {
                                //An error with the Google Play services API contains some additional details.
                                ApiException apiException = (ApiException) e;
                                game.DeviceCheck(false, true, ((ApiException) e).getStatusCode(), false, false);
                            }
                            else
                            {
                                //Some other error occurred.
                                game.DeviceCheck(true, false, 0, false, false);
                            }
                        }
                    });

                }
                catch(NoSuchAlgorithmException ex)
                {
                    //Some ridiculous error occurred.
                    game.DeviceCheck(true, false, 0, false, false);
                }
            }
        });
    }

    @Override
    public void DisplayGeneralError()
    {
        ShowBasicOKDialog(getString(R.string.general_error));
    }

    @Override
    public void TempBanned(final String strReason, final long oDuration)
    {
        interactionMode = InteractionMode.CANT_LOG_IN;
        game.Suspend();

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SetView(new BannedView(game, me, strReason, oDuration));
            }
        });
    }

    @Override
    public void PermBanned(final String strReason)
    {
        interactionMode = InteractionMode.CANT_LOG_IN;
        game.Suspend();

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SetView(new BannedView(game, me, strReason));
            }
        });
    }

    @Override
    public void ReceiveUser(User user)
    {
        if(CurrentView instanceof MainNormalView)
        {
            LaunchView bottomView = ((MainNormalView)CurrentView).GetBottomView();

            if(bottomView instanceof PlayerView)
            {
                ((PlayerView)bottomView).UserReceived(user);
            }
        }
    }

    public int GetTheme()
    {
        return lMapTheme;
    }

    public void SetFirebaseToken()
    {
        tokenTask = FirebaseMessaging.getInstance().getToken();
        tokenTask.addOnCompleteListener(new OnCompleteListener<String>()
        {
            @Override
            public void onComplete(Task<String> task)
            {
                try
                {
                    strFirebaseToken = tokenTask.getResult();
                    game.SetToken(strFirebaseToken);
                }
                catch(RuntimeExecutionException ex)
                {
                    NewEvent(new LaunchEvent("Firebase token task failed.", LaunchEvent.SoundEffect.RESPAWN));
                }
            }
        });
    }

    @Override
    public void UpdateFirebaseToken()
    {
        SetFirebaseToken();
    }

    public boolean ZoomedIn()
    {
        if(fltZoomLevel >= ZOOMED_IN_THRESHOLD)
        {
            return true;
        }

        return false;
    }

    public void SetMaxZoom(float maxZoom)
    {
        if(maxZoom <= 21 && maxZoom >= 3)
        {
            fltMaxZoom = maxZoom;
            map.setMaxZoomPreference(fltMaxZoom);
        }
    }

    /*public void DrawAirspaceCircles()
    {
        Player ourPlayer = game.GetOurPlayer();
        Context context = this;

        if(!AirspaceOverlays.isEmpty())
        {
            for(Circle circle : AirspaceOverlays)
            {
                circle.remove();
            }
            AirspaceOverlays.clear();
        }

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for(Player player : game.GetPlayers())
                {
                    if(game.GetAllegiance(ourPlayer, player) == LaunchGame.Allegiance.ENEMY || game.GetAllegiance(ourPlayer, player) == LaunchGame.Allegiance.NEUTRAL || game.GetAllegiance(ourPlayer, player) == LaunchGame.Allegiance.PENDING_TREATY)
                    {
                        AirspaceOverlays.add(map.addCircle(new CircleOptions()
                                .center(Utilities.GetLatLng(player.GetPosition()))
                                .radius(game.GetConfig().GetAirspaceDistance() * Defs.METRES_PER_KM)
                                .strokeColor(Utilities.ColourFromAttr(context, R.attr.BadColour))
                                .strokeWidth(4.0f)));
                    }
                }
            }
        });
    }*/

    public void ReturnAircraft(List<EntityPointer> aircrafts)
    {
        game.UnitCommand(MoveOrders.RETURN, aircrafts, null, null, CargoSystem.LootType.NONE, -1, -1);
    }

    public void ToggleShowBlastRadii(boolean bShow)
    {
        bShowBlastRadii = bShow;

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for(Circle circle : BlastRadii.values())
                {
                    circle.setVisible(bShow);
                }

                for(Circle circle : EMPMarkers.values())
                {
                    circle.setVisible(bShow);
                }

                for(Circle circle : ABMEMPMarkers.values())
                {
                    circle.setVisible(bShow);
                }

                //TODO: This can be made much more efficient by just having this code hide or show the circles in BlastRadii and EMPMarkers, as the polyline code does there. To enable this, we will need to simply always generate the circles, but they will need to be setVisible(bShowBlastRadii) at time of creation.
                /*if(bShow)
                {
                    for(Missile missile : game.GetMissiles())
                    {
                        if(missile.Flying())
                        {
                            MissileType type = game.GetConfig().GetMissileType(missile.GetType());
                            GeoCoord geoTarget = missile.GetTracking() ? game.GetMissileTarget(missile) : missile.GetTarget();

                            if(type.GetIncendiary())
                            {
                                BlastRadii.put(missile.GetID(), map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(geoTarget))
                                        .radius(MissileStats.GetBlastRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                        .fillColor(Utilities.ColourFromAttr(me, R.attr.IncendiaryBlastRadiusColour))
                                        .strokeWidth(0.0f)));
                            }
                            else
                            {
                                BlastRadii.put(missile.GetID(), map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(geoTarget))
                                        .radius(MissileStats.GetBlastRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                        .fillColor(Utilities.ColourFromAttr(me, R.attr.BlastRadiusColour))
                                        .strokeWidth(0.0f)));
                            }

                            EMPMarkers.put(missile.GetID(), map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(geoTarget))
                                    .radius(game.GetConfig().GetMissileEMPRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                    .fillColor(Utilities.ColourFromAttr(me, R.attr.EMPColour))
                                    .strokeWidth(0.0f)));

                            if(type.GetSonobuoy())
                            {
                                BlastRadii.put(missile.GetID(), map.addCircle(new CircleOptions()
                                        .center(Utilities.GetLatLng(geoTarget))
                                        .radius(Defs.SONAR_RANGE * Defs.METRES_PER_KM)
                                        .strokeColor(Utilities.ColourFromAttr(me, R.attr.SonarColour))
                                        .strokeWidth(3.0f)));
                            }
                        }
                    }
                }
                else
                {
                    if(!BlastRadii.isEmpty())
                    {
                        for(Map.Entry<Integer, Circle> entry : BlastRadii.entrySet())
                        {
                            Circle circle = entry.getValue();

                            if(circle != null)
                                circle.remove();
                        }
                        BlastRadii.clear();
                    }

                    if(!EMPMarkers.isEmpty())
                    {
                        for(Map.Entry<Integer, Circle> entry : EMPMarkers.entrySet())
                        {
                            Circle circle = entry.getValue();

                            if(circle != null)
                                circle.remove();
                        }
                        EMPMarkers.clear();
                    }
                }*/
            }
        });
    }

    public void ToggleShowMissileTrails(boolean bShow)
    {
        bShowMissileTrails = bShow;
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for(Polyline line : MissileTrails.values())
                {
                    line.setVisible(bShow);
                }

                if(bShow)
                {
                    for(Missile missile : game.GetMissiles())
                    {
                        MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                        if(type != null)
                        {
                            GeoCoord geoTarget = null;

                            if(missile.GetTracking())
                                geoTarget = game.GetMissileTarget(missile);
                            else
                                geoTarget = missile.GetTarget();

                            if(MissileTrails.containsKey(missile.GetID()))
                            {
                                if(MissileTrails.get(missile.GetID()) != null)
                                    MissileTrails.get(missile.GetID()).remove();

                                MissileTrails.remove(missile.GetID());
                            }

                            if(geoTarget != null)
                            {
                                MissileTrails.put(missile.GetID(), map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(missile.GetOrigin()))
                                        .add(Utilities.GetLatLng(geoTarget))
                                        .width(TRAIL_LINE_WIDTH)
                                        .geodesic(true) //TEST
                                        .visible(bShowMissileTrails)
                                        .color(Utilities.ColourFromAttr(me, missile.GetTracking()? R.attr.MissilePathTrackingColour : R.attr.MissilePathColour))));
                            }
                        }
                    }
                }
                else
                {
                    for(Polyline line : MissileTrails.values())
                    {
                        line.remove();
                    }

                    MissileTrails.clear();
                }
            }
        });
    }

    /*public void BlastAnimationFinished(BlastEffect effect)
    {
        if(BlastEffects.contains(effect))
            BlastEffects.remove(effect);
    }*/

    public void DrawAllMovableUI()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(!MovableTrajectories.isEmpty())
                {
                    for(Polyline line : MovableTrajectories)
                        line.remove();

                    MovableTrajectories.clear();
                }

                if(!MassCommandTrajectories.isEmpty())
                {
                    for(Polyline line : MassCommandTrajectories.values())
                    {
                        if(line != null)
                        {
                            line.remove();
                            line = null;
                        }
                    }

                    MassCommandTrajectories.clear();
                }

                PatternItem dash = new Dash(10);
                PatternItem gap = new Gap(20);

                List<PatternItem> trajectoryPattern = Arrays.asList(gap, dash);

                for(Movable movable : game.GetAllMovables())
                {
                    if(!movable.ApparentlyEquals(selectedEntity) && game.EntityIsFriendly(movable, game.GetOurPlayer()))
                    {
                        switch(movable.GetMoveOrders())
                        {
                            case MOVE:
                            {
                                if(movable.GetGeoTarget() != null)
                                {
                                    if(movable.HasGeoCoordChain())
                                    {
                                        Map<Integer, GeoCoord> Coordinates = movable.GetCoordinates();
                                        List<LatLng> points = new ArrayList<LatLng>();
                                        GeoCoord geoFinal = movable.GetPosition();

                                        points.add(Utilities.GetLatLng(movable.GetPosition()));

                                        for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
                                        {
                                            GeoCoord geoCoord = Coordinates.get(i);

                                            if(geoCoord != null)
                                            {
                                                points.add(Utilities.GetLatLng(geoCoord));
                                            }
                                        }

                                        Polyline line = map.addPolyline(new PolylineOptions()
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.GoodColour)));

                                        line.setPoints(points);

                                        MovableTrajectories.add(line);

                                        MoveMarkers.add(map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(Coordinates.get(LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()))))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint))));
                                    }
                                    else if(movable.HasGeoTarget())
                                    {
                                        MovableTrajectories.add(map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(movable.GetPosition()))
                                                .add(Utilities.GetLatLng(movable.GetGeoTarget()))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.GoodColour))));

                                        MoveMarkers.add(map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(movable.GetGeoTarget()))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_movement_waypoint))));
                                    }
                                }
                            }
                            break;

                            case RETURN:
                            {
                                //Only aircraft use RETURN, so we'll just assume this is an aircraft.
                                AirplaneInterface aircraft = (AirplaneInterface)movable;

                                if(aircraft.GetHomeBase() != null)
                                {
                                    MapEntity entity = aircraft.GetHomeBase().GetMapEntity(game);

                                    if(entity != null)
                                    {
                                        MovableTrajectories.add(map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(movable.GetPosition()))
                                                .add(Utilities.GetLatLng(entity.GetPosition()))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.GoodColour))));
                                    }
                                }
                            }
                            break;

                            case ATTACK:
                            case CAPTURE:
                            {
                                if(movable.GetTarget() != null || movable.GetGeoTarget() != null)
                                {
                                    GeoCoord geoTarget = null;
                                    MapEntity target = null;

                                    if(movable.GetTarget() != null && movable.GetTarget().GetMapEntity(game) != null)
                                    {
                                        target = movable.GetTarget().GetMapEntity(game);
                                        geoTarget = movable.GetTarget().GetMapEntity(game).GetPosition().GetCopy();
                                    }
                                    else if(movable.GetGeoTarget() != null)
                                    {
                                        geoTarget = movable.GetGeoTarget();
                                    }

                                    if(geoTarget != null)
                                    {
                                        MovableTrajectories.add(map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(movable.GetPosition()))
                                                .add(Utilities.GetLatLng(geoTarget))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.BadColour))));

                                        MoveMarkers.add(map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(geoTarget))
                                                .anchor(0.5f, 0.5f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_attack_waypoint))));
                                    }
                                }
                            }
                            break;

                            case PROVIDE_FUEL:
                            case SEEK_FUEL:
                            {
                                if(movable.GetTarget() != null)
                                {
                                    MapEntity mapTarget = movable.GetTarget().GetMapEntity(game);

                                    if(mapTarget != null)
                                    {
                                        MovableTrajectories.add(map.addPolyline(new PolylineOptions()
                                                .add(Utilities.GetLatLng(movable.GetPosition()))
                                                .add(Utilities.GetLatLng(mapTarget.GetPosition()))
                                                .pattern(trajectoryPattern)
                                                .geodesic(true)
                                                .width(3.0f)
                                                .color(Utilities.ColourFromAttr(me, R.attr.FuelColour))));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                //Artillery Gun lines
                for(ArtilleryGun gun : game.GetArtilleryGuns())
                {
                    if(game.EntityIsFriendly(gun, game.GetOurPlayer()))
                    {
                        if(gun.HasFireOrder())
                        {
                            FireOrder order = gun.GetFireOrder();

                            if(order != null)
                            {
                                geoTarget = order.GetGeoTarget();
                            }

                            if(geoTarget != null)
                            {
                                MovableTrajectories.add(map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(gun.GetPosition()))
                                        .add(Utilities.GetLatLng(geoTarget))
                                        .pattern(trajectoryPattern)
                                        .geodesic(true)
                                        .width(3.0f)
                                        .color(Utilities.ColourFromAttr(me, R.attr.BadColour))));

                                MoveMarkers.add(map.addMarker(new MarkerOptions()
                                        .position(Utilities.GetLatLng(geoTarget))
                                        .anchor(0.5f, 0.5f)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_attack_waypoint))));
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void MissileExploded(Missile missile)
    {
        if(missile != null)
        {
            MissileType type = game.GetConfig().GetMissileType(missile.GetType());

            if(type != null)
            {
                if(type.GetNuclear())
                    BlastEffects.add(new BlastEffect(me, map, game, missile));
            }
        }
    }

    @Override
    public void InterceptorReachedTarget(Interceptor interceptor)
    {
        if(game.GetConfig().GetInterceptorType(interceptor.GetType()).GetNuclear())
            BlastEffects.add(new BlastEffect(me, map, game, interceptor));
    }

    @Override
    public void TorpedoExploded(Torpedo torpedo)
    {
        if(game.GetConfig().GetTorpedoType(torpedo.GetType()).GetNuclear())
            BlastEffects.add(new BlastEffect(me, map, game, torpedo));
    }

    private Map<Integer, Marker> GetEntityMarkerMap(MapEntity entity)
    {
        if(entity instanceof ResourceDeposit)
            return DepositMarkers;
        else if(entity instanceof Interceptor)
            return InterceptorMarkers;
        else if(entity instanceof Torpedo)
            return TorpedoMarkers;
        else
        {
            EntityType type = entity.GetEntityType();

            if(entity instanceof Airplane)
                type = EntityType.AIRPLANE;
            else if(entity instanceof Tank)
                type = EntityType.TANK;
            else if(entity instanceof Ship)
                type = EntityType.SHIP;
            else if(entity instanceof Submarine)
                type = EntityType.SUBMARINE;

            return AllMarkers.get(type);
        }
    }

    private Map<Integer, GroundOverlay> GetEntityOverlayMap(MapEntity entity)
    {
        return AllOverlays.get(entity.GetEntityType());
    }

    private void CheckAndRequestLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // Permission is already granted — safe to proceed
            locatifier.Resume();
        }
        else
        {
            // Ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public ActivityResultLauncher<PickVisualMediaRequest> registerPhotoPicker(ActivityResultCallback<Uri> callback)
    {
        return registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), callback);
    }

    public void LaunchPhotoPicker(UploadAvatarView view)
    {
        pendingAvatarView = view;

        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()
        );
    }

    //----------------------------------------------------------------------------------------------

    //Rendering-related stuff.

    //----------------------------------------------------------------------------------------------

    /**
     * Create an entity UI, determining its visibility on the way.
     * @param entity The entity to create UI for.
     */
    private void CreateEntityUI(final MapEntity entity)
    {
        final Context context = this;
        final MainActivity self = this;

        if(entity.GetPosition().GetValid() && LaunchUtilities.GetEntityVisibility(game, entity))
        {
            if(map != null)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        RemoveEntityUI(entity, false);

                        EntityType typeToAdd = entity.GetEntityType();

                        if(entity instanceof Airplane)
                            typeToAdd = EntityType.AIRPLANE;
                        else if(entity instanceof Tank)
                            typeToAdd = EntityType.TANK;
                        else if(entity instanceof Ship)
                            typeToAdd = EntityType.SHIP;
                        else if(entity instanceof Submarine)
                            typeToAdd = EntityType.SUBMARINE;

                        if(AllMarkers.get(typeToAdd) == null)
                        {
                            AllMarkers.put(typeToAdd, new ConcurrentHashMap<>());
                        }

                        //"Non-marker" markers.
                        if(entity instanceof Radiation)
                        {
                            Radiation radiation = (Radiation) entity;
                            if(RadiationMarkers.containsKey(radiation.GetID()))
                            {
                                Circle circle = RadiationMarkers.get(radiation.GetID());
                                RadiationMarkers.get(radiation.GetID()).remove();
                                RadiationMarkers.remove(radiation.GetID());

                                if(circle != null)
                                    circle.remove();
                            }

                            RadiationMarkers.put(radiation.GetID(), map.addCircle(new CircleOptions()
                                    .center(Utilities.GetLatLng(radiation.GetPosition()))
                                    .radius(radiation.GetRadius() * Defs.METRES_PER_KM)
                                    .fillColor(Utilities.ColourFromAttr(context, R.attr.RadiationColour))
                                    .strokeWidth(0.0f)));
                        }
                        else if(entity instanceof Interceptor)
                        {
                            Interceptor interceptor = (Interceptor) entity;
                            InterceptorType type = game.GetConfig().GetInterceptorType(interceptor.GetType());

                            GeoCoord interceptorTarget = null;

                            switch(interceptor.GetTargetType())
                            {
                                case MISSILE:
                                {
                                    if(game.GetMissile(interceptor.GetTargetID()) != null && game.GetMissile(interceptor.GetTargetID()).GetPosition() != null)
                                    {
                                        interceptorTarget = game.GetMissile(interceptor.GetTargetID()).GetPosition();
                                    }
                                }
                                break;

                                case AIRPLANE:
                                {
                                    if(game.GetAirplane(interceptor.GetTargetID()) != null && game.GetAirplane(interceptor.GetTargetID()).GetPosition() != null)
                                    {
                                        interceptorTarget = game.GetAirplane(interceptor.GetTargetID()).GetPosition();
                                    }
                                }
                                break;
                            }

                            if(interceptorTarget != null)
                            {
                                MarkerOptions options = new MarkerOptions();
                                options.position(Utilities.GetLatLng(entity.GetPosition()));
                                options.anchor(0.5f, 0.5f);
                                options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetInterceptorBitmap(self, game, type, game.GetAllegiance(game.GetOurPlayer(), interceptor), type.GetAssetID())));
                                options.rotation((float) GeoCoord.ToDegrees(entity.GetPosition().BearingTo(interceptorTarget)));

                                if(InterceptorMarkers.containsKey(interceptor.GetID()))
                                {
                                    InterceptorMarkers.get(interceptor.GetID()).remove();
                                    InterceptorMarkers.remove(interceptor.GetID());
                                }

                                InterceptorMarkers.put(interceptor.GetID(), map.addMarker(options));

                                if (InterceptorTrajectories.containsKey(interceptor.GetID()))
                                {
                                    InterceptorTrajectories.get(interceptor.GetID()).remove();
                                    InterceptorTrajectories.remove(interceptor.GetID());
                                }

                                InterceptorTrajectories.put(interceptor.GetID(), map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(interceptor.GetPosition()))
                                        .add(Utilities.GetLatLng(interceptorTarget))
                                        .width(INTERCEPTOR_TRAJ_WIDTH)
                                        .geodesic(true)
                                        .color(Utilities.ColourFromAttr(context, R.attr.InterceptorPathColour))));

                                if(type.GetABM())
                                {
                                    ABMEMPMarkers.put(interceptor.GetID(), map.addCircle(new CircleOptions()
                                            .center(Utilities.GetLatLng(interceptorTarget))
                                            .visible(bShowBlastRadii)
                                            .radius(type.GetBlastRadius() * Defs.ABM_EMP_RADIUS_MULTIPLIR * Defs.METRES_PER_KM)
                                            .fillColor(Utilities.ColourFromAttr(me, R.attr.EMPColour))
                                            .strokeWidth(0.0f)));
                                }
                            }
                        }
                        else if (entity instanceof Torpedo)
                        {
                            Torpedo torpedo = (Torpedo) entity;
                            TorpedoType type = game.GetConfig().GetTorpedoType(torpedo.GetType());
                            GeoCoord geoTarget = null;
                            MarkerOptions options = new MarkerOptions();
                            options.position(Utilities.GetLatLng(entity.GetPosition()));
                            options.anchor(0.5f, 0.5f);


                            if(torpedo.HasTarget())
                            {
                                MapEntity target = torpedo.GetTarget().GetMapEntity(game);

                                if(target != null)
                                    geoTarget = target.GetPosition();
                            }
                            else if(torpedo.GetGeoTarget() != null)
                            {
                                geoTarget = torpedo.GetGeoTarget();
                            }

                            options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetTorpedoBitmap(self, game, type, game.GetAllegiance(game.GetOurPlayer(), torpedo), type.GetAssetID())));
                            options.rotation((float) GeoCoord.ToDegrees(torpedo.GetPosition().GetLastBearing()));

                            if(TorpedoMarkers.containsKey(torpedo.GetID()))
                            {
                                TorpedoMarkers.get(torpedo.GetID()).remove();
                                TorpedoMarkers.remove(torpedo.GetID());
                            }

                            TorpedoMarkers.put(torpedo.GetID(), map.addMarker(options));

                            if(TorpedoTrajectories.containsKey(torpedo.GetID()))
                            {
                                TorpedoTrajectories.get(torpedo.GetID()).remove();
                                TorpedoTrajectories.remove(torpedo.GetID());
                            }

                            if(geoTarget != null && (torpedo.GetState() == Torpedo.TorpedoState.TRAVELLING || torpedo.GetState() == Torpedo.TorpedoState.HOMING))
                            {
                                TorpedoTrajectories.put(torpedo.GetID(), map.addPolyline(new PolylineOptions()
                                        .add(Utilities.GetLatLng(torpedo.GetPosition()))
                                        .add(Utilities.GetLatLng(geoTarget))
                                        .width(TORPEDO_TRAJ_WIDTH)
                                        .geodesic(true)
                                        .color(Utilities.ColourFromAttr(context, R.attr.MissilePathColour))));
                            }
                        }
                        else
                        {
                            //Map markers generally.
                            MarkerOptions options = new MarkerOptions();
                            options.position(Utilities.GetLatLng(entity.GetPosition()));
                            options.anchor(0.5f, 0.5f);
                            options.flat(true);

                            if(entity instanceof Player)
                            {
                                Player player = (Player) entity;
                                options.icon(GetPlayerIcon(player));
                                options.alpha(player.GetRespawnProtected() ? 0.5f : 1.0f);

                                if(player.GetID() != game.GetOurPlayerID() && !game.GetOurPlayer().GetIsAnAdmin())
                                {
                                    return;
                                }
                            }
                            else if(entity instanceof Shipyard)
                            {
                                options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetShipyardBitmap(context, game, (Shipyard)entity)));
                            }
                            else if(entity instanceof Loot)
                            {
                                options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetLootBitmap(context, (Loot)entity)));
                            }
                            else if(entity instanceof Rubble)
                            {
                                options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetRubbleBitmap(context, game, (Rubble)entity)));
                            }
                            else if(entity instanceof Structure)
                            {
                                Structure structure = (Structure)entity;

                                if(game.EntityIsFriendly(structure, game.GetOurPlayer()) || structure.GetVisible())
                                {
                                    options.icon(BitmapDescriptorFactory.fromBitmap(StructureIconBitmaps.GetStructureBitmap(context, game, (Structure)entity)));
                                    options.alpha(structure.GetRespawnProtected() ? 0.5f : 1.0f);
                                }
                                else
                                {
                                    if(AllOverlays.get(entity.GetEntityType()) == null)
                                    {
                                        AllOverlays.put(entity.GetEntityType(), new ConcurrentHashMap<>());
                                    }

                                    GroundOverlayOptions overlayOptions = new GroundOverlayOptions()
                                            .image(BitmapDescriptorFactory.fromBitmap(StructureIconBitmaps.GetStructureBitmap(context, game, (Structure)entity)))
                                            .position(Utilities.GetLatLng(structure.GetPosition()), 50f, 50f)
                                            .transparency(structure.GetRespawnProtected() ? 0.5f : 0.0f)
                                            .visible(true)
                                            .clickable(true)
                                            .zIndex(10f)
                                            .bearing(0f);

                                    Map<Integer, GroundOverlay> Overlays = GetEntityOverlayMap(entity);

                                    if(Overlays != null)
                                    {
                                        if(Overlays.containsKey(entity.GetID()))
                                        {
                                            Overlays.get(entity.GetID()).remove();
                                            Overlays.remove(entity.GetID());
                                        }

                                        GroundOverlay overlay = map.addGroundOverlay(overlayOptions);
                                        overlay.setTag(entity.GetPointer());

                                        MapChunkKey key = GetChunkForLatLng(overlay.getPosition());

                                        List<GroundOverlay> list;

                                        list = OverlayChunks.get(key);

                                        if(list == null)
                                        {
                                            list = new CopyOnWriteArrayList<>();
                                            OverlayChunks.put(key, list);
                                        }

                                        list.add(overlay);

                                        Overlays.put(entity.GetID(), overlay);
                                    }

                                    options = null;
                                }
                            }
                            else if(game.EntityIsFriendly(entity, game.GetOurPlayer()) || entity.GetVisible())
                            {
                                if(entity instanceof Airplane)
                                {
                                    Airplane aircraft = ((Airplane)entity);
                                    options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetAircraftBitmap(context, game, aircraft)));
                                    options.rotation((float) GeoCoord.ToDegrees(aircraft.GetPosition().GetLastBearing()));
                                }
                                else if(entity instanceof Infantry)
                                {
                                    Infantry infantry = ((Infantry)entity);
                                    options.icon(BitmapDescriptorFactory.fromBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(context, game, infantry)));
                                }
                                else if(entity instanceof Tank)
                                {
                                    Tank tank = (Tank)entity;
                                    options.icon(BitmapDescriptorFactory.fromBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(context, game, tank)));
                                }
                                else if(entity instanceof CargoTruck)
                                {
                                    CargoTruck truck = (CargoTruck)entity;
                                    options.icon(BitmapDescriptorFactory.fromBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(context, game, truck)));
                                }
                                else if(entity instanceof Ship)
                                {
                                    Ship ship = (Ship)entity;
                                    options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(context, game, ship)));
                                }
                                else if(entity instanceof Submarine)
                                {
                                    Submarine submarine = (Submarine)entity;
                                    options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(context, game, submarine)));
                                    options.alpha(submarine.Submerged() ? 0.5f : 1.0f);
                                }
                                if(entity instanceof Missile)
                                {
                                    Missile missile = (Missile) entity;
                                    MissileType type = game.GetConfig().GetMissileType(missile.GetType());
                                    options.icon(BitmapDescriptorFactory.fromBitmap(EntityIconBitmaps.GetMissileBitmap(self, game, type, game.GetAllegiance(game.GetOurPlayer(), missile), type.GetAssetID())));
                                    options.rotation((float) GeoCoord.ToDegrees(entity.GetPosition().GetLastBearing()));
                                    GeoCoord geoTarget;

                                    if(missile.GetTracking())
                                        geoTarget = game.GetMissileTarget(missile);
                                    else
                                        geoTarget = missile.GetTarget();

                                    if(missile.GetThreatensUs() || missile.GetThreatensAllies())
                                    {
                                        ThreatMarkers.put(missile.GetID(), map.addMarker(new MarkerOptions()
                                                .position(Utilities.GetLatLng(missile.GetPosition()))
                                                .anchor(0.5f, 1.0f)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_threat))
                                                .zIndex(1.0f)));
                                    }

                                    Circle blast = map.addCircle(new CircleOptions()
                                            .center(Utilities.GetLatLng(geoTarget))
                                            .visible(bShowBlastRadii)
                                            .radius(MissileStats.GetBlastRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                            .fillColor(Utilities.ColourFromAttr(me, R.attr.BlastRadiusColour))
                                            .strokeWidth(0.0f)
                                            .clickable(true));

                                    blast.setTag(missile);

                                    BlastRadii.put(missile.GetID(), blast);

                                    Circle emp = map.addCircle(new CircleOptions()
                                            .center(Utilities.GetLatLng(geoTarget))
                                            .visible(bShowBlastRadii)
                                            .radius(MissileStats.GetMissileEMPRadius(type, missile.GetAirburst()) * Defs.METRES_PER_KM)
                                            .fillColor(Utilities.ColourFromAttr(me, R.attr.EMPColour))
                                            .strokeWidth(0.0f)
                                            .clickable(true));

                                    emp.setTag(missile);

                                    EMPMarkers.put(missile.GetID(), emp);

                                    if(type.GetSonobuoy())
                                    {
                                        Circle circle = map.addCircle(new CircleOptions()
                                                .center(Utilities.GetLatLng(geoTarget))
                                                .visible(bShowBlastRadii)
                                                .radius(Defs.SONAR_RANGE * Defs.METRES_PER_KM)
                                                .strokeColor(Utilities.ColourFromAttr(me, R.attr.SonarColour))
                                                .strokeWidth(3.0f)
                                                .clickable(true));

                                        circle.setTag(missile);

                                        BlastRadii.put(missile.GetID(), circle);
                                    }
                                }
                            }
                            else
                            {
                                //Marker is something that shouldn't be drawn. Cancel.
                                return;
                            }

                            Map<Integer, Marker> Markers = GetEntityMarkerMap(entity);

                            if(Markers != null && options != null)
                            {
                                if(Markers.containsKey(entity.GetID()))
                                {
                                    Markers.get(entity.GetID()).remove();
                                    Markers.remove(entity.GetID());
                                }

                                Marker marker = map.addMarker(options);
                                marker.setTag(entity.GetPointer());

                                MapChunkKey key = GetChunkForLatLng(marker.getPosition());

                                List<Marker> list;

                                list = MarkerChunks.get(key);

                                if(list == null)
                                {
                                    list = new CopyOnWriteArrayList<>();
                                    MarkerChunks.put(key, list);
                                }

                                list.add(marker);

                                Markers.put(entity.GetID(), marker);
                            }
                        }
                    }
                });
            }
        }
    }

    public void HighLevelUIRefresh()
    {
        runOnUiThread(() ->
        {
            LatLngBounds visibleBounds = map.getProjection().getVisibleRegion().latLngBounds;

            Set<MapChunkKey> visibleChunks = GetVisibleChunks(visibleBounds);

            float fltNewZoom = map.getCameraPosition().zoom;
            int newZoomInt = (int)fltNewZoom;
            int oldZoomInt = (int)fltZoomLevel;

            boolean zoomChanged = (newZoomInt != oldZoomInt);

            if(zoomChanged)
            {
                // Reset per-chunk generation state for the new zoom level
                ChunkZoomLevelGenerated.clear();
            }

            fltZoomLevel = fltNewZoom;

            // 1. Hide markers in non-visible chunks
            for(Map.Entry<MapChunkKey, List<Marker>> entry : MarkerChunks.entrySet())
            {
                if(!visibleChunks.contains(entry.getKey()))
                {
                    for(Marker m : entry.getValue())
                    {
                        m.setVisible(false);
                    }
                }
            }

            for(Map.Entry<MapChunkKey, List<GroundOverlay>> entry : OverlayChunks.entrySet())
            {
                if(!visibleChunks.contains(entry.getKey()))
                {
                    for(GroundOverlay g : entry.getValue())
                    {
                        g.setVisible(false);
                    }
                }
            }

            // 2. Process each visible chunk
            for(MapChunkKey key : visibleChunks)
            {
                EnsureChunkUICreated(key, fltNewZoom, visibleBounds);

                List<Marker> markers = MarkerChunks.get(key);

                if(markers == null) continue;

                for(Marker marker : markers)
                {
                    LatLng pos = marker.getPosition();
                    marker.setVisible(visibleBounds.contains(pos));
                }
            }

            for(MapChunkKey key : visibleChunks)
            {
                EnsureChunkUICreated(key, fltNewZoom, visibleBounds);

                List<GroundOverlay> overlays = OverlayChunks.get(key);

                if(overlays == null) continue;

                for(GroundOverlay overlay : overlays)
                {
                    LatLng pos = overlay.getPosition();

                    overlay.setVisible(visibleBounds.contains(pos));
                }
            }
        });
    }

    private void EnsureChunkUICreated(MapChunkKey key, float fltZoom, LatLngBounds visibleBounds)
    {
        int zoomInt = (int) fltZoom;

        Integer lastZoom = ChunkZoomLevelGenerated.get(key);

        boolean zoomIn = lastZoom == null || zoomInt >= lastZoom;
        boolean zoomOut = lastZoom != null && zoomInt < lastZoom;

        List<EntityPointer> entities = ChunkEntities.get(key);

        if(entities == null) return;

        ChunkZoomLevelGenerated.put(key, zoomInt);

        for(MapEntity entity : EntityPointer.GetMapEntitiesFromPointers(entities, game))
        {
            if(entity instanceof Structure && !game.EntityIsFriendly(entity, game.GetOurPlayer()) && !entity.GetVisible())
            {
                Map<Integer, GroundOverlay> typeMap = AllOverlays.get(entity.GetEntityType());

                if(typeMap == null)
                {
                    typeMap = new ConcurrentHashMap<>();
                    AllOverlays.put(entity.GetEntityType(), typeMap);
                }

                GroundOverlay g = typeMap.get(entity.GetID());

                //Any structure overlay over zoom level 10 should be generated.
                if(fltZoom > 10f)
                {
                    if(g == null)
                    {
                        CreateEntityUI(entity);
                    }

                    if(g != null)
                    {
                        LatLng pos = g.getPosition();
                        g.setVisible(visibleBounds.contains(pos));
                    }
                }
            }
            else
            {
                EntityType typeToCheck = entity.GetEntityType();

                if(entity instanceof Airplane)
                    typeToCheck = EntityType.AIRPLANE;
                else if(entity instanceof Tank)
                    typeToCheck = EntityType.TANK;
                else if(entity instanceof Ship)
                    typeToCheck = EntityType.SHIP;
                else if(entity instanceof Submarine)
                    typeToCheck = EntityType.SUBMARINE;

                Map<Integer, Marker> typeMap = AllMarkers.get(typeToCheck);

                if(typeMap == null)
                {
                    typeMap = new ConcurrentHashMap<>();
                    AllMarkers.put(typeToCheck, typeMap);
                }

                Marker m = typeMap.get(entity.GetID());

                float probability = GetGenerationProbability(fltZoom, 3.5f, 10f, typeToCheck);

                if(zoomIn)
                {
                    if(m == null && Math.random() <= probability)
                    {
                        CreateEntityUI(entity);
                    }
                }
                else if(zoomOut)
                {
                    if(m != null && Math.random() > probability)
                    {
                        m.setVisible(false);
                    }
                }

                if(m != null)
                {
                    LatLng pos = m.getPosition();
                    m.setVisible(visibleBounds.contains(pos));
                }
            }
        }
    }

    public void RebuildMap()
    {
        bRebuildMap = true;
        GameTicked(0);
    }

    public void RemoveEntityUI(MapEntity entity, boolean clearSelected)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(clearSelected)
                {
                    //If the selected entity is removed, deselect it and back out of any associated modes.
                    if(selectedEntity != null && interactionMode != InteractionMode.SELECT_NEW_HOMEBASE)
                    {
                        if(selectedEntity.equals(entity))
                        {
                            ClearSelectedEntity();
                            InformationMode(false);
                        }
                    }
                }

                if(entity instanceof Interceptor)
                {
                    Marker marker = InterceptorMarkers.get(entity.GetID());
                    Polyline line = InterceptorTrajectories.get(entity.GetID());
                    Circle circleEMP = ABMEMPMarkers.get(entity.GetID());

                    if(circleEMP != null)
                    {
                        ABMEMPMarkers.remove(entity.GetID());
                        circleEMP.remove();
                        circleEMP = null;
                    }

                    if(marker != null)
                    {
                        InterceptorMarkers.remove(entity.GetID());
                        marker.remove();
                    }

                    if(line != null)
                    {
                        InterceptorTrajectories.remove(entity.GetID());
                        line.remove();
                    }
                }
                else if(entity instanceof Torpedo)
                {
                    Marker marker = TorpedoMarkers.get(entity.GetID());
                    Polyline line = TorpedoTrajectories.get(entity.GetID());

                    if(marker != null)
                    {
                        TorpedoMarkers.remove(entity.GetID());
                        marker.remove();
                    }

                    if(line != null)
                    {
                        TorpedoTrajectories.remove(entity.GetID());
                        line.remove();
                    }
                }
                else if(entity instanceof Radiation)
                {
                    final Circle circle = RadiationMarkers.get(entity.GetID());

                    if(circle != null)
                    {
                        RadiationMarkers.remove(entity.GetID());
                        circle.remove();
                    }
                }
                else if(entity instanceof ResourceDeposit)
                {
                    Marker marker = DepositMarkers.get(entity.GetID());

                    if(marker != null)
                    {
                        DepositMarkers.remove(entity.GetID());
                        marker.remove();
                    }
                }
                else if(entity instanceof Structure || entity instanceof Loot || entity instanceof LandUnit || entity instanceof NavalVessel || entity instanceof Missile)
                {
                    GroundOverlay overlay = null;
                    Map<Integer, GroundOverlay> OverlayMap = GetEntityOverlayMap(entity);

                    if(OverlayMap != null)
                        overlay = OverlayMap.get(entity.GetID());

                    if(overlay != null)
                    {
                        OverlayMap.remove(entity.GetID());
                        overlay.remove();
                    }

                    Marker marker = null;
                    Map<Integer, Marker> MarkerMap = GetEntityMarkerMap(entity);

                    if(MarkerMap != null)
                        marker = MarkerMap.get(entity.GetID());

                    if(marker != null)
                    {
                        MarkerMap.remove(entity.GetID());
                        marker.remove();

                        for(List<Marker> list : MarkerChunks.values())
                        {
                            list.remove(marker);
                        }
                    }

                    if(entity instanceof Missile)
                    {
                        //Polyline trajectory = MissileTrajectories.get(entity.GetID());
                        Polyline trail = MissileTrails.get(entity.GetID());
                        Circle circleRAD = RadiationMarkers.get(entity.GetID());
                        Circle circleEMP = EMPMarkers.get(entity.GetID());
                        Circle circleBlast = BlastRadii.get(entity.GetID());
                        Marker threatMarker = ThreatMarkers.get(entity.GetID());

                        if(threatMarker != null)
                        {
                            ThreatMarkers.remove(entity.GetID());
                            threatMarker.remove();
                            threatMarker = null;
                        }

                        /*if(trajectory != null)
                        {
                            MissileTrajectories.remove(entity.GetID());
                            trajectory.remove();
                            trajectory = null;
                        }*/

                        if(trail != null)
                        {
                            MissileTrails.remove(entity.GetID());
                            trail.remove();
                            trail = null;
                        }

                        if(circleRAD != null)
                        {
                            RadiationMarkers.remove(entity.GetID());
                            circleRAD.remove();
                            circleRAD = null;
                        }

                        if(circleEMP != null)
                        {
                            EMPMarkers.remove(entity.GetID());
                            circleEMP.remove();
                            circleEMP = null;
                        }

                        if(circleBlast != null)
                        {
                            BlastRadii.remove(entity.GetID());
                            circleBlast.remove();
                            circleBlast = null;
                        }
                    }
                }
            }
        });
    }

    class MapChunkKey
    {
        final int latIndex;
        final int lngIndex;

        MapChunkKey(int latIndex, int lngIndex)
        {
            this.latIndex = latIndex;
            this.lngIndex = lngIndex;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;

            if (!(o instanceof MapChunkKey)) return false;

            MapChunkKey other = (MapChunkKey) o;

            return this.latIndex == other.latIndex && this.lngIndex == other.lngIndex;
        }

        @Override
        public int hashCode()
        {
            int result = latIndex;
            result = 31 * result + lngIndex;

            return result;
        }

        @Override
        public String toString()
        {
            return "(" + latIndex + "," + lngIndex + ")";
        }
    }

    private MapChunkKey GetChunkForLatLng(LatLng pos)
    {
        int latIndex = (int)Math.floor(pos.latitude  / CHUNK_SIZE_DEG);
        int lngIndex = (int)Math.floor(pos.longitude / CHUNK_SIZE_DEG);

        return new MapChunkKey(latIndex, lngIndex);
    }

    private Set<MapChunkKey> GetVisibleChunks(LatLngBounds bounds)
    {
        int minLat = (int)Math.floor(bounds.southwest.latitude  / CHUNK_SIZE_DEG);
        int maxLat = (int)Math.floor(bounds.northeast.latitude  / CHUNK_SIZE_DEG);
        int minLng = (int)Math.floor(bounds.southwest.longitude / CHUNK_SIZE_DEG);
        int maxLng = (int)Math.floor(bounds.northeast.longitude / CHUNK_SIZE_DEG);

        Set<MapChunkKey> result = new HashSet<>();

        for (int lat = minLat; lat <= maxLat; lat++)
        {
            for (int lng = minLng; lng <= maxLng; lng++)
            {
                result.add(new MapChunkKey(lat, lng));
            }
        }

        return result;
    }

    private void AddEntityToChunk(MapEntity entity)
    {
        MapChunkKey key = GetChunkForLatLng(Utilities.GetLatLng(entity.GetPosition()));

        // Get or create the list for this chunk
        List<EntityPointer> list = ChunkEntities.get(key);

        if(list == null)
        {
            list = new ArrayList<>();
            ChunkEntities.put(key, list);
        }

        list.add(entity.GetPointer());
    }

    private float GetGenerationProbability(float currentZoom, float minZoom, float maxZoom, EntityType type)
    {
        if(EntityShouldAlwaysRender(type))
            return 1.0f;

        float p = (currentZoom - minZoom) / (maxZoom - minZoom);

        return Math.max(0f, Math.min(1f, p));
    }

    private boolean EntityShouldAlwaysRender(EntityType type)
    {
        if(type == EntityType.PLAYER ||
                type == EntityType.AIRDROP ||
                type == EntityType.AIRPLANE ||
                type == EntityType.SHIP ||
                type == EntityType.SUBMARINE ||
                type == EntityType.MISSILE ||
                type == EntityType.INTERCEPTOR)
            return true;

        if(interactionMode == InteractionMode.SELECT_NEW_HOMEBASE && (type == EntityType.AIRBASE))
            return true;

        return false;
    }

    private int GetTickRateForZoom(int zoom)
    {
        int lMinZoom = 5;
        int lMaxZoom = 11;
        zoom = Math.max(lMinZoom, Math.min(lMaxZoom, zoom));
        float minFPS = 0.3f;
        float maxFPS = 30f;

        float t = (float)(zoom - lMinZoom) / (lMaxZoom - lMinZoom);   // normalize 0–1
        float fps = minFPS + t * (maxFPS - minFPS);

        return (int)(1000f / fps);
    }
}
