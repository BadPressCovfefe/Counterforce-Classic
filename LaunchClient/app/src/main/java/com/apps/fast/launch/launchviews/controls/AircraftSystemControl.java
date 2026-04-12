package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.AircraftBaseView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.MainNormalView;
import com.apps.fast.launch.launchviews.entities.AirplaneView;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.PurchaseButton;

import java.util.ArrayList;
import java.util.List;

import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.MapEntity;
import launch.game.entities.Ship;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.systems.AircraftSystem;

/**
 * Created by tobster on 19/10/16.
 */
public class AircraftSystemControl extends LaunchView
{
    private LinearLayout lytAircraftSlots;
    private LinearLayout btnUpgradeAircraftSlots;
    private LinearLayout lytAirbaseOptions;
    private LinearLayout lytShipOptions;
    private TextView txtAircraftSlotUpgrade;
    private TextView txtAircraftSlotUpgradeCost;
    private TextView txtAirbaseSlots;
    private boolean bOwnedByPlayer;
    private AircraftSystem system;
    boolean bDisplaySlotUpgrade = false;
    private MapEntity host;
    private static boolean bUpgradeConfirmHasBeenShown = false;
    private List<LaunchView> AircraftSlots;

    public AircraftSystemControl(LaunchClientGame game, MainActivity activity, Airbase airbase)
    {
        super(game, activity, true);
        this.system = airbase.GetAircraftSystem();
        bOwnedByPlayer = airbase.GetOwnedBy(game.GetOurPlayerID());
        bDisplaySlotUpgrade = true;
        host = airbase;

        Setup();
    }

    public AircraftSystemControl(LaunchClientGame game, MainActivity activity, Ship ship)
    {
        super(game, activity, true);
        this.system = ship.GetAircraftSystem();
        bOwnedByPlayer = ship.GetOwnedBy(game.GetOurPlayerID());
        bDisplaySlotUpgrade = false;
        host = ship;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_aircraft_system, this);

        lytAircraftSlots = (LinearLayout)findViewById(R.id.lytAircraftSlots);

        btnUpgradeAircraftSlots = (LinearLayout) findViewById(R.id.btnUpgradeAircraftSlots);
        txtAircraftSlotUpgrade = (TextView) findViewById(R.id.txtAircraftSlotUpgrade);
        txtAircraftSlotUpgradeCost = (TextView) findViewById(R.id.txtAircraftSlotUpgradeCost);
        txtAirbaseSlots = findViewById(R.id.txtAirbaseSlots);
        lytAirbaseOptions = findViewById(R.id.lytAirbaseOptions);
        lytShipOptions = findViewById(R.id.lytShipOptions);

        //Set upgrade visibility.
        btnUpgradeAircraftSlots.setVisibility(bOwnedByPlayer && bDisplaySlotUpgrade? VISIBLE : GONE);

        if(bOwnedByPlayer)
        {
            PurchaseButton btnBuildFighter = findViewById(R.id.btnBuildFighter);
            btnBuildFighter.SetUnit(game, activity, host.GetPointer(), EntityType.FIGHTER, ResourceType.FOOD, Defs.FIGHTER_BUILD_COST);

            PurchaseButton btnBuildBomber = findViewById(R.id.btnBuildBomber);
            btnBuildBomber.SetUnit(game, activity, host.GetPointer(), EntityType.BOMBER, ResourceType.FOOD, Defs.BOMBER_BUILD_COST);

            PurchaseButton btnBuildRefueler = findViewById(R.id.btnBuildRefueler);
            btnBuildRefueler.SetUnit(game, activity, host.GetPointer(), EntityType.REFUELER, ResourceType.FOOD, Defs.REFUELER_BUILD_COST);

            PurchaseButton btnBuildAttackAircraft = findViewById(R.id.btnBuildAttackAircraft);
            btnBuildAttackAircraft.SetUnit(game, activity, host.GetPointer(), EntityType.ATTACK_AIRCRAFT, ResourceType.FOOD, Defs.ATTACK_AIRCRAFT_BUILD_COST);

            PurchaseButton btnBuildSSB = findViewById(R.id.btnBuildSSB);
            btnBuildSSB.SetUnit(game, activity, host.GetPointer(), EntityType.SSB, ResourceType.FOOD, Defs.SSB_BUILD_COST);

            PurchaseButton btnBuildMultiRole = findViewById(R.id.btnBuildMultiRole);
            btnBuildMultiRole.SetUnit(game, activity, host.GetPointer(), EntityType.MULTI_ROLE, ResourceType.FOOD, Defs.MULTI_ROLE_BUILD_COST);

            if(host instanceof Airbase)
            {
                lytAirbaseOptions.setVisibility(VISIBLE);
                lytShipOptions.setVisibility(VISIBLE);
            }
            else
            {
                lytAirbaseOptions.setVisibility(GONE);
                lytShipOptions.setVisibility(VISIBLE);
            }
        }

