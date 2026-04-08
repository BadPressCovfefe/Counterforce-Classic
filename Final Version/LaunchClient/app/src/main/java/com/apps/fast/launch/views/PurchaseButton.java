package com.apps.fast.launch.views;

import static android.view.Gravity.CENTER_VERTICAL;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.Armory;
import launch.game.entities.CommandPost;
import launch.game.entities.LandUnit;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Shipyard;
import launch.game.entities.Structure;
import launch.game.entities.Warehouse;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem;
import launch.game.systems.ResourceSystem;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PurchaseButton extends LinearLayout
{
    private ImageView unitIcon;
    private ImageView btnShowSubstitutes;
    private LinearLayout costContainer;
    private EntityType entityType;
    private ResourceType resourceType;
    private boolean bRespectStructureSeparation = false;
    private boolean bTooCloseToStructures;
    private boolean bHostOffline;
    private Context context;
    private MainActivity activity;
    private LaunchClientGame game;
    private EntityPointer pointerOrigin;
    private Map<ResourceType, Long> Costs;
    private Map<ResourceType, Long> requiredCosts;
    private Map<ResourceType, Long> substituteCosts;
    private CargoSystem cargoToCheck = null;
    private ResourceSystem resourcesToCheck = null;
    private boolean bShowingRequirements = true;

    public PurchaseButton(Context context)
    {
        super(context);
        this.context = context;

        init(context);
    }

    public PurchaseButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public PurchaseButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    private void init(Context context)
    {
        LayoutInflater.from(context).inflate(R.layout.purchase_button, this, true);
        setClickable(true);
        setFocusable(true);

        unitIcon = findViewById(R.id.unitIcon);
        costContainer = findViewById(R.id.costContainer);
        btnShowSubstitutes = findViewById(R.id.btnShowSubstitutes);

        btnShowSubstitutes.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bShowingRequirements = !bShowingRequirements;
                DrawCosts();
            }
        });

        super.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                HandlePurchase();
            }
        });
    }

    public void SetUnit(LaunchClientGame game, MainActivity activity, EntityPointer pointerOrigin, EntityType entityType, ResourceType resourceType, Map<ResourceType, Long> Costs)
    {
        this.entityType = entityType;
        this.resourceType = resourceType;
        this.game = game;
        this.activity = activity;
        this.pointerOrigin = pointerOrigin;

        switch(entityType)
        {
            case MISSILE_SITE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_missile_launcher);
                Costs = Defs.MISSILE_SITE_STRUCTURE_COST;
            }
            break;

            case NUCLEAR_MISSILE_SITE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_icbm_silo);
                Costs = Defs.ICBM_SILO_STRUCTURE_COST;
            }
            break;

            case SAM_SITE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_sam_site);
                Costs = Defs.SAM_SITE_STRUCTURE_COST;
            }
            break;

            case ABM_SILO:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_abm_silo);
                Costs = Defs.ABM_SILO_STRUCTURE_COST;
            }
            break;

            case SENTRY_GUN:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_sentry);
                Costs = Defs.SENTRY_GUN_STRUCTURE_COST;
            }
            break;

            case WATCH_TOWER:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_watch_tower);
                Costs = Defs.WATCH_TOWER_STRUCTURE_COST;
            }
            break;

            case RADAR_STATION:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_radarstation);
                Costs = Defs.RADAR_STATION_STRUCTURE_COST;
            }
            break;

            case COMMAND_POST:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_command_post);
                Costs = game.GetNextCommandPostCost(game.GetOurPlayer());
            }
            break;

            case ORE_MINE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_oremine);
                Costs = Defs.ORE_MINE_STRUCTURE_COST;
            }
            break;

            case AIRBASE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_airbase);
                Costs = Defs.AIRBASE_STRUCTURE_COST;
            }
            break;

            case WAREHOUSE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_logisticsdepot);
                Costs = Defs.WAREHOUSE_STRUCTURE_COST;
            }
            break;

            case MBT:
            {
                unitIcon.setImageResource(R.drawable.build_mbt);
                Costs = Defs.TANK_BUILD_COST;
            }
            break;

            case SPAAG:
            {
                unitIcon.setImageResource(R.drawable.build_aa_gun);
                Costs = Defs.TANK_BUILD_COST;
            }
            break;

            case MISSILE_TANK:
            {
                unitIcon.setImageResource(R.drawable.build_missile_tank);
                Costs = Defs.TANK_BUILD_COST;
            }
            break;

            case SAM_TANK:
            {
                unitIcon.setImageResource(R.drawable.build_sam_tank);
                Costs = Defs.TANK_BUILD_COST;
            }
            break;

            case HOWITZER:
            {
                unitIcon.setImageResource(R.drawable.build_howitzer);
                Costs = Defs.TANK_BUILD_COST;
            }
            break;

            case INFANTRY:
            {
                unitIcon.setImageResource(R.drawable.build_infantry);
                Costs = Defs.INFANTRY_UNIT_BUILD_COST;
            }
            break;

            case ARMORY:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_armory);
                Costs = Defs.BARRACKS_STRUCTURE_COST;
            }
            break;

            case BARRACKS:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_barracks);
                Costs = Defs.BARRACKS_STRUCTURE_COST;
            }
            break;

            case CARGO_TRUCK:
            {
                unitIcon.setImageResource(R.drawable.build_truck);
                Costs = Defs.CARGO_TRUCK_BUILD_COST;
            }
            break;

            case PROCESSOR:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_processor);
                Costs = Defs.GetProcessorCost(resourceType);
            }
            break;

            case DISTRIBUTOR:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_distributor);
                Costs = Defs.DISTRIBUTOR_STRUCTURE_COST;
            }
            break;

            case ARTILLERY_GUN:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_artillery_shell);
                Costs = Defs.ARTILLERY_GUN_STRUCTURE_COST;
            }
            break;

            case SCRAP_YARD:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_scrap_yard);
                Costs = Defs.SCRAPYARD_STRUCTURE_COST;
            }
            break;

            case FRIGATE:
            {
                unitIcon.setImageResource(R.drawable.build_frigate);
                Costs = Defs.FRIGATE_BUILD_COST;
            }
            break;

            case DESTROYER:
            {
                unitIcon.setImageResource(R.drawable.build_destroyer);
                Costs = Defs.DESTROYER_BUILD_COST;
            }
            break;

            case AMPHIB:
            {
                unitIcon.setImageResource(R.drawable.build_amphib);
                Costs = Defs.AMPHIB_BUILD_COST;
            }
            break;

            case CARGO_SHIP:
            {
                unitIcon.setImageResource(R.drawable.build_cargo_ship);
                Costs = Defs.CARGO_SHIP_BUILD_COST;
            }
            break;

            case FLEET_OILER:
            {
                unitIcon.setImageResource(R.drawable.build_fleet_oiler);
                Costs = Defs.FLEET_OILER_BUILD_COST;
            }
            break;

            case SUPER_CARRIER:
            {
                unitIcon.setImageResource(R.drawable.build_super_carrier);
                Costs = Defs.SUPER_CARRIER_BUILD_COST;
            }
            break;

            case ATTACK_SUB:
            {
                unitIcon.setImageResource(R.drawable.build_attack_sub);
                Costs = Defs.ATTACK_SUB_BUILD_COST;
            }
            break;

            case SSBN:
            {
                unitIcon.setImageResource(R.drawable.build_ssbn);
                Costs = Defs.SSBN_BUILD_COST;
            }
            break;

            case BOMBER:
            {
                unitIcon.setImageResource(R.drawable.build_bomber);
                Costs = Defs.BOMBER_BUILD_COST;
            }
            break;

            case FIGHTER:
            {
                unitIcon.setImageResource(R.drawable.build_fighter);
                Costs = Defs.FIGHTER_BUILD_COST;
            }
            break;

            case STEALTH_FIGHTER:
            {
                unitIcon.setImageResource(R.drawable.build_stealth_fighter);
                Costs = Defs.STEALTH_FIGHTER_BUILD_COST;
            }
            break;

            case STEALTH_BOMBER:
            {
                unitIcon.setImageResource(R.drawable.build_stealth_bomber);
                Costs = Defs.STEALTH_BOMBER_BUILD_COST;
            }
            break;

            case ATTACK_AIRCRAFT:
            {
                unitIcon.setImageResource(R.drawable.build_attack_aircraft);
                Costs = Defs.ATTACK_AIRCRAFT_BUILD_COST;
            }
            break;

            case AWACS:
            {
                unitIcon.setImageResource(R.drawable.build_awacs);
                Costs = Defs.AWACS_BUILD_COST;
            }
            break;

            case REFUELER:
            {
                unitIcon.setImageResource(R.drawable.build_refueler);
                Costs = Defs.REFUELER_BUILD_COST;
            }
            break;

            case CARGO_PLANE:
            {
                unitIcon.setImageResource(R.drawable.build_cargo_plane);
                Costs = Defs.CARGO_PLANE_BUILD_COST;
            }
            break;

            case MULTI_ROLE:
            {
                unitIcon.setImageResource(R.drawable.build_multi_role);
                Costs = Defs.MULTI_ROLE_BUILD_COST;
            }
            break;

            case SSB:
            {
                unitIcon.setImageResource(R.drawable.build_ssb);
                Costs = Defs.SSB_BUILD_COST;
            }
            break;

            default: unitIcon.setImageResource(R.drawable.todo); break;
        }

        //Assign substitute costs and normal costs.
        requiredCosts = game.GetRequiredCost(Costs);
        substituteCosts = game.GetSubstitutionCost(Costs);

        //Draw costs (separate method.)
        DrawCosts();

        if(pointerOrigin != null)
        {
            switch(pointerOrigin.GetType())
            {
                case WAREHOUSE:
                case COMMAND_POST:
                case ARMORY:
                case BARRACKS:
                case AIRBASE:
                {
                    Structure structure = pointerOrigin.GetStructure(game);

                    if(structure != null)
                    {
                        bHostOffline = !structure.GetOnline();
                    }
                }
                break;
            }
        }

        if(bRespectStructureSeparation)
        {
            bTooCloseToStructures = !game.GetNearbyStructures(game.GetOurPlayer()).isEmpty();
        }
    }

    private void DrawCosts()
    {
        if(bShowingRequirements)
            btnShowSubstitutes.setAlpha(0.5f);
        else
            btnShowSubstitutes.setAlpha(1.0f);

        if(!bShowingRequirements && pointerOrigin != null)
        {
            cargoToCheck = null;
            resourcesToCheck = null;

            switch(pointerOrigin.GetType())
            {
                case PLAYER:
                {
                    cargoToCheck = game.GetOurPlayer().GetCargoSystem();
                }
                break;

                case SHIPYARD:
                {
                    cargoToCheck = ((Shipyard)pointerOrigin.GetEntity(game)).GetCargoSystem();
                }
                break;

                case WAREHOUSE:
                {
                    resourcesToCheck = ((Warehouse)pointerOrigin.GetEntity(game)).GetResourceSystem();
                }
                break;

                case ARMORY:
                case BARRACKS:
                {
                    resourcesToCheck = ((Armory)pointerOrigin.GetEntity(game)).GetResourceSystem();
                }
                break;

                case AIRBASE:
                {
                    resourcesToCheck = ((Airbase)pointerOrigin.GetEntity(game)).GetResourceSystem();
                }
                break;

                case COMMAND_POST:
                {
                    resourcesToCheck = ((CommandPost)pointerOrigin.GetEntity(game)).GetResourceSystem();
                }
                break;
            }
        }
        else
        {
            cargoToCheck = game.GetOurPlayer().GetCargoSystem();
        }

        //Clear old costs
        costContainer.removeAllViews();
        Map<ResourceType, Long> costsToShow = bShowingRequirements ? requiredCosts : substituteCosts;

        if(!costsToShow.isEmpty())
        {
            // Add cost views dynamically
            for(Map.Entry<ResourceType, Long> cost : costsToShow.entrySet())
            {
                boolean bCanAfford = false;

                if(cargoToCheck != null)
                    bCanAfford = cargoToCheck.GetAmountOfType(cost.getKey()) >= cost.getValue();
                else
                    bCanAfford = resourcesToCheck.HasQuantity(cost.getKey(), cost.getValue());

                // Resource icon
                ImageView resIcon = new ImageView(getContext());
                resIcon.setImageBitmap(EntityIconBitmaps.GetResourceTypeBitmap(context, cost.getKey())); // drawable resource id
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                iconParams.setMargins(0, getInset(), 0, getInset());
                resIcon.setLayoutParams(iconParams);
                costContainer.addView(resIcon);

                // Resource amount
                TextView resAmount = new TextView(getContext());

                String strAmount = "";

                if(costsToShow.size() > 2)
                {
                    if(cost.getKey() == ResourceType.WEALTH)
                    {
                        strAmount = (int)(cost.getValue()/1000) + "k";
                    }
                    else if(cost.getKey() == ResourceType.ELECTRICITY)
                    {
                        strAmount = (int)(cost.getValue()/1000) + "mw";
                    }
                    else
                    {
                        strAmount = (int)(cost.getValue()/1000) + "t";
                    }
                }
                else
                {
                    if(cost.getKey() == ResourceType.WEALTH)
                    {
                        strAmount = String.valueOf(cost.getValue());
                    }
                    else if(cost.getKey() == ResourceType.ELECTRICITY)
                    {
                        strAmount = cost.getValue() + " kw";
                    }
                    else
                    {
                        strAmount = cost.getValue() + " kg";
                    }
                }

                resAmount.setText(strAmount);
                resAmount.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
                resAmount.setTextColor(bCanAfford ? Utilities.ColourFromAttr(context, R.attr.GoodColour) : Utilities.ColourFromAttr(context, R.attr.BadColour));
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                textParams.setMargins(0, getInset(), Utilities.GetdpToPx(context, 4), getInset());
                resAmount.setLayoutParams(textParams);
                resAmount.setGravity(CENTER_VERTICAL);
                costContainer.addView(resAmount);
            }
        }
        else
        {
            // Resource amount
            TextView resAmount = new TextView(getContext());
            resAmount.setText(context.getString(R.string.free));
            resAmount.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
            resAmount.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0, getInset(), getSideMargin(), getInset());
            resAmount.setLayoutParams(textParams);
            resAmount.setGravity(CENTER_VERTICAL);
            costContainer.addView(resAmount);
        }
    }

    private int getInset()
    {
        return (int) getResources().getDimension(R.dimen.ButtonInset);
    }

    private int getSideMargin()
    {
        return (int) getResources().getDimension(R.dimen.MainViewSideMargin);
    }

    private void HandlePurchase()
    {
        if(bTooCloseToStructures && pointerOrigin.GetType() != EntityType.COMMAND_POST)
        {
            activity.ShowBasicOKDialog(context.getString(R.string.cannot_build));
        }
        else if(bHostOffline)
        {
            activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
        }
        else
        {
            boolean bResourcesAvailable = false;
            Map<ResourceType, Long> costsToUse = bShowingRequirements ? requiredCosts : substituteCosts;

            if(!bShowingRequirements)
            {
                if(cargoToCheck != null)
                {
                    if(cargoToCheck.ContainsQuantities(costsToUse))
                    {
                        bResourcesAvailable = true;
                    }
                }
                else if(resourcesToCheck != null)
                {
                    if(resourcesToCheck.HasNecessaryResources(costsToUse))
                    {
                        bResourcesAvailable = true;
                    }
                }
            }
            else
            {
                bResourcesAvailable = game.GetOurPlayer().GetCargoSystem().ContainsQuantities(costsToUse);
            }

            if(bResourcesAvailable)
            {
                if(pointerOrigin != null && pointerOrigin.GetType() == EntityType.COMMAND_POST)
                {
                    activity.BuildStructureMode(pointerOrigin.GetID(), entityType, resourceType, !bShowingRequirements);
                }
                else
                {
                    if(bShowingRequirements)
                        costsToUse = game.GetRequiredCost(costsToUse);
                    else
                        costsToUse = game.GetSubstitutionCost(costsToUse);

                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderConstruct();
                    launchDialog.SetMessage(context.getString(R.string.construct_confirm, TextUtilities.GetEntityTypeName(entityType, resourceType), TextUtilities.GetCostStatement(costsToUse)));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();

                            switch(pointerOrigin.GetType())
                            {
                                case PLAYER: game.ConstructStructure(entityType, resourceType, LaunchEntity.ID_NONE, null, !bShowingRequirements); break;

                                case ARMORY: game.PurchaseTank(pointerOrigin.GetID(), entityType, !bShowingRequirements); break;

                                case BARRACKS: game.PurchaseInfantry(pointerOrigin.GetID(), !bShowingRequirements); break;

                                case WAREHOUSE: game.PurchaseCargoTruck(pointerOrigin.GetID(), !bShowingRequirements); break;

                                case AMPHIB:
                                case SUPER_CARRIER:
                                case AIRBASE: game.PurchaseAircraft(pointerOrigin, entityType, !bShowingRequirements); break;

                                case SHIPYARD: game.PurchaseShip(pointerOrigin.GetID(), entityType, !bShowingRequirements); break;
                            }

                            activity.ReturnToMainView();
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
            }
            else
            {
                activity.ShowBasicOKDialog(context.getString(R.string.insufficient_resources));
            }
        }
    }

    public void SetTooClose(boolean bTooClose)
    {
        bTooCloseToStructures = bTooClose;
    }
}


