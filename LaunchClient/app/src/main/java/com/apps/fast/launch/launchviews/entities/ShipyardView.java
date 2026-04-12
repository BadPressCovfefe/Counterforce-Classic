package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
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
import com.apps.fast.launch.launchviews.EmptyShipyardSlotView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.launchviews.controls.NavalProductionOrderView;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.EntityPointer.EntityType;
import launch.game.LaunchClientGame;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Shipyard;
import launch.game.entities.conceptuals.ShipProductionOrder;

/**
 * Created by Corbin.
 */
public class ShipyardView extends LaunchView implements LaunchUICommon.ShipyardInfoProvider
{
    private Shipyard shipyardShadow;
    private PurchaseButton btnBuildAttackSub;
    private PurchaseButton btnBuildSSBN;
    private PurchaseButton btnBuildFrigate;
    private PurchaseButton btnBuildDestroyer;
    private PurchaseButton btnBuildAmphib;
    private PurchaseButton btnBuildCargoShip;
    private PurchaseButton btnBuildFleetOiler;
    private PurchaseButton btnBuildSuperCarrier;
    private TextView txtHP;
    private LinearLayout lytBuildOptions;
    private LinearLayout lytQueue;
    private TextView txtQueue;
    private View view3;
    private View view4;
    private FrameLayout lytInputs;
    private LinearLayout btnRepair;
    private ImageView imgProduction;
    private LinearLayout btnPurchaseUpgrade;
    private TextView txtDescUpgradeCapacity;
    private TextView txtReloadUpgradeCost;
    private TextView txtSlotUpgrade;
    private boolean bOwnedByPlayer;
    private boolean bDisplayUpgrade;
    private List<LaunchView> ProductionOrders;

    public ShipyardView(LaunchClientGame game, MainActivity activity, Shipyard shipyard)
    {
        super(game, activity, true);
        this.shipyardShadow = shipyard;

        bOwnedByPlayer = game.EntityIsFriendly(shipyard, game.GetOurPlayer());
        bDisplayUpgrade = (!shipyard.IsCaptured() || game.EntityIsFriendly(game.GetOurPlayer(), game.GetOwner(shipyard))) && shipyard.GetProductionCapacity() < Defs.MAX_SHIPYARD_CAPACITY;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_shipyard, this);

        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        btnRepair = findViewById(R.id.btnRepair);
        txtHP = findViewById(R.id.txtHP);
        imgProduction = findViewById(R.id.imgProduction);
        lytQueue = findViewById(R.id.lytQueue);
        lytBuildOptions = findViewById(R.id.lytBuildOptions);

        btnBuildAttackSub = findViewById(R.id.btnBuildAttackSub);
        btnBuildSSBN = findViewById(R.id.btnBuildSSBN);
        btnBuildFrigate = findViewById(R.id.btnBuildFrigate);
        btnBuildDestroyer = findViewById(R.id.btnBuildDestroyer);
        btnBuildAmphib = findViewById(R.id.btnBuildAmphib);
        btnBuildCargoShip = findViewById(R.id.btnBuildCargoShip);
        btnBuildFleetOiler = findViewById(R.id.btnBuildFleetOiler);
        btnBuildSuperCarrier = findViewById(R.id.btnBuildSuperCarrier);

