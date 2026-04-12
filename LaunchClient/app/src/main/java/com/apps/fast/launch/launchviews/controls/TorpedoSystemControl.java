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
import com.apps.fast.launch.launchviews.PurchaseTorpedoView;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Airbase;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.NavalVessel;
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
import launch.game.types.TorpedoType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 19/10/16.
 */
public class TorpedoSystemControl extends LaunchView implements SlotListener
{
    private LinearLayout lytReload;
    private TextView txtReloading;
    private LinearLayout lytMissileSlots;

    private LinearLayout btnUpgradeSlots;

    private LinearLayout btnUpgradeReload;

    private LinearLayout btnSell;

    private boolean bOwnedByPlayer = false;
    private boolean bShip = false;
    private boolean bSubmarine = false;

    private MissileSystem system;
    private LaunchEntity host;
    private int lFittedToID;
    private SystemType systemType;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    private List<SlotControl> MissileSlots;

    public TorpedoSystemControl(LaunchClientGame game, MainActivity activity, int lFittedToID, LaunchEntity host)
    {
        super(game, activity, true);

        this.host = host;
        this.lFittedToID = lFittedToID;

        if(host instanceof Ship)
        {
            bShip = true;
            Ship ship = (Ship)host;

            if(ship.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            systemType = SystemType.SHIP_TORPEDOES;
            system = ship.GetTorpedoSystem();
        }
        else if(host instanceof Submarine)
        {
            bSubmarine = true;
            Submarine submarine = (Submarine)host;

            if(submarine.GetOwnedBy(game.GetOurPlayerID()))
            {
                bOwnedByPlayer = true;
            }

            systemType = SystemType.SUBMARINE_TORPEDO;
            system = submarine.GetTorpedoSystem();
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
        btnUpgradeReload = (LinearLayout) findViewById(R.id.btnUpgradeReload);
        btnSell = (LinearLayout) findViewById(R.id.btnSell);

        GenerateSlotTable(system);

        btnSell.setVisibility(GONE);
        btnUpgradeSlots.setVisibility(GONE);
        btnUpgradeReload.setVisibility(GONE);

        Update();
    }

    @Override
    public void Update()
    {
        switch(systemType)
        {
            case SHIP_TORPEDOES: host = game.GetShip(lFittedToID); break;
            case SUBMARINE_TORPEDO: host = game.GetSubmarine(lFittedToID); break;
        }

        system = GetTorpedoSystem();

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
                activity.ShowBasicOKDialog(context.getString(R.string.preparing_cant_fire_torpedo));
            }
            else
            {
                activity.TorpedoTargetMode(host.GetID(), lSlotNumber, systemType);
            }
        }
        else
        {
            switch(systemType)
            {
                case SHIP_TORPEDOES:
                {
                    if(!game.ShipInPort(game.GetShip(lFittedToID)))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.not_in_port_cant_rearm, TextUtilities.GetDistanceStringFromKM(Defs.SHIPYARD_REPAIR_DISTANCE)));
                    }
                    else
                        activity.SetView(new PurchaseTorpedoView(game, activity, lSlotNumber, (NavalVessel)host));
                }
                break;

                case SUBMARINE_TORPEDO:
                {
                    if(game.GetSubmarine(lFittedToID).Submerged())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.submerged_cant_do_thing));
                        break;
                    }
                    else if(!game.ShipInPort(game.GetSubmarine(lFittedToID)))
                        activity.ShowBasicOKDialog(context.getString(R.string.not_in_port_prep_extended, TextUtilities.GetDistanceStringFromKM(Defs.SHIPYARD_REPAIR_DISTANCE)));

                    activity.SetView(new PurchaseTorpedoView(game, activity, lSlotNumber, (NavalVessel)host));
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
            TorpedoType type = game.GetConfig().GetTorpedoType(lType);
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
                    game.SellLaunchable(host.GetID(), lSlotNumber, systemType);
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
        return GetTorpedoSystem().GetSlotHasMissile(lSlotNumber);
    }

    @Override
    public String GetSlotContents(int lSlotNumber)
    {
        system = GetTorpedoSystem();
        Integer lTorpedoType = system.GetSlotMissileType(lSlotNumber);
        return game.GetConfig().GetTorpedoType(lTorpedoType).GetName();
    }

    @Override
    public boolean GetOnline()
    {
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
        if (system.GetSlotHasMissile(lSlotNumber))
        {
            if (game.GetConfig().GetTorpedoType(system.GetSlotMissileType(lSlotNumber)).GetNuclear())
            {
                return SlotControl.ImageType.NUKE;
            }
        }

        return SlotControl.ImageType.TORPEDO;
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

    private MissileSystem GetTorpedoSystem()
    {
        if(host instanceof Ship)
            return game.GetShip(host.GetID()).GetTorpedoSystem();
        else if(host instanceof Submarine)
            return game.GetSubmarine(host.GetID()).GetTorpedoSystem();

        return null;
    }
}
