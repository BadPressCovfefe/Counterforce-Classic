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
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.entities.Submarine;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.LaunchSystem;
import launch.game.systems.MissileSystem;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 19/10/16.
 */
public class SLBMSystemControl extends LaunchView implements SlotListener
{
    private LinearLayout lytReload;
    private TextView txtReloading;
    private LinearLayout lytMissileSlots;
    private LinearLayout btnUpgradeSlots;
    private LinearLayout btnUpgradeReload;
    private LinearLayout btnSell;
    private int lFittedToID;
    private boolean bOwnedByPlayer;
    private boolean bRed = true;
    private Submarine hostSub;
    private List<SlotControl> MissileSlots;

    public SLBMSystemControl(LaunchClientGame game, MainActivity activity, int lHostID)
    {
        super(game, activity, true);
        lFittedToID = lHostID;
        this.hostSub = game.GetSubmarine(lHostID);

        if(hostSub != null)
            bOwnedByPlayer = game.GetOurPlayerID() == hostSub.GetOwnerID();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_missile_system, this);

        lytReload = findViewById(R.id.lytReload);
        txtReloading = findViewById(R.id.txtReloading);
        lytMissileSlots = findViewById(R.id.lytMissileSlots);

        btnUpgradeSlots = findViewById(R.id.btnUpgradeSlots);

        btnUpgradeReload = findViewById(R.id.btnUpgradeReload);

        btnSell = findViewById(R.id.btnSell);

        final MissileSystem system = GetMissileSystem();

        //Create table of missile slots.
        GenerateSlotTable(system);

        //Set upgrade visibility.
        btnUpgradeSlots.setVisibility(GONE);
        btnUpgradeReload.setVisibility(GONE);
        btnSell.setVisibility(GONE);

        Update();
    }

    @Override
    public void Update()
    {
        Submarine submarine = game.GetSubmarine(lFittedToID);

        if(bOwnedByPlayer)
        {
            MissileSystem system = GetMissileSystem();

            //Reload.
            long oReloadTimeRemaining = system.GetReloadTimeRemaining();

            if(oReloadTimeRemaining > 0)
            {
                lytReload.setVisibility(GONE);
                txtReloading.setText(TextUtilities.GetTimeAmount(oReloadTimeRemaining));
            }
            else
            {
                lytReload.setVisibility(GONE);
            }

            for(SlotControl slot : MissileSlots)
            {
                //Update all slots.
                slot.Update();
            }
        }
        else
        {
            lytReload.setVisibility(GONE);
        }
    }

    @Override
    public void SlotClicked(int lSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

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
                activity.MissileTargetMode(lFittedToID, lSlotNumber, LaunchSystem.SystemType.SUBMARINE_ICBM, false);
            }
        }
        else
        {
            activity.SetView(new PurchaseLaunchableView(game, activity, game.GetSubmarine(lFittedToID), LaunchSystem.SystemType.SUBMARINE_ICBM, lSlotNumber));
        }
    }

    @Override
    public void SlotLongClicked(final int lSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

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

                    game.SellLaunchable(lFittedToID, lSlotNumber, MissileSystem.SystemType.SUBMARINE_ICBM);
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
        return GetMissileSystem().GetSlotHasMissile(lSlotNumber);
    }

    @Override
    public String GetSlotContents(int lSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        return game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetName();
    }

    @Override
    public boolean GetOnline()
    {
        return game.GetSubmarine(lFittedToID).CanFireMissiles();
    }

    @Override
    public long GetSlotPrepTime(int lSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        return Math.max(system.GetSlotPrepTimeRemaining(lSlotNumber), system.GetReloadTimeRemaining());
    }

    @Override
    public SlotControl.ImageType GetImageType(int lSlotNumber)
    {
        return SlotControl.ImageType.NUKE;
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
        return game.GetSubmarine(lFittedToID).GetICBMSystem();
    }
}
