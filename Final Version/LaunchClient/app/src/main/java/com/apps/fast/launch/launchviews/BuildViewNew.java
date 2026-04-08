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
    private enum StructureCategory
    {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        OFFENSIVE,
        DEFENSIVE,
        ECONOMIC,
        FACTORY,
        ALL,
    }

    private String[] SortOrderNames;

    private void InitialiseSortTitles()
    {
        SortOrderNames = new String[]
                {
                        context.getString(R.string.sort_order_beginner),
                        context.getString(R.string.sort_order_intermediate),
                        context.getString(R.string.sort_order_advanced),
                        context.getString(R.string.sort_order_offensive),
                        context.getString(R.string.sort_order_defensive),
                        context.getString(R.string.sort_order_economic),
                        context.getString(R.string.sort_order_factory),
                        context.getString(R.string.sort_order_all),
                };
    }

    private StructureCategory category;

    //Layout expanders.

    /**
     * Missile factory is an indefinitely removed feature until some other purpose can be found for it. -Corbin
     * private LinearLayout lytBuildMissileFactory;
     * private LinearLayout btnBuildMissileFactory;
     * private TextView txtCostMissileFactory;
     * private ImageView btnMissileFactoryBlueprint;
     */

    private LinearLayout lytBuildMissileSite;
    private LinearLayout lytBuildSAM;
    private LinearLayout lytBuildSentry;
    private LinearLayout lytBuildWatchTower;
    private LinearLayout lytBuildArtilleryGun;
    private LinearLayout lytBuildAirbase;
    private LinearLayout lytBuildICBMSilo;
    private LinearLayout lytBuildArmory;
    private LinearLayout lytBuildBarracks;
    private LinearLayout lytBuildABMSilo;
    private LinearLayout lytBuildCommandPost;
    private LinearLayout lytBuildOreMine;
    private LinearLayout lytBuildWarehouse;
    private LinearLayout lytBuildRadarStation;
    private LinearLayout lytBuildProcessor;
    private PurchaseButton btnBuildMissileLauncher;
    private PurchaseButton btnBuildNukeLauncher;
    private PurchaseButton btnBuildAirbase;
    private PurchaseButton btnBuildArmory;
    private PurchaseButton btnBuildBarracks;
    private PurchaseButton btnBuildSAM;
    private PurchaseButton btnBuildSentryGun;
    private PurchaseButton btnBuildWatchTower;
    private PurchaseButton btnBuildCommandPost;
    private PurchaseButton btnBuildOreMine;
    private PurchaseButton btnBuildWarehouse;
    private PurchaseButton btnBuildRadarStation;
    private PurchaseButton btnBuildArtilleryGun;
    private PurchaseButton btnBuildABMSilo;
    private PurchaseButton btnBuildScrapYard;
    private LinearLayout btnBuildProcessor;
    private LinearLayout btnBuildDistributor;
    private LinearLayout lytBuildScrapYard;
    private TextView txtDescWarehouse;
    private TextView btnGoToPage;
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

        InitialiseSortTitles();

        lytBuildMissileSite = findViewById(R.id.lytBuildMissileSite);
        lytBuildSAM = findViewById(R.id.lytBuildSAM);
        lytBuildSentry = findViewById(R.id.lytBuildSentry);
        lytBuildWatchTower = findViewById(R.id.lytBuildWatchTower);
        lytBuildArtilleryGun = findViewById(R.id.lytBuildArtilleryGun);
        lytBuildAirbase = findViewById(R.id.lytBuildAirbase);
        lytBuildICBMSilo = findViewById(R.id.lytBuildICBMSilo);
        lytBuildArmory = findViewById(R.id.lytBuildArmory);
        lytBuildBarracks = findViewById(R.id.lytBuildBarracks);
        lytBuildABMSilo = findViewById(R.id.lytBuildABMSilo);
        lytBuildCommandPost = findViewById(R.id.lytBuildCommandPost);
        lytBuildOreMine = findViewById(R.id.lytBuildOreMine);
        lytBuildWarehouse = findViewById(R.id.lytBuildWarehouse);
        lytBuildRadarStation = findViewById(R.id.lytBuildRadarStation);
        lytBuildProcessor = findViewById(R.id.lytBuildProcessor);
        lytBuildScrapYard = findViewById(R.id.lytBuildScrapYard);

        btnBuildMissileLauncher = findViewById(R.id.btnBuildMissileLauncher);
        btnBuildNukeLauncher = findViewById(R.id.btnBuildNukeLauncher);
        btnBuildAirbase = findViewById(R.id.btnBuildAirbase);
        btnBuildArmory = findViewById(R.id.btnBuildArmory);
        btnBuildBarracks = findViewById(R.id.btnBuildBarracks);
        btnBuildSAM = findViewById(R.id.btnBuildSAM);
        btnBuildSentryGun = findViewById(R.id.btnBuildSentryGun);
        btnBuildWatchTower = findViewById(R.id.btnBuildWatchTower);
        btnBuildCommandPost = findViewById(R.id.btnBuildCommandPost);
        btnBuildOreMine = findViewById(R.id.btnBuildOreMine);
        btnBuildWarehouse = findViewById(R.id.btnBuildWarehouse);
        btnBuildRadarStation = findViewById(R.id.btnBuildRadarStation);
        btnBuildArtilleryGun = findViewById(R.id.btnBuildArtilleryGun);
        btnBuildABMSilo = findViewById(R.id.btnBuildABMSilo);
        btnBuildProcessor = findViewById(R.id.btnBuildProcessor);
        btnBuildDistributor = findViewById(R.id.btnBuildDistributor);
        btnBuildScrapYard = findViewById(R.id.btnBuildScrapYard);
        txtDescWarehouse = findViewById(R.id.txtDescWarehouse);
        btnGoToPage = findViewById(R.id.btnGoToPage);
        bTooCloseToStructures = !game.GetNearbyStructures(game.GetOurPlayer()).isEmpty();

        category = StructureCategory.BEGINNER;

        SharedPreferences sharedPreferences = activity.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        category = StructureCategory.values()[sharedPreferences.getInt(ClientDefs.LAST_BUILD_CATEGORY, StructureCategory.BEGINNER.ordinal())];

        ExpandPage();

        if(bTooCloseToStructures)
        {
            btnBuildABMSilo.setAlpha(0.5f);
            btnBuildAirbase.setAlpha(0.5f);
            btnBuildArmory.setAlpha(0.5f);
            btnBuildBarracks.setAlpha(0.5f);
            btnBuildArtilleryGun.setAlpha(0.5f);
            btnBuildCommandPost.setAlpha(0.5f);
            btnBuildMissileLauncher.setAlpha(0.5f);
            btnBuildNukeLauncher.setAlpha(0.5f);
            btnBuildOreMine.setAlpha(0.5f);
            btnBuildProcessor.setAlpha(0.5f);
            btnBuildDistributor.setAlpha(0.5f);
            btnBuildSAM.setAlpha(0.5f);
            btnBuildSentryGun.setAlpha(0.5f);
            btnBuildWatchTower.setAlpha(0.5f);
            btnBuildScrapYard.setAlpha(0.5f);
            btnBuildRadarStation.setAlpha(0.5f);
            btnBuildWarehouse.setAlpha(0.5f);
        }
        else
        {
            btnBuildABMSilo.setAlpha(1.0f);
            btnBuildAirbase.setAlpha(1.0f);
            btnBuildArmory.setAlpha(1.0f);
            btnBuildBarracks.setAlpha(1.0f);
            btnBuildArtilleryGun.setAlpha(1.0f);
            btnBuildCommandPost.setAlpha(1.0f);
            btnBuildMissileLauncher.setAlpha(1.0f);
            btnBuildNukeLauncher.setAlpha(1.0f);
            btnBuildOreMine.setAlpha(1.0f);
            btnBuildProcessor.setAlpha(1.0f);
            btnBuildDistributor.setAlpha(1.0f);
            btnBuildSAM.setAlpha(1.0f);
            btnBuildSentryGun.setAlpha(1.0f);
            btnBuildWatchTower.setAlpha(1.0f);
            btnBuildScrapYard.setAlpha(1.0f);
            btnBuildRadarStation.setAlpha(1.0f);
            btnBuildWarehouse.setAlpha(1.0f);
        }

        btnGoToPage.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.select_help_page));

                builder.setSingleChoiceItems(SortOrderNames, category.ordinal(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        category = StructureCategory.values()[i];
                        dialogInterface.dismiss();
                        SharedPreferences.Editor editor = activity.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
                        editor.putInt(ClientDefs.LAST_BUILD_CATEGORY, category.ordinal());
                        editor.commit();

                        ExpandPage();
                    }
                });

                builder.show();
            }
        });

        txtDescWarehouse.setText(context.getString(R.string.desc_warehouse));

        btnBuildProcessor.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new BuildProcessorView(game, activity));
            }
        });

        btnBuildDistributor.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new BuildDistributorView(game, activity));
            }
        });

        btnBuildMissileLauncher.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.MISSILE_SITE, Resource.ResourceType.FOOD, Defs.MISSILE_SITE_STRUCTURE_COST);

        btnBuildMissileLauncher.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.MISSILE_SITE);
                return false;
            }
        });

        btnBuildArtilleryGun.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ARTILLERY_GUN, Resource.ResourceType.FOOD, Defs.ARTILLERY_GUN_STRUCTURE_COST);

        btnBuildArtilleryGun.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.ARTILLERY_GUN);
                return false;
            }
        });

        btnBuildNukeLauncher.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.NUCLEAR_MISSILE_SITE, Resource.ResourceType.FOOD, Defs.ICBM_SILO_STRUCTURE_COST);

        btnBuildNukeLauncher.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.NUCLEAR_MISSILE_SITE);
                return false;
            }
        });

        btnBuildAirbase.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.AIRBASE, Resource.ResourceType.FOOD, Defs.AIRBASE_STRUCTURE_COST);

        btnBuildAirbase.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.AIRBASE);
                return false;
            }
        });

        btnBuildArmory.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ARMORY, Resource.ResourceType.FOOD, Defs.BARRACKS_STRUCTURE_COST);

        btnBuildArmory.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.ARMORY);
                return false;
            }
        });

        btnBuildBarracks.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.BARRACKS, Resource.ResourceType.FOOD, Defs.BARRACKS_STRUCTURE_COST);

        btnBuildBarracks.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.BARRACKS);
                return false;
            }
        });

        btnBuildSAM.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SAM_SITE, Resource.ResourceType.FOOD, Defs.SAM_SITE_STRUCTURE_COST);

        btnBuildSAM.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.SAM_SITE);
                return false;
            }
        });

        btnBuildABMSilo.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ABM_SILO, Resource.ResourceType.FOOD, Defs.ABM_SILO_STRUCTURE_COST);

        btnBuildABMSilo.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.ABM_SILO);
                return false;
            }
        });

        btnBuildSentryGun.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SENTRY_GUN, Resource.ResourceType.FOOD, Defs.SENTRY_GUN_STRUCTURE_COST);

        btnBuildSentryGun.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.SENTRY_GUN);
                return false;
            }
        });

        btnBuildWatchTower.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.WATCH_TOWER, Resource.ResourceType.FOOD, Defs.WATCH_TOWER_STRUCTURE_COST);

        btnBuildWatchTower.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.WATCH_TOWER);
                return false;
            }
        });

        btnBuildRadarStation.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.RADAR_STATION, Resource.ResourceType.FOOD, Defs.RADAR_STATION_STRUCTURE_COST);

        btnBuildRadarStation.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.RADAR_STATION);
                return false;
            }
        });

        btnBuildOreMine.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.ORE_MINE, Resource.ResourceType.FOOD, Defs.WAREHOUSE_STRUCTURE_COST);

        btnBuildOreMine.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.RADAR_STATION);
                return false;
            }
        });

        btnBuildWarehouse.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.WAREHOUSE, Resource.ResourceType.FOOD, Defs.WAREHOUSE_STRUCTURE_COST);

        btnBuildWarehouse.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.WAREHOUSE);
                return false;
            }
        });

        btnBuildScrapYard.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.SCRAP_YARD, Resource.ResourceType.FOOD, Defs.SCRAPYARD_STRUCTURE_COST);

        btnBuildScrapYard.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.SCRAP_YARD);
                return false;
            }
        });

        btnBuildCommandPost.SetUnit(game, activity, game.GetOurPlayer().GetPointer(), EntityType.COMMAND_POST, Resource.ResourceType.FOOD, Defs.COMMAND_POST_STRUCTURE_COST);

        btnBuildCommandPost.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                handleBlueprintClick(EntityType.COMMAND_POST);
                return false;
            }
        });

        Update();
    }

    private void handleBlueprintClick(final EntityPointer.EntityType entityType)
    {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);

        if(!sharedPreferences.getBoolean(ClientDefs.HAS_CLICKED_BLUEPRINT_BUTTON, ClientDefs.HAS_CLICKED_BLUEPRINT_BUTTON_DEFAULT))
        {
            SharedPreferences.Editor editor = context.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE).edit();
            editor.putBoolean(ClientDefs.HAS_CLICKED_BLUEPRINT_BUTTON, true);
            editor.commit();

            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderConstruct();
            launchDialog.SetMessage(context.getString(R.string.blueprint_virgin_info));
            launchDialog.SetOnClickYes(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.PlaceBlueprintMode(entityType, Resource.ResourceType.CROPS);
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
        else
        {
            if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
            {
                activity.PlaceBlueprintMode(entityType, Resource.ResourceType.CROPS);
            }
            else
            {
                activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
            }
        }
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
                    btnBuildBarracks.setAlpha(0.5f);
                    btnBuildArtilleryGun.setAlpha(0.5f);
                    btnBuildCommandPost.setAlpha(0.5f);
                    btnBuildMissileLauncher.setAlpha(0.5f);
                    btnBuildNukeLauncher.setAlpha(0.5f);
                    btnBuildOreMine.setAlpha(0.5f);
                    btnBuildProcessor.setAlpha(0.5f);
                    btnBuildDistributor.setAlpha(0.5f);
                    btnBuildSAM.setAlpha(0.5f);
                    btnBuildSentryGun.setAlpha(0.5f);
                    btnBuildWatchTower.setAlpha(0.5f);
                    btnBuildScrapYard.setAlpha(0.5f);
                    btnBuildRadarStation.setAlpha(0.5f);
                    btnBuildWarehouse.setAlpha(0.5f);
                    btnBuildOreMine.setAlpha(0.5f);

                    btnBuildABMSilo.SetTooClose(true);
                    btnBuildAirbase.SetTooClose(true);
                    btnBuildArmory.SetTooClose(true);
                    btnBuildBarracks.SetTooClose(true);
                    btnBuildArtilleryGun.SetTooClose(true);
                    btnBuildCommandPost.SetTooClose(true);
                    btnBuildMissileLauncher.SetTooClose(true);
                    btnBuildNukeLauncher.SetTooClose(true);
                    btnBuildSAM.SetTooClose(true);
                    btnBuildSentryGun.SetTooClose(true);
                    btnBuildWatchTower.SetTooClose(true);
                    btnBuildScrapYard.SetTooClose(true);
                    btnBuildRadarStation.SetTooClose(true);
                    btnBuildWarehouse.SetTooClose(true);
                    btnBuildOreMine.SetTooClose(true);
                }
                else
                {
                    bTooCloseToStructures = false;

                    btnBuildABMSilo.setAlpha(1.0f);
                    btnBuildAirbase.setAlpha(1.0f);
                    btnBuildArmory.setAlpha(1.0f);
                    btnBuildBarracks.setAlpha(1.0f);
                    btnBuildArtilleryGun.setAlpha(1.0f);
                    btnBuildCommandPost.setAlpha(1.0f);
                    btnBuildMissileLauncher.setAlpha(1.0f);
                    btnBuildNukeLauncher.setAlpha(1.0f);
                    btnBuildOreMine.setAlpha(1.0f);
                    btnBuildProcessor.setAlpha(1.0f);
                    btnBuildSAM.setAlpha(1.0f);
                    btnBuildSentryGun.setAlpha(1.0f);
                    btnBuildWatchTower.setAlpha(1.0f);
                    btnBuildScrapYard.setAlpha(1.0f);
                    btnBuildRadarStation.setAlpha(1.0f);
                    btnBuildWarehouse.setAlpha(1.0f);
                    btnBuildOreMine.setAlpha(1.0f);

                    btnBuildABMSilo.SetTooClose(false);
                    btnBuildAirbase.SetTooClose(false);
                    btnBuildArmory.SetTooClose(false);
                    btnBuildBarracks.SetTooClose(false);
                    btnBuildArtilleryGun.SetTooClose(false);
                    btnBuildCommandPost.SetTooClose(false);
                    btnBuildMissileLauncher.SetTooClose(false);
                    btnBuildNukeLauncher.SetTooClose(false);
                    btnBuildSAM.SetTooClose(false);
                    btnBuildSentryGun.SetTooClose(false);
                    btnBuildWatchTower.SetTooClose(false);
                    btnBuildScrapYard.SetTooClose(false);
                    btnBuildRadarStation.SetTooClose(false);
                    btnBuildWarehouse.SetTooClose(false);
                    btnBuildOreMine.SetTooClose(false);
                }
            }
        });
    }

    public void ExpandPage()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                CollapseAllPages();

                switch(category)
                {
                    case BEGINNER:
                    {
                        lytBuildSAM.setVisibility(VISIBLE);
                        lytBuildSentry.setVisibility(VISIBLE);
                        //lytBuildWatchTower.setVisibility(VISIBLE);
                        lytBuildArtilleryGun.setVisibility(VISIBLE);
                        lytBuildCommandPost.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_beginner)));
                    }
                    break;

                    case INTERMEDIATE:
                    {
                        lytBuildAirbase.setVisibility(VISIBLE);
                        lytBuildMissileSite.setVisibility(VISIBLE);
                        lytBuildArtilleryGun.setVisibility(VISIBLE);
                        lytBuildArmory.setVisibility(VISIBLE);
                        //lytBuildBarracks.setVisibility(VISIBLE);
                        lytBuildScrapYard.setVisibility(VISIBLE);
                        lytBuildWarehouse.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_intermediate)));
                    }
                    break;

                    case ADVANCED:
                    {
                        lytBuildRadarStation.setVisibility(VISIBLE);
                        lytBuildICBMSilo.setVisibility(VISIBLE);
                        lytBuildABMSilo.setVisibility(VISIBLE);
                        lytBuildOreMine.setVisibility(VISIBLE);
                        lytBuildProcessor.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_advanced)));
                    }
                    break;

                    case OFFENSIVE:
                    {
                        lytBuildMissileSite.setVisibility(VISIBLE);
                        lytBuildAirbase.setVisibility(VISIBLE);
                        lytBuildICBMSilo.setVisibility(VISIBLE);
                        lytBuildArmory.setVisibility(VISIBLE);
                        //lytBuildBarracks.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_offensive)));
                    }
                    break;

                    case DEFENSIVE:
                    {
                        lytBuildSAM.setVisibility(VISIBLE);
                        lytBuildSentry.setVisibility(VISIBLE);
                        //lytBuildWatchTower.setVisibility(VISIBLE);
                        lytBuildArtilleryGun.setVisibility(VISIBLE);
                        lytBuildAirbase.setVisibility(VISIBLE);
                        lytBuildABMSilo.setVisibility(VISIBLE);
                        lytBuildCommandPost.setVisibility(VISIBLE);
                        lytBuildRadarStation.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_defensive)));
                    }
                    break;

                    case ECONOMIC:
                    {
                        lytBuildOreMine.setVisibility(VISIBLE);
                        lytBuildProcessor.setVisibility(VISIBLE);
                        lytBuildWarehouse.setVisibility(VISIBLE);
                        lytBuildScrapYard.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_economic)));
                    }
                    break;

                    case FACTORY:
                    {
                        lytBuildAirbase.setVisibility(VISIBLE);
                        lytBuildArmory.setVisibility(VISIBLE);
                        //lytBuildBarracks.setVisibility(VISIBLE);
                        lytBuildWarehouse.setVisibility(VISIBLE);
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_factory)));
                    }
                    break;

                    case ALL:
                    {
                        ExpandAllPages();
                        btnGoToPage.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.sort_order_all)));
                    }
                    break;
                }
            }
        });
    }

    public void CollapseAllPages()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytBuildMissileSite.setVisibility(GONE);
                lytBuildSAM.setVisibility(GONE);
                lytBuildSentry.setVisibility(GONE);
                //lytBuildWatchTower.setVisibility(GONE);
                lytBuildArtilleryGun.setVisibility(GONE);
                lytBuildAirbase.setVisibility(GONE);
                lytBuildICBMSilo.setVisibility(GONE);
                lytBuildArmory.setVisibility(GONE);
                lytBuildBarracks.setVisibility(GONE);
                lytBuildABMSilo.setVisibility(GONE);
                lytBuildCommandPost.setVisibility(GONE);
                lytBuildOreMine.setVisibility(GONE);
                lytBuildWarehouse.setVisibility(GONE);
                lytBuildRadarStation.setVisibility(GONE);
                lytBuildProcessor.setVisibility(GONE);
            }
        });
    }

    public void ExpandAllPages()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytBuildMissileSite.setVisibility(VISIBLE);
                lytBuildSAM.setVisibility(VISIBLE);
                lytBuildSentry.setVisibility(VISIBLE);
                //lytBuildWatchTower.setVisibility(VISIBLE);
                lytBuildArtilleryGun.setVisibility(VISIBLE);
                lytBuildAirbase.setVisibility(VISIBLE);
                lytBuildICBMSilo.setVisibility(VISIBLE);
                lytBuildArmory.setVisibility(VISIBLE);
                //lytBuildBarracks.setVisibility(VISIBLE);
                lytBuildABMSilo.setVisibility(VISIBLE);
                lytBuildCommandPost.setVisibility(VISIBLE);
                lytBuildOreMine.setVisibility(VISIBLE);
                lytBuildWarehouse.setVisibility(VISIBLE);
                lytBuildRadarStation.setVisibility(VISIBLE);
                lytBuildProcessor.setVisibility(VISIBLE);
                lytBuildScrapYard.setVisibility(VISIBLE);
            }
        });
    }
}
