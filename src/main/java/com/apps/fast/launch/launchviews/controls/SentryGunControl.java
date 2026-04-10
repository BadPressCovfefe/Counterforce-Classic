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
import launch.game.entities.RadarStation;
import launch.game.entities.SentryGun;

public class SentryGunControl extends LaunchView
{
    private int lID;
    private LinearLayout lytReload;
    private TextView txtReloading;
    private TextView txtHitChance;

    private LinearLayout btnUpgradeSentryRange;
    private TextView txtSentryRangeUpgrade;
    private TextView txtSentryRangeUpgradeCost;

    private TextView txtRange;

    private boolean bOurStructure;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public SentryGunControl(LaunchClientGame game, MainActivity activity, int lSentryGunID)
    {
        super(game, activity, true);
        lID = lSentryGunID;

        if(game.GetSentryGun(lID) != null)
        {
            bOurStructure = (game.GetSentryGun(lID).GetOwnerID() == game.GetOurPlayerID() ? true : false);
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
        inflate(context, R.layout.control_sentry_gun, this);

        lytReload = (LinearLayout) findViewById(R.id.lytReload);
        txtReloading = (TextView) findViewById(R.id.txtReloading);
        txtHitChance = findViewById(R.id.txtHitChance);

        btnUpgradeSentryRange = findViewById(R.id.btnUpgradeSentryRange);
        txtSentryRangeUpgrade = findViewById(R.id.txtSentryRangeUpgrade);
        txtSentryRangeUpgradeCost = findViewById(R.id.txtSentryRangeUpgradeCost);
        txtRange = findViewById(R.id.txtRange);

        txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetSentryGun(lID).GetRange()));
        txtHitChance.setText(TextUtilities.GetAccuracyPercentage(game.GetSentryHitChance(game.GetSentryGun(lID))));

        if(bOurStructure)
        {
            btnUpgradeSentryRange.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    SentryRangeUpgradeClicked();
                }
            });
        }

        Update();
    }

    @Override
    public void Update()
    {
        SentryGun sentryGun = game.GetSentryGun(lID);

        txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetSentryGun(lID).GetRange()));
        txtHitChance.setText(TextUtilities.GetAccuracyPercentage(game.GetSentryHitChance(game.GetSentryGun(lID))));

        //Reload.
        long oReloadTimeRemaining = sentryGun.GetReloadTimeRemaining();

        if (oReloadTimeRemaining > 0)
        {
            lytReload.setVisibility(VISIBLE);
            txtReloading.setText(TextUtilities.GetTimeAmount(oReloadTimeRemaining));
        }
        else
        {
            lytReload.setVisibility(GONE);
        }

        if(bOurStructure)
        {
            int lCost = game.GetSentryRangeUpgradeCost(sentryGun);

            if(sentryGun.GetRange() < game.GetConfig().GetMaxSentryRange())
            {
                txtSentryRangeUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                txtSentryRangeUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                txtSentryRangeUpgrade.setText(context.getString(R.string.upgrade, TextUtilities.GetDistanceStringFromKM(sentryGun.GetRange()), TextUtilities.GetDistanceStringFromKM(game.GetSentryRangeUpgradeRange(sentryGun))));
                btnUpgradeSentryRange.setVisibility(VISIBLE);
            }
            else
            {
                btnUpgradeSentryRange.setVisibility(GONE);
            }
        }
        else
        {
            btnUpgradeSentryRange.setVisibility(GONE);
        }
    }

    private void SentryRangeUpgradeClicked()
    {
        SentryGun sentryGun = game.GetSentryGun(lID);

        int lCost = game.GetSentryRangeUpgradeCost(sentryGun);

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.upgrade_sentry_range_confirm, TextUtilities.GetDistanceStringFromKM(sentryGun.GetRange()), TextUtilities.GetDistanceStringFromKM(game.GetSentryRangeUpgradeRange(sentryGun)), TextUtilities.GetCurrencyString(game.GetSentryRangeUpgradeCost(sentryGun))));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        game.PurchaseSentryRangeUpgrade(lID);
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
                game.PurchaseSentryRangeUpgrade(lID);
            }
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }
}