        btnBuildAttackSub.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.ATTACK_SUB, null, Defs.ATTACK_SUB_BUILD_COST);
        btnBuildSSBN.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.SSBN, null, Defs.SSBN_BUILD_COST);
        btnBuildFrigate.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.FRIGATE, null, Defs.FRIGATE_BUILD_COST);
        btnBuildDestroyer.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.DESTROYER, null, Defs.DESTROYER_BUILD_COST);
        btnBuildAmphib.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.AMPHIB, null, Defs.AMPHIB_BUILD_COST);
        btnBuildCargoShip.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.CARGO_SHIP, null, Defs.CARGO_SHIP_BUILD_COST);
        btnBuildFleetOiler.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.FLEET_OILER, null, Defs.FLEET_OILER_BUILD_COST);
        btnBuildSuperCarrier.SetUnit(game, activity, shipyardShadow.GetPointer(), EntityType.SUPER_CARRIER, null, Defs.SUPER_CARRIER_BUILD_COST);

        txtDescUpgradeCapacity = findViewById(R.id.txtDescUpgradeCapacity);
        btnPurchaseUpgrade = findViewById(R.id.btnPurchaseUpgrade);
        txtReloadUpgradeCost = findViewById(R.id.txtReloadUpgradeCost);
        txtSlotUpgrade = findViewById(R.id.txtSlotUpgrade);

        txtQueue = findViewById(R.id.txtQueue);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);

        lytInputs = findViewById(R.id.lytInputs);

        TextUtilities.AssignHealthStringAndAppearance(txtHP, shipyardShadow);
        imgProduction.setImageResource(R.drawable.marker_shipyard);

        FrameLayout lytUnitControls = findViewById(R.id.lytUnitControls);

        UnitControls controls = new UnitControls(game, activity, shipyardShadow);
        lytUnitControls.removeAllViews();
        lytUnitControls.addView(controls);

        lytQueue.removeAllViews();
        GenerateSlotTable();

        txtQueue.setText(context.getString(R.string.shipyard_queue, shipyardShadow.GetQueue().size(), shipyardShadow.GetProductionCapacity()));

        if(bDisplayUpgrade && !shipyardShadow.FullyUpgraded())
        {
            txtDescUpgradeCapacity.setVisibility(VISIBLE);
            btnPurchaseUpgrade.setVisibility(VISIBLE);

            txtReloadUpgradeCost.setText(String.valueOf(Defs.SHIPYARD_UPGRADE_WEALTH_COST));
            txtReloadUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, Defs.SHIPYARD_UPGRADE_WEALTH_COST > game.GetOurPlayer().GetWealth() ? R.attr.BadColour : R.attr.GoodColour));
            txtSlotUpgrade.setText(context.getString(R.string.upgrade, String.valueOf(shipyardShadow.GetProductionCapacity()), String.valueOf(shipyardShadow.GetProductionCapacity() + 1)));

            btnPurchaseUpgrade.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    long oOurMoney = game.GetOurPlayer().GetWealth();

                    if(oOurMoney >= Defs.SHIPYARD_UPGRADE_WEALTH_COST)
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderConstruct();
                        launchDialog.SetMessage(context.getString(R.string.shipyard_capacity_upgrade_confirm, TextUtilities.GetCurrencyString(Defs.SHIPYARD_UPGRADE_WEALTH_COST)));
                        launchDialog.SetOnClickYes(new OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                game.UpgradeShipyard(shipyardShadow.GetID());
                                activity.ReturnToMainView();
                            }
                        });
                        launchDialog.SetOnClickNo(new OnClickListener()
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
                        activity.ShowBasicOKDialog(context.getString(R.string.insufficient_wealth));
                    }
                }
            });
        }

        if(shipyardShadow.GetOwnerID() == LaunchEntity.ID_NONE || bOwnedByPlayer || game.EntityIsFriendly(shipyardShadow, game.GetOurPlayer()))
        {
            lytBuildOptions.setVisibility(VISIBLE);

            btnRepair.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.InBattle(game.GetOurPlayer()) && bOwnedByPlayer)
                    {
                        Map<ResourceType, Long> Costs = game.GetRepairCost(shipyardShadow);

                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderHealth();
                        launchDialog.SetMessage(context.getString(R.string.repair_confirm, shipyardShadow.GetName(), TextUtilities.GetCostStatement(Costs)));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();

                                if(game.GetOurPlayer().GetWealth() >= Costs.get(ResourceType.WEALTH))
                                {
                                    game.RepairEntity(shipyardShadow.GetPointer());
                                }
                                else
                                {
                                    activity.ShowBasicOKDialog(context.getString(R.string.insufficient_wealth));
                                }
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
                        activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_repair));
                    }
                }
            });

            if(shipyardShadow.AtFullHealth())
            {
                btnRepair.setVisibility(GONE);
            }
            else
            {
                btnRepair.setVisibility(VISIBLE);
            }
        }
        else
        {
            btnRepair.setVisibility(GONE);

            lytBuildOptions.setVisibility(GONE);
        }

        if(!shipyardShadow.HasCapacityRemaining())
        {
            lytBuildOptions.setVisibility(GONE);
            txtQueue.setVisibility(VISIBLE);
            view4.setVisibility(VISIBLE);
        }
        else
        {
            //txtQueue.setVisibility(GONE);
            //view4.setVisibility(GONE);
        }

        if(shipyardShadow.Destroyed())
        {
            lytBuildOptions.setVisibility(GONE);
            txtQueue.setVisibility(GONE);
            view3.setVisibility(GONE);
            view4.setVisibility(GONE);
        }

        if(shipyardShadow.Destroyed())
        {
            lytBuildOptions.setVisibility(GONE);
            view3.setVisibility(GONE);
            txtQueue.setVisibility(GONE);
            view4.setVisibility(GONE);
            lytInputs.setVisibility(GONE);
        }
    }

    @Override
    public void Update()
    {
        super.Update();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Shipyard shipyard = game.GetShipyard(shipyardShadow.GetID());

                if(shipyard != null)
                {
                    txtQueue.setText(context.getString(R.string.shipyard_queue, shipyard.GetQueue().size(), shipyard.GetProductionCapacity()));

                    TextUtilities.AssignHealthStringAndAppearance(txtHP, shipyard);

                    if(shipyard.FullyUpgraded())
                    {
                        txtDescUpgradeCapacity.setVisibility(GONE);
                        btnPurchaseUpgrade.setVisibility(GONE);
                    }
                    else
                    {
                        txtReloadUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, Defs.SHIPYARD_UPGRADE_WEALTH_COST > game.GetOurPlayer().GetWealth() ? R.attr.BadColour : R.attr.GoodColour));
                    }

                    if(shipyard.GetProducing())
                    {
                        txtQueue.setVisibility(VISIBLE);
                        view4.setVisibility(VISIBLE);

                        for(LaunchView view : ProductionOrders)
                        {
                            view.Update();
                        }
                    }
                    /*else
                    {
                        txtQueue.setVisibility(GONE);
                        view4.setVisibility(GONE);
                    }*/

                    if(shipyardShadow.GetOwnerID() == LaunchEntity.ID_NONE || bOwnedByPlayer || game.EntityIsFriendly(shipyardShadow, game.GetOurPlayer()))
                    {
                        if(shipyardShadow.AtFullHealth())
                        {
                            btnRepair.setVisibility(GONE);
                        }
                        else
                        {
                            btnRepair.setVisibility(VISIBLE);
                        }

                        if(shipyardShadow.GetProducing())
                        {
                            txtQueue.setVisibility(VISIBLE);
                            view4.setVisibility(VISIBLE);

                            if(!shipyardShadow.HasCapacityRemaining())
                            {
                                lytBuildOptions.setVisibility(GONE);
                            }
                            else
                            {
                                lytBuildOptions.setVisibility(VISIBLE);
                            }
                        }
                        else
                        {
                            lytBuildOptions.setVisibility(VISIBLE);
                        }
                    }

                    if(shipyard.IsCaptured() && !game.EntityIsFriendly(shipyard, game.GetOurPlayer()))
                    {
                        btnRepair.setVisibility(GONE);
                        lytBuildOptions.setVisibility(GONE);
                    }

                    if(shipyard.Destroyed())
                    {
                        lytBuildOptions.setVisibility(GONE);
                        view3.setVisibility(GONE);
                        txtQueue.setVisibility(GONE);
                        view4.setVisibility(GONE);
                        lytInputs.setVisibility(GONE);
                    }
                }
                else
                {
                    Log.i("LaunchWTF", "City is null. Finishing... (CityView ln 560)");
                    Finish(true);
                }
            }
        });
    }


    private void GenerateSlotTable()
    {
        lytQueue.removeAllViews();
        ProductionOrders = new ArrayList<>();

        for(final ShipProductionOrder order : shipyardShadow.GetQueue())
        {
            NavalProductionOrderView orderView = new NavalProductionOrderView(game, activity, order);
            ProductionOrders.add(orderView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            orderView.setLayoutParams(layoutParams);
            lytQueue.addView(orderView);
        }

        if(shipyardShadow.GetOwnerID() == LaunchEntity.ID_NONE || bOwnedByPlayer || game.EntityIsFriendly(shipyardShadow, game.GetOurPlayer()))
        {
            for(int i = 0; i < shipyardShadow.GetRemainingCapacity(); i++)
            {
                EmptyShipyardSlotView emptySlot = new EmptyShipyardSlotView(game, activity);
                ProductionOrders.add(emptySlot);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1.0f;
                emptySlot.setLayoutParams(layoutParams);
                lytQueue.addView(emptySlot);
            }
        }
    }

    @Override
    public boolean IsSingleShipyard()
    {
        return true;
    }

    @Override
    public Shipyard GetCurrentShipyard()
    {
        return game.GetShipyard(shipyardShadow.GetID());
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(entity.ApparentlyEquals(shipyardShadow))
                {
                    GenerateSlotTable();
                }
            }
        });
    }
}
