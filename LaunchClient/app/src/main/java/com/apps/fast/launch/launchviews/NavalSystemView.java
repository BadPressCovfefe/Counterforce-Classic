package com.apps.fast.launch.launchviews;

import android.widget.LinearLayout;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.launchviews.controls.MissileSystemControl;
import com.apps.fast.launch.launchviews.controls.SLBMSystemControl;
import com.apps.fast.launch.launchviews.controls.TorpedoSystemControl;

import launch.game.LaunchClientGame;
import launch.game.entities.NavalVessel;
import launch.game.entities.Ship;
import launch.game.systems.LaunchSystem.SystemType;

/**
 * Created by tobster on 11/10/16.
 */
public class NavalSystemView extends LaunchView
{
    private LinearLayout lytMissiles;
    private LaunchView systemControl;
    private NavalVessel host;
    private SystemType systemType;

    public NavalSystemView(LaunchClientGame game, MainActivity activity, SystemType type, NavalVessel vessel)
    {
        super(game, activity, true);
        this.host = vessel;
        this.systemType = type;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_naval_missiles, this);

        lytMissiles = (LinearLayout) findViewById(R.id.lytMissiles);
        lytMissiles.removeAllViews();

        switch(systemType)
        {
            case SUBMARINE_MISSILES:
            case SHIP_MISSILES:
            {
                systemControl = new MissileSystemControl(game, activity, host.GetID(), host, true);
            }
            break;

            case SHIP_INTERCEPTORS:
            {
                systemControl = new MissileSystemControl(game, activity, host.GetID(), host, false);
            }
            break;

            case SUBMARINE_TORPEDO:
            case SHIP_TORPEDOES:
            {
                systemControl = new TorpedoSystemControl(game, activity, host.GetID(), host);
            }
            break;

            case SHIP_AIRCRAFT:
            {
                //TODO
            }
            break;

            case SUBMARINE_ICBM:
            {
                systemControl = new SLBMSystemControl(game, activity, host.GetID());
            }
            break;
        }

        lytMissiles.addView(systemControl);
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                systemControl.Update();
            }
        });
    }
}
