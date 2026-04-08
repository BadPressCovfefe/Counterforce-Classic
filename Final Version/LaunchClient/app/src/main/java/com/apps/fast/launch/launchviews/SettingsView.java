package com.apps.fast.launch.launchviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.utilities.LaunchUtilities;

/**
 * Created by tobster on 20/10/15.
 */
public class SettingsView extends LaunchView
{

    private ImageView imgShowMap;
    private ImageView imgShowSatellite;
    private ImageView imgShowOnline;
    private ImageView imgShowBooting;
    private ImageView imgShowOffline;
    private ImageView imgShowMissileSites;
    private ImageView imgShowSAMSites;
    private ImageView imgShowSentryGuns;
    private ImageView imgShowOreMines;
    private ImageView imgShowCommandPosts;
    private ImageView imgShowAirbases;
    private ImageView imgShowWarehouses;
    private ImageView imgShowArmory;
    private ImageView imgShowBarracks;
    private ImageView imgShowRadarStations;
    private ImageView imgShowArtilleryGuns;
    private ImageView imgShowDistributors;
    private ImageView imgShowProcessors;
    private ImageView imgShowScrapYards;
    private ImageView imgShowNeutral;
    private ImageView imgShowFriendly;
    private ImageView imgShowEnemy;
    private ImageView imgShowBlastRadii;
    private ImageView imgShowRadarEffects;
    private ImageView imgShowMissileTrails;
    private ImageView imgShowRadiations;
    private ImageView imgShowLoots;
    private ImageView imgShowShips;
    private ImageView imgShowSubmarines;
    private ImageView imgShowAircraft;
    private ImageView imgShowShipyards;
    private ImageView imgShowPorts;
    private ImageView imgShowMissiles;
    private ImageView imgShowInterceptors;
    private ImageView imgShowMonuments;
    private ImageView imgShowInfantries;
    private ImageView imgShowCargoTrucks;
    private ImageView imgShowTanks;
    private ImageView imgShowSPAAGs;
    private ImageView imgShowIronLoot;
    private ImageView imgShowCoalLoot;
    private ImageView imgShowOilLoot;
    private ImageView imgShowCropLoot;
    private ImageView imgShowUraniumLoot;
    private ImageView imgShowGoldLoot;
    private ImageView imgShowLumberLoot;
    private ImageView imgShowFertilizerLoot;
    private ImageView imgShowWealthLoot;
    private ImageView imgShowElectricityLoot;
    private ImageView imgShowConcreteLoot;
    private ImageView imgShowFuelLoot;
    private ImageView imgShowFoodLoot;
    private ImageView imgShowSteelLoot;
    private ImageView imgShowConstructionSuppliesLoot;
    private ImageView imgShowExplosivesLoot;
    private ImageView imgShowNerveAgentLoot;
    private ImageView imgShowMachineryLoot;
    private ImageView imgShowElectronicsLoot;
    private ImageView imgShowMedicineLoot;
    private ImageView imgShowEnrichedUraniumLoot;
    private ImageView imgShowAntimatterLoot;
    private ImageView imgShowLaborLoot;
    private ImageView imgShowKnowledgeLoot;

    private CheckBox chkNotifications;
    private LinearLayout lytNotificationSettings;
    private CheckBox chkNukeEscalationNotifications;
    private CheckBox chkAllyNotifications;
    private CheckBox chkDebugNotifications;
    private TextView txtNotifications;
    private TextView txtInterval;
    private TextView btnTest;
    private TextView txtUnits;

    private CheckBox chkWarheadUnit;
    private CheckBox chkMeters;
    private CheckBox chkYards;
    private CheckBox chkFeet;
    private CheckBox chkKilometers;
    private CheckBox chkStatMiles;
    private CheckBox chkNautMiles;
    private CheckBox chkKph;
    private CheckBox chkMph;
    private CheckBox chkKts;

    private TextView txtCurrency;

    private LinearLayout btnChangeAvatar;
    private TextView btnCloseAccount;

    private CheckBox chkDisableAudio;
    private CheckBox chkInitialZoom;
    private CheckBox chkHints;

    private TextView txtClustering;
    private TextView txtDefaultZoom;
    private TextView txtMaxZoom;

    private TextView btnTheme;

    private TextView txtRenamePlayer;
    private EditText txtNameEdit;
    private LinearLayout btnApplyName;

    private EditText txtURL;
    private EditText txtPort;

    private LinearLayout lytDebug;

    private int lNotificationInterval;
    private int lClusteringIcons;
    private float fltDefaultZoom;
    private float fltMaxZoom;
    private int lSecretTaps = 0;

    private TextView txtServerName;
    private TextView txtServerDescription;
    private LinearLayout lytConnectionSettings;

    private String strURL;
    private int lPort;

    private int lInitialAvatar = ClientDefs.DEFAULT_AVATAR_ID;

