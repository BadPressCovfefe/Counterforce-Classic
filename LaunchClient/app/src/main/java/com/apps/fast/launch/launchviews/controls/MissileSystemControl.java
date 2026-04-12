package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.PurchaseLaunchableView;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.entities.Tank;
import launch.game.entities.TankInterface;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.MissileSystem;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 19/10/16.
 */
public class MissileSystemControl extends LaunchView implements SlotListener
{
    private LinearLayout lytReload;
    private TextView txtReloading;
    private LinearLayout lytMissileSlots;

    private LinearLayout btnUpgradeSlots;
    private TextView txtSlotUpgrade;
    private TextView txtSlotUpgradeCost;

    private LinearLayout btnUpgradeReload;
    private TextView txtReloadUpgrade;
    private TextView txtReloadUpgradeCost;
    private LinearLayout btnSetTarget;
    private LinearLayout lytSetTarget;

    private LinearLayout btnSell;

    private boolean bIsMissiles;
    private boolean bOwnedByPlayer = false;
    private boolean bDisplayUpgrades = false;
    private boolean bDisplaySetTarget = false;
    private boolean bDisplaySell = false;
    private boolean bFittedToPlayer;
    private boolean bAircraft = false;
    private boolean bHelicopter = false;
    private boolean bTank = false;
    private boolean bStoredTank = false;
    private boolean bShip = false;
    private boolean bSubmarine = false;

    private MissileSystem system;
    private LaunchEntity host;
    private int lFittedToID;
    private SystemType systemType;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    private List<SlotControl> MissileSlots;

