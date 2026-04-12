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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import launch.comm.clienttasks.CeaseFireTask;
import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.ArtilleryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.Player;
import launch.game.entities.SAMSite;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.entities.Tank;
import launch.game.entities.TankInterface;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 19/10/16.
 */
public class ArtillerySystemControl extends LaunchView implements SlotListener
{
    private LinearLayout lytReload;
    private TextView txtReloading;
    private LinearLayout lytMissileSlots;

    private LinearLayout btnSetTarget;
    private LinearLayout btnCeaseFire;

    private boolean bOwnedByPlayer = false;

    private MissileSystem system;
    private ArtilleryInterface host;
    private int lFittedToID;
    private SystemType systemType;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    private List<SlotControl> MissileSlots;

    public ArtillerySystemControl(LaunchClientGame game, MainActivity activity, int lFittedToID, ArtilleryInterface host)
    {
        super(game, activity, true);

        this.host = host;
        this.lFittedToID = lFittedToID;

        if(host instanceof ArtilleryGun)
        {
            ArtilleryGun artillery = (ArtilleryGun)host;

            if(artillery.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            systemType = SystemType.ARTILLERY_GUN;
            system = artillery.GetMissileSystem();
        }
        else if(host instanceof Ship)
        {
            Ship ship = (Ship)host;

            if(ship.HasArtillery() && ship.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            systemType = SystemType.SHIP_ARTILLERY;
            system = ship.GetArtillerySystem();
        }
        else if(host instanceof Tank)
        {
            Tank tank = (Tank)host;

            if(tank.HasArtillery() && tank.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            systemType = SystemType.TANK_ARTILLERY;
            system = tank.GetMissileSystem();
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_artillery_system, this);

        lytReload = (LinearLayout)findViewById(R.id.lytReload);
        txtReloading = (TextView)findViewById(R.id.txtReloading);
        lytMissileSlots = (LinearLayout)findViewById(R.id.lytMissileSlots);
        btnSetTarget = findViewById(R.id.btnSetTarget);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);

        GenerateSlotTable(system);

        if(bOwnedByPlayer && (host instanceof ArtilleryGun || (host instanceof Ship && ((Ship)host).HasArtillery()) || (host instanceof Tank && ((Tank)host).HasArtillery())))
        {
            btnSetTarget.setVisibility(VISIBLE);

            btnSetTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetOurPlayer().Functioning())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.you_must_respawn));
                    }
                    else if(host instanceof Tank && ((Tank)host).GetOnWater())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.cant_fire_in_water));
                    }
                    else if(host instanceof ArtilleryGun && !((ArtilleryGun)host).GetOnline())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.structure_offline));
                    }
                    else
                    {
                        activity.SetTargetMode(((LaunchEntity)host).GetPointer(), null);
                    }
                }
            });

            if(host.HasFireOrder())
            {
                btnCeaseFire.setVisibility(VISIBLE);

                btnCeaseFire.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.cease_fire_artillery_confirm));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                game.CeaseFire(Collections.singletonList(((LaunchEntity)host).GetPointer()));
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
                btnCeaseFire.setVisibility(GONE);
            }
        }
        else
        {
            btnSetTarget.setVisibility(GONE);
            btnCeaseFire.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
        switch(systemType)
        {
            case ARTILLERY_GUN: host = game.GetArtilleryGun(lFittedToID); break;
            case SHIP_ARTILLERY: host = game.GetShip(lFittedToID); break;
            case TANK_ARTILLERY: host = game.GetTank(lFittedToID); break;
        }

        system = GetMissileSystem();

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

            if(host.HasFireOrder() && bOwnedByPlayer && (host instanceof ArtilleryGun || (host instanceof Tank && ((Tank)host).HasArtillery())))
            {
                btnCeaseFire.setVisibility(VISIBLE);
            }
            else
            {
                btnCeaseFire.setVisibility(GONE);
            }
        }
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
                    activity.MissileTargetMode(((LaunchEntity)host).GetID(), lSlotNumber, systemType, false);
                }
                else if(host instanceof Tank && ((Tank)host).GetOnWater())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_fire_in_water));
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.offline_cant_fire));
                }
            }
        }
        else
        {
            switch(systemType)
            {
                case TANK_ARTILLERY:
                case ARTILLERY_GUN:
                {
                    activity.SetView(new PurchaseLaunchableView(game, activity, ((LaunchEntity)host), systemType, lSlotNumber));
                }
                break;

                case SHIP_ARTILLERY:
                {
                    if(!game.ShipInPort(game.GetShip(lFittedToID)))
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
            MissileType type = game.GetConfig().GetMissileType(lType);
            strTypeName = type.GetName();
            long oCost = type.GetCost();

            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.sell_confirm, strTypeName, TextUtilities.GetCurrencyString(oCost)));
            launchDialog.SetOnClickYes(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();
                    game.SellLaunchable(((LaunchEntity)host).GetID(), lSlotNumber, systemType);
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
    }

    @Override
    public boolean GetSlotOccupied(int lSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        if(system == null)
        {
            Finish(true);
            return false;
        }
        else
        {
            return system.GetSlotHasMissile(lSlotNumber);
        }
    }

    @Override
    public String GetSlotContents(int lSlotNumber)
    {
        system = GetMissileSystem();
        Integer lMissileType = system.GetSlotMissileType(lSlotNumber);
        return game.GetConfig().GetMissileType(lMissileType).GetName();
    }

    @Override
    public boolean GetOnline()
    {
        switch(systemType)
        {
            case ARTILLERY_GUN:
            {
                ArtilleryGun artillery = (ArtilleryGun)host;
                return artillery.GetOnline();
            }

            case TANK_ARTILLERY:
            {
                return !((Tank)host).GetOnWater();
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
        if(host instanceof ArtilleryGun)
        {
            if(game.GetArtilleryGun(((LaunchEntity)host).GetID()) != null)
                return game.GetArtilleryGun(((LaunchEntity)host).GetID()).GetMissileSystem();
        }
        else if(host instanceof Ship)
        {
            if(game.GetShip(((LaunchEntity)host).GetID()) != null)
                return game.GetShip(((LaunchEntity)host).GetID()).GetArtillerySystem();
        }
        else if(host instanceof Tank)
        {
            if(game.GetTank(((LaunchEntity)host).GetID()) != null)
                return game.GetTank(((LaunchEntity)host).GetID()).GetMissileSystem();
        }

        return null;
    }
}