        if(bOwnedByPlayer || system.HostsPlayerAircraft(game.GetOurPlayerID()))
        {
            lytAircraftSlots.removeAllViews();
            GenerateSlotTable();

            int lUsedCapacity = system.GetOccupiedSlotCount();
            int lCapacity = system.GetSlotCount();
            txtAirbaseSlots.setVisibility(VISIBLE);
            txtAirbaseSlots.setText(context.getString(R.string.airbase_slots, lUsedCapacity, lCapacity));

            if(bOwnedByPlayer)
            {
                Config config = game.GetConfig();

                if(bDisplaySlotUpgrade)
                {
                    Airbase airbase = (Airbase)host;

                    int lSlots = airbase.GetAircraftSystem().GetSlotCount();

                    btnUpgradeAircraftSlots.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            AircraftSlotUpgradeClicked();
                        }
                    });

                    if(lSlots < config.GetMaxAirbaseCapacity())
                    {
                        int lSlotUpgrade = lSlots + config.GetAircraftSlotUpgradeCount();
                        int lCost = game.GetAircraftSlotUpgradeCost(airbase.GetAircraftSystem().GetSlotCount(), config.GetAirbaseBaseCapacity());
                        txtAircraftSlotUpgrade.setText(context.getString(R.string.upgrade, Integer.toString(lSlots), Integer.toString(lSlotUpgrade)));
                        txtAircraftSlotUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                        txtAircraftSlotUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                        btnUpgradeAircraftSlots.setVisibility(VISIBLE);
                    }
                    else
                    {
                        btnUpgradeAircraftSlots.setVisibility(GONE);
                    }
                }
                else
                {
                    btnUpgradeAircraftSlots.setVisibility(GONE);
                }

                /*if(system.Full())
                {
                    btnPurchaseAircraft.setVisibility(GONE);
                }
                else
                {
                    btnPurchaseAircraft.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if(!system.Full())
                            {
                                if(host instanceof Ship)
                                    activity.SetView(new PurchaseAircraftView(game, activity, (Ship)host));
                                else if(host instanceof Airbase)
                                    activity.SetView(new PurchaseAircraftView(game, activity, (Airbase)host));
                            }
                            else
                            {
                                activity.ShowBasicOKDialog(context.getString(R.string.airbase_full));
                            }
                        }
                    });
                }*/
            }
        }
        else
        {
            btnUpgradeAircraftSlots.setVisibility(GONE);
            //btnPurchaseAircraft.setVisibility(GONE);
            txtAirbaseSlots.setVisibility(GONE);
            lytAircraftSlots.setVisibility(GONE);
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
                host = host.GetPointer().GetMapEntity(game);
                system = GetAircraftSystem();

                if(system != null)
                {
                    if(bOwnedByPlayer || system.HostsPlayerAircraft(game.GetOurPlayerID()))
                    {
                        /*lytAircraftSlots.removeAllViews();
                        GenerateSlotTable();*/

                        int lUsedCapacity = system.GetOccupiedSlotCount();
                        int lCapacity = system.GetSlotCount();
                        txtAirbaseSlots.setVisibility(VISIBLE);
                        txtAirbaseSlots.setText(context.getString(R.string.airbase_slots, lUsedCapacity, lCapacity));

                        if(bOwnedByPlayer)
                        {
                            if(bDisplaySlotUpgrade)
                            {
                                Config config = game.GetConfig();
                                Airbase airbase = (Airbase)host;

                                int lSlots = airbase.GetAircraftSystem().GetSlotCount();

                                if(lSlots < config.GetMaxAirbaseCapacity())
                                {
                                    int lSlotUpgrade = lSlots + config.GetAircraftSlotUpgradeCount();
                                    int lCost = game.GetAircraftSlotUpgradeCost(airbase.GetAircraftSystem().GetSlotCount(), config.GetAirbaseBaseCapacity());
                                    txtAircraftSlotUpgrade.setText(context.getString(R.string.upgrade, Integer.toString(lSlots), Integer.toString(lSlotUpgrade)));
                                    txtAircraftSlotUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                                    txtAircraftSlotUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                                    btnUpgradeAircraftSlots.setVisibility(VISIBLE);
                                }
                                else
                                {
                                    btnUpgradeAircraftSlots.setVisibility(GONE);
                                }
                            }

                            for(LaunchView baseView : AircraftSlots)
                            {
                                baseView.Update();
                            }

                            /*if(system.Full())
                            {
                                btnPurchaseAircraft.setVisibility(GONE);
                            }
                            else
                            {
                                btnPurchaseAircraft.setVisibility(VISIBLE);
                            }*/
                        }
                    }
                    else
                    {
                        btnUpgradeAircraftSlots.setVisibility(GONE);
                        //btnPurchaseAircraft.setVisibility(GONE);
                        txtAirbaseSlots.setVisibility(GONE);
                        lytAircraftSlots.setVisibility(GONE);
                    }
                }
            }
        });
    }

    private void AircraftSlotUpgradeClicked()
    {
        Config config = game.GetConfig();

        Resource.ResourceType type = Defs.AIRBASE_CAPACITY_UPGRADE_TYPE;
        int lSlots = system.GetSlotCount();
        int lSlotUpgrade = lSlots + config.GetAircraftSlotUpgradeCount();
        final int lCost = game.GetAircraftSlotUpgradeCost(system.GetSlotCount(), config.GetAirbaseBaseCapacity());

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.upgrade_airbase_capacity_confirm, lSlots, lSlotUpgrade, TextUtilities.GetResourceQuantityString(type, lCost)));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        game.PurchaseAircraftSlotUpgrade(host.GetID());
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
                game.PurchaseAircraftSlotUpgrade(host.GetID());
                Sounds.PlayEquip();
            }
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }

    private void GenerateSlotTable()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                host = host.GetPointer().GetMapEntity(game);
                AircraftSlots = new ArrayList<>();

                for(final StoredAirplane aircraft : system.GetStoredAirplanes().values())
                {
                    AircraftBaseView aircraftView = new AircraftBaseView(game, activity, aircraft, false);
                    AircraftSlots.add(aircraftView);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
                    layoutParams.weight = 1.0f;
                    aircraftView.setLayoutParams(layoutParams);
                    lytAircraftSlots.addView(aircraftView);

                    aircraftView.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if(aircraft.DoneBuilding())
                            {
                                MainNormalView mainView = new MainNormalView(game, activity);
                                activity.SetView(mainView);
                                mainView.BottomLayoutShowView(new AirplaneView(game, activity, aircraft, true));
                            }
                            else
                            {
                                activity.ShowBasicOKDialog(context.getString(R.string.unit_not_finished));
                            }
                        }
                    });

                    aircraftView.setOnLongClickListener(new OnLongClickListener()
                    {
                        @Override
                        public boolean onLongClick(View view)
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderPurchase();
                            launchDialog.SetMessage(context.getString(R.string.sell_confirm, TextUtilities.GetEntityTypeAndName(aircraft, game), TextUtilities.GetCostStatement(game.GetSaleValue(game.GetAircraftValue(aircraft)))));
                            launchDialog.SetOnClickYes(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.SellEntity(aircraft.GetPointer());
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

                            return true;
                        }
                    });
                }
            }
        });
    }

    private AircraftSystem GetAircraftSystem()
    {
        if(host instanceof Ship)
        {
            if(game.GetShip(host.GetID()) != null)
            {
                return game.GetShip(host.GetID()).GetAircraftSystem();
            }
        }
        else if(host instanceof Airbase)
        {
            if(game.GetAirbase(host.GetID()) != null)
            {
                return game.GetAirbase(host.GetID()).GetAircraftSystem();
            }
        }

        return null;
    }
}
