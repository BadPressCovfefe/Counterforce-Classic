package com.apps.fast.launch.launchviews;

import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.DistancedEntityView;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import launch.game.Config;
import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.entities.Processor;
import launch.game.entities.Structure;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem;

public class BuildProcessorView extends LaunchView
{
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
    boolean bTooCloseToStructures;

    public BuildProcessorView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_build_processor, this);

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
        btnCancel = findViewById(R.id.btnCancel);

        bTooCloseToStructures = !game.GetNearbyStructures(game.GetOurPlayer()).isEmpty();

        btnBuildGranary.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.FOOD, Defs.RESOURCE_COST_GRANARY);
        btnBuildFoundry.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.STEEL, Defs.RESOURCE_COST_FOUNDRY);
        btnBuildOilRefinery.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.FUEL, Defs.RESOURCE_COST_OIL_REFINERY);
        btnBuildPowerPlant.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.ELECTRICITY, Defs.RESOURCE_COST_POWER_PLANT);
        btnBuildNuclearPowerPlant.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.NUCLEAR_ELECTRICITY, Defs.RESOURCE_COST_NUCLEAR_POWER_PLANT);
        btnBuildEnrichmentFacility.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.ENRICHED_URANIUM, Defs.RESOURCE_COST_ENRICHMENT_FACILITY);
        btnBuildConstructionYard.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.CONSTRUCTION_SUPPLIES, Defs.RESOURCE_COST_CONSTRUCTION_YARD);
        btnBuildMachineShop.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.MACHINERY, Defs.RESOURCE_COST_MACHINE_SHOP);
        btnBuildLaboratory.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.MEDICINE, Defs.RESOURCE_COST_LABORATORY);
        btnBuildLithographyPlant.SetUnit(game, activity, null, EntityType.PROCESSOR, ResourceType.ELECTRONICS, Defs.RESOURCE_COST_LITHOGRAPHY_PLANT);

        btnBuildGranary.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.FOOD);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildFoundry.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.STEEL);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildOilRefinery.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.FUEL);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildPowerPlant.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.ELECTRICITY);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildNuclearPowerPlant.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.NUCLEAR_ELECTRICITY);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildConstructionYard.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.CONSTRUCTION_SUPPLIES);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildEnrichmentFacility.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.ENRICHED_URANIUM);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildLaboratory.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.MEDICINE);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildMachineShop.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.MACHINERY);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnBuildLithographyPlant.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= game.GetConfig().GetBlueprintCost())
                {
                    activity.PlaceBlueprintMode(EntityType.PROCESSOR, ResourceType.ELECTRONICS);
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
                }

                return true;
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new BuildViewNew(game, activity));
            }
        });
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                CargoSystem system = game.GetOurPlayer().GetCargoSystem();

                if(!game.GetNearbyStructures(game.GetOurPlayer()).isEmpty())
                {
                    bTooCloseToStructures = true;

                    btnBuildGranary.setAlpha(0.5f);
                    btnBuildFoundry.setAlpha(0.5f);
                    btnBuildOilRefinery.setAlpha(0.5f);
                    btnBuildConstructionYard.setAlpha(0.5f);
                    btnBuildPowerPlant.setAlpha(0.5f);
                    btnBuildNuclearPowerPlant.setAlpha(0.5f);
                    btnBuildEnrichmentFacility.setAlpha(0.5f);
                    btnBuildLaboratory.setAlpha(0.5f);
                    btnBuildMachineShop.setAlpha(0.5f);
                    btnBuildLithographyPlant.setAlpha(0.5f);
                }
                else
                {
                    bTooCloseToStructures = false;

                    btnBuildGranary.setAlpha(1.0f);
                    btnBuildFoundry.setAlpha(1.0f);
                    btnBuildOilRefinery.setAlpha(1.0f);
                    btnBuildConstructionYard.setAlpha(1.0f);
                    btnBuildPowerPlant.setAlpha(1.0f);
                    btnBuildNuclearPowerPlant.setAlpha(1.0f);
                    btnBuildEnrichmentFacility.setAlpha(1.0f);
                    btnBuildLaboratory.setAlpha(1.0f);
                    btnBuildMachineShop.setAlpha(1.0f);
                    btnBuildLithographyPlant.setAlpha(1.0f);
                }
            }
        });
    }
}
