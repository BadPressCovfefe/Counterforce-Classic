package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.LaunchClientGame;
import launch.game.entities.Bunker;

public class BunkerControl extends LaunchView
{
    private int lID;

    private LinearLayout btnUpgradeBunkerHP;
    private TextView txtBunkerHPUpgrade;
    private TextView txtBunkerHPUpgradeCost;

    private boolean bOurStructure;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public BunkerControl(LaunchClientGame game, MainActivity activity, int lBunkerID)
    {
        super(game, activity, true);
        lID = lBunkerID;

        if(game.GetBunker(lID) != null)
        {
            bOurStructure = (game.GetBunker(lID).GetOwnerID() == game.GetOurPlayerID() ? true : false);
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
        inflate(context, R.layout.control_bunker, this);

        btnUpgradeBunkerHP = findViewById(R.id.btnUpgradeBunkerHP);
        txtBunkerHPUpgrade = findViewById(R.id.txtBunkerHPUpgrade);
        txtBunkerHPUpgradeCost = findViewById(R.id.txtBunkerHPUpgradeCost);

        if(bOurStructure)
        {
            btnUpgradeBunkerHP.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    BunkerHPUpgradeClicked();
                }
            });
        }

        Update();
    }

    @Override
    public void Update()
    {
        Bunker bunker = game.GetBunker(lID);

        if(bOurStructure)
        {
            int lCost = game.GetBunkerHPUpgradeCost(bunker);

            if(bunker.GetMaxHP() < game.GetConfig().GetBunkerMaxHPUpgrade())
            {
                txtBunkerHPUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                txtBunkerHPUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                txtBunkerHPUpgrade.setText(context.getString(R.string.upgrade, String.valueOf(bunker.GetMaxHP()), String.valueOf(game.GetBunkerHPUpgradeHP(bunker))));
                btnUpgradeBunkerHP.setVisibility(VISIBLE);
            }
            else
            {
                btnUpgradeBunkerHP.setVisibility(GONE);
            }
        }
        else
        {
            btnUpgradeBunkerHP.setVisibility(GONE);
        }
    }

    private void BunkerHPUpgradeClicked()
    {
        Bunker bunker = game.GetBunker(lID);

        int lCost = game.GetBunkerHPUpgradeCost(bunker);

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.upgrade_bunker_hp_confirm, String.valueOf(bunker.GetMaxHP()), String.valueOf(game.GetBunkerHPUpgradeHP(bunker)), TextUtilities.GetCurrencyString(game.GetBunkerHPUpgradeCost(bunker))));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        game.PurchaseBunkerHPUpgrade(lID);
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
                game.PurchaseBunkerHPUpgrade(lID);
            }
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }
}
