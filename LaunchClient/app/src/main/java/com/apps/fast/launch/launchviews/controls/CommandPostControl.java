package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.PurchaseButton;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.CommandPost;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class CommandPostControl extends LaunchView
{
    private int lID;
    private ScrollView lytStructures;
    private PurchaseButton btnBuildMissileLauncher;
    private PurchaseButton btnBuildNukeLauncher;
    private PurchaseButton btnBuildAirbase;
    private PurchaseButton btnBuildArmory;
    private PurchaseButton btnBuildBarracks;
    private PurchaseButton btnBuildSAM;
    private PurchaseButton btnBuildSentryGun;
    private PurchaseButton btnBuildWatchTower;
    private PurchaseButton btnBuildWarehouse;
    private PurchaseButton btnBuildArtilleryGun;
    private PurchaseButton btnBuildABMSilo;
    private TextView btnCancel;
    private boolean bOurStructure;
    private CommandPost post = null;

    public CommandPostControl(LaunchClientGame game, MainActivity activity, int lCommandPostID)
    {
        super(game, activity, true);
        lID = lCommandPostID;

        if(game.GetCommandPost(lID) != null)
        {
            post = game.GetCommandPost(lID);
            bOurStructure = (game.GetCommandPost(lID).GetOwnerID() == game.GetOurPlayerID() ? true : false);
        }
        else
        {
            bOurStructure = false;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_command_post, this);

        lytStructures = findViewById(R.id.lytStructures);
        btnBuildMissileLauncher = findViewById(R.id.btnBuildMissileLauncher);
        btnBuildNukeLauncher = findViewById(R.id.btnBuildNukeLauncher);
        btnBuildAirbase = findViewById(R.id.btnBuildAirbase);
        btnBuildArmory = findViewById(R.id.btnBuildArmory);
        btnBuildBarracks = findViewById(R.id.btnBuildBarracks);
        btnBuildSAM = findViewById(R.id.btnBuildSAM);
        btnBuildSentryGun = findViewById(R.id.btnBuildSentryGun);
        btnBuildWatchTower = findViewById(R.id.btnBuildWatchTower);
        btnBuildWarehouse = findViewById(R.id.btnBuildWarehouse);
        btnBuildArtilleryGun = findViewById(R.id.btnBuildArtilleryGun);
        btnBuildABMSilo = findViewById(R.id.btnBuildABMSilo);

        btnCancel = findViewById(R.id.btnCancel);

        if(bOurStructure)
        {
            btnBuildMissileLauncher.SetUnit(game, activity, post.GetPointer(), EntityType.MISSILE_SITE, ResourceType.FOOD, Defs.MISSILE_SITE_STRUCTURE_COST);
            btnBuildArtilleryGun.SetUnit(game, activity, post.GetPointer(), EntityType.ARTILLERY_GUN, ResourceType.FOOD, Defs.ARTILLERY_GUN_STRUCTURE_COST);
            btnBuildNukeLauncher.SetUnit(game, activity, post.GetPointer(), EntityType.NUCLEAR_MISSILE_SITE, ResourceType.FOOD, Defs.ICBM_SILO_STRUCTURE_COST);
            btnBuildAirbase.SetUnit(game, activity, post.GetPointer(), EntityType.AIRBASE, ResourceType.FOOD, Defs.AIRBASE_STRUCTURE_COST);
            btnBuildArmory.SetUnit(game, activity, post.GetPointer(), EntityType.ARMORY, ResourceType.FOOD, Defs.BARRACKS_STRUCTURE_COST);
            btnBuildBarracks.SetUnit(game, activity, post.GetPointer(), EntityType.BARRACKS, ResourceType.FOOD, Defs.BARRACKS_STRUCTURE_COST);
            btnBuildSAM.SetUnit(game, activity, post.GetPointer(), EntityType.SAM_SITE, ResourceType.FOOD, Defs.SAM_SITE_STRUCTURE_COST);
            btnBuildABMSilo.SetUnit(game, activity, post.GetPointer(), EntityType.ABM_SILO, ResourceType.FOOD, Defs.ABM_SILO_STRUCTURE_COST);
            btnBuildSentryGun.SetUnit(game, activity, post.GetPointer(), EntityType.SENTRY_GUN, ResourceType.FOOD, Defs.SENTRY_GUN_STRUCTURE_COST);
            btnBuildWatchTower.SetUnit(game, activity, post.GetPointer(), EntityType.WATCH_TOWER, ResourceType.FOOD, Defs.SENTRY_GUN_STRUCTURE_COST);
            btnBuildWarehouse.SetUnit(game, activity, post.GetPointer(), EntityType.WAREHOUSE, ResourceType.FOOD, Defs.WAREHOUSE_STRUCTURE_COST);
        }
        else
        {
            lytStructures.setVisibility(GONE);
        }

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
                CommandPost commandPost = game.GetCommandPost(lID);

                if(bOurStructure)
                {

                }
            }
        });
    }
}
