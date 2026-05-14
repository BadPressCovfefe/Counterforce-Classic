package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.LogisticsDepot;
import launch.game.entities.Warehouse;

public class LogisticsDepotControl extends LaunchView
{
    private int lID;

    private TextView txtDepotMoneyStats;
    private LinearLayout btnCollect;

    private boolean bOurStructure;
    private LogisticsDepot depot;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public LogisticsDepotControl(LaunchClientGame game, MainActivity activity, int lLogisticsDepotID)
    {
        super(game, activity, true);
        lID = lLogisticsDepotID;

        if(game.GetLogisticsDepot(lID) != null)
        {
            bOurStructure = (game.GetLogisticsDepot(lID).GetOwnerID() == game.GetOurPlayerID());
            depot = game.GetLogisticsDepot(lID);
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
        inflate(context, R.layout.control_logisticsdepot, this);

        txtDepotMoneyStats = findViewById(R.id.txtDepotMoneyStats);
        btnCollect = findViewById(R.id.btnCollect);

        if(bOurStructure)
        {
            txtDepotMoneyStats.setText(context.getString(R.string.bank_money_stats, TextUtilities.GetCurrencyString(depot.GetWealth()), TextUtilities.GetCurrencyString(Defs.LOGISTICS_DEPOT_WEALTH_CAPACITY)));

            btnCollect.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(depot.GetOnline())
                    {
                        if(!depot.GetReadyToCollect())
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.depot_not_ready));
                        }
                        else if(depot.Full())
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.depot_full));
                        }
                        else
                        {
                            game.DepotCollect(lID);
                        }
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });
        }
        else
        {
            btnCollect.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
         LogisticsDepot depot = game.GetLogisticsDepot(lID);

        if(depot != null)
        {
            txtDepotMoneyStats.setText(context.getString(R.string.bank_money_stats, TextUtilities.GetCurrencyString(depot.GetWealth()), TextUtilities.GetCurrencyString(Defs.LOGISTICS_DEPOT_WEALTH_CAPACITY)));
        }
    }
}
