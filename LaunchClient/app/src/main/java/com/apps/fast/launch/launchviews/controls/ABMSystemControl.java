package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.PurchaseLaunchableView;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 19/10/16.
 */
public class ABMSystemControl extends LaunchView implements SlotListener
{
    private LinearLayout lytReload;
    private TextView txtReloading;
    private LinearLayout lytMissileSlots;

    private LinearLayout btnUpgradeSlots;

    private LinearLayout btnUpgradeReload;

    private int lFittedToID;
    private boolean bIsMissiles;
    private boolean bOwnedByPlayer;
    private MissileSystem.SystemType systemType = MissileSystem.SystemType.SAM_SITE;

    private List<SlotControl> MissileSlots;

    public ABMSystemControl(LaunchClientGame game, MainActivity activity, int lHostID)
    {
        super(game, activity, true);
        lFittedToID = lHostID;
        this.bIsMissiles = false;

        bOwnedByPlayer = game.GetSAMSite(lFittedToID).GetOwnerID() == game.GetOurPlayerID();

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

        final MissileSystem system = GetMissileSystem();

        //Create table of missile slots.
        GenerateSlotTable(system);

        //Set upgrade visibility.
        btnUpgradeSlots.setVisibility(GONE);
        btnUpgradeReload.setVisibility(GONE);

        Update();
    }

    @Override
    public void Update()
    {
        MissileSystem system = GetMissileSystem();

        if(bOwnedByPlayer)
        {
            btnUpgradeReload.setVisibility(GONE);
            btnUpgradeSlots.setVisibility(GONE);

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
                if(!GetOnline())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.offline_cant_fire));
                }
                else
                {

                    activity.InterceptorTargetMode(lFittedToID, lSlotNumber, systemType);

                }
            }
        }
        else
        {
            activity.SetView(new PurchaseLaunchableView(game, activity, game.GetSAMSite(lFittedToID), systemType, lSlotNumber));
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
            InterceptorType type = game.GetConfig().GetInterceptorType(lType);
            strTypeName = type.GetName();
            long oCost = type.GetCost();

            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.sell_confirm, strTypeName, TextUtilities.GetCurrencyString(oCost)));
            launchDialog.SetOnClickYes(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();

                    game.SellLaunchable(lFittedToID, lSlotNumber, systemType);
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
        MissileSystem system = GetMissileSystem();

        return bIsMissiles ? game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetName() : game.GetConfig().GetInterceptorType(system.GetSlotMissileType(lSlotNumber)).GetName();
    }

    @Override
    public boolean GetOnline()
    {
        return true;
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
        if(bIsMissiles)
        {
            MissileSystem system = GetMissileSystem();

            if (system.GetSlotHasMissile(lSlotNumber))
            {
                if (game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetNuclear())
                {
                    return SlotControl.ImageType.NUKE;
                }
            }

            return SlotControl.ImageType.MISSILE;
        }

        return SlotControl.ImageType.INTERCEPTOR;
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
        return game.GetSAMSite(lFittedToID).GetInterceptorSystem();
    }
}
