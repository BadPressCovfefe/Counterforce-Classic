package com.apps.fast.launch.launchviews.controls;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.EmptyShipyardSlotView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.LogisticsDepot;
import launch.game.entities.Shipyard;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.ShipProductionOrder;

public class ShipyardControl extends LaunchView
{
    private Shipyard shipyard;
    private PurchaseButton btnBuildAttackSub;
    private PurchaseButton btnBuildSSBN;
    private PurchaseButton btnBuildFrigate;
    private PurchaseButton btnBuildDestroyer;
    private PurchaseButton btnBuildAmphib;
    private PurchaseButton btnBuildCargoShip;
    private PurchaseButton btnBuildFleetOiler;
    private PurchaseButton btnBuildSuperCarrier;
    private LinearLayout lytBuildOptions;
    private LinearLayout lytQueue;
    private TextView txtQueue;
    private View view3;
    private View view4;
    private LinearLayout btnPurchaseUpgrade;
    private TextView txtDescUpgradeCapacity;
    private TextView txtReloadUpgradeCost;
    private TextView txtSlotUpgrade;
    private boolean bDisplayUpgrade;
    private List<LaunchView> ProductionOrders;
    private int lID;
    private boolean bOurStructure;

    public ShipyardControl(LaunchClientGame game, MainActivity activity, int lShipyardID)
    {
        super(game, activity, true);
        lID = lShipyardID;

        if(game.GetShipyard(lID) != null)
        {
            shipyard = game.GetShipyard(lID);
            bOurStructure = (shipyard.GetOwnerID() == game.GetOurPlayerID());
            bDisplayUpgrade = shipyard.GetOwnedBy(game.GetOurPlayerID());
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
        inflate(context, R.layout.control_shipyard, this);

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

        btnBuildAttackSub.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.ATTACK_SUB);
        btnBuildSSBN.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.SSBN);
        btnBuildFrigate.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.FRIGATE);
        btnBuildDestroyer.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.DESTROYER);
        btnBuildAmphib.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.AMPHIB);
        btnBuildCargoShip.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.CARGO_SHIP);
        btnBuildFleetOiler.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.FLEET_OILER);
        btnBuildSuperCarrier.SetUnit(game, activity, shipyard.GetPointer(), EntityPointer.EntityType.SUPER_CARRIER);

        txtDescUpgradeCapacity = findViewById(R.id.txtDescUpgradeCapacity);
        btnPurchaseUpgrade = findViewById(R.id.btnPurchaseUpgrade);
        txtReloadUpgradeCost = findViewById(R.id.txtReloadUpgradeCost);
        txtSlotUpgrade = findViewById(R.id.txtSlotUpgrade);

        txtQueue = findViewById(R.id.txtQueue);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);

        lytQueue.removeAllViews();
        GenerateSlotTable();

        txtQueue.setText(context.getString(R.string.shipyard_queue, shipyard.GetQueue().size(), shipyard.GetProductionCapacity()));

        if(bDisplayUpgrade && !shipyard.FullyUpgraded())
        {
            txtDescUpgradeCapacity.setVisibility(VISIBLE);
            btnPurchaseUpgrade.setVisibility(VISIBLE);

            txtReloadUpgradeCost.setText(TextUtilities.GetCurrencyString(Defs.SHIPYARD_UPGRADE_WEALTH_COST));
            txtReloadUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, Defs.SHIPYARD_UPGRADE_WEALTH_COST > game.GetOurPlayer().GetWealth() ? R.attr.BadColour : R.attr.GoodColour));
            txtSlotUpgrade.setText(context.getString(R.string.upgrade, String.valueOf(shipyard.GetProductionCapacity()), String.valueOf(shipyard.GetProductionCapacity() + 1)));

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
                                game.UpgradeShipyard(shipyard.GetID());
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

        if(game.EntityIsFriendly(shipyard, game.GetOurPlayer()))
        {
            lytBuildOptions.setVisibility(VISIBLE);
        }
        else
        {
            lytBuildOptions.setVisibility(GONE);
        }

        if(!shipyard.HasCapacityRemaining())
        {
            lytBuildOptions.setVisibility(GONE);
            txtQueue.setVisibility(VISIBLE);
            view4.setVisibility(VISIBLE);
        }

        if(shipyard.Destroyed())
        {
            lytBuildOptions.setVisibility(GONE);
            txtQueue.setVisibility(GONE);
            view3.setVisibility(GONE);
            view4.setVisibility(GONE);
        }

        if(shipyard.Destroyed())
        {
            lytBuildOptions.setVisibility(GONE);
            view3.setVisibility(GONE);
            txtQueue.setVisibility(GONE);
            view4.setVisibility(GONE);
        }

        Update();
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
                Shipyard shipyard = game.GetShipyard(lID);

                if(shipyard != null)
                {
                    txtQueue.setText(context.getString(R.string.shipyard_queue, shipyard.GetQueue().size(), shipyard.GetProductionCapacity()));

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

                    if(game.EntityIsFriendly(shipyard, game.GetOurPlayer()))
                    {
                        if(shipyard.GetProducing())
                        {
                            txtQueue.setVisibility(VISIBLE);
                            view4.setVisibility(VISIBLE);

                            if(!shipyard.HasCapacityRemaining())
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

                    if(!game.EntityIsFriendly(shipyard, game.GetOurPlayer()))
                    {
                        lytBuildOptions.setVisibility(GONE);
                    }

                    if(shipyard.Destroyed())
                    {
                        lytBuildOptions.setVisibility(GONE);
                        view3.setVisibility(GONE);
                        txtQueue.setVisibility(GONE);
                        view4.setVisibility(GONE);
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

        for(final ShipProductionOrder order : shipyard.GetQueue())
        {
            NavalProductionOrderView orderView = new NavalProductionOrderView(game, activity, order);
            ProductionOrders.add(orderView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            orderView.setLayoutParams(layoutParams);
            lytQueue.addView(orderView);
        }

        if(game.EntityIsFriendly(shipyard, game.GetOurPlayer()))
        {
            for(int i = 0; i < shipyard.GetRemainingCapacity(); i++)
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
}
