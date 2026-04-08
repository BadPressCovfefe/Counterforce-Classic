package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Armory;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Player;
import launch.game.entities.ScrapYard;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.CargoSystem;

public class ScrapYardControl extends LaunchView
{
    private int lID;
    private ScrapYard scrapyard;
    private boolean bOurStructure;
    private static boolean bUpgradeConfirmHasBeenShown = false;

    public ScrapYardControl(LaunchClientGame game, MainActivity activity, int lScrapYardID)
    {
        super(game, activity, true);
        lID = lScrapYardID;

        ScrapYard scrapyard = game.GetScrapYard(lScrapYardID);

        if(scrapyard != null)
        {
            bOurStructure = (scrapyard.GetOwnerID() == game.GetOurPlayerID());
            this.scrapyard = scrapyard;

            if(game.EntityIsFriendly(scrapyard, game.GetOurPlayer()))
            {
                //cargoSystem = new CargoSystemControl(game, activity, scrapyard);
            }
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
        inflate(context, R.layout.control_scrapyard, this);

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
                ScrapYard scrapyard = game.GetScrapYard(lID);

                if(scrapyard != null)
                {
                    bOurStructure = scrapyard.GetOwnedBy(game.GetOurPlayerID());
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals(scrapyard))
        {
            Update();
        }
    }
}
