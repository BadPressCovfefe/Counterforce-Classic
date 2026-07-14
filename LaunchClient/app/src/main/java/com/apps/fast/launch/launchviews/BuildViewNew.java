package com.apps.fast.launch.launchviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.CommandPost;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource;

/**
 * Created by Biscuit
 */
public class BuildViewNew extends LaunchView
{
    private PurchaseButton btnBuildMissileLauncher;
    private PurchaseButton btnBuildNukeLauncher;
    private PurchaseButton btnBuildAirbase;
    private PurchaseButton btnBuildArmory;
    private PurchaseButton btnBuildSAM;
    private PurchaseButton btnBuildSentryGun;
    private PurchaseButton btnBuildCommandPost;
    private PurchaseButton btnBuildWarehouse;
    private PurchaseButton btnBuildArtilleryGun;
    private PurchaseButton btnBuildABMSilo;
    private PurchaseButton btnBuildSolarPanel;
    private PurchaseButton btnBuildFarm;
    private PurchaseButton btnBuildOreMine;
    private PurchaseButton btnBuildLogisticsDepot;
    private PurchaseButton btnBuildShipyard;
    private TextView txtBuildHQ;
    boolean bTooCloseToStructures;

    public BuildViewNew(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity, true);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_build_new, this);

        btnBuildMissileLauncher = findViewById(R.id.btnBuildMissileLauncher);
        btnBuildNukeLauncher = findViewById(R.id.btnBuildNukeLauncher);
        btnBuildAirbase = findViewById(R.id.btnBuildAirbase);
        btnBuildArmory = findViewById(R.id.btnBuildArmory);
        btnBuildSAM = findViewById(R.id.btnBuildSAM);
        btnBuildSentryGun = findViewById(R.id.btnBuildSentryGun);
        btnBuildCommandPost = findViewById(R.id.btnBuildCommandPost);
        btnBuildWarehouse = findViewById(R.id.btnBuildWarehouse);
        btnBuildArtilleryGun = findViewById(R.id.btnBuildArtilleryGun);
        btnBuildABMSilo = findViewById(R.id.btnBuildABMSilo);
        btnBuildSolarPanel = findViewById(R.id.btnBuildSolarPanel);
        btnBuildFarm = findViewById(R.id.btnBuildFarm);
        btnBuildOreMine = findViewById(R.id.btnBuildOreMine);
        btnBuildLogisticsDepot = findViewById(R.id.btnBuildLogisticsDepot);
        btnBuildShipyard = findViewById(R.id.btnBuildShipyard);
        txtBuildHQ = findViewById(R.id.txtBuildHQ);

        bTooCloseToStructures = !game.GetNearbyStructures(game.GetOurPlayer()).isEmpty();

        if(bTooCloseToStructures)
        {
            btnBuildABMSilo.setAlpha(0.5f);
            btnBuildAirbase.setAlpha(0.5f);
            btnBuildArmory.setAlpha(0.5f);
            btnBuildArtilleryGun.setAlpha(0.5f);
            btnBuildCommandPost.setAlpha(0.5f);
            btnBuildMissileLauncher.setAlpha(0.5f);
            btnBuildNukeLauncher.setAlpha(0.5f);
            btnBuildSAM.setAlpha(0.5f);
            btnBuildSentryGun.setAlpha(0.5f);
            btnBuildWarehouse.setAlpha(0.5f);
            btnBuildSolarPanel.setAlpha(0.5f);
            btnBuildFarm.setAlpha(0.5f);
            btnBuildOreMine.setAlpha(0.5f);
            btnBuildLogisticsDepot.setAlpha(0.5f);
            btnBuildShipyard.setAlpha(0.5f);
        }
        else
        {
            btnBuildABMSilo.setAlpha(1.0f);
            btnBuildAirbase.setAlpha(1.0f);
            btnBuildArmory.setAlpha(1.0f);
            btnBuildArtilleryGun.setAlpha(1.0f);
            btnBuildCommandPost.setAlpha(1.0f);
            btnBuildMissileLauncher.setAlpha(1.0f);
            btnBuildNukeLauncher.setAlpha(1.0f);
            btnBuildSAM.setAlpha(1.0f);
            btnBuildSentryGun.setAlpha(1.0f);
            btnBuildWarehouse.setAlpha(1.0f);
            btnBuildSolarPanel.setAlpha(1.0f);
            btnBuildFarm.setAlpha(1.0f);
            btnBuildOreMine.setAlpha(1.0f);
            btnBuildLogisticsDepot.setAlpha(1.0f);
            btnBuildShipyard.setAlpha(1.0f);
        }

        btnBuildMissileLauncher.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.MISSILE_SITE);
        btnBuildArtilleryGun.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ARTILLERY_GUN);
        btnBuildNukeLauncher.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.NUCLEAR_MISSILE_SITE);
        btnBuildAirbase.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.AIRBASE);
        btnBuildArmory.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ARMORY);
        btnBuildSAM.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SAM_SITE);
        btnBuildABMSilo.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ABM_SILO);
        btnBuildSentryGun.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SENTRY_GUN);
        btnBuildWarehouse.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.WAREHOUSE);
        btnBuildSolarPanel.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SOLAR_PANEL);
        btnBuildFarm.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.FARM);
        btnBuildOreMine.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ORE_MINE);
        btnBuildLogisticsDepot.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.LOGISTICS_DEPOT);
        btnBuildCommandPost.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.COMMAND_POST);
        btnBuildShipyard.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SHIPYARD);

        txtBuildHQ.setVisibility(GONE);

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(game.GetNearbyStructures(game.GetOurPlayer()).size() > 0)
                {
                    bTooCloseToStructures = true;

                    btnBuildABMSilo.setAlpha(0.5f);
                    btnBuildAirbase.setAlpha(0.5f);
                    btnBuildArmory.setAlpha(0.5f);
                    btnBuildArtilleryGun.setAlpha(0.5f);
                    btnBuildCommandPost.setAlpha(0.5f);
                    btnBuildMissileLauncher.setAlpha(0.5f);
                    btnBuildNukeLauncher.setAlpha(0.5f);
                    btnBuildSAM.setAlpha(0.5f);
                    btnBuildSentryGun.setAlpha(0.5f);
                    btnBuildWarehouse.setAlpha(0.5f);
                    btnBuildSolarPanel.setAlpha(0.5f);
                    btnBuildFarm.setAlpha(0.5f);
                    btnBuildOreMine.setAlpha(0.5f);
                    btnBuildLogisticsDepot.setAlpha(0.5f);

                    btnBuildABMSilo.SetTooClose(true);
                    btnBuildAirbase.SetTooClose(true);
                    btnBuildArmory.SetTooClose(true);
                    btnBuildArtilleryGun.SetTooClose(true);
                    btnBuildCommandPost.SetTooClose(true);
                    btnBuildMissileLauncher.SetTooClose(true);
                    btnBuildNukeLauncher.SetTooClose(true);
                    btnBuildSAM.SetTooClose(true);
                    btnBuildSentryGun.SetTooClose(true);
                    btnBuildWarehouse.SetTooClose(true);
                    btnBuildSolarPanel.SetTooClose(true);
                    btnBuildFarm.SetTooClose(true);
                    btnBuildOreMine.SetTooClose(true);
                    btnBuildLogisticsDepot.SetTooClose(true);
                }
                else
                {
                    bTooCloseToStructures = false;

                    btnBuildABMSilo.setAlpha(1.0f);
                    btnBuildAirbase.setAlpha(1.0f);
                    btnBuildArmory.setAlpha(1.0f);
                    btnBuildArtilleryGun.setAlpha(1.0f);
                    btnBuildCommandPost.setAlpha(1.0f);
                    btnBuildMissileLauncher.setAlpha(1.0f);
                    btnBuildNukeLauncher.setAlpha(1.0f);
                    btnBuildSAM.setAlpha(1.0f);
                    btnBuildSentryGun.setAlpha(1.0f);
                    btnBuildWarehouse.setAlpha(1.0f);
                    btnBuildSolarPanel.setAlpha(1.0f);
                    btnBuildFarm.setAlpha(1.0f);
                    btnBuildOreMine.setAlpha(1.0f);
                    btnBuildLogisticsDepot.setAlpha(1.0f);

                    btnBuildABMSilo.SetTooClose(false);
                    btnBuildAirbase.SetTooClose(false);
                    btnBuildArmory.SetTooClose(false);
                    btnBuildArtilleryGun.SetTooClose(false);
                    btnBuildCommandPost.SetTooClose(false);
                    btnBuildMissileLauncher.SetTooClose(false);
                    btnBuildNukeLauncher.SetTooClose(false);
                    btnBuildSAM.SetTooClose(false);
                    btnBuildSentryGun.SetTooClose(false);
                    btnBuildWarehouse.SetTooClose(false);
                    btnBuildSolarPanel.SetTooClose(false);
                    btnBuildFarm.SetTooClose(false);
                    btnBuildOreMine.SetTooClose(false);
                    btnBuildLogisticsDepot.SetTooClose(false);
                }
            }
        });
    }
}