    public MissileSystemControl(LaunchClientGame game, MainActivity activity, int lFittedToID, LaunchEntity host, boolean bMissiles)
    {
        super(game, activity, true);

        this.bIsMissiles = bMissiles;
        this.host = host;
        this.lFittedToID = lFittedToID;

        if(host instanceof MissileSite)
        {
            MissileSite missileSite = (MissileSite)host;

            if(missileSite.GetOwnedBy(game.GetOurPlayerID()))
            {
                bDisplayUpgrades = true;
                bOwnedByPlayer = true;
            }

            systemType = SystemType.MISSILE_SITE;
            system = missileSite.GetMissileSystem();
        }
        else if(host instanceof ArtilleryGun)
        {
            ArtilleryGun artillery = (ArtilleryGun)host;

            if(artillery.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
                bDisplaySetTarget = true;
            }

            systemType = SystemType.ARTILLERY_GUN;
            system = artillery.GetMissileSystem();
        }
        else if(host instanceof SAMSite)
        {
            SAMSite samSite = (SAMSite)host;

            if(samSite.GetOwnedBy(game.GetOurPlayerID()))
            {
                bDisplayUpgrades = true;
                bOwnedByPlayer = true;
            }

            systemType = SystemType.SAM_SITE;
            system = samSite.GetInterceptorSystem();
        }
        else if(host instanceof Ship)
        {
            bShip = true;
            Ship ship = (Ship)host;

            if(ship.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            if(bMissiles)
            {
                systemType = SystemType.SHIP_MISSILES;
                system = ship.GetMissileSystem();
            }
            else
            {
                systemType = SystemType.SHIP_INTERCEPTORS;
                system = ship.GetInterceptorSystem();
            }
        }
        else if(host instanceof Submarine)
        {
            bSubmarine = true;
            Submarine submarine = (Submarine)host;

            if(submarine.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            if(bMissiles)
            {
                systemType = SystemType.SUBMARINE_MISSILES;
                system = submarine.GetMissileSystem();
            }
        }
        else if(host instanceof AirplaneInterface)
        {
            AirplaneInterface aircraft = (AirplaneInterface)host;
            bAircraft = true;

            if(aircraft.GetOwnerID() == game.GetOurPlayerID())
            {
                bOwnedByPlayer = true;
            }

            if(aircraft.Flying())
            {
                if(bMissiles)
                {
                    systemType = SystemType.AIRCRAFT_MISSILES;
                    system = aircraft.GetMissileSystem();
                }
                else
                {
                    systemType = SystemType.AIRCRAFT_INTERCEPTORS;
                    system = aircraft.GetInterceptorSystem();
                }
            }
            else
            {
                if(bMissiles)
                {
                    systemType = SystemType.STORED_AIRCRAFT_MISSILES;
                    system = aircraft.GetMissileSystem();
                }
                else
                {
                    systemType = SystemType.STORED_AIRCRAFT_INTERCEPTORS;
                    system = aircraft.GetInterceptorSystem();
                }
            }
        }
        else if(host instanceof Tank)
        {
            bTank = true;
            Tank tank = (Tank)host;

            if(tank.GetOwnerID() == game.GetOurPlayerID())
            {
                bOwnedByPlayer = true;
            }

            if(tank.HasMissiles())
            {
                systemType = SystemType.TANK_MISSILES;
            }
            else
            {
                systemType = SystemType.TANK_INTERCEPTORS;
            }

            system = tank.GetMissileSystem();
        }
        else if(host instanceof StoredTank)
        {
            bStoredTank = true;
            StoredTank tank = (StoredTank)host;

            if(tank.GetOwnerID() == game.GetOurPlayerID())
            {
                bOwnedByPlayer = true;
            }

            if(tank.HasMissiles())
            {
                systemType = SystemType.STORED_TANK_MISSILES;
            }
            else
            {
                systemType = SystemType.STORED_TANK_INTERCEPTORS;
            }

            system = tank.GetMissileSystem();
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_missile_system, this);

        lytReload = (LinearLayout)findViewById(R.id.lytReload);
        txtReloading = (TextView)findViewById(R.id.txtReloading);
        lytMissileSlots = (LinearLayout)findViewById(R.id.lytMissileSlots);
        btnUpgradeSlots = (LinearLayout) findViewById(R.id.btnUpgradeSlots);
        txtSlotUpgrade = (TextView) findViewById(R.id.txtSlotUpgrade);
        txtSlotUpgradeCost = (TextView) findViewById(R.id.txtSlotUpgradeCost);
        btnUpgradeReload = (LinearLayout) findViewById(R.id.btnUpgradeReload);
        txtReloadUpgrade = (TextView) findViewById(R.id.txtReloadUpgrade);
        txtReloadUpgradeCost = (TextView) findViewById(R.id.txtReloadUpgradeCost);
        btnSell = (LinearLayout) findViewById(R.id.btnSell);
        btnSetTarget = findViewById(R.id.btnSetTarget);
        lytSetTarget = findViewById(R.id.lytSetTarget);

        GenerateSlotTable(system);

        if(bDisplaySell)
        {
            btnSell.setVisibility(VISIBLE);

            btnSell.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderPurchase();
                    launchDialog.SetMessage(context.getString(R.string.sell_confirm, bIsMissiles ? context.getString(R.string.missile_system) : context.getString(R.string.air_defence_system), TextUtilities.GetCostStatement(game.GetSaleValue(GetMissileSystem(), bIsMissiles))));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            if(bIsMissiles)
                                game.SellMissileSystem();
                            else
                                game.SellSAMSystem();
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
            });
        }
        else
        {
            btnSell.setVisibility(GONE);
        }

        if(bDisplaySetTarget)
        {
            lytSetTarget.setVisibility(VISIBLE);

            btnSetTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetOurPlayer().Functioning())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.you_must_respawn));
                    }
                    else if(host instanceof MissileSite && !((MissileSite)host).GetOnline())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
                    }
                    else
                    {
                        activity.SetTargetMode(host.GetPointer(), null);
                    }
                }
            });
        }
        else
        {
            lytSetTarget.setVisibility(GONE);
        }

        if(bDisplayUpgrades && bOwnedByPlayer)
        {
            btnUpgradeSlots.setVisibility(VISIBLE);
            btnUpgradeReload.setVisibility(VISIBLE);

            btnUpgradeSlots.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SlotUpgradeClicked();
                }
            });

            if(!bFittedToPlayer)
            {
                btnUpgradeSlots.setOnLongClickListener(new OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        SlotUpgradeLongClicked();
                        return false;
                    }
                });
            }

            btnUpgradeReload.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    ReloadUpgradeClicked();
                }
            });
        }
        else
        {
            btnUpgradeSlots.setVisibility(GONE);
            btnUpgradeReload.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
        switch(systemType)
        {
            case MISSILE_SITE: host = game.GetMissileSite(lFittedToID); break;
            case ARTILLERY_GUN: host = game.GetArtilleryGun(lFittedToID); break;
            case SAM_SITE: host = game.GetSAMSite(lFittedToID); break;
            case AIRCRAFT_MISSILES:
            case AIRCRAFT_INTERCEPTORS: host = game.GetAirplane(lFittedToID); break;
            case STORED_AIRCRAFT_MISSILES:
            case STORED_AIRCRAFT_INTERCEPTORS: host = game.GetStoredAirplane(lFittedToID); break;
            case TANK_MISSILES:
            case TANK_INTERCEPTORS: host = game.GetTank(lFittedToID); break;
            case STORED_TANK_MISSILES:
            case STORED_TANK_INTERCEPTORS: host = game.GetStoredTank(lFittedToID); break;
            case SHIP_INTERCEPTORS:
            case SHIP_MISSILES: host = game.GetShip(lFittedToID); break;
            case SUBMARINE_MISSILES: host = game.GetSubmarine(lFittedToID); break;
        }

        system = GetMissileSystem();

        if(system != null)
        {
            if(bOwnedByPlayer)
            {
                //Reload.
                long oReloadTimeRemaining = system.GetReloadTimeRemaining();

                if(oReloadTimeRemaining > 0)
                {
                    lytReload.setVisibility(VISIBLE);
                    txtReloading.setText(TextUtilities.GetTimeAmount(oReloadTimeRemaining));
                }
                else
                {
                    lytReload.setVisibility(GONE);
                }

                if(MissileSlots.size() != system.GetSlotCount())
                {
                    GenerateSlotTable(system);
                }

                for(SlotControl slot : MissileSlots)
                {
                    slot.Update();
                }
            }

            if(bOwnedByPlayer && bDisplayUpgrades)
            {
                int lSlots = system.GetSlotCount();

                if(lSlots < 126)
                {
                    Config config = game.GetConfig();

                    int lSlotUpgrade = lSlots + config.GetMissileUpgradeCount();
                    int lCost = game.GetMissileSlotUpgradeCost(system, bIsMissiles ? config.GetInitialMissileSlots() : config.GetInitialInterceptorSlots());
                    txtSlotUpgrade.setText(context.getString(R.string.upgrade, Integer.toString(lSlots), Integer.toString(lSlotUpgrade)));
                    txtSlotUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                    txtSlotUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                    btnUpgradeSlots.setVisibility(VISIBLE);
                }
                else
                {
                    btnUpgradeSlots.setVisibility(GONE);
                }
            }
            else
            {
                btnUpgradeSlots.setVisibility(GONE);
            }

            //Reload upgrade button.
            if(bOwnedByPlayer && bDisplayUpgrades)
            {
                int lCost = game.GetReloadUpgradeCost(system);

                if(lCost < Defs.UPGRADE_COST_MAXED)
                {
                    txtReloadUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                    txtReloadUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                    txtReloadUpgrade.setText(context.getString(R.string.upgrade, TextUtilities.GetTimeAmount(system.GetReloadTime()), TextUtilities.GetTimeAmount(game.GetReloadUpgradeTime(system))));
                    btnUpgradeReload.setVisibility(VISIBLE);
                }
                else
                {
                    btnUpgradeReload.setVisibility(GONE);
                }
            }
            else
            {
                btnUpgradeReload.setVisibility(GONE);
                lytReload.setVisibility(GONE);
            }
        }

        //Sell button.
        btnSell.setVisibility(bDisplaySell ? VISIBLE : GONE);
    }

    @Override
    public void SlotClicked(int lSlotNumber)
    {
        if(system.GetSlotHasMissile(lSlotNumber))
        {
            if(!system.ReadyToFire())
            {
                activity.ShowBasicOKDialog(context.getString(R.string.reloading_cant_fire));
            }
            else if(!system.GetSlotReadyToFire(lSlotNumber))
            {
                activity.ShowBasicOKDialog(context.getString(R.string.preparing_cant_fire));
            }
            else
            {
                if(GetOnline())
                {
                    if(bIsMissiles)
                        activity.MissileTargetMode(host.GetID(), lSlotNumber, systemType, false);
                    else
                        activity.InterceptorTargetMode(host.GetID(), lSlotNumber, systemType);
                }
                else
                {
                    if(bAircraft || bHelicopter)
                        activity.ShowBasicOKDialog(context.getString(R.string.aircraft_grounded_cant_fire));
                    else if(bTank && host instanceof Tank && ((Tank)host).GetOnWater())
                        activity.ShowBasicOKDialog(context.getString(R.string.cant_fire_in_water));
                    else if(bStoredTank)
                        activity.ShowBasicOKDialog(context.getString(R.string.stored_tank_cant_fire));
                    else
                        activity.ShowBasicOKDialog(context.getString(R.string.offline_cant_fire));
                }
            }
        }
        else
        {
            switch(systemType)
            {
                case AIRCRAFT_MISSILES:
                case AIRCRAFT_INTERCEPTORS:
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.flying_cant_purchase));
                }
                break;

                case STORED_AIRCRAFT_MISSILES:
                case STORED_AIRCRAFT_INTERCEPTORS:
                {
                    AirplaneInterface aircraft = (AirplaneInterface)host;

                    if(aircraft != null)
                    {
                        if(!aircraft.Flying() && aircraft.GetHomeBase() != null)
                        {
                            MapEntity homebase = aircraft.GetHomeBase().GetMapEntity(game);
                            boolean bCanArmAircraft = false;

                            if(homebase instanceof Ship)
                                bCanArmAircraft = true;
                            else if(homebase instanceof Airbase && ((Airbase)homebase).GetOnline())
                                bCanArmAircraft = true;

                            if(bCanArmAircraft)
                            {
                                activity.SetView(new PurchaseLaunchableView(game, activity, host, systemType, lSlotNumber));
                            }
                            else
                                activity.ShowBasicOKDialog(context.getString(R.string.airbase_offline_cant_rearm));
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.flying_cant_purchase));
                        }
                    }
                }
                break;

                case MISSILE_SITE:
                case ARTILLERY_GUN:
                case STORED_TANK_MISSILES:
                case TANK_MISSILES:
                case SAM_SITE:
                case TANK_INTERCEPTORS:
                case STORED_TANK_INTERCEPTORS:
                {
                    activity.SetView(new PurchaseLaunchableView(game, activity, host, systemType, lSlotNumber));
                }
                break;

                case SHIP_INTERCEPTORS:
                case SHIP_MISSILES:
                {
                    if(!game.ShipInPort(game.GetShip(lFittedToID)))
                        activity.ShowBasicOKDialog(context.getString(R.string.not_in_port_prep_extended, TextUtilities.GetDistanceStringFromKM(Defs.SHIPYARD_REPAIR_DISTANCE)));

                    activity.SetView(new PurchaseLaunchableView(game, activity, ((LaunchEntity)host), systemType, lSlotNumber));
                }
                break;

                case SUBMARINE_MISSILES:
                case SUBMARINE_ICBM:
                {
                    if(game.GetSubmarine(lFittedToID).Submerged())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.submerged_cant_do_thing));
                        break;
                    }
                    else if(!game.ShipInPort(game.GetSubmarine(lFittedToID)))
                        activity.ShowBasicOKDialog(context.getString(R.string.not_in_port_prep_extended, TextUtilities.GetDistanceStringFromKM(Defs.SHIPYARD_REPAIR_DISTANCE)));

                    activity.SetView(new PurchaseLaunchableView(game, activity, ((LaunchEntity)host), systemType, lSlotNumber));
                }
                break;
            }
        }
    }

    @Override
    public void SlotLongClicked(final int lSlotNumber)
    {
        if(system.GetSlotHasMissile(lSlotNumber))
        {
            int lType = system.GetSlotMissileType(lSlotNumber);
            String strTypeName = "UNSPECIFIED - TELL THE DEV";
            long oCost = 0;

            if(bIsMissiles)
            {
                MissileType type = game.GetConfig().GetMissileType(lType);
                strTypeName = type.GetName();
                oCost = type.GetCost();
            }
            else
            {
                InterceptorType type = game.GetConfig().GetInterceptorType(lType);
                strTypeName = type.GetName();
                oCost = type.GetCost();
            }

            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.sell_confirm, strTypeName, TextUtilities.GetCurrencyString(oCost)));
            launchDialog.SetOnClickYes(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();
                    game.SellLaunchable(host.GetID(), lSlotNumber, systemType);
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

    @Override
    public boolean GetSlotOccupied(int lSlotNumber)
    {
        return GetMissileSystem().GetSlotHasMissile(lSlotNumber);
    }

    @Override
    public String GetSlotContents(int lSlotNumber)
    {
        if(bIsMissiles)
        {
            system = GetMissileSystem();
            Integer lMissileType = system.GetSlotMissileType(lSlotNumber);
            return game.GetConfig().GetMissileType(lMissileType).GetName();
        }
        else
        {
            system = GetMissileSystem();
            Integer lInterceptorType = system.GetSlotMissileType(lSlotNumber);
            return game.GetConfig().GetInterceptorType(lInterceptorType).GetName();
        }
    }

    @Override
    public boolean GetOnline()
    {
        switch(systemType)
        {
            case MISSILE_SITE:
            {
                MissileSite site = (MissileSite)host;
                return site.GetOnline();
            }

            case ARTILLERY_GUN:
            {
                ArtilleryGun artillery = (ArtilleryGun)host;
                return artillery.GetOnline();
            }

            case SAM_SITE:
            {
                SAMSite site = (SAMSite)host;
                return site.GetOnline();
            }

            case AIRCRAFT_INTERCEPTORS:
            case AIRCRAFT_MISSILES:
            case STORED_AIRCRAFT_INTERCEPTORS:
            case STORED_AIRCRAFT_MISSILES:
            {
                AirplaneInterface aircraft = (AirplaneInterface) host;
                return aircraft.Flying();
            }

            case TANK_MISSILES:
            case TANK_ARTILLERY:
            case TANK_INTERCEPTORS:
            {
                return !((Tank)host).GetOnWater();
            }

            case STORED_TANK_MISSILES:
            case STORED_TANK_INTERCEPTORS:
            {
                return false;
            }

            case SUBMARINE_MISSILES:
            {
                return ((Submarine)host).CanFireMissiles();
            }
        }

        return true;
    }

    @Override
    public long GetSlotPrepTime(int lSlotNumber)
    {
        return Math.max(system.GetSlotPrepTimeRemaining(lSlotNumber), system.GetReloadTimeRemaining());
    }

    @Override
    public SlotControl.ImageType GetImageType(int lSlotNumber)
    {
        if(bIsMissiles)
        {
            if(system.GetSlotHasMissile(lSlotNumber))
            {
                if(game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetNuclear())
                {
                    return SlotControl.ImageType.NUKE;
                }
                else if(game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetArtillery())
                {
                    return SlotControl.ImageType.ARTILLERY;
                }
            }

            return SlotControl.ImageType.MISSILE;
        }

        return SlotControl.ImageType.INTERCEPTOR;
    }

    private void SlotUpgradeClicked()
    {
        Config config = game.GetConfig();

        int lSlots = system.GetSlotCount();
        int lSlotUpgrade = lSlots + config.GetMissileUpgradeCount();
        final int lCost = game.GetMissileSlotUpgradeCost(system, bIsMissiles ? config.GetInitialMissileSlots() : config.GetInitialInterceptorSlots());

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

                String strConfirm;

                if(bFittedToPlayer)
                {
                    strConfirm = context.getString(R.string.upgrade_slots_confirm, lSlots, lSlotUpgrade, TextUtilities.GetCurrencyString(lCost));
                }
                else
                {
                    strConfirm = context.getString(R.string.upgrade_slots_confirm_site, lSlots, lSlotUpgrade, TextUtilities.GetCurrencyString(lCost));
                }

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(strConfirm);
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        if(bFittedToPlayer)
                        {
                            if(bIsMissiles)
                                game.PurchaseMissileSlotUpgradePlayer();
                            else
                                game.PurchaseSAMSlotUpgradePlayer();
                        }
                        else
                        {
                            if(bIsMissiles)
                                game.PurchaseMissileSlotUpgrade(host.GetID());
                            else
                                game.PurchaseSAMSlotUpgrade(host.GetID());
                        }

                        Sounds.PlayEquip();
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
                //Confirmation window has already been shown once, now just upgrade it so there's less hassle for the player.
                if(bFittedToPlayer)
                {
                    if(bIsMissiles)
                        game.PurchaseMissileSlotUpgradePlayer();
                    else
                        game.PurchaseSAMSlotUpgradePlayer();
                }
                else
                {
                    if(bIsMissiles)
                        game.PurchaseMissileSlotUpgrade(host.GetID());
                    else
                        game.PurchaseSAMSlotUpgrade(host.GetID());
                }

                Sounds.PlayEquip();
            }
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }

    private void SlotUpgradeLongClicked()
    {
        Config config = game.GetConfig();

        final int lCost = game.GetMissileSlotUpgradeCostToMax(system, bIsMissiles ? config.GetInitialMissileSlots() : config.GetInitialInterceptorSlots());

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.upgrade_slots_to_max_confirm, Defs.MAX_MISSILE_SLOTS - 1, TextUtilities.GetCurrencyString(lCost)));
            launchDialog.SetOnClickYes(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();

                    if(bIsMissiles)
                        game.PurchaseMissileSlotUpgradeToMax(host.GetID());
                    else
                        game.PurchaseInterceptorSlotUpgradeToMax(host.GetID());
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
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }

    private void ReloadUpgradeClicked()
    {
        int lCost = game.GetReloadUpgradeCost(system);

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.upgrade_reload_confirm, TextUtilities.GetTimeAmount(system.GetReloadTime()), TextUtilities.GetTimeAmount(game.GetReloadUpgradeTime(system)), TextUtilities.GetCurrencyString(game.GetReloadUpgradeCost(system))));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        if(bFittedToPlayer)
                        {
                            if(bIsMissiles)
                                game.PurchaseMissileReloadUpgradePlayer();
                            else
                                game.PurchaseSAMReloadUpgradePlayer();
                        }
                        else
                        {
                            if(bIsMissiles)
                                game.PurchaseMissileReloadUpgrade(host.GetID());
                            else
                                game.PurchaseSAMReloadUpgrade(host.GetID());
                        }

                        Sounds.PlayEquip();
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
                if(bFittedToPlayer)
                {
                    if(bIsMissiles)
                        game.PurchaseMissileReloadUpgradePlayer();
                    else
                        game.PurchaseSAMReloadUpgradePlayer();
                }
                else
                {
                    if(bIsMissiles)
                        game.PurchaseMissileReloadUpgrade(host.GetID());
                    else
                        game.PurchaseSAMReloadUpgrade(host.GetID());
                }

                Sounds.PlayEquip();
            }
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }

    private void GenerateSlotTable(MissileSystem system)
    {
        lytMissileSlots.removeAllViews();
        MissileSlots = new ArrayList<>();

        for(int i = 0; i < system.GetSlotCount(); i++)
        {
            SlotControl slotControl = new SlotControl(game, activity, this, i, bOwnedByPlayer);
            MissileSlots.add(slotControl);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            slotControl.setLayoutParams(layoutParams);
            lytMissileSlots.addView(slotControl);
        }
    }

    private MissileSystem GetMissileSystem()
    {
        if(host instanceof MissileSite)
            return game.GetMissileSite(host.GetID()).GetMissileSystem();
        else if(host instanceof SAMSite)
            return game.GetSAMSite(host.GetID()).GetInterceptorSystem();
        else if(host instanceof ArtilleryGun)
            return game.GetArtilleryGun(host.GetID()).GetMissileSystem();
        else if(host instanceof AirplaneInterface)
        {
            if(((AirplaneInterface) host).Flying())
            {
                return bIsMissiles ? game.GetAirplane(host.GetID()).GetMissileSystem() : game.GetAirplane(host.GetID()).GetInterceptorSystem();
            }
            else
            {
                return bIsMissiles ? game.GetStoredAirplane(host.GetID()).GetMissileSystem() : game.GetStoredAirplane(host.GetID()).GetInterceptorSystem();
            }
        }
        else if(host instanceof TankInterface)
            return game.GetTank(host.GetID()).GetMissileSystem();
        else if(host instanceof Ship && bIsMissiles)
            return game.GetShip(host.GetID()).GetMissileSystem();
        else if(host instanceof Ship)
            return game.GetShip(host.GetID()).GetInterceptorSystem();
        else if(host instanceof Submarine && bIsMissiles)
            return game.GetSubmarine(host.GetID()).GetMissileSystem();

        return null;
    }
}
