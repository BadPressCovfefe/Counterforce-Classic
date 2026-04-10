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

import java.util.List;

import launch.game.Config;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.RadarStation;
import launch.game.entities.Structure;
import launch.game.systems.MissileSystem;

public class RadarStationControl extends LaunchView
{
    private int lID;

    private LinearLayout btnUpgradeRadarRange;
    private TextView txtRadarRangeUpgrade;
    private TextView txtRadarRangeUpgradeCost;

    private LinearLayout btnUpgradeRadarBoost;
    private TextView txtRadarBoostUpgrade;
    private TextView txtRadarBoostUpgradeCost;

    private TextView txtHitChance;

    private boolean bOurStructure;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public RadarStationControl(LaunchClientGame game, MainActivity activity, int lRadarStationID)
    {
        super(game, activity, true);
        lID = lRadarStationID;
        bOurStructure = (game.GetRadarStation(lID).GetOwnerID() == game.GetOurPlayerID() ? true : false);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_radar_station, this);

        btnUpgradeRadarRange = (LinearLayout) findViewById(R.id.btnUpgradeRadarRange);
        txtRadarRangeUpgrade = (TextView) findViewById(R.id.txtRadarRangeUpgrade);
        txtRadarRangeUpgradeCost = (TextView) findViewById(R.id.txtRadarRangeUpgradeCost);

        btnUpgradeRadarBoost = (LinearLayout) findViewById(R.id.btnUpgradeRadarBoost);
        txtRadarBoostUpgrade = (TextView) findViewById(R.id.txtRadarBoostUpgrade);
        txtRadarBoostUpgradeCost = (TextView) findViewById(R.id.txtRadarBoostUpgradeCost);

        txtHitChance = findViewById(R.id.txtHitChance);




        btnUpgradeRadarRange.setVisibility(bOurStructure ? VISIBLE : GONE);
        btnUpgradeRadarBoost.setVisibility(bOurStructure ? VISIBLE : GONE);

        if(bOurStructure)
        {
            btnUpgradeRadarRange.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    RadarRangeUpgradeClicked();
                }
            });

            btnUpgradeRadarBoost.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    RadarBoostUpgradeClicked();
                }
            });
        }

        Update();
    }

    @Override
    public void Update()
    {
        RadarStation radarStation = game.GetRadarStation(lID);

        txtHitChance.setText(TextUtilities.GetAccuracyPercentage(radarStation.GetRadarAccuracyBoost()));

        //Radar range upgrade button.
        if(bOurStructure)
        {
            int lCost = game.GetRadarRangeUpgradeCost(radarStation);

            if(lCost < Defs.UPGRADE_COST_MAXED)
            {
                txtRadarRangeUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                txtRadarRangeUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                txtRadarRangeUpgrade.setText(context.getString(R.string.upgrade, TextUtilities.GetDistanceStringFromKM(radarStation.GetRadarRange()), TextUtilities.GetDistanceStringFromKM(game.GetRadarRangeUpgradeRange(radarStation))));
                btnUpgradeRadarRange.setVisibility(VISIBLE);
            }
            else
            {
                btnUpgradeRadarRange.setVisibility(GONE);
            }
        }
        else
        {
            btnUpgradeRadarRange.setVisibility(GONE);
        }

        //Radar boost upgrade button.
        if(bOurStructure)
        {
            int lCost = game.GetRadarBoostUpgradeCost(radarStation);

            if(radarStation.GetRadarAccuracyBoost() < game.GetConfig().GetMaxRadarAccuracyBoost())
            {
                txtRadarBoostUpgradeCost.setText(TextUtilities.GetCurrencyString(lCost));
                txtRadarBoostUpgradeCost.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lCost ? R.attr.GoodColour : R.attr.BadColour));
                txtRadarBoostUpgrade.setText(context.getString(R.string.upgrade, TextUtilities.GetAccuracyPercentage(radarStation.GetRadarAccuracyBoost()), TextUtilities.GetAccuracyPercentage(game.GetRadarBoostUpgradeBoost(radarStation))));
                btnUpgradeRadarBoost.setVisibility(VISIBLE);
            }
            else
            {
                btnUpgradeRadarBoost.setVisibility(GONE);
            }
        }
        else
        {
            btnUpgradeRadarBoost.setVisibility(GONE);
        }
    }

    private void RadarRangeUpgradeClicked()
    {
        RadarStation radarStation = game.GetRadarStation(lID);

        int lCost = game.GetRadarRangeUpgradeCost(radarStation);

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            final LaunchDialog launchDialog = new LaunchDialog();
            launchDialog.SetHeaderPurchase();
            launchDialog.SetMessage(context.getString(R.string.upgrade_radar_range_confirm, TextUtilities.GetDistanceStringFromKM(radarStation.GetRadarRange()), TextUtilities.GetDistanceStringFromKM(game.GetRadarRangeUpgradeRange(radarStation)), TextUtilities.GetCurrencyString(game.GetRadarRangeUpgradeCost(radarStation))));
            launchDialog.SetOnClickYes(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    launchDialog.dismiss();

                    game.PurchaseRadarRangeUpgrade(lID);
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

    private void RadarBoostUpgradeClicked()
    {
        RadarStation radarStation = game.GetRadarStation(lID);

        int lCost = game.GetRadarBoostUpgradeCost(radarStation);

        if(game.GetOurPlayer().GetWealth() >= lCost)
        {
            if(!bUpgradeConfirmHasBeenShown)
            {
                bUpgradeConfirmHasBeenShown = true;

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderPurchase();
                launchDialog.SetMessage(context.getString(R.string.upgrade_radar_boost_confirm, TextUtilities.GetAccuracyPercentage(radarStation.GetRadarAccuracyBoost()), TextUtilities.GetAccuracyPercentage(game.GetRadarBoostUpgradeBoost(radarStation)), TextUtilities.GetCurrencyString(game.GetRadarBoostUpgradeCost(radarStation))));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();

                        game.PurchaseRadarBoostUpgrade(lID);

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
                game.PurchaseRadarBoostUpgrade(lID);
            }
        }
        else
        {
            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_funds));
        }
    }
}
