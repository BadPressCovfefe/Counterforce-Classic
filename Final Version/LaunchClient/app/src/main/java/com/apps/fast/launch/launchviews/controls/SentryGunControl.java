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
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.RadarStation;
import launch.game.entities.SentryGun;

public class SentryGunControl extends LaunchView
{
    private int lID;
    private LinearLayout lytReload;
    private TextView txtReloading;

    private TextView txtRange;

    private boolean bOurStructure;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public SentryGunControl(LaunchClientGame game, MainActivity activity, int lSentryGunID)
    {
        super(game, activity, true);
        lID = lSentryGunID;

        if(game.GetSentryGun(lID) != null)
        {
            bOurStructure = (game.GetSentryGun(lID).GetOwnerID() == game.GetOurPlayerID());
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
        txtRange = findViewById(R.id.txtRange);

        SentryGun gun = game.GetSentryGun(lID);

        if(gun != null)
            txtRange.setText(TextUtilities.GetDistanceStringFromKM(Defs.SENTRY_RANGE));

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                SentryGun sentryGun = game.GetSentryGun(lID);

                if(sentryGun != null)
                {
                    txtRange.setText(TextUtilities.GetDistanceStringFromKM(Defs.SENTRY_RANGE));

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
                }
            }
        });
    }
}
