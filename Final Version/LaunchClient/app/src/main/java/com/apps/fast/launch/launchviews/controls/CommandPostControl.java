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
    private ScrollView lytProcessors;
    private ScrollView lytDistributors;
    private PurchaseButton btnBuildMissileLauncher;
    private PurchaseButton btnBuildNukeLauncher;
    private PurchaseButton btnBuildAirbase;
    private PurchaseButton btnBuildArmory;
    private PurchaseButton btnBuildBarracks;
    private PurchaseButton btnBuildSAM;
    private PurchaseButton btnBuildSentryGun;
    private PurchaseButton btnBuildWatchTower;
    private PurchaseButton btnBuildOreMine;
    private PurchaseButton btnBuildWarehouse;
    private PurchaseButton btnBuildRadarStation;
    private PurchaseButton btnBuildArtilleryGun;
    private PurchaseButton btnBuildABMSilo;
    private PurchaseButton btnBuildScrapYard;
    private LinearLayout btnBuildProcessor;
    private TextView txtDescWarehouse;
    private PurchaseButton btnBuildGranary;
    private PurchaseButton btnBuildFoundry;
    private PurchaseButton btnBuildOilRefinery;
    private PurchaseButton btnBuildConstructionYard;
    private PurchaseButton btnBuildPowerPlant;
    private PurchaseButton btnBuildNuclearPowerPlant;
    private PurchaseButton btnBuildEnrichmentFacility;
    private PurchaseButton btnBuildLaboratory;
    private PurchaseButton btnBuildMachineShop;
    private PurchaseButton btnBuildLithographyPlant;
    private TextView btnCancel;
    private PurchaseButton btnIron;
    private PurchaseButton btnCoal;
    private PurchaseButton btnOil;
    private PurchaseButton btnCrops;
    private PurchaseButton btnUranium;
    private PurchaseButton btnElectricity;
    private PurchaseButton btnConcrete;
    private PurchaseButton btnLumber;
    private PurchaseButton btnFood;
    private PurchaseButton btnFuel;
    private PurchaseButton btnSteel;
    private PurchaseButton btnConstructionSupplies;
    private PurchaseButton btnMachinery;
    private PurchaseButton btnElectronics;
    private PurchaseButton btnMedicine;
    private PurchaseButton btnFissile;
    private LinearLayout btnBuildDistributor;
    private TextView btnCancelDistributors;
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
        lytProcessors = findViewById(R.id.lytProcessors);
        btnBuildMissileLauncher = findViewById(R.id.btnBuildMissileLauncher);
        btnBuildNukeLauncher = findViewById(R.id.btnBuildNukeLauncher);
        btnBuildAirbase = findViewById(R.id.btnBuildAirbase);
        btnBuildArmory = findViewById(R.id.btnBuildArmory);
        btnBuildBarracks = findViewById(R.id.btnBuildBarracks);
        btnBuildSAM = findViewById(R.id.btnBuildSAM);
        btnBuildSentryGun = findViewById(R.id.btnBuildSentryGun);
        btnBuildWatchTower = findViewById(R.id.btnBuildWatchTower);
        btnBuildOreMine = findViewById(R.id.btnBuildOreMine);
        btnBuildWarehouse = findViewById(R.id.btnBuildWarehouse);
        btnBuildRadarStation = findViewById(R.id.btnBuildRadarStation);
        btnBuildArtilleryGun = findViewById(R.id.btnBuildArtilleryGun);
        btnBuildABMSilo = findViewById(R.id.btnBuildABMSilo);
        btnBuildProcessor = findViewById(R.id.btnBuildProcessor);
        btnBuildScrapYard = findViewById(R.id.btnBuildScrapYard);
        txtDescWarehouse = findViewById(R.id.txtDescWarehouse);

        btnBuildGranary = findViewById(R.id.btnBuildGranary);
        btnBuildFoundry = findViewById(R.id.btnBuildFoundry);
        btnBuildOilRefinery = findViewById(R.id.btnBuildOilRefinery);
        btnBuildConstructionYard = findViewById(R.id.btnBuildConstructionYard);
        btnBuildPowerPlant = findViewById(R.id.btnBuildPowerPlant);
        btnBuildNuclearPowerPlant = findViewById(R.id.btnBuildNuclearPowerPlant);
        btnBuildEnrichmentFacility = findViewById(R.id.btnBuildEnrichmentFacility);
        btnBuildLaboratory = findViewById(R.id.btnBuildLaboratory);
        btnBuildMachineShop = findViewById(R.id.btnBuildMachineShop);
        btnBuildLithographyPlant = findViewById(R.id.btnBuildLithographyPlant);

        btnIron = findViewById(R.id.btnIron);
        btnCoal = findViewById(R.id.btnCoal);
        btnOil = findViewById(R.id.btnOil);
        btnCrops = findViewById(R.id.btnCrops);
        btnUranium = findViewById(R.id.btnUranium);
        btnElectricity = findViewById(R.id.btnElectricity);
        btnConcrete = findViewById(R.id.btnConcrete);
        btnLumber = findViewById(R.id.btnLumber);
        btnFood = findViewById(R.id.btnFood);
        btnFuel = findViewById(R.id.btnFuel);
        btnSteel = findViewById(R.id.btnSteel);
        btnConstructionSupplies = findViewById(R.id.btnConstructionSupplies);
        btnMachinery = findViewById(R.id.btnMachinery);
        btnElectronics = findViewById(R.id.btnElectronics);
        btnMedicine = findViewById(R.id.btnMedicine);
        btnFissile = findViewById(R.id.btnFissile);
        btnBuildDistributor = findViewById(R.id.btnBuildDistributor);
        lytDistributors = findViewById(R.id.lytDistributors);
        btnCancelDistributors = findViewById(R.id.btnCancelDistributors);

        btnCancel = findViewById(R.id.btnCancel);

        if(bOurStructure)
        {
            txtDescWarehouse.setText(context.getString(R.string.desc_warehouse));

            btnBuildProcessor.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    lytProcessors.setVisibility(VISIBLE);
                    lytStructures.setVisibility(GONE);
                }
            });

            btnCancel.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    lytProcessors.setVisibility(GONE);
                    lytStructures.setVisibility(VISIBLE);
                }
            });

            //Structures generally

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
            btnBuildRadarStation.SetUnit(game, activity, post.GetPointer(), EntityType.RADAR_STATION, ResourceType.FOOD, Defs.RADAR_STATION_STRUCTURE_COST);
            btnBuildOreMine.SetUnit(game, activity, post.GetPointer(), EntityType.ORE_MINE, ResourceType.FOOD, Defs.WAREHOUSE_STRUCTURE_COST);
            btnBuildWarehouse.SetUnit(game, activity, post.GetPointer(), EntityType.WAREHOUSE, ResourceType.FOOD, Defs.WAREHOUSE_STRUCTURE_COST);
            btnBuildScrapYard.SetUnit(game, activity, post.GetPointer(), EntityType.SCRAP_YARD, ResourceType.FOOD, Defs.SCRAPYARD_STRUCTURE_COST);

            //Processors

            btnBuildGranary.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.FOOD, Defs.RESOURCE_COST_GRANARY);
            btnBuildFoundry.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.STEEL, Defs.RESOURCE_COST_FOUNDRY);
            btnBuildOilRefinery.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.FUEL, Defs.RESOURCE_COST_OIL_REFINERY);
            btnBuildPowerPlant.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.ELECTRICITY, Defs.RESOURCE_COST_POWER_PLANT);
            btnBuildNuclearPowerPlant.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.NUCLEAR_ELECTRICITY, Defs.RESOURCE_COST_NUCLEAR_POWER_PLANT);
            btnBuildEnrichmentFacility.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.ENRICHED_URANIUM, Defs.RESOURCE_COST_ENRICHMENT_FACILITY);
            btnBuildConstructionYard.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.CONSTRUCTION_SUPPLIES, Defs.RESOURCE_COST_CONSTRUCTION_YARD);
            btnBuildMachineShop.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.MACHINERY, Defs.RESOURCE_COST_MACHINE_SHOP);
            btnBuildLaboratory.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.MEDICINE, Defs.RESOURCE_COST_LABORATORY);
            btnBuildLithographyPlant.SetUnit(game, activity, post.GetPointer(), EntityType.PROCESSOR, ResourceType.ELECTRONICS, Defs.RESOURCE_COST_LITHOGRAPHY_PLANT);

            btnIron.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.IRON, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnCoal.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.COAL, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnOil.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.OIL, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnCrops.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.CROPS, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnUranium.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.URANIUM, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnElectricity.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.ELECTRICITY, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnConcrete.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.CONCRETE, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnLumber.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.LUMBER, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnFood.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.FOOD, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnFuel.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.FUEL, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnSteel.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.STEEL, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnConstructionSupplies.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.CONSTRUCTION_SUPPLIES, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnMachinery.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.MACHINERY, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnElectronics.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.ELECTRONICS, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnMedicine.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.MEDICINE, Defs.DISTRIBUTOR_STRUCTURE_COST);
            btnFissile.SetUnit(game, activity, post.GetPointer(), EntityType.DISTRIBUTOR, ResourceType.ENRICHED_URANIUM, Defs.DISTRIBUTOR_STRUCTURE_COST);

            btnCancelDistributors.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    lytDistributors.setVisibility(GONE);
                    lytStructures.setVisibility(VISIBLE);
                }
            });

            btnBuildDistributor.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    lytDistributors.setVisibility(VISIBLE);
                    lytStructures.setVisibility(GONE);
                }
            });
        }
        else
        {
            lytStructures.setVisibility(GONE);
            lytProcessors.setVisibility(GONE);
            lytDistributors.setVisibility(GONE);
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
