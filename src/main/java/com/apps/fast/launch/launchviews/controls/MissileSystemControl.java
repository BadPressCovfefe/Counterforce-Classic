package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.PurchaseLaunchableView;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.List;

import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.MissileSite;
import launch.game.entities.Player;
import launch.game.entities.SAMSite;
import launch.game.systems.MissileSystem;
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

    private TextView txtHitChance;
    private TextView txtHitChanceTitle;

    private TextView txtMissileSiteDescription;
    private TextView txtSAMDescription;

    private LinearLayout btnSell;

    private int lFittedToID;
    private boolean bFittedToPlayer;
    private boolean bIsMissiles;
    private boolean bOwnedByPlayer;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    private List<SlotControl> MissileSlots;

    public MissileSystemControl(LaunchClientGame game, MainActivity activity, int lHostID, boolean bIsMissiles, boolean bHostIsPlayer)
    {
        super(game, activity, true);
        lFittedToID = lHostID;
        bFittedToPlayer = bHostIsPlayer;
        this.bIsMissiles = bIsMissiles;

        if(bFittedToPlayer)
        {
            bOwnedByPlayer = game.GetOurPlayerID() == lFittedToID;
        }
        else
        {
            if(bIsMissiles)
            {
                if(game.GetMissileSite(lFittedToID) != null)
                {
                    bOwnedByPlayer = game.GetOurPlayerID() == game.GetMissileSite(lFittedToID).GetOwnerID();
                }
                else
                {
                    bOwnedByPlayer = false;
                }
            }
            else
            {
                if(game.GetSAMSite(lFittedToID) != null)
                {
                    bOwnedByPlayer = game.GetOurPlayerID() == game.GetSAMSite(lFittedToID).GetOwnerID();
                }
                else
                {
                    bOwnedByPlayer = false;
                }
            }
        }

        bOwnedByPlayer = bFittedToPlayer ? game.GetOurPlayerID() == lFittedToID : game.GetOurPlayerID() == (bIsMissiles ? game.GetMissileSite(lFittedToID).GetOwnerID() : game.GetSAMSite(lFittedToID).GetOwnerID()); //TODO: This line causes a null object reference crash because the lFittedToID references a null missile/samsite.

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

        txtHitChance = findViewById(R.id.txtHitChance);
        txtHitChanceTitle = findViewById(R.id.HitChance_Title);

        txtMissileSiteDescription = findViewById(R.id.txtMissileSiteDescription);
        txtSAMDescription = findViewById(R.id.txtSAMDescription);

        txtHitChance.setVisibility(bIsMissiles || bFittedToPlayer ? GONE : VISIBLE); //Corbin TODO: Remove this line when missile accuracy is implemented.
        txtHitChanceTitle.setVisibility(bIsMissiles || bFittedToPlayer ? GONE : VISIBLE);

        if(!bFittedToPlayer && !bIsMissiles)
        {
            txtHitChance.setText(TextUtilities.GetAccuracyPercentage(game.GetRadarAccuracyBonus(game.GetSAMSite(lFittedToID))));
            txtSAMDescription.setVisibility(VISIBLE);
        }

        if(!bFittedToPlayer && bIsMissiles)
        {
            txtMissileSiteDescription.setVisibility(VISIBLE);
        }

        final MissileSystem system = GetMissileSystem();

        //Create table of missile slots.
        GenerateSlotTable(system);

        //Set upgrade visibility.
        btnUpgradeSlots.setVisibility(bOwnedByPlayer? VISIBLE : GONE);

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

        if(bFittedToPlayer && bOwnedByPlayer)
        {
            btnSell.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderPurchase();
                    launchDialog.SetMessage(context.getString(R.string.sell_confirm, bIsMissiles ? context.getString(R.string.missile_system) : context.getString(R.string.air_defence_system), TextUtilities.GetCurrencyString(game.GetSaleValue(GetMissileSystem(), bIsMissiles))));
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

        Update();
    }

    @Override
    public void Update()
    {
        MissileSystem system = GetMissileSystem();

        //Slot upgrade.
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

            //Slots.
            if(MissileSlots.size() != system.GetSlotCount())
            {
                //Regenerate slot table when upgraded.
                GenerateSlotTable(system);
            }

            for(SlotControl slot : MissileSlots)
            {
                //Update all slots.
                slot.Update();
            }

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
        if(bOwnedByPlayer)
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

        if(!bFittedToPlayer && !bIsMissiles)
        {
            txtHitChance.setText(TextUtilities.GetAccuracyPercentage(game.GetRadarAccuracyBonus(game.GetSAMSite(lFittedToID))));
        }

        //Sell button.
        btnSell.setVisibility(bFittedToPlayer && bOwnedByPlayer ? VISIBLE : GONE);
    }

    @Override
    public void SlotClicked(byte cSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        if(system.GetSlotHasMissile(cSlotNumber))
        {
            if(!system.ReadyToFire())
            {
                activity.ShowBasicOKDialog(context.getString(R.string.reloading_cant_fire));
            }
            else if(!system.GetSlotReadyToFire(cSlotNumber))
            {
                activity.ShowBasicOKDialog(context.getString(R.string.preparing_cant_fire));
            }
            else
            {
                if(bFittedToPlayer)
                {
                    if(bIsMissiles)
                        activity.MissileTargetModePlayer(cSlotNumber);
                    else
                        activity.InterceptorTargetModePlayer(cSlotNumber);
                }
                else
                {
                    if(!GetOnline())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.offline_cant_fire));
                    }
                    else
                    {
                        if (bIsMissiles)
                        {
                            activity.MissileTargetMode(lFittedToID, cSlotNumber);
                        } else
                        {
                            activity.InterceptorTargetMode(lFittedToID, cSlotNumber);
                        }
                    }
                }
            }
        }
        else
        {
            if(bFittedToPlayer)
            {
                Player player = game.GetPlayer(lFittedToID);
                activity.SetView(new PurchaseLaunchableView(game, activity, bIsMissiles, cSlotNumber));
            }
            else
            {
                if(bIsMissiles)
                {
                    MissileSite site = game.GetMissileSite(lFittedToID);
                    activity.SetView(new PurchaseLaunchableView(game, activity, site, cSlotNumber));
                }
                else
                {
                    SAMSite site = game.GetSAMSite(lFittedToID);
                    activity.SetView(new PurchaseLaunchableView(game, activity, site, cSlotNumber));
                }
            }
        }
    }

    @Override
    public void SlotLongClicked(final byte cSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        if(system.GetSlotHasMissile(cSlotNumber))
        {
            int lType = system.GetSlotMissileType(cSlotNumber);
            String strTypeName = "UNSPECIFIED - TELL THE DEV";
            int lCost = Integer.MAX_VALUE;

            if(bIsMissiles)
            {
                MissileType type = game.GetConfig().GetMissileType(lType);
                strTypeName = type.GetName();
                lCost = game.GetSaleValue(game.GetConfig().GetMissileCost(type));
            }
            else
            {
                InterceptorType type = game.GetConfig().GetInterceptorType(lType);
                strTypeName = type.GetName();
                lCost = game.GetSaleValue(game.GetConfig().GetInterceptorCost(type));
            }

            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.sell_confirm, strTypeName, TextUtilities.GetCurrencyString(lCost)));
            launchDialog.SetOnClickYes(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();
                    if(bIsMissiles)
                    {
                        if(bFittedToPlayer)
                            game.SellMissilePlayer(cSlotNumber);
                        else
                            game.SellMissile(lFittedToID, cSlotNumber);
                    }
                    else
                    {
                        if(bFittedToPlayer)
                            game.SellInterceptorPlayer(cSlotNumber);
                        else
                            game.SellInterceptor(lFittedToID, cSlotNumber);
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
    }

    @Override
    public boolean GetSlotOccupied(byte cSlotNumber)
    {
        return GetMissileSystem().GetSlotHasMissile(cSlotNumber);
    }

    @Override
    public String GetSlotContents(byte cSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        return bIsMissiles ? game.GetConfig().GetMissileType(system.GetSlotMissileType(cSlotNumber)).GetName() : game.GetConfig().GetInterceptorType(system.GetSlotMissileType(cSlotNumber)).GetName();
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
    public long GetSlotPrepTime(byte cSlotNumber)
    {
        MissileSystem system = GetMissileSystem();

        return Math.max(system.GetSlotPrepTimeRemaining(cSlotNumber), system.GetReloadTimeRemaining());
    }

    @Override
    public SlotControl.ImageType GetImageType(byte cSlotNumber)
    {
        if(bIsMissiles)
        {
            MissileSystem system = GetMissileSystem();

            if (system.GetSlotHasMissile(cSlotNumber))
            {
                if (game.GetConfig().GetMissileType(system.GetSlotMissileType(cSlotNumber)).GetNuclear())
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
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

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
                //Confirmation window ahs already been shown once, now just upgrade it so there's less hassle for the player.
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
            }
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
                                game.PurchaseMissileReloadUpgrade(lFittedToID);
                            else
                                game.PurchaseSAMReloadUpgrade(lFittedToID);
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

        for(byte i = 0; i < system.GetSlotCount(); i++)
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
        return bFittedToPlayer ?
                (bIsMissiles ? game.GetPlayer(lFittedToID).GetMissileSystem() : game.GetPlayer(lFittedToID).GetInterceptorSystem()) :
                (bIsMissiles ? game.GetMissileSite(lFittedToID).GetMissileSystem() : game.GetSAMSite(lFittedToID).GetInterceptorSystem());
    }
}
