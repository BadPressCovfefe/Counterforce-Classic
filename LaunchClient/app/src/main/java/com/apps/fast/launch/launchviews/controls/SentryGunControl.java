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

    public SentryGunControl(LaunchClientGame game, MainActivity activity, int lSentryGunID)
    {
        super(game, activity, true);
        lID = lSentryGunID;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_sentry_gun, this);

        lytReload = findViewById(R.id.lytReload);
        txtReloading = findViewById(R.id.txtReloading);

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
