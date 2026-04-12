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
import launch.game.entities.conceptuals.Resource;

/**
 * Created by Biscuit
 */
public class BuildViewNew extends LaunchView
{
    private LinearLayout lytBuildMissileSite;
    private LinearLayout lytBuildSAM;
    private LinearLayout lytBuildSentry;
    private LinearLayout lytBuildArtilleryGun;
    private LinearLayout lytBuildAirbase;
    private LinearLayout lytBuildICBMSilo;
    private LinearLayout lytBuildArmory;
    private LinearLayout lytBuildABMSilo;
    private LinearLayout lytBuildCommandPost;
    private LinearLayout lytBuildWarehouse;
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

        lytBuildMissileSite = findViewById(R.id.lytBuildMissileSite);
        lytBuildSAM = findViewById(R.id.lytBuildSAM);
        lytBuildSentry = findViewById(R.id.lytBuildSentry);
        lytBuildArtilleryGun = findViewById(R.id.lytBuildArtilleryGun);
        lytBuildAirbase = findViewById(R.id.lytBuildAirbase);
        lytBuildICBMSilo = findViewById(R.id.lytBuildICBMSilo);
        lytBuildArmory = findViewById(R.id.lytBuildArmory);
        lytBuildABMSilo = findViewById(R.id.lytBuildABMSilo);
        lytBuildCommandPost = findViewById(R.id.lytBuildCommandPost);
        lytBuildWarehouse = findViewById(R.id.lytBuildWarehouse);

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
        }

        btnBuildMissileLauncher.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.MISSILE_SITE, Resource.ResourceType.FOOD, Defs.MISSILE_SITE_STRUCTURE_COST);
        btnBuildArtilleryGun.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ARTILLERY_GUN, Resource.ResourceType.FOOD, Defs.ARTILLERY_GUN_STRUCTURE_COST);
        btnBuildNukeLauncher.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.NUCLEAR_MISSILE_SITE, Resource.ResourceType.FOOD, Defs.ICBM_SILO_STRUCTURE_COST);
        btnBuildAirbase.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.AIRBASE, Resource.ResourceType.FOOD, Defs.AIRBASE_STRUCTURE_COST);
        btnBuildArmory.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ARMORY, Resource.ResourceType.FOOD, Defs.BARRACKS_STRUCTURE_COST);
        btnBuildSAM.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SAM_SITE, Resource.ResourceType.FOOD, Defs.SAM_SITE_STRUCTURE_COST);
        btnBuildABMSilo.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ABM_SILO, Resource.ResourceType.FOOD, Defs.ABM_SILO_STRUCTURE_COST);
        btnBuildSentryGun.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SENTRY_GUN, Resource.ResourceType.FOOD, Defs.SENTRY_GUN_STRUCTURE_COST);
        btnBuildWarehouse.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.WAREHOUSE, Resource.ResourceType.FOOD, Defs.WAREHOUSE_STRUCTURE_COST);
        btnBuildCommandPost.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.COMMAND_POST, Resource.ResourceType.FOOD, Defs.COMMAND_POST_STRUCTURE_COST);


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
                }
            }
        });
    }
}
