package com.apps.fast.launch.launchviews;

import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Alliance;
import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.Bank;
import launch.game.entities.Armory;
import launch.game.entities.CommandPost;
import launch.game.entities.CargoTruck;
import launch.game.entities.CargoTruckInterface;
import launch.game.entities.MissileFactory;
import launch.game.entities.Distributor;
import launch.game.entities.InfantryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Processor;
import launch.game.entities.ScrapYard;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.entities.Tank;
import launch.game.entities.TankInterface;
import launch.game.entities.Warehouse;
import launch.game.entities.MissileSite;
import launch.game.entities.OreMine;
import launch.game.entities.Player;
import launch.game.entities.RadarStation;
import launch.game.entities.SAMSite;
import launch.game.entities.SentryGun;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class WealthRulesView extends LaunchView
{
    private static final int HOURS_PER_24 = 24;

    private LinearLayout lytHourlyCosts;
    private List<LaunchView> EntityViews;
    private ImageView imgShowOnline;
    private ImageView imgShowOffline;
    private ImageView imgShowMissileSites;
    private ImageView imgShowSAMSites;
    private ImageView imgShowSentryGuns;
    private ImageView imgShowWatchTowers;
    private ImageView imgShowOreMines;
    private ImageView imgShowRadarStations;
    private ImageView imgShowCommandPosts;
    private ImageView imgShowAirbases;
    private ImageView imgShowArmory;
    private ImageView imgShowWarehouses;
    private ImageView imgShowScrapYards;
    private ImageView imgShowProcessors;
    private ImageView imgShowDistributors;
    private ImageView imgShowArtilleryGuns;
    private ImageView imgShowAircrafts;
    private ImageView imgShowTanks;
    private ImageView imgShowShips;
    private ImageView imgShowSubmarines;
    private ImageView imgShowInfantry;
    private ImageView imgShowCargoTrucks;
    private TextView txtRankPayQuantity;
    private TextView txtRankPayEach;
    private TextView txtRankPay;
    private TextView txtAllianceTaxEach;
    private TextView txtAllianceTax;
    private TextView txtStructureQuantity;
    private TextView txtStructureCostEach;
    private TextView txtStructureCost;
    private TextView txtAircraftQuantity;
    private TextView txtAircraftCost;

    private TextView txtInfantryQuantity;
    private TextView txtInfantryCostEach;
    private TextView txtInfantryCost;
    private TextView txtTankQuantity;
    private TextView txtTankCostEach;
    private TextView txtTankCost;
    private TextView txtCargoTruckQuantity;
    private TextView txtCargoTruckCostEach;
    private TextView txtCargoTruckCost;
    private TextView txtShipQuantity;
    private TextView txtShipCost;
    private TextView txtSubmarineQuantity;
    private TextView txtSubmarineCost;
    private TextView txtICBMQuantity;
    private TextView txtICBMCostEach;
    private TextView txtICBMCost;
    private TextView txtABMQuantity;
    private TextView txtABMCostEach;
    private TextView txtABMCost;
    private TextView txtMissileQuantity;
    private TextView txtMissileCostEach;
    private TextView txtMissileCost;
    private TextView txtInterceptorQuantity;
    private TextView txtInterceptorCostEach;
    private TextView txtInterceptorCost;
    private TextView txtTorpedoQuantity;
    private TextView txtTorpedoCostEach;
    private TextView txtTorpedoCost;
    private TextView txtBasicIncomeEach;
    private TextView txtBasicIncome;
    private TextView txtGrossHourly;
    private TextView txtHourlyExpenses;
    private TextView txtNetHourly;
    private TextView txtNetDaily;
    private TextView txtNegativeEquity;

    private boolean bShowOnline;
    private boolean bShowOffline;
    private boolean bShowMissileSites;
    private boolean bShowSAMSites;
    private boolean bShowSentryGuns;
    private boolean bShowWatchTowers;
    private boolean bShowOreMines;
    private boolean bShowRadarStations;
    private boolean bShowSolarPanels;
    private boolean bShowFarms;
    private boolean bShowCommandPosts;
    private boolean bShowAirbases;
    private boolean bShowArmory;
    private boolean bShowBanks;
    private boolean bShowWarehouses;
    private boolean bShowMissileFactorys;
    private boolean bShowArtilleryGuns;
    private boolean bShowProcessors;
    private boolean bShowDistributors;
    private boolean bShowScrapYards;
    private boolean bShowAircrafts;
    private boolean bShowInfantry;
    private boolean bShowTanks;
    private boolean bShowCargoTrucks;
    private boolean bShowShips;
    private boolean bShowSubmarines;
    private boolean bHasBonuses = false;
    private int lBasicIncome;
    private int lRankIncome;
    private float fltAllianceTaxRate;
    private int lStructureMaintenance;
    private int lExtractorMaintenance;
    private int lAircraftMaintenance;
    private int lHelicopterMaintenance;
    private int lShipMaintenance;
    private int lSubmarineMaintenance;
    private int lICBMMaintenance;
    private int lABMMaintenance;
    private int lMissileMaintenance;
    private int lNukeMaintenance;
    private int lInterceptorsMaintenance;
    private int lTorpedoMaintenance;
    private int lInfantryMaintenance;
    private int lCargoTruckMaintenance;
    private int lTankMaintenance;
    private int lGrossHourly;
    private int lAllianceTax;
    private int lNetHourly;
    private int lNetDaily;

    private Player ourPlayer;
    private Alliance ourAlliance = null;

    public WealthRulesView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity, true);
        ourPlayer = game.GetOurPlayer();

        if(ourPlayer.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
        {
            if(game.GetAlliance(ourPlayer.GetAllianceMemberID()) != null)
                ourAlliance = game.GetAlliance(ourPlayer.GetAllianceMemberID());
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_wealth_rules, this);

        lytHourlyCosts = findViewById(R.id.lytHourlyCosts);
        imgShowOnline = findViewById(R.id.imgShowOnline);
        imgShowOffline = findViewById(R.id.imgShowOffline);
        imgShowMissileSites = findViewById(R.id.imgShowMissileSites);
        imgShowSAMSites = findViewById(R.id.imgShowSAMSites);
        imgShowSentryGuns = findViewById(R.id.imgShowSentryGuns);
        imgShowWatchTowers = findViewById(R.id.imgShowWatchTowers);
        imgShowOreMines = findViewById(R.id.imgShowOreMines);
        imgShowCommandPosts = findViewById(R.id.imgShowCommandPosts);
        imgShowRadarStations = findViewById(R.id.imgShowRadarStations);
        imgShowAirbases = findViewById(R.id.imgShowAirbases);
        imgShowArmory = findViewById(R.id.imgShowArmory);
        imgShowWarehouses = findViewById(R.id.imgShowWarehouses);

        imgShowScrapYards = findViewById(R.id.imgShowScrapYards);
        imgShowProcessors = findViewById(R.id.imgShowProcessors);
        imgShowDistributors = findViewById(R.id.imgShowDistributors);
        imgShowArtilleryGuns = findViewById(R.id.imgShowArtilleryGuns);
        imgShowAircrafts = findViewById(R.id.imgShowAircrafts);
        imgShowTanks = findViewById(R.id.imgShowTanks);
        imgShowInfantry = findViewById(R.id.imgShowInfantries);
        imgShowCargoTrucks = findViewById(R.id.imgShowCargoTrucks);
        imgShowShips = findViewById(R.id.imgShowShips);
        imgShowSubmarines = findViewById(R.id.imgShowSubmarines);

        txtRankPayQuantity = findViewById(R.id.txtRankPayQuantity);
        txtRankPayEach = findViewById(R.id.txtRankPayEach);
        txtRankPay = findViewById(R.id.txtRankPay);
        txtAllianceTaxEach = findViewById(R.id.txtAllianceTaxEach);
        txtAllianceTax = findViewById(R.id.txtAllianceTax);
        txtStructureQuantity = findViewById(R.id.txtStructureQuantity);
        txtStructureCostEach = findViewById(R.id.txtStructureCostEach);
        txtStructureCost = findViewById(R.id.txtStructureCost);
        txtAircraftQuantity = findViewById(R.id.txtAircraftQuantity);
        txtAircraftCost = findViewById(R.id.txtAircraftCost);
        txtInfantryQuantity = findViewById(R.id.txtInfantryQuantity);
        txtInfantryCostEach = findViewById(R.id.txtInfantryCostEach);
        txtInfantryCost = findViewById(R.id.txtInfantryCost);
        txtTankQuantity = findViewById(R.id.txtTankQuantity);
        txtTankCostEach = findViewById(R.id.txtTankCostEach);
        txtTankCost = findViewById(R.id.txtTankCost);
        txtCargoTruckQuantity = findViewById(R.id.txtCargoTruckQuantity);
        txtCargoTruckCostEach = findViewById(R.id.txtCargoTruckCostEach);
        txtCargoTruckCost = findViewById(R.id.txtCargoTruckCost);
        txtShipQuantity = findViewById(R.id.txtShipQuantity);
        txtShipCost = findViewById(R.id.txtShipCost);
        txtSubmarineQuantity = findViewById(R.id.txtSubmarineQuantity);
        txtSubmarineCost = findViewById(R.id.txtSubmarineCost);
        txtICBMQuantity = findViewById(R.id.txtICBMQuantity);
        txtICBMCostEach = findViewById(R.id.txtICBMCostEach);
        txtICBMCost = findViewById(R.id.txtICBMCost);
        txtABMQuantity = findViewById(R.id.txtABMQuantity);
        txtABMCostEach = findViewById(R.id.txtABMCostEach);
        txtABMCost = findViewById(R.id.txtABMCost);
        txtMissileQuantity = findViewById(R.id.txtMissileQuantity);
        txtMissileCostEach = findViewById(R.id.txtMissileCostEach);
        txtMissileCost = findViewById(R.id.txtMissileCost);
        txtInterceptorQuantity = findViewById(R.id.txtInterceptorQuantity);
        txtInterceptorCostEach = findViewById(R.id.txtInterceptorCostEach);
        txtInterceptorCost = findViewById(R.id.txtInterceptorCost);
        txtTorpedoQuantity = findViewById(R.id.txtTorpedoQuantity);
        txtTorpedoCostEach = findViewById(R.id.txtTorpedoCostEach);
        txtTorpedoCost = findViewById(R.id.txtTorpedoCost);
        txtBasicIncomeEach = findViewById(R.id.txtBasicIncomeEach);
        txtBasicIncome = findViewById(R.id.txtBasicIncome);
        txtGrossHourly = findViewById(R.id.txtGrossHourly);
        txtHourlyExpenses = findViewById(R.id.txtHourlyExpenses);
        txtNetHourly = findViewById(R.id.txtNetHourly);
        txtNetDaily = findViewById(R.id.txtNetDaily);
        txtNegativeEquity = findViewById(R.id.txtNegativeEquity);

        //Calculate all the expense and income values.

        lBasicIncome = game.GetHourlyIncome(ourPlayer);
        lRankIncome = game.GetPlayerRankIncome(ourPlayer);

        int lOnlineStructureCount = 0;

        for(Structure structure : new ArrayList<>(ourPlayer.GetStructures()))
        {
            if(structure != null && !structure.GetOffline() && !(structure instanceof OreMine))
            {
                lOnlineStructureCount++;
            }
        }

        lStructureMaintenance = game.GetStructureMaintenanceCost(ourPlayer.GetID());
        lAircraftMaintenance = game.GetAircraftMaintenanceCost(ourPlayer.GetID());
        lShipMaintenance = game.GetShipMaintenanceCost(ourPlayer.GetID());
        lSubmarineMaintenance = game.GetSubmarineMaintenanceCost(ourPlayer.GetID());
        lICBMMaintenance = game.GetICBMMaintenanceCost(ourPlayer.GetID());
        lABMMaintenance = game.GetABMMaintenanceCost(ourPlayer.GetID());
        lMissileMaintenance = game.GetHourlyMissileMaintenance(ourPlayer.GetID());
        lInterceptorsMaintenance = game.GetHourlyInterceptorMaintenance(ourPlayer.GetID());
        lTorpedoMaintenance = game.GetHourlyTorpedoMaintenance(ourPlayer.GetID());
        lInfantryMaintenance = game.GetInfantryMaintenanceCost(ourPlayer.GetID());
        lCargoTruckMaintenance = game.GetCargoTruckMaintenanceCost(ourPlayer.GetID());
        lTankMaintenance = game.GetTankMaintenanceCost(ourPlayer.GetID());
        lGrossHourly = lBasicIncome + lRankIncome;

        if(ourAlliance != null)
        {
            fltAllianceTaxRate = ourAlliance.GetTaxRate();
            lAllianceTax = (int)(lGrossHourly * fltAllianceTaxRate);
            txtAllianceTaxEach.setText(TextUtilities.GetPercentStringFromFraction(fltAllianceTaxRate));
            txtAllianceTax.setText(TextUtilities.GetCurrencyString(lAllianceTax));
            txtAllianceTax.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            txtAllianceTaxEach.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
        else
        {
            fltAllianceTaxRate = 0.0f;
            lAllianceTax = 0;
            txtAllianceTaxEach.setText(context.getText(R.string.not_applicable));
            txtAllianceTax.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            txtAllianceTaxEach.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }

        int lTotalHourlyMaintenance = lAllianceTax + lStructureMaintenance + lExtractorMaintenance + lAircraftMaintenance + lHelicopterMaintenance + lShipMaintenance + lSubmarineMaintenance + lICBMMaintenance + lABMMaintenance + lMissileMaintenance + lInterceptorsMaintenance + lTorpedoMaintenance + lInfantryMaintenance + lCargoTruckMaintenance + lTankMaintenance;

        lNetHourly = lGrossHourly - lTotalHourlyMaintenance;

        lNetDaily = (lNetHourly * Defs.HOURS_PER_DAY)/* + lTaxRevenue*/;

        //Populate the wealth spreadsheet.
        txtBasicIncomeEach.setText(TextUtilities.GetCurrencyString(lBasicIncome));
        txtBasicIncome.setText(TextUtilities.GetCurrencyString(lBasicIncome));
        txtRankPayQuantity.setText(String.valueOf(ourPlayer.GetRank()));
        txtRankPayEach.setText(TextUtilities.GetCurrencyString(Defs.INCOME_PER_RANK));
        txtRankPay.setText(TextUtilities.GetCurrencyString(lRankIncome));

        txtStructureQuantity.setText(String.valueOf(lOnlineStructureCount));
        txtStructureCostEach.setText(TextUtilities.GetCurrencyString(Defs.ONLINE_MAINTENANCE_COST));
        txtStructureCost.setText(TextUtilities.GetCurrencyString(lStructureMaintenance));

        txtAircraftQuantity.setText(String.valueOf(game.GetAircraftCount(ourPlayer.GetID())));
        txtAircraftCost.setText(TextUtilities.GetCurrencyString(lAircraftMaintenance));

        int lInfantryCount = game.GetInfantryCount(ourPlayer.GetID());

        txtInfantryQuantity.setText(String.valueOf(lInfantryCount));
        txtInfantryCostEach.setText(TextUtilities.GetCurrencyString(Defs.INFANTRY_MAINTENANCE_COST));
        txtInfantryCost.setText(TextUtilities.GetCurrencyString(lInfantryCount * Defs.INFANTRY_MAINTENANCE_COST));

        int lTankCount = game.GetTankCount(ourPlayer.GetID());

        txtTankQuantity.setText(String.valueOf(lTankCount));
        txtTankCostEach.setText(TextUtilities.GetCurrencyString(Defs.TANK_MAINTENANCE_COST));
        txtTankCost.setText(TextUtilities.GetCurrencyString(lInfantryCount * Defs.TANK_MAINTENANCE_COST));

        int lCargoTruckCount = 0;

        for(CargoTruck truck : game.GetCargoTrucks())
        {
            if(truck.GetOwnedBy(game.GetOurPlayerID()))
            {
                lCargoTruckCount++;
            }
        }

        txtCargoTruckQuantity.setText(String.valueOf(lCargoTruckCount));
        txtCargoTruckCostEach.setText(TextUtilities.GetCurrencyString(Defs.CARGO_TRUCK_MAINTENANCE_COST));
        txtCargoTruckCost.setText(TextUtilities.GetCurrencyString(lCargoTruckCount * Defs.CARGO_TRUCK_MAINTENANCE_COST));

        txtShipQuantity.setText(String.valueOf(game.GetShipCount(ourPlayer.GetID())));
        txtShipCost.setText(TextUtilities.GetCurrencyString(lShipMaintenance));

        txtSubmarineQuantity.setText(String.valueOf(game.GetSubmarineCount(ourPlayer.GetID())));
        txtSubmarineCost.setText(TextUtilities.GetCurrencyString(lSubmarineMaintenance));

        //ICBMs

        txtICBMQuantity.setText(String.valueOf(game.GetICBMCount(ourPlayer.GetID())));
        txtICBMCostEach.setText(TextUtilities.GetCurrencyString(Defs.MISSILE_ICBM_MAINTENANCE));
        txtICBMCost.setText(TextUtilities.GetCurrencyString(game.GetICBMMaintenanceCost(ourPlayer.GetID())));

        //ABMs

        txtABMQuantity.setText(String.valueOf(game.GetABMCount(ourPlayer.GetID())));
        txtABMCostEach.setText(TextUtilities.GetCurrencyString(Defs.INTERCEPTOR_ABM_MAINTENANCE));
        txtABMCost.setText(TextUtilities.GetCurrencyString(game.GetABMMaintenanceCost(ourPlayer.GetID())));

        //Missiles

        txtMissileQuantity.setText(String.valueOf(game.GetMissileCount(ourPlayer.GetID())));
        txtMissileCostEach.setText(TextUtilities.GetCurrencyString(Defs.MISSILE_MAINTENANCE));
        txtMissileCost.setText(TextUtilities.GetCurrencyString(game.GetHourlyMissileMaintenance(ourPlayer.GetID())));

        //Interceptors

        txtInterceptorQuantity.setText(String.valueOf(game.GetInterceptorCount(ourPlayer.GetID())));
        txtInterceptorCostEach.setText(TextUtilities.GetCurrencyString(Defs.INTERCEPTOR_MAINTENANCE));
        txtInterceptorCost.setText(TextUtilities.GetCurrencyString(game.GetHourlyInterceptorMaintenance(ourPlayer.GetID())));

        //Torpedoes

        txtTorpedoQuantity.setText(String.valueOf(game.GetTorpedoCount(ourPlayer.GetID())));
        txtTorpedoCostEach.setText(TextUtilities.GetCurrencyString(Defs.TORPEDO_MAINTENANCE));
        txtTorpedoCost.setText(TextUtilities.GetCurrencyString(game.GetHourlyTorpedoMaintenance(ourPlayer.GetID())));

        //Gross hourly

        txtGrossHourly.setText(TextUtilities.GetCurrencyString(lGrossHourly));

        if(lGrossHourly >= 0)
        {
            txtGrossHourly.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else
        {
            txtGrossHourly.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }

        txtHourlyExpenses.setText(TextUtilities.GetCurrencyString(lTotalHourlyMaintenance));

        //Net hourly

        txtNetHourly.setText(TextUtilities.GetCurrencyString(lNetHourly));

        if(lNetHourly >= 0)
        {
            txtNetHourly.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else
        {
            txtNetHourly.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }

        //Net daily

        txtNetDaily.setText(TextUtilities.GetCurrencyString(lNetDaily));

        if(lNetDaily >= 0)
        {
            txtNetDaily.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else
        {
            txtNetDaily.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }

        if(lGrossHourly < lTotalHourlyMaintenance)
        {
            txtNegativeEquity.setVisibility(VISIBLE);
        }
        else
        {
            txtNegativeEquity.setVisibility(GONE);
        }

        bShowOnline = true;
        bShowOffline = true;
        bShowMissileSites = true;
        bShowSAMSites = true;
        bShowSentryGuns = true;
        bShowWatchTowers = true;
        bShowOreMines = true;
        bShowRadarStations = true;
        bShowSolarPanels = true;
        bShowFarms = true;
        bShowDistributors = true;
        bShowArtilleryGuns = true;
        bShowProcessors = true;
        bShowScrapYards = true;
        bShowCommandPosts = true;
        bShowAirbases = true;
        bShowArmory = true;
        bShowBanks = true;
        bShowWarehouses = true;
        bShowMissileFactorys = true;
        bShowAircrafts = true;
        bShowTanks = true;
        bShowInfantry = true;
        bShowCargoTrucks = true;
        bShowShips = true;
        bShowSubmarines = true;

        RebuildCostableEntityList();

        //Assign visibility button on click listeners.
        imgShowOnline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowOnline = !bShowOnline;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowOnline.setColorFilter(bShowOnline? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowOffline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowOffline = !bShowOffline;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowOffline.setColorFilter(bShowOffline? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowMissileSites.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowMissileSites = !bShowMissileSites;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowMissileSites.setColorFilter(bShowMissileSites? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowSAMSites.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowSAMSites = !bShowSAMSites;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowSAMSites.setColorFilter(bShowSAMSites? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowSentryGuns.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowSentryGuns = !bShowSentryGuns;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowSentryGuns.setColorFilter(bShowSentryGuns? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowWatchTowers.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowWatchTowers = !bShowWatchTowers;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowWatchTowers.setColorFilter(bShowWatchTowers? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowOreMines.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowOreMines = !bShowOreMines;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowOreMines.setColorFilter(bShowOreMines? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowCommandPosts.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowCommandPosts = !bShowCommandPosts;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowCommandPosts.setColorFilter(bShowCommandPosts? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowRadarStations.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowRadarStations = !bShowRadarStations;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowRadarStations.setColorFilter(bShowRadarStations? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowAirbases.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowAirbases = !bShowAirbases;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowAirbases.setColorFilter(bShowAirbases? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowArmory.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowArmory = !bShowArmory;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowArmory.setColorFilter(bShowArmory? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        /*imgShowBanks.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowBanks = !bShowBanks;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowBanks.setColorFilter(bShowBanks? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });*/

        imgShowWarehouses.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowWarehouses = !bShowWarehouses;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowWarehouses.setColorFilter(bShowWarehouses? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        /*imgShowMissileFactorys.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowMissileFactorys = !bShowMissileFactorys;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowMissileFactorys.setColorFilter(bShowMissileFactorys? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });*/

        imgShowArtilleryGuns.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowArtilleryGuns = !bShowArtilleryGuns;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowArtilleryGuns.setColorFilter(bShowArtilleryGuns? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowDistributors.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowDistributors = !bShowDistributors;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowDistributors.setColorFilter(bShowDistributors? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowScrapYards.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowScrapYards = !bShowScrapYards;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowScrapYards.setColorFilter(bShowScrapYards? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowProcessors.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowProcessors = !bShowProcessors;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowProcessors.setColorFilter(bShowProcessors? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowAircrafts.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowAircrafts = !bShowAircrafts;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowAircrafts.setColorFilter(bShowAircrafts? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowTanks.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowTanks = !bShowTanks;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowTanks.setColorFilter(bShowTanks? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowInfantry.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowInfantry = !bShowInfantry;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowInfantry.setColorFilter(bShowInfantry? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowCargoTrucks.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowCargoTrucks = !bShowCargoTrucks;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowCargoTrucks.setColorFilter(bShowCargoTrucks? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowShips.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowShips = !bShowShips;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowShips.setColorFilter(bShowShips? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        imgShowSubmarines.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowSubmarines = !bShowSubmarines;

                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        imgShowSubmarines.setColorFilter(bShowSubmarines? 0 : LaunchUICommon.COLOUR_TINTED);
                        RebuildCostableEntityList();
                    }
                });
            }
        });

        //Compute income & bonuses.
        Player ourPlayer = game.GetOurPlayer();
        game.GetPlayerStats(ourPlayer);
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(EntityViews != null)
                {
                    for(LaunchView view : EntityViews)
                    {
                        view.Update();
                    }
                }
            }
        });
    }

    /**
     * Rebuild the player wealth statement, and return their hourly generation.
     * @return Player's hourly wealth generation.
     */
    private int RebuildPlayerWealthStatement()
    {
        Player ourPlayer = game.GetOurPlayer();

        final int lHourlyGeneration = game.GetHourlyIncome(ourPlayer);

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //txtWealthStatement.setText(context.getString(R.string.wealth_generating, TextUtilities.GetCurrencyString(lHourlyGeneration)));
            }
        });

        return lHourlyGeneration;
    }

    private void RebuildCostableEntityList()
    {
        int lHourlyGeneration = RebuildPlayerWealthStatement();

        //Compute hourly maintenances.
        List<Structure> OurStructures = game.GetOurStructures();
        List<AirplaneInterface> OurAircraft = game.GetOurAircrafts();
        List<InfantryInterface> OurInfantry = game.GetOurInfantries();
        List<TankInterface> OurTanks = game.GetOurTanks();
        List<CargoTruckInterface> OurCargoTrucks = game.GetOurCargoTrucks();
        List<Ship> OurShips = game.GetOurShips();
        List<Submarine> OurSubmarines = game.GetOurSubmarines();

        int lHourlyCosts = 0;

        if(!OurStructures.isEmpty())
        {
            findViewById(R.id.txtMaintenanceNone).setVisibility(View.GONE);
        }
        else
        {
            lytHourlyCosts.setVisibility(View.GONE);
        }

        if(EntityViews == null)
        {
            EntityViews = new ArrayList<>();
        }
        else
        {
            lytHourlyCosts.removeAllViews();
            EntityViews.clear();
        }

        for(final Structure structure : OurStructures)
        {
            if(ShouldBeVisible(structure))
            {
                StructureMaintenanceView mev = new StructureMaintenanceView(game, activity, structure);
                EntityViews.add(mev);

                mev.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SelectEntity(structure);
                    }
                });

                lytHourlyCosts.addView(mev);
            }

            if(structure.GetOnline() || structure.GetBooting())
            {
                lHourlyCosts += game.GetConfig().GetMaintenanceCost(structure);
            }
            else if(structure.GetOffline())
            {
                lHourlyCosts += 5;
            }
        }

        for(final AirplaneInterface aircraft : OurAircraft)
        {
            if(aircraft != null && aircraft.GetAirplane() != null)
            {
                if(ShouldBeVisible(aircraft.GetAirplane()))
                {
                    AircraftMaintenanceView mev = new AircraftMaintenanceView(game, activity, aircraft);
                    EntityViews.add(mev);

                    mev.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SelectLaunchEntity(aircraft.GetAirplane());
                        }
                    });

                    lytHourlyCosts.addView(mev);
                }

                if(aircraft.Flying())
                {
                    lHourlyCosts += Defs.GetAircraftMaintenanceCost(aircraft.GetAircraftType());
                }
            }
        }

        for(final InfantryInterface infantry : OurInfantry)
        {
            if(ShouldBeVisible(infantry.GetInfantry()))
            {
                InfantryMaintenanceView mev = new InfantryMaintenanceView(game, activity, infantry);
                EntityViews.add(mev);

                mev.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SelectLaunchEntity(infantry.GetInfantry());
                    }
                });

                lytHourlyCosts.addView(mev);
            }
        }

        for(final TankInterface tank : OurTanks)
        {
            if(ShouldBeVisible(tank.GetTank()))
            {
                TankMaintenanceView mev = new TankMaintenanceView(game, activity, tank);
                EntityViews.add(mev);

                mev.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SelectLaunchEntity(tank.GetTank());
                    }
                });

                lytHourlyCosts.addView(mev);
            }
        }

        for(final CargoTruckInterface truck : OurCargoTrucks)
        {
            if(ShouldBeVisible(truck.GetCargoTruck()))
            {
                CargoTruckMaintenanceView mev = new CargoTruckMaintenanceView(game, activity, truck);
                EntityViews.add(mev);

                mev.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SelectLaunchEntity(truck.GetCargoTruck());
                    }
                });

                lytHourlyCosts.addView(mev);
            }
        }

        for(final Ship ship : OurShips)
        {
            if(ShouldBeVisible(ship))
            {
                ShipMaintenanceView mev = new ShipMaintenanceView(game, activity, ship);
                EntityViews.add(mev);

                mev.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SelectLaunchEntity(ship);
                    }
                });

                lytHourlyCosts.addView(mev);
            }

            if(game.ShipInPort(ship))
            {
                lHourlyCosts += Defs.GetNavalMaintenanceCost(ship.GetEntityType());
            }
        }

        for(final Submarine submarine : OurSubmarines)
        {
            if(ShouldBeVisible(submarine))
            {
                SubmarineMaintenanceView mev = new SubmarineMaintenanceView(game, activity, submarine);
                EntityViews.add(mev);

                mev.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        activity.SelectLaunchEntity(submarine);
                    }
                });

                lytHourlyCosts.addView(mev);
            }

            if(game.ShipInPort(submarine))
            {
                lHourlyCosts += Defs.GetNavalMaintenanceCost(submarine.GetEntityType());
            }
        }
    }

    private boolean ShouldBeVisible(LaunchEntity entity)
    {
        if(!bShowOffline && entity instanceof Structure && ((Structure)entity).GetOffline())
            return false;

        if(!bShowOnline && entity instanceof Structure && !((Structure)entity).GetOffline()) //Deliberately negative offline; all other modes should show under "online".
            return false;

        if(!bShowMissileSites && entity instanceof MissileSite)
            return false;

        if(!bShowSAMSites && entity instanceof SAMSite)
            return false;

        if(!bShowSentryGuns && entity instanceof SentryGun && !((SentryGun)entity).GetIsWatchTower())
            return false;

        if(!bShowWatchTowers && entity instanceof SentryGun && ((SentryGun)entity).GetIsWatchTower())
            return false;

        if(!bShowOreMines && entity instanceof OreMine)
            return false;

        if(!bShowRadarStations && entity instanceof RadarStation)
            return false;

        if(!bShowCommandPosts && entity instanceof CommandPost)
            return false;

        if(!bShowAirbases && entity instanceof Airbase)
            return false;

        if(!bShowArmory && entity instanceof Armory)
            return false;

        if(!bShowBanks && entity instanceof Bank)
            return false;

        if(!bShowWarehouses && entity instanceof Warehouse)
            return false;

        if(!bShowMissileFactorys && entity instanceof MissileFactory)
            return false;

        if(!bShowArtilleryGuns && entity instanceof ArtilleryGun)
            return false;

        if(!bShowDistributors && entity instanceof Distributor)
            return false;

        if(!bShowProcessors && entity instanceof Processor)
            return false;

        if(!bShowScrapYards && entity instanceof ScrapYard)
            return false;

        if(!bShowAircrafts && entity instanceof AirplaneInterface)
            return false;

        if(!bShowInfantry && entity instanceof InfantryInterface)
            return false;

        if(!bShowTanks && entity instanceof TankInterface)
            return false;

        if(!bShowCargoTrucks && entity instanceof CargoTruckInterface)
            return false;

        if(!bShowShips && entity instanceof Ship)
            return false;

        if(!bShowSubmarines && entity instanceof Submarine)
            return false;

        return true;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity instanceof Player)
        {
            if(entity.GetID() == game.GetOurPlayerID())
            {
                if(!bHasBonuses)
                {
                    if(((Player)entity).GetHasFullStats())
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //txtGettingStats.setVisibility(GONE);

                                final Config config = game.GetConfig();
                                final Player ourPlayer = game.GetOurPlayer();

                                int lActivePlayers = 0;
                                int lFriends = 0;
                                int lEnemies = 0;
                                float fltNearestPlayerDistance = Float.MAX_VALUE;
                                Player nearestPlayer = null;

                                for(Player player : game.GetPlayers())
                                {
                                    if(!player.GetAWOL())
                                    {
                                        lActivePlayers++;

                                        switch(game.GetAllegiance(player, ourPlayer))
                                        {
                                            case YOU:
                                            case ALLY:
                                            case AFFILIATE:
                                                lFriends++;
                                                break;

                                            case ENEMY:
                                                lEnemies++;
                                                break;
                                        }

                                        if(!player.ApparentlyEquals(ourPlayer))
                                        {
                                            float fltDistance = ourPlayer.GetPosition().DistanceTo(player.GetPosition());

                                            if(fltDistance < fltNearestPlayerDistance)
                                            {
                                                fltNearestPlayerDistance = fltDistance;
                                                nearestPlayer = player;
                                            }
                                        }
                                    }
                                }

                                //TODO: Collected yesterday
                                //txtCollectedYesterday.setText(TextUtilities.GetCurrencyString(ourPlayer.GetWealthYesterday()));
                                //txtCollectedYesterday.setVisibility(VISIBLE);
                                /*txtBasicIncomeVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyWealth(ourPlayer)));
                                if(game.GetBasicIncomeEligible(ourPlayer))
                                {
                                    lytBonusBasicIncome.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtBasicIncomeVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusBasicIncome.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtBasicIncomeVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusBasicIncome.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_basic_income_description));
                                    }
                                });

                                //txtDiplomaticPresenceVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusDiplomaticPresence()));
                                if(game.GetDiplomaticPresenceEligible(ourPlayer))
                                {
                                    lytBonusDiplomaticPresence.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtDiplomaticPresenceVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusDiplomaticPresence.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtDiplomaticPresenceVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusDiplomaticPresence.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_diplomatic_presence_description));
                                    }
                                });

                                //txtPoliticalEngagementVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusPoliticalEngagement()));
                                if(game.GetPoliticalEngagementEligible(ourPlayer))
                                {
                                    lytBonusPoliticalEngagement.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtPoliticalEngagementVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusPoliticalEngagement.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtPoliticalEngagementVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusPoliticalEngagement.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_political_engagement_description));
                                    }
                                });

                                //txtDefenderOfTheNationVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusDefenderOfTheNation()));
                                if(game.GetDefenderOfTheNationEligible(ourPlayer))
                                {
                                    lytBonusDefenderOfTheNation.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtDefenderOfTheNationVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusDefenderOfTheNation.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtDefenderOfTheNationVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusDefenderOfTheNation.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_defender_of_the_nation_description));
                                    }
                                });

                                //txtNuclearSuperpowerVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusNuclearSuperpower()));
                                if(game.GetNuclearSuperpowerEligible(ourPlayer))
                                {
                                    lytBonusNuclearSuperpower.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtNuclearSuperpowerVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusNuclearSuperpower.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtNuclearSuperpowerVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusNuclearSuperpower.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_nuclear_superpower_description));
                                    }
                                });

                                //txtSurvivorVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusSurvivor()));
                                if(game.GetSurvivorEligible(ourPlayer))
                                {
                                    lytBonusSurvivor.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtSurvivorVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusSurvivor.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtSurvivorVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusSurvivor.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_survivor_description));
                                    }
                                });

                                //txtHippyVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusHippy()));
                                if(game.GetHippyEligible(ourPlayer))
                                {
                                    lytBonusHippy.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                    txtHippyVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                }
                                else
                                {
                                    lytBonusHippy.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                    txtHippyVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                }

                                lytBonusHippy.setOnClickListener(new OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        activity.ShowBasicOKDialog(context.getString(R.string.bonus_hippy_description));
                                    }
                                });

                                if(lActivePlayers > 0)
                                {
                                    //txtPeaceMakerVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusPeaceMaker()));
                                    final float fltFriends = (float)lFriends / (float)lActivePlayers;

                                    if((fltFriends > Defs.RELATIONSHIP_BONUS_THRESHOLD))
                                    {
                                        lytBonusPeaceMaker.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                        txtPeaceMakerVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                    }
                                    else
                                    {
                                        lytBonusPeaceMaker.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                        txtPeaceMakerVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                    }

                                    lytBonusPeaceMaker.setOnClickListener(new OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            activity.ShowBasicOKDialog(context.getString(R.string.bonus_peace_maker_description, TextUtilities.GetPercentStringFromFraction(Defs.RELATIONSHIP_BONUS_THRESHOLD), TextUtilities.GetPercentStringFromFraction(fltFriends)));
                                        }
                                    });

                                    //txtWarMongerVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusWarMonger()));
                                    final float fltEnemies = (float)lEnemies / (float)lActivePlayers;

                                    if((fltEnemies > Defs.RELATIONSHIP_BONUS_THRESHOLD))
                                    {
                                        lytBonusWarMonger.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                        txtWarMongerVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                    }
                                    else
                                    {
                                        lytBonusWarMonger.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                        txtWarMongerVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                    }

                                    lytBonusWarMonger.setOnClickListener(new OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            activity.ShowBasicOKDialog(context.getString(R.string.bonus_war_monger_description, TextUtilities.GetPercentStringFromFraction(Defs.RELATIONSHIP_BONUS_THRESHOLD), TextUtilities.GetPercentStringFromFraction(fltEnemies)));
                                        }
                                    });

                                    //txtLoneWolfVal.setText(TextUtilities.GetCurrencyString(config.GetHourlyBonusLoneWolf()));
                                    if(false /*nearestPlayer.GetPosition().DistanceTo(ourPlayer.GetPosition()) > config.GetLoneWolfDistance())
                                    {
                                        lytBonusLoneWolf.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableNormal));
                                        txtLoneWolfVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                                    }
                                    else
                                    {
                                        lytBonusLoneWolf.setBackground(Utilities.DrawableFromAttr(context, R.attr.DetailButtonDrawableDisabled));
                                        txtLoneWolfVal.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                                    }

                                    final Player finalNearestPlayer = nearestPlayer;
                                    final float finalFltNearestPlayerDistance = fltNearestPlayerDistance;
                                    lytBonusLoneWolf.setOnClickListener(new OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            //activity.ShowBasicOKDialog(context.getString(R.string.bonus_lone_wolf_description, TextUtilities.GetDistanceStringFromKM(config.GetLoneWolfDistance()), finalNearestPlayer.GetName(), TextUtilities.GetDistanceStringFromKM(finalFltNearestPlayerDistance)));
                                        }
                                    });

                                    lytBonusPeaceMaker.setVisibility(VISIBLE);
                                    lytBonusWarMonger.setVisibility(VISIBLE);
                                    lytBonusLoneWolf.setVisibility(VISIBLE);
                                }

                                lytBonusBasicIncome.setVisibility(VISIBLE);
                                lytBonusDiplomaticPresence.setVisibility(VISIBLE);
                                lytBonusPoliticalEngagement.setVisibility(VISIBLE);
                                lytBonusDefenderOfTheNation.setVisibility(VISIBLE);
                                lytBonusNuclearSuperpower.setVisibility(VISIBLE);
                                lytBonusSurvivor.setVisibility(VISIBLE);
                                lytBonusHippy.setVisibility(VISIBLE);*/
                            }
                        });

                        bHasBonuses = true;
                    }
                }

                RebuildPlayerWealthStatement();
            }
        }

        if(entity instanceof Structure)
        {
            if(((Structure)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final StructureMaintenanceView structureMaintenanceView : GetStructureViews())
                {
                    if(entity.ApparentlyEquals(structureMaintenanceView.GetCurrentStructure()))
                    {
                        if(!ShouldBeVisible((Structure)entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(structureMaintenanceView);
                                    lytHourlyCosts.removeView(structureMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(entity instanceof AirplaneInterface)
        {
            if(((AirplaneInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final AircraftMaintenanceView aircraftMaintenanceView : GetAircraftViews())
                {
                    if(entity.ApparentlyEquals((LaunchEntity)aircraftMaintenanceView.GetCurrentAircraft()))
                    {
                        if(!ShouldBeVisible(entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(aircraftMaintenanceView);
                                    lytHourlyCosts.removeView(aircraftMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(entity instanceof InfantryInterface)
        {
            if(((InfantryInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final InfantryMaintenanceView infantryMaintenanceView : GetInfantryViews())
                {
                    if(entity.ApparentlyEquals((LaunchEntity)infantryMaintenanceView.GetCurrentInfantry()))
                    {
                        if(!ShouldBeVisible(entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(infantryMaintenanceView);
                                    lytHourlyCosts.removeView(infantryMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(entity instanceof TankInterface)
        {
            if(((TankInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final TankMaintenanceView tankMaintenanceView : GetTankViews())
                {
                    if(entity.ApparentlyEquals(tankMaintenanceView.GetCurrentTank()))
                    {
                        if(!ShouldBeVisible(entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(tankMaintenanceView);
                                    lytHourlyCosts.removeView(tankMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(entity instanceof CargoTruckInterface)
        {
            if(((CargoTruckInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final CargoTruckMaintenanceView cargoTruckMaintenanceView : GetCargoTruckViews())
                {
                    if(entity.ApparentlyEquals((LaunchEntity)cargoTruckMaintenanceView.GetCurrentCargoTruck()))
                    {
                        if(!ShouldBeVisible(entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(cargoTruckMaintenanceView);
                                    lytHourlyCosts.removeView(cargoTruckMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(entity instanceof Ship)
        {
            if(((Ship)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final ShipMaintenanceView shipMaintenanceView : GetShipViews())
                {
                    if(entity.ApparentlyEquals(shipMaintenanceView.GetCurrentShip()))
                    {
                        if(!ShouldBeVisible(entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(shipMaintenanceView);
                                    lytHourlyCosts.removeView(shipMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }

        if(entity instanceof Submarine)
        {
            if(((Submarine)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final SubmarineMaintenanceView submarineMaintenanceView : GetSubmarineViews())
                {
                    if(entity.ApparentlyEquals(submarineMaintenanceView.GetCurrentSubmarine()))
                    {
                        if(!ShouldBeVisible(entity))
                        {
                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    EntityViews.remove(submarineMaintenanceView);
                                    lytHourlyCosts.removeView(submarineMaintenanceView);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    public void EntityRemoved(LaunchEntity entity)
    {
        if(entity instanceof Structure)
        {
            if(((Structure)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final StructureMaintenanceView structureMaintenanceView : GetStructureViews())
                {
                    if(entity.ApparentlyEquals(structureMaintenanceView.GetCurrentStructure()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(structureMaintenanceView);
                                lytHourlyCosts.removeView(structureMaintenanceView);
                            }
                        });
                    }
                }
            }
        }

        if(entity instanceof AirplaneInterface)
        {
            if(((AirplaneInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final AircraftMaintenanceView aircraftMaintenanceView : GetAircraftViews())
                {
                    if(entity.ApparentlyEquals((LaunchEntity)aircraftMaintenanceView.GetCurrentAircraft()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(aircraftMaintenanceView);
                                lytHourlyCosts.removeView(aircraftMaintenanceView);
                            }
                        });
                    }
                }
            }
        }

        if(entity instanceof InfantryInterface)
        {
            if(((InfantryInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final InfantryMaintenanceView infantryMaintenanceView : GetInfantryViews())
                {
                    if(entity.ApparentlyEquals((LaunchEntity)infantryMaintenanceView.GetCurrentInfantry()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(infantryMaintenanceView);
                                lytHourlyCosts.removeView(infantryMaintenanceView);
                            }
                        });
                    }
                }
            }
        }

        if(entity instanceof TankInterface)
        {
            if(((TankInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final TankMaintenanceView tankMaintenanceView : GetTankViews())
                {
                    if(entity.ApparentlyEquals(tankMaintenanceView.GetCurrentTank()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(tankMaintenanceView);
                                lytHourlyCosts.removeView(tankMaintenanceView);
                            }
                        });
                    }
                }
            }
        }

        if(entity instanceof CargoTruckInterface)
        {
            if(((CargoTruckInterface)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final CargoTruckMaintenanceView cargoTruckMaintenanceView : GetCargoTruckViews())
                {
                    if(entity.ApparentlyEquals((LaunchEntity)cargoTruckMaintenanceView.GetCurrentCargoTruck()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(cargoTruckMaintenanceView);
                                lytHourlyCosts.removeView(cargoTruckMaintenanceView);
                            }
                        });
                    }
                }
            }
        }

        if(entity instanceof Ship)
        {
            if(((Ship)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final ShipMaintenanceView shipMaintenanceView : GetShipViews())
                {
                    if(entity.ApparentlyEquals(shipMaintenanceView.GetCurrentShip()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(shipMaintenanceView);
                                lytHourlyCosts.removeView(shipMaintenanceView);
                            }
                        });
                    }
                }
            }
        }

        if(entity instanceof Submarine)
        {
            if(((Submarine)entity).GetOwnerID() == game.GetOurPlayerID())
            {
                for(final SubmarineMaintenanceView submarineMaintenanceView : GetSubmarineViews())
                {
                    if(entity.ApparentlyEquals(submarineMaintenanceView.GetCurrentSubmarine()))
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                EntityViews.remove(submarineMaintenanceView);
                                lytHourlyCosts.removeView(submarineMaintenanceView);
                            }
                        });
                    }
                }
            }
        }
    }

    public List<StructureMaintenanceView> GetStructureViews()
    {
        List<StructureMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof StructureMaintenanceView)
                Result.add((StructureMaintenanceView)view);
        }

        return Result;
    }

    public List<AircraftMaintenanceView> GetAircraftViews()
    {
        List<AircraftMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof AircraftMaintenanceView)
                Result.add((AircraftMaintenanceView)view);
        }

        return Result;
    }

    public List<InfantryMaintenanceView> GetInfantryViews()
    {
        List<InfantryMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof InfantryMaintenanceView)
                Result.add((InfantryMaintenanceView)view);
        }

        return Result;
    }

    public List<TankMaintenanceView> GetTankViews()
    {
        List<TankMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof TankMaintenanceView)
                Result.add((TankMaintenanceView)view);
        }

        return Result;
    }

    public List<CargoTruckMaintenanceView> GetCargoTruckViews()
    {
        List<CargoTruckMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof CargoTruckMaintenanceView)
                Result.add((CargoTruckMaintenanceView)view);
        }

        return Result;
    }

    public List<ShipMaintenanceView> GetShipViews()
    {
        List<ShipMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof ShipMaintenanceView)
                Result.add((ShipMaintenanceView)view);
        }

        return Result;
    }

    public List<SubmarineMaintenanceView> GetSubmarineViews()
    {
        List<SubmarineMaintenanceView> Result = new ArrayList<>();

        for(LaunchView view : new ArrayList<>(EntityViews))
        {
            if(view instanceof SubmarineMaintenanceView)
                Result.add((SubmarineMaintenanceView)view);
        }

        return Result;
    }
}
