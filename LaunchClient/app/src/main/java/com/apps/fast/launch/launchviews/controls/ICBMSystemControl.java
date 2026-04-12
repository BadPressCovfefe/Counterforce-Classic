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
import launch.game.LaunchClientGame;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.LaunchSystem;
import launch.game.systems.MissileSystem;
import launch.game.types.MissileType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 19/10/16.
 */
public class ICBMSystemControl extends LaunchView implements SlotListener
{
    private LinearLayout lytReload;
    private TextView txtReloading;
    private LinearLayout lytMissileSlots;

    private LinearLayout btnUpgradeSlots;

    private LinearLayout btnUpgradeReload;

    private TextView txtRetaliating;
    private TextView txtLaunching;
    private LinearLayout btnCancelFire;
    private LinearLayout btnSell;

    private int lFittedToID;
    private boolean bFittedToPlayer;
    private boolean bIsMissiles;
    private boolean bOwnedByPlayer;

    private boolean bRed = true;

    private MissileSite hostSite;

    private List<SlotControl> MissileSlots;

    public ICBMSystemControl(LaunchClientGame game, MainActivity activity, int lHostID, boolean bIsMissiles, boolean bHostIsPlayer)
    {
        super(game, activity, true);
        lFittedToID = lHostID;
        bFittedToPlayer = bHostIsPlayer;
        this.bIsMissiles = bIsMissiles;
        this.hostSite = game.GetMissileSite(lFittedToID);

        if(hostSite != null)
            bOwnedByPlayer = game.GetOurPlayerID() == hostSite.GetOwnerID();

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_missile_system, this);

        lytReload = (LinearLayout)findViewById(R.id.lytReload);
        txtReloading = (TextView)findViewById(R.id.txtReloading);
        lytMissileSlots = (LinearLayout)findViewById(R.id.lytMissileSlots);
        txtRetaliating = findViewById(R.id.txtRetaliating);
        txtLaunching = findViewById(R.id.txtLaunching);
        btnCancelFire = findViewById(R.id.btnCancelFire);

        btnUpgradeSlots = (LinearLayout) findViewById(R.id.btnUpgradeSlots);

        btnUpgradeReload = (LinearLayout) findViewById(R.id.btnUpgradeReload);

        btnSell = (LinearLayout) findViewById(R.id.btnSell);

        final MissileSystem system = GetMissileSystem();

        //Create table of missile slots.
        GenerateSlotTable(system);

        //Set upgrade visibility.
        btnUpgradeSlots.setVisibility(GONE);
        btnUpgradeReload.setVisibility(GONE);

        if(bOwnedByPlayer)
        {
            btnUpgradeSlots.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SlotUpgradeClicked();
                }
            });

            btnUpgradeReload.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    ReloadUpgradeClicked();
                }
            });
        }

        Update();
    }

    @Override
    public void Update()
    {
        MissileSite site = game.GetMissileSite(lFittedToID);

        /*bRed = !bRed;

        if(site != null)
        {
            txtRetaliating.setVisibility(site.Retaliating() ? VISIBLE : GONE);
            txtRetaliating.setTextColor(Utilities.ColourFromAttr(context, bRed ? R.attr.BadColour : R.attr.WarningColour));
        }*/

        if(bOwnedByPlayer)
        {
            MissileSystem system = GetMissileSystem();

            btnUpgradeSlots.setVisibility(GONE);
            btnUpgradeReload.setVisibility(GONE);

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

        //Sell button.
        btnSell.setVisibility(bFittedToPlayer && bOwnedByPlayer ? VISIBLE : GONE);
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
                    activity.MissileTargetMode(lFittedToID, lSlotNumber, LaunchSystem.SystemType.MISSILE_SITE, false);
                }
            }
        }
        else
        {
            activity.SetView(new PurchaseLaunchableView(game, activity, game.GetMissileSite(lFittedToID), LaunchSystem.SystemType.MISSILE_SITE, lSlotNumber));
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
            launchDialog.SetOnClickYes(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();

                    game.SellLaunchable(lFittedToID, lSlotNumber, MissileSystem.SystemType.MISSILE_SITE);
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
        if(!bFittedToPlayer)
        {
            if(bIsMissiles)
            {
                MissileSite site = game.GetMissileSite(lFittedToID);
                return site.GetOnline();
            }
            else
            {
                SAMSite site = game.GetSAMSite(lFittedToID);
                return site.GetOnline();
            }
        }

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
                if (game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetNuclear() || game.GetConfig().GetMissileType(system.GetSlotMissileType(lSlotNumber)).GetICBM())
                {
                    return SlotControl.ImageType.NUKE;
                }
            }

            return SlotControl.ImageType.MISSILE;
        }

        return SlotControl.ImageType.INTERCEPTOR;
    }

    private void SlotUpgradeClicked()
    {
        MissileSystem system = GetMissileSystem();

        Config config = game.GetConfig();

        int lSlots = system.GetSlotCount();
        int lSlotUpgrade = lSlots + config.GetMissileUpgradeCount();
        final int lCost = game.GetMissileSlotUpgradeCost(system, bIsMissiles ? config.GetInitialMissileSlots() : config.GetInitialInterceptorSlots());

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.upgrade_slots_confirm, lSlots, lSlotUpgrade, TextUtilities.GetCurrencyString(lCost)));
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
                            game.PurchaseMissileSlotUpgrade(lFittedToID);
                        else
                            game.PurchaseSAMSlotUpgrade(lFittedToID);
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
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }

    private void ReloadUpgradeClicked()
    {
        MissileSystem system = GetMissileSystem();

        int lCost = game.GetReloadUpgradeCost(system);

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
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
                            game.PurchaseMissileReloadUpgrade(lFittedToID);
                        else
                            game.PurchaseSAMReloadUpgrade(lFittedToID);
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
        if(game.GetMissileSite(lFittedToID) != null)
            return game.GetMissileSite(lFittedToID).GetMissileSystem();
        else
            return null;
    }
}