    public SettingsView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_settings, this);

        imgShowMap = findViewById(R.id.imgShowMap);
        imgShowSatellite = findViewById(R.id.imgShowSatellite);
        imgShowOnline = findViewById(R.id.imgShowOnline);
        imgShowBooting = findViewById(R.id.imgShowBooting);
        imgShowOffline = findViewById(R.id.imgShowOffline);
        imgShowMissileSites = findViewById(R.id.imgShowMissileSites);
        imgShowSAMSites = findViewById(R.id.imgShowSAMSites);
        imgShowSentryGuns = findViewById(R.id.imgShowSentryGuns);
        imgShowOreMines = findViewById(R.id.imgShowOreMines);
        imgShowCommandPosts = findViewById(R.id.imgShowCommandPosts);
        imgShowAirbases = findViewById(R.id.imgShowAirbases);
        imgShowWarehouses = findViewById(R.id.imgShowWarehouses);
        imgShowArmory = findViewById(R.id.imgShowArmory);
        imgShowBarracks = findViewById(R.id.imgShowBarracks);
        imgShowRadarStations = findViewById(R.id.imgShowRadarStations);
        imgShowArtilleryGuns = findViewById(R.id.imgShowArtilleryGuns);
        imgShowDistributors = findViewById(R.id.imgShowDistributors);
        imgShowProcessors = findViewById(R.id.imgShowProcessors);
        imgShowScrapYards = findViewById(R.id.imgShowScrapYards);
        imgShowNeutral = findViewById(R.id.imgShowNeutral);
        imgShowFriendly = findViewById(R.id.imgShowFriendly);
        imgShowEnemy = findViewById(R.id.imgShowEnemy);
        imgShowBlastRadii = findViewById(R.id.imgShowBlastRadii);
        imgShowRadarEffects = findViewById(R.id.imgShowRadarEffects);
        imgShowMissileTrails = findViewById(R.id.imgShowMissileTrails);
        imgShowRadiations = findViewById(R.id.imgShowRadiations);
        imgShowLoots = findViewById(R.id.imgShowLoots);
        imgShowShips = findViewById(R.id.imgShowShips);
        imgShowSubmarines = findViewById(R.id.imgShowSubmarines);
        imgShowAircraft = findViewById(R.id.imgShowAircraft);
        imgShowShipyards = findViewById(R.id.imgShowShipyards);
        imgShowPorts = findViewById(R.id.imgShowPorts);
        imgShowMissiles = findViewById(R.id.imgShowMissiles);
        imgShowInterceptors = findViewById(R.id.imgShowInterceptors);
        imgShowMonuments = findViewById(R.id.imgShowMonuments);
        imgShowIronLoot = findViewById(R.id.imgShowIronLoot);
        imgShowCoalLoot = findViewById(R.id.imgShowCoalLoot);
        imgShowOilLoot = findViewById(R.id.imgShowOilLoot);
        imgShowCropLoot = findViewById(R.id.imgShowCropLoot);
        imgShowUraniumLoot = findViewById(R.id.imgShowUraniumLoot);
        imgShowGoldLoot = findViewById(R.id.imgShowGoldLoot);
        imgShowLumberLoot = findViewById(R.id.imgShowLumberLoot);
        imgShowFertilizerLoot = findViewById(R.id.imgShowFertilizerLoot);
        imgShowWealthLoot = findViewById(R.id.imgShowWealthLoot);
        imgShowElectricityLoot = findViewById(R.id.imgShowElectricityLoot);
        imgShowConcreteLoot = findViewById(R.id.imgShowConcreteLoot);
        imgShowFuelLoot = findViewById(R.id.imgShowFuelLoot);
        imgShowFoodLoot = findViewById(R.id.imgShowFoodLoot);
        imgShowSteelLoot = findViewById(R.id.imgShowSteelLoot);
        imgShowConstructionSuppliesLoot = findViewById(R.id.imgShowConstructionSuppliesLoot);
        imgShowExplosivesLoot = findViewById(R.id.imgShowExplosivesLoot);
        imgShowNerveAgentLoot = findViewById(R.id.imgShowNerveAgentLoot);
        imgShowMachineryLoot = findViewById(R.id.imgShowMachineryLoot);
        imgShowElectronicsLoot = findViewById(R.id.imgShowElectronicsLoot);
        imgShowMedicineLoot = findViewById(R.id.imgShowMedicineLoot);
        imgShowEnrichedUraniumLoot = findViewById(R.id.imgShowEnrichedUraniumLoot);
        imgShowAntimatterLoot = findViewById(R.id.imgShowAntimatterLoot);
        imgShowLaborLoot = findViewById(R.id.imgShowLaborLoot);
        imgShowKnowledgeLoot = findViewById(R.id.imgShowKnowledgeLoot);
        imgShowInfantries = findViewById(R.id.imgShowInfantries);
        imgShowCargoTrucks = findViewById(R.id.imgShowCargoTrucks);
        imgShowTanks = findViewById(R.id.imgShowTanks);
        imgShowSPAAGs = findViewById(R.id.imgShowSPAAGs);

        chkNotifications = findViewById(R.id.chkNotifications);
        lytNotificationSettings = findViewById(R.id.lytNotificationSettings);
        chkNukeEscalationNotifications = findViewById(R.id.chkNukeEscalationNotifications);
        chkAllyNotifications = findViewById(R.id.chkAllyNotifications);
        chkDebugNotifications = findViewById(R.id.chkDebugNotifications);
        txtNotifications = findViewById(R.id.txtNotifications);
        txtInterval = findViewById(R.id.txtInterval);
        btnTest = findViewById(R.id.btnTest);
        txtUnits = findViewById(R.id.txtUnits);

        chkWarheadUnit = findViewById(R.id.chkWarheadUnit);
        chkMeters = findViewById(R.id.chkMeters);
        chkYards = findViewById(R.id.chkYards);
        chkFeet = findViewById(R.id.chkFeet);
        chkKilometers = findViewById(R.id.chkKilometers);
        chkStatMiles = findViewById(R.id.chkStatMiles);
        chkNautMiles = findViewById(R.id.chkNautMiles);
        chkKph = findViewById(R.id.chkKph);
        chkMph = findViewById(R.id.chkMph);
        chkKts = findViewById(R.id.chkKts);

        txtCurrency = findViewById(R.id.txtCurrency);

        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnCloseAccount = findViewById(R.id.btnCloseAccount);

        chkDisableAudio = findViewById(R.id.chkDisableAudio);
        chkInitialZoom = findViewById(R.id.chkInitialZoom);
        chkHints = findViewById(R.id.chkHints);

        txtClustering = findViewById(R.id.txtClustering);
        txtDefaultZoom = findViewById(R.id.txtDefaultZoom);
        txtMaxZoom = findViewById(R.id.txtMaxZoom);

        btnTheme = findViewById(R.id.btnTheme);

        txtRenamePlayer = findViewById(R.id.txtRenamePlayer);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);

        txtServerDescription = findViewById(R.id.txtServerDescription);
        txtServerName = findViewById(R.id.txtServerName);

        lytConnectionSettings = findViewById(R.id.lytConnectionSettings);

        if(activity.GetMapSatellite())
        {
            imgShowMap.setVisibility(GONE);
            imgShowSatellite.setVisibility(VISIBLE);
        }
        else
        {
            imgShowMap.setVisibility(VISIBLE);
            imgShowSatellite.setVisibility(GONE);
        }

        imgShowMap.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetMapSatellite(true);
                imgShowMap.setVisibility(GONE);
                imgShowSatellite.setVisibility(VISIBLE);
            }
        });

        imgShowSatellite.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetMapSatellite(false);
                imgShowMap.setVisibility(VISIBLE);
                imgShowSatellite.setVisibility(GONE);
            }
        });

        imgShowOnline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ONLINE_VISIBLE = !LaunchUtilities.ONLINE_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowBooting.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.BOOTING_VISIBLE = !LaunchUtilities.BOOTING_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowOffline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.OFFLINE_VISIBLE = !LaunchUtilities.OFFLINE_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowMissileSites.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.MISSILE_SITES_VISIBLE = !LaunchUtilities.MISSILE_SITES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowSAMSites.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SAM_SITES_VISIBLE = !LaunchUtilities.SAM_SITES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowSentryGuns.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SENTRY_GUNS_VISIBLE = !LaunchUtilities.SENTRY_GUNS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowOreMines.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ORE_MINES_VISIBLE = !LaunchUtilities.ORE_MINES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowCommandPosts.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.COMMAND_POSTS_VISIBLE = !LaunchUtilities.COMMAND_POSTS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowAirbases.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.AIRBASES_VISIBLE = !LaunchUtilities.AIRBASES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowWarehouses.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.WAREHOUSES_VISIBLE = !LaunchUtilities.WAREHOUSES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowArmory.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ARMORIES_VISIBLE = !LaunchUtilities.ARMORIES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowBarracks.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.BARRACKS_VISIBLE = !LaunchUtilities.BARRACKS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowRadarStations.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.RADAR_STATIONS_VISIBLE = !LaunchUtilities.RADAR_STATIONS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowArtilleryGuns.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ARTILLERY_GUNS_VISIBLE = !LaunchUtilities.ARTILLERY_GUNS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowDistributors.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.DISTRIBUTORS_VISIBLE = !LaunchUtilities.DISTRIBUTORS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowProcessors.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.PROCESSORS_VISIBLE = !LaunchUtilities.PROCESSORS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowScrapYards.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SCRAP_YARDS_VISIBLE = !LaunchUtilities.SCRAP_YARDS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowNeutral.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.NEUTRAL_VISIBLE = !LaunchUtilities.NEUTRAL_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowFriendly.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.FRIENDLY_VISIBLE = !LaunchUtilities.FRIENDLY_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowEnemy.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ENEMY_VISIBLE = !LaunchUtilities.ENEMY_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowBlastRadii.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ToggleShowBlastRadii(!activity.bShowBlastRadii);
                MapToolIconsChanged();
                Update();
            }
        });

        imgShowRadarEffects.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ToggleShowRadarEffects(!activity.bShowRadarEffects);
                MapToolIconsChanged();
                Update();
            }
        });

        imgShowMissileTrails.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ToggleShowMissileTrails(!activity.bShowMissileTrails);
                MapToolIconsChanged();
                Update();
            }
        });

        imgShowRadiations.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.RADIATIONS_VISIBLE = !LaunchUtilities.RADIATIONS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowLoots.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.LOOTS_VISIBLE = !LaunchUtilities.LOOTS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowInfantries.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.INFANTRIES_VISIBLE = !LaunchUtilities.INFANTRIES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowCargoTrucks.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.CARGO_TRUCKS_VISIBLE = !LaunchUtilities.CARGO_TRUCKS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowTanks.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.TANKS_VISIBLE = !LaunchUtilities.TANKS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowSPAAGs.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SPAAGS_VISIBLE = !LaunchUtilities.SPAAGS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowShips.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SHIPS_VISIBLE = !LaunchUtilities.SHIPS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowSubmarines.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SUBMARINES_VISIBLE = !LaunchUtilities.SUBMARINES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowAircraft.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.AIRCRAFTS_VISIBLE = !LaunchUtilities.AIRCRAFTS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowShipyards.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.SHIPYARDS_VISIBLE = !LaunchUtilities.SHIPYARDS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowPorts.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.PORTS_VISIBLE = !LaunchUtilities.PORTS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowMissiles.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.MISSILES_VISIBLE = !LaunchUtilities.MISSILES_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowInterceptors.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.INTERCEPTORS_VISIBLE = !LaunchUtilities.INTERCEPTORS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowMonuments.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.MONUMENTS_VISIBLE = !LaunchUtilities.MONUMENTS_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowIronLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.IRON_LOOT_VISIBLE = !LaunchUtilities.IRON_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowCoalLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.COAL_LOOT_VISIBLE = !LaunchUtilities.COAL_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowOilLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.OIL_LOOT_VISIBLE = !LaunchUtilities.OIL_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowCropLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.CROP_LOOT_VISIBLE = !LaunchUtilities.CROP_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowUraniumLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.URANIUM_LOOT_VISIBLE = !LaunchUtilities.URANIUM_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowGoldLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.GOLD_LOOT_VISIBLE = !LaunchUtilities.GOLD_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowLaborLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.LABOR_LOOT_VISIBLE = !LaunchUtilities.LABOR_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowKnowledgeLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.KNOWLEDGE_LOOT_VISIBLE = !LaunchUtilities.KNOWLEDGE_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowLumberLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.LUMBER_LOOT_VISIBLE = !LaunchUtilities.LUMBER_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowFertilizerLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.FERTILIZER_LOOT_VISIBLE = !LaunchUtilities.FERTILIZER_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowWealthLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.WEALTH_LOOT_VISIBLE = !LaunchUtilities.WEALTH_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowElectricityLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ELECTRICITY_LOOT_VISIBLE = !LaunchUtilities.ELECTRICITY_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowConcreteLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.CONCRETE_LOOT_VISIBLE = !LaunchUtilities.CONCRETE_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowFuelLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.FUEL_LOOT_VISIBLE = !LaunchUtilities.FUEL_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowFoodLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.FOOD_LOOT_VISIBLE = !LaunchUtilities.FOOD_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowSteelLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.STEEL_LOOT_VISIBLE = !LaunchUtilities.STEEL_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowConstructionSuppliesLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.CONSTRUCTION_SUPPLIES_LOOT_VISIBLE = !LaunchUtilities.CONSTRUCTION_SUPPLIES_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowExplosivesLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.EXPLOSIVES_LOOT_VISIBLE = !LaunchUtilities.EXPLOSIVES_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowNerveAgentLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.NERVE_AGENT_LOOT_VISIBLE = !LaunchUtilities.NERVE_AGENT_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowMachineryLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.MACHINERY_LOOT_VISIBLE = !LaunchUtilities.MACHINERY_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowElectronicsLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ELECTRONICS_LOOT_VISIBLE = !LaunchUtilities.ELECTRONICS_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowMedicineLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.MEDICINE_LOOT_VISIBLE = !LaunchUtilities.MEDICINE_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowEnrichedUraniumLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.FISSILE_LOOT_VISIBLE = !LaunchUtilities.FISSILE_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        imgShowAntimatterLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LaunchUtilities.ANTIMATTER_LOOT_VISIBLE = !LaunchUtilities.ANTIMATTER_LOOT_VISIBLE;
                MapToolIconsChanged();
                activity.RebuildMap();
            }
        });

        MapToolIconsChanged();

        if(game.GetConfig() != null)
        {
            txtServerName.setText(context.getString(R.string.server_name, game.GetConfig().GetServerName()));
            txtServerDescription.setText(context.getString(R.string.server_description, game.GetConfig().GetServerDescription()));
        }
        else
        {
            txtServerName.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            txtServerDescription.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            txtServerName.setText(context.getString(R.string.server_name, context.getString(R.string.not_connected)));
            txtServerDescription.setText(context.getString(R.string.server_description, context.getString(R.string.not_connected)));
        }


        txtURL = findViewById(R.id.txtURL);
        txtPort = findViewById(R.id.txtPort);

        lytDebug = findViewById(R.id.lytDebug);

        SharedPreferences sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        strURL = sharedPreferences.getString(ClientDefs.SETTINGS_SERVER_URL, ClientDefs.SETTINGS_SERVER_URL_DEFAULT);
        lPort = sharedPreferences.getInt(ClientDefs.SETTINGS_SERVER_PORT, ClientDefs.GetDefaultServerPort());

        lNotificationInterval = sharedPreferences.getInt(ClientDefs.SETTINGS_NOTIFICATION_MINUTES, ClientDefs.SETTINGS_NOTIFICATION_MINUTES_DEFAULT);

        chkMeters.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_SHORT_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.ShortUnits.METERS.ordinal());
        chkYards.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_SHORT_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.ShortUnits.YARDS.ordinal());
        chkFeet.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_SHORT_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.ShortUnits.FEET.ordinal());

        chkKilometers.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_LONG_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.LongUnits.KILOMETERS.ordinal());
        chkStatMiles.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_LONG_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.LongUnits.STATUTE_MILES.ordinal());
        chkNautMiles.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_LONG_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.LongUnits.NAUTICAL_MILES.ordinal());

        chkKph.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_SPEEDS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.Speeds.KILOMETERS_PER_HOUR.ordinal());
        chkMph.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_SPEEDS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.Speeds.MILES_PER_HOUR.ordinal());
        chkKts.setChecked(sharedPreferences.getInt(ClientDefs.SETTINGS_SPEEDS, ClientDefs.SETTINGS_UNITS_DEFAULT) == TextUtilities.Speeds.KNOTS.ordinal());

        txtCurrency.setText(sharedPreferences.getString(ClientDefs.SETTINGS_CURRENCY, ClientDefs.SETTINGS_CURRENCY_DEFAULT));

        if(lNotificationInterval > 0)
        {
            txtNotifications.setVisibility(View.VISIBLE);
            txtInterval.setVisibility(View.VISIBLE);
            txtNotifications.setText(Integer.toString(lNotificationInterval));
        }
        else
        {
            txtNotifications.setVisibility(View.INVISIBLE);
            txtInterval.setVisibility(View.INVISIBLE);
            txtNotifications.setText("");
        }

        txtNotifications.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                try
                {
                    lNotificationInterval = Integer.parseInt(txtNotifications.getText().toString());
                }
                catch(Exception ex)
                {
                    lNotificationInterval = ClientDefs.SETTINGS_NOTIFICATION_MINUTES_DEFAULT;
                }
            }
        });

        //chkNotifications.setChecked(lNotificationInterval > 0);
        lytNotificationSettings.setVisibility(GONE);
        /*chkNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(b)
                {
                    lNotificationInterval = ClientDefs.SETTINGS_NOTIFICATION_MINUTES_DEFAULT;
                    txtNotifications.setVisibility(VISIBLE);
                    txtNotifications.setText(Integer.toString(lNotificationInterval));
                    txtInterval.setVisibility(VISIBLE);
                    lytNotificationSettings.setVisibility(VISIBLE);
                    chkNukeEscalationNotifications.setVisibility(VISIBLE);
                    chkAllyNotifications.setVisibility(VISIBLE);
                }
                else
                {
                    lNotificationInterval = 0;
                    txtNotifications.setText("");
                    txtNotifications.setVisibility(GONE);
                    txtInterval.setVisibility(GONE);
                    lytNotificationSettings.setVisibility(GONE);
                    chkNukeEscalationNotifications.setVisibility(GONE);
                    chkAllyNotifications.setVisibility(GONE);
                }
            }
        });*/

        //chkNukeEscalationNotifications.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_NOTIFICATION_NUKEESC, ClientDefs.SETTINGS_NOTIFICATION_NUKEESC_DEFAULT));
        chkNukeEscalationNotifications.setVisibility(GONE);

        //chkAllyNotifications.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_NOTIFICATION_ALLIES, ClientDefs.SETTINGS_NOTIFICATION_ALLIES_DEFAULT));
        chkAllyNotifications.setVisibility(GONE);

        /*if(game.GetInteractionReady())
        {
            if (game.GetOurPlayer().GetIsAnAdmin())
            {
                chkDebugNotifications.setVisibility(VISIBLE);
                chkDebugNotifications.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_NOTIFICATION_DEBUG, ClientDefs.SETTINGS_NOTIFICATION_DEBUG_DEFAULT));
            }
        }*/

        final CompoundButton.OnCheckedChangeListener shortUnitChangedListener = new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                chkMeters.setOnCheckedChangeListener(null);
                chkYards.setOnCheckedChangeListener(null);
                chkFeet.setOnCheckedChangeListener(null);
                chkMeters.setChecked(compoundButton == chkMeters);
                chkYards.setChecked(compoundButton == chkYards);
                chkFeet.setChecked(compoundButton == chkFeet);
                chkMeters.setOnCheckedChangeListener(this);
                chkYards.setOnCheckedChangeListener(this);
                chkFeet.setOnCheckedChangeListener(this);
            }
        };

        CompoundButton.OnCheckedChangeListener longUnitChangedListener = new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                chkKilometers.setOnCheckedChangeListener(null);
                chkStatMiles.setOnCheckedChangeListener(null);
                chkNautMiles.setOnCheckedChangeListener(null);
                chkKilometers.setChecked(compoundButton == chkKilometers);
                chkStatMiles.setChecked(compoundButton == chkStatMiles);
                chkNautMiles.setChecked(compoundButton == chkNautMiles);
                chkKilometers.setOnCheckedChangeListener(this);
                chkStatMiles.setOnCheckedChangeListener(this);
                chkNautMiles.setOnCheckedChangeListener(this);
            }
        };

        CompoundButton.OnCheckedChangeListener speedUnitChangedListener = new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                chkKph.setOnCheckedChangeListener(null);
                chkMph.setOnCheckedChangeListener(null);
                chkKts.setOnCheckedChangeListener(null);
                chkKph.setChecked(compoundButton == chkKph);
                chkMph.setChecked(compoundButton == chkMph);
                chkKts.setChecked(compoundButton == chkKts);
                chkKph.setOnCheckedChangeListener(this);
                chkMph.setOnCheckedChangeListener(this);
                chkKts.setOnCheckedChangeListener(this);
            }
        };

        chkMeters.setOnCheckedChangeListener(shortUnitChangedListener);
        chkYards.setOnCheckedChangeListener(shortUnitChangedListener);
        chkFeet.setOnCheckedChangeListener(shortUnitChangedListener);
        chkKilometers.setOnCheckedChangeListener(longUnitChangedListener);
        chkStatMiles.setOnCheckedChangeListener(longUnitChangedListener);
        chkNautMiles.setOnCheckedChangeListener(longUnitChangedListener);
        chkKph.setOnCheckedChangeListener(speedUnitChangedListener);
        chkMph.setOnCheckedChangeListener(speedUnitChangedListener);
        chkKts.setOnCheckedChangeListener(speedUnitChangedListener);

        btnTest.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SaveSettings();
                //LaunchAlertManager.FireAlert(context, false, false);
            }
        });

        btnChangeAvatar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new UploadAvatarView(game, activity, LaunchUICommon.AvatarPurpose.PLAYER));
            }
        });

        btnCloseAccount.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderLaunch();
                launchDialog.SetMessage(context.getString(R.string.close_account_confirm));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.CloseAccount();
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
                launchDialog.show(activity.getFragmentManager(), "");
            }
        });

        chkWarheadUnit.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_USE_LBS, ClientDefs.SETTINGS_USE_LBS_DEFAULT));

        chkWarheadUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                editor.putBoolean(ClientDefs.SETTINGS_USE_LBS, b);
                editor.commit();

                Sounds.SetDisabled(b);
            }
        });

        chkDisableAudio.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_DISABLE_AUDIO, ClientDefs.SETTINGS_DISABLE_AUDIO_DEFAULT));

        chkDisableAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                editor.putBoolean(ClientDefs.SETTINGS_DISABLE_AUDIO, b);
                editor.commit();

                Sounds.SetDisabled(b);
            }
        });

        chkHints.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_SHOW_HINTS, ClientDefs.SETTINGS_SHOW_HINTS_DEFAULT));

        chkHints.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                editor.putBoolean(ClientDefs.SETTINGS_SHOW_HINTS, b);
                editor.commit();
            }
        });

        chkInitialZoom.setChecked(sharedPreferences.getBoolean(ClientDefs.SETTINGS_INITIAL_ZOOM, ClientDefs.SETTINGS_INITIAL_ZOOM_DEFAULT));

        chkInitialZoom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                editor.putBoolean(ClientDefs.SETTINGS_INITIAL_ZOOM, b);
                editor.commit();

                Sounds.SetDisabled(b);
            }
        });

        txtUnits.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                lSecretTaps++;
            }
        });

        lClusteringIcons = sharedPreferences.getInt(ClientDefs.SETTINGS_CLUSTERING, ClientDefs.SETTINGS_CLUSTERING_DEFAULT);

        txtClustering.setText(Integer.toString(lClusteringIcons));

        txtClustering.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                try
                {
                    lClusteringIcons = Integer.parseInt(txtClustering.getText().toString());
                }
                catch(Exception ex)
                {
                    lClusteringIcons = 0;
                }
            }
        });

        fltDefaultZoom = sharedPreferences.getFloat(ClientDefs.SETTINGS_ZOOM_LEVEL, ClientDefs.SETTINGS_ZOOM_LEVEL_DEFAULT);

        txtDefaultZoom.setText(Float.toString(fltDefaultZoom));

        fltMaxZoom = sharedPreferences.getFloat(ClientDefs.SETTINGS_MAX_ZOOM, ClientDefs.SETTINGS_MAX_ZOOM_DEFAULT);

        txtMaxZoom.setText(Float.toString(fltMaxZoom));

        txtMaxZoom.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                try
                {
                    float inputFloat = Float.parseFloat(txtMaxZoom.getText().toString());

                    if(inputFloat <= 21 && inputFloat >= 3)
                    {
                        fltMaxZoom = inputFloat;
                        activity.SetMaxZoom(fltMaxZoom);
                    }
                    else
                    {
                        fltMaxZoom = 16.0f;
                    }

                }
                catch(Exception ex)
                {
                    fltMaxZoom = 16.0f;
                }
            }
        });

        txtDefaultZoom.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable)
            {
                try
                {
                    fltDefaultZoom = Float.parseFloat(txtDefaultZoom.getText().toString());
                }
                catch(Exception ex)
                {
                    fltDefaultZoom = 0.0f;
                }
            }
        });

        btnTheme.setText(context.getString(R.string.theme, ClientDefs.Themes[sharedPreferences.getInt(ClientDefs.SETTINGS_THEME, ClientDefs.SETTINGS_THEME_DEFAULT)]));

        btnTheme.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Display dialog with sort by options.
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.select_theme));

                SharedPreferences sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

                builder.setSingleChoiceItems(ClientDefs.Themes, sharedPreferences.getInt(ClientDefs.SETTINGS_THEME, ClientDefs.SETTINGS_THEME_DEFAULT), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        //Exceptionally, this is saved and applied on change.
                        SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                        editor.putInt(ClientDefs.SETTINGS_THEME, i);
                        editor.commit();
                        dialogInterface.dismiss();
                        btnTheme.setText(context.getString(R.string.theme, ClientDefs.Themes[i]));
                        SaveSettings();

                        activity.recreate();
                    }
                });

                builder.show();
            }
        });

        if(game.GetInteractionReady())
        {
            txtNameEdit.setText(game.GetOurPlayer().GetName());
        }
        else
        {
            txtRenamePlayer.setVisibility(GONE);
            txtNameEdit.setVisibility(GONE);
            btnApplyName.setVisibility(GONE);
        }

        btnApplyName.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Utilities.DismissKeyboard(activity, activity.getCurrentFocus());

                //Clear any existing errors.
                txtNameEdit.setError(null);

                //Get the values.
                String strPlayer = txtNameEdit.getText().toString();

                if(Utilities.ValidateName(strPlayer))
                {
                    game.SetPlayerName(txtNameEdit.getText().toString());
                }
                else
                {
                    txtNameEdit.setError(context.getString(R.string.specify_username));
                }

                Utilities.DismissKeyboard(activity, txtNameEdit);
            }
        });

        txtURL.setText(strURL);
        txtPort.setText(Integer.toString(lPort));

        findViewById(R.id.btnPrivacyZones).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.PrivacyZoneMode();
            }
        });

        if(game.GetInteractionReady())
        {
            if (game.GetOurPlayer().GetAvatarID() != ClientDefs.DEFAULT_AVATAR_ID)
            {
                lInitialAvatar = game.GetOurPlayer().GetAvatarID();
                ((ImageView) findViewById(R.id.imgAvatar)).setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, game.GetOurPlayer()));
            }
        }
        else
        {
            findViewById(R.id.imgAvatar).setVisibility(GONE);

            btnCloseAccount.setVisibility(GONE);
        }

        RedrawDebug();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(lSecretTaps >= 10)
                {
                    lytConnectionSettings.setVisibility(VISIBLE);
                }
            }
        });
    }

    public void SaveSettings()
    {
        /*if(lNotificationInterval < ClientDefs.SETTINGS_NOTIFICATION_MINUTES_DEFAULT)
        {
            lNotificationInterval = ClientDefs.SETTINGS_NOTIFICATION_MINUTES_DEFAULT;
            txtNotifications.setText(Integer.toString(lNotificationInterval));
        }*/

        //Save the settings.
        SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putInt(ClientDefs.SETTINGS_NOTIFICATION_MINUTES, lNotificationInterval);
        editor.putInt(ClientDefs.SETTINGS_CLUSTERING, lClusteringIcons);
        editor.putFloat(ClientDefs.SETTINGS_ZOOM_LEVEL, fltDefaultZoom);
        editor.putFloat(ClientDefs.SETTINGS_MAX_ZOOM, fltMaxZoom);

        ClientDefs.CLUSTERING_SIZE = lClusteringIcons;

        if(chkMeters.isChecked())
            editor.putInt(ClientDefs.SETTINGS_SHORT_UNITS, TextUtilities.ShortUnits.METERS.ordinal());
        else if(chkYards.isChecked())
            editor.putInt(ClientDefs.SETTINGS_SHORT_UNITS, TextUtilities.ShortUnits.YARDS.ordinal());
        else
            editor.putInt(ClientDefs.SETTINGS_SHORT_UNITS, TextUtilities.ShortUnits.FEET.ordinal());

        if(chkKilometers.isChecked())
            editor.putInt(ClientDefs.SETTINGS_LONG_UNITS, TextUtilities.LongUnits.KILOMETERS.ordinal());
        else if(chkStatMiles.isChecked())
            editor.putInt(ClientDefs.SETTINGS_LONG_UNITS, TextUtilities.LongUnits.STATUTE_MILES.ordinal());
        else
            editor.putInt(ClientDefs.SETTINGS_LONG_UNITS, TextUtilities.LongUnits.NAUTICAL_MILES.ordinal());

        if(chkKph.isChecked())
            editor.putInt(ClientDefs.SETTINGS_SPEEDS, TextUtilities.Speeds.KILOMETERS_PER_HOUR.ordinal());
        else if(chkMph.isChecked())
            editor.putInt(ClientDefs.SETTINGS_SPEEDS, TextUtilities.Speeds.MILES_PER_HOUR.ordinal());
        else
            editor.putInt(ClientDefs.SETTINGS_SPEEDS, TextUtilities.Speeds.KNOTS.ordinal());

        editor.putString(ClientDefs.SETTINGS_CURRENCY, txtCurrency.getText().toString());

        String strNewURL = txtURL.getText().toString();
        int lNewPort = 0;
        boolean bConnectionChangeFailed = false;
        boolean bConnectionDidChange = false;

        try
        {
            lNewPort = Integer.parseInt(txtPort.getText().toString());
        }
        catch(Exception ex)
        {
            bConnectionChangeFailed = true;
        }

        if(lNewPort < 1000 || lNewPort > 65535)
        {
            bConnectionChangeFailed = true;
        }

        if(lNewPort != lPort || !strNewURL.equals(strURL))
        {
            //New connection settings. Check them.
            if(bConnectionChangeFailed)
            {
                activity.ShowBasicOKDialog(context.getString(R.string.invalid_connection));
            }
            else
            {
                editor.putString(ClientDefs.SETTINGS_SERVER_URL, strNewURL);
                editor.putInt(ClientDefs.SETTINGS_SERVER_PORT, lNewPort);
                bConnectionDidChange = true;
            }
        }

        editor.commit();

        //Set alert notification interval.
        //LaunchAlertManager.CheckIntervalChanged(context);

        TextUtilities.Initialise(context);

        activity.SettingsChanged();

        RedrawDebug();

        if(bConnectionDidChange)
        {
            //Purge all client stuff (config, avatars and imageassets) as they will differ on the new server.
            activity.PurgeClient();

            activity.recreate();
        }
    }

    private void RedrawDebug()
    {
        lytDebug.removeAllViews();

        SharedPreferences sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(sharedPreferences.getBoolean(ClientDefs.SETTINGS_NOTIFICATION_DEBUG, ClientDefs.SETTINGS_NOTIFICATION_DEBUG_DEFAULT))
        {
            sharedPreferences = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

            int lDebugIndex = sharedPreferences.getInt(ClientDefs.DEBUG_INDEX, 0);

            int lEntriesDone = 0;

            while(lEntriesDone < ClientDefs.NOTIFICATION_DEBUG_LOG_SIZE)
            {
                String strDebug = sharedPreferences.getString(ClientDefs.DEBUG_PREFIX + lDebugIndex++, "");

                if(lDebugIndex >= ClientDefs.NOTIFICATION_DEBUG_LOG_SIZE)
                    lDebugIndex = 0;

                if(!strDebug.equals(""))
                {
                    TextView txtView = new TextView(context);
                    txtView.setText(strDebug);
                    lytDebug.addView(txtView, 0);
                }

                lEntriesDone++;
            }
        }
    }

    @Override
    public void AvatarSaved(int lAvatarID)
    {
        if(game.GetOurPlayer().GetAvatarID() == lAvatarID)
        {
            lInitialAvatar = lAvatarID;
            ((ImageView) findViewById(R.id.imgAvatar)).setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, game.GetOurPlayer()));
        }
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(game.GetOurPlayer().ApparentlyEquals(entity))
        {
            if(game.GetOurPlayer().GetAvatarID() != lInitialAvatar)
            {
                lInitialAvatar = game.GetOurPlayer().GetAvatarID();
                ((ImageView) findViewById(R.id.imgAvatar)).setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, game.GetOurPlayer()));
            }
        }
    }

    private void MapToolIconsChanged()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                imgShowBlastRadii.setColorFilter(activity.bShowBlastRadii ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowRadarEffects.setColorFilter(activity.bShowRadarEffects ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowMissileTrails.setColorFilter(activity.bShowMissileTrails ?  0 : LaunchUICommon.COLOUR_TINTED);

                imgShowOnline.setColorFilter(LaunchUtilities.ONLINE_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowBooting.setColorFilter(LaunchUtilities.BOOTING_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowOffline.setColorFilter(LaunchUtilities.OFFLINE_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowMissileSites.setColorFilter(LaunchUtilities.MISSILE_SITES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowSAMSites.setColorFilter(LaunchUtilities.SAM_SITES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowSentryGuns.setColorFilter(LaunchUtilities.SENTRY_GUNS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowOreMines.setColorFilter(LaunchUtilities.ORE_MINES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowCommandPosts.setColorFilter(LaunchUtilities.COMMAND_POSTS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowAirbases.setColorFilter(LaunchUtilities.AIRBASES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowWarehouses.setColorFilter(LaunchUtilities.WAREHOUSES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowArmory.setColorFilter(LaunchUtilities.ARMORIES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowRadarStations.setColorFilter(LaunchUtilities.RADAR_STATIONS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowArtilleryGuns.setColorFilter(LaunchUtilities.ARTILLERY_GUNS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowDistributors.setColorFilter(LaunchUtilities.DISTRIBUTORS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowProcessors.setColorFilter(LaunchUtilities.PROCESSORS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowScrapYards.setColorFilter(LaunchUtilities.SCRAP_YARDS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowNeutral.setColorFilter(LaunchUtilities.NEUTRAL_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowFriendly.setColorFilter(LaunchUtilities.FRIENDLY_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowEnemy.setColorFilter(LaunchUtilities.ENEMY_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowRadiations.setColorFilter(LaunchUtilities.RADIATIONS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowLoots.setColorFilter(LaunchUtilities.LOOTS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowShips.setColorFilter(LaunchUtilities.SHIPS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowSubmarines.setColorFilter(LaunchUtilities.SUBMARINES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowAircraft.setColorFilter(LaunchUtilities.AIRCRAFTS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowShipyards.setColorFilter(LaunchUtilities.SHIPYARDS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowPorts.setColorFilter(LaunchUtilities.PORTS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowMissiles.setColorFilter(LaunchUtilities.MISSILES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowInterceptors.setColorFilter(LaunchUtilities.INTERCEPTORS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowMonuments.setColorFilter(LaunchUtilities.MONUMENTS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowIronLoot.setColorFilter(LaunchUtilities.IRON_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowCoalLoot.setColorFilter(LaunchUtilities.COAL_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowOilLoot.setColorFilter(LaunchUtilities.OIL_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowCropLoot.setColorFilter(LaunchUtilities.CROP_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowUraniumLoot.setColorFilter(LaunchUtilities.URANIUM_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowGoldLoot.setColorFilter(LaunchUtilities.GOLD_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowLaborLoot.setColorFilter(LaunchUtilities.LABOR_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowKnowledgeLoot.setColorFilter(LaunchUtilities.KNOWLEDGE_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowLumberLoot.setColorFilter(LaunchUtilities.LUMBER_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowFertilizerLoot.setColorFilter(LaunchUtilities.FERTILIZER_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowWealthLoot.setColorFilter(LaunchUtilities.WEALTH_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowElectricityLoot.setColorFilter(LaunchUtilities.ELECTRICITY_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowConcreteLoot.setColorFilter(LaunchUtilities.CONCRETE_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowFuelLoot.setColorFilter(LaunchUtilities.FUEL_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowFoodLoot.setColorFilter(LaunchUtilities.FOOD_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowSteelLoot.setColorFilter(LaunchUtilities.STEEL_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowConstructionSuppliesLoot.setColorFilter(LaunchUtilities.CONSTRUCTION_SUPPLIES_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowExplosivesLoot.setColorFilter(LaunchUtilities.EXPLOSIVES_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowNerveAgentLoot.setColorFilter(LaunchUtilities.NERVE_AGENT_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowMachineryLoot.setColorFilter(LaunchUtilities.MACHINERY_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowElectronicsLoot.setColorFilter(LaunchUtilities.ELECTRONICS_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowMedicineLoot.setColorFilter(LaunchUtilities.MEDICINE_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowEnrichedUraniumLoot.setColorFilter(LaunchUtilities.FISSILE_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowAntimatterLoot.setColorFilter(LaunchUtilities.ANTIMATTER_LOOT_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);

                imgShowInfantries.setColorFilter(LaunchUtilities.INFANTRIES_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowCargoTrucks.setColorFilter(LaunchUtilities.CARGO_TRUCKS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowTanks.setColorFilter(LaunchUtilities.TANKS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
                imgShowSPAAGs.setColorFilter(LaunchUtilities.SPAAGS_VISIBLE ? 0 : LaunchUICommon.COLOUR_TINTED);
            }
        });
    }
}
