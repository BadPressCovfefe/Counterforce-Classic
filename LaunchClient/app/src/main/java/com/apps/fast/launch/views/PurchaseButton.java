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
    private TextView txtCost;
    private ImageView unitIcon;
    private EntityType entityType;
    private ResourceType resourceType;
    private boolean bRespectStructureSeparation = false;
    private boolean bTooCloseToStructures;
    private boolean bHostOffline;
    private Context context;
    private MainActivity activity;
    private LaunchClientGame game;
    private EntityPointer pointerOrigin;
    private TextView txtTitle;
    private TextView txtDescription;
    
    private long oCost = 0;

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
        txtCost = findViewById(R.id.txtCost);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);

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

        txtTitle.setText(TextUtilities.GetEntityTypeTitle(entityType));

        switch(entityType)
        {
            case MISSILE_SITE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_missile_site);
                oCost = Defs.MISSILE_SITE_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_missile_launcher));
            }
            break;

            case NUCLEAR_MISSILE_SITE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_icbm_silo);
                oCost = Defs.ICBM_SILO_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_icbm_silo));
            }
            break;

            case SAM_SITE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_sam_site);
                oCost = Defs.SAM_SITE_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_sam_site));
            }
            break;

            case ABM_SILO:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_abm_site);
                oCost = Defs.ABM_SILO_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_abm_silo));
            }
            break;

            case SENTRY_GUN:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_sentry_gun);
                oCost = Defs.SENTRY_GUN_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_sentry_gun));
            }
            break;

            case COMMAND_POST:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_command_post);

                if(game.GetNextCommandPostCost(game.GetOurPlayer()).get(ResourceType.WEALTH) != null)
                {
                    oCost = game.GetNextCommandPostCost(game.GetOurPlayer()).get(ResourceType.WEALTH);
                }

                txtDescription.setText(context.getString(R.string.desc_command_post));
            }
            break;

            case AIRBASE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_airbase);
                oCost = Defs.AIRBASE_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.airbase_description));
            }
            break;

            case WAREHOUSE:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_bank);
                oCost = Defs.WAREHOUSE_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_bank));
            }
            break;

            case MBT:
            {
                unitIcon.setImageResource(R.drawable.build_tank);
                oCost = Defs.TANK_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_mbt));
            }
            break;

            case ARMORY:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_armory);
                oCost = Defs.BARRACKS_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.armory_description));
            }
            break;

            case CARGO_TRUCK:
            {
                unitIcon.setImageResource(R.drawable.todo);
                oCost = Defs.CARGO_TRUCK_BUILD_COST.get(ResourceType.WEALTH);
            }
            break;

            case ARTILLERY_GUN:
            {
                bRespectStructureSeparation = true;
                unitIcon.setImageResource(R.drawable.build_artillery_gun);
                oCost = Defs.ARTILLERY_GUN_STRUCTURE_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_artillery_gun));
            }
            break;

            case FRIGATE:
            {
                unitIcon.setImageResource(R.drawable.build_frigate);
                oCost = Defs.FRIGATE_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_frigate));
            }
            break;

            case DESTROYER:
            {
                unitIcon.setImageResource(R.drawable.build_destroyer);
                oCost = Defs.DESTROYER_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_destroyer));
            }
            break;

            case FLEET_OILER:
            {
                unitIcon.setImageResource(R.drawable.todo);
                oCost = Defs.FLEET_OILER_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_fleet_oiler));
            }
            break;

            case SUPER_CARRIER:
            {
                unitIcon.setImageResource(R.drawable.build_super_carrier);
                oCost = Defs.SUPER_CARRIER_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_super_carrier));
            }
            break;

            case ATTACK_SUB:
            {
                unitIcon.setImageResource(R.drawable.build_attack_sub);
                oCost = Defs.ATTACK_SUB_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_attack_sub));
            }
            break;

            case SSBN:
            {
                unitIcon.setImageResource(R.drawable.build_ssbn_2);
                oCost = Defs.SSBN_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_ssbn));
            }
            break;

            case BOMBER:
            {
                unitIcon.setImageResource(R.drawable.build_bomber);
                oCost = Defs.BOMBER_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_bomber));
            }
            break;

            case FIGHTER:
            {
                unitIcon.setImageResource(R.drawable.build_fighter);
                oCost = Defs.FIGHTER_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_fighter));
            }
            break;

            case ATTACK_AIRCRAFT:
            {
                unitIcon.setImageResource(R.drawable.build_ground_attack);
                oCost = Defs.ATTACK_AIRCRAFT_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_attack_aircraft));
            }
            break;

            case REFUELER:
            {
                unitIcon.setImageResource(R.drawable.build_refueler);
                oCost = Defs.REFUELER_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_refueler));
            }
            break;

            case MULTI_ROLE:
            {
                unitIcon.setImageResource(R.drawable.build_refueler);
                oCost = Defs.MULTI_ROLE_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_multi_role));
            }
            break;

            case SSB:
            {
                unitIcon.setImageResource(R.drawable.build_ssb);
                oCost = Defs.SSB_BUILD_COST.get(ResourceType.WEALTH);
                txtDescription.setText(context.getString(R.string.desc_ssb));
            }
            break;

            default: unitIcon.setImageResource(R.drawable.todo); break;
        }

        if(oCost > 0)
        {
            txtCost.setText(TextUtilities.GetCurrencyString(oCost));
            txtCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= oCost ? R.attr.GoodColour : R.attr.BadColour));
        }
        else
        {
            txtCost.setText(context.getString(R.string.free));
            txtCost.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }


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

            if(game.GetOurPlayer().GetWealth() >= oCost)
            {
                if(pointerOrigin != null && pointerOrigin.GetType() == EntityType.COMMAND_POST)
                {
                    activity.BuildStructureMode(pointerOrigin.GetID(), entityType, resourceType, false);
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderConstruct();
                    launchDialog.SetMessage(context.getString(R.string.construct_confirm, TextUtilities.GetEntityTypeName(entityType), TextUtilities.GetCurrencyString(oCost)));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();

                            switch(pointerOrigin.GetType())
                            {
                                case PLAYER: game.ConstructStructure(entityType, resourceType, LaunchEntity.ID_NONE, null, false); break;

                                case ARMORY: game.PurchaseTank(pointerOrigin.GetID(), entityType, false); break;

                                case BARRACKS: game.PurchaseInfantry(pointerOrigin.GetID(), false); break;

                                case WAREHOUSE: game.PurchaseCargoTruck(pointerOrigin.GetID(), false); break;

                                case AMPHIB:
                                case SUPER_CARRIER:
                                case AIRBASE: game.PurchaseAircraft(pointerOrigin, entityType, false); break;

                                case SHIPYARD: game.PurchaseShip(pointerOrigin.GetID(), entityType, false); break;
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
                activity.ShowBasicOKDialog(context.getString(R.string.insufficient_wealth));
            }
        }
    }

    public void SetTooClose(boolean bTooClose)
    {
        bTooCloseToStructures = bTooClose;
    }
}


