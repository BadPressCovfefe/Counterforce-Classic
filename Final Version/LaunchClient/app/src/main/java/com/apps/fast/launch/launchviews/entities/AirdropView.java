package com.apps.fast.launch.launchviews.entities;

import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.EntityControls;

import launch.game.LaunchClientGame;
import launch.game.entities.Airdrop;
import launch.game.entities.Loot;

/**
 * Created by tobster on 09/11/15.
 */
public class
AirdropView extends LaunchView
{
    private int lAirdropID;

    private TextView txtArrivingIn;

    public AirdropView(LaunchClientGame game, MainActivity activity, int lAirdropID)
    {
        super(game, activity, true);
        this.lAirdropID = lAirdropID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        Airdrop airdrop = game.GetAirdrop(lAirdropID);

        if(airdrop != null)
        {
            inflate(context, R.layout.view_airdrop, this);
            ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

            ((TextView) findViewById(R.id.txtAirdropTitle)).setText(TextUtilities.GetEntityTypeAndName(airdrop, game));

            txtArrivingIn = findViewById(R.id.txtArrivingIn);
            txtArrivingIn.setText(TextUtilities.GetTimeAmount(airdrop.GetArrivalRemaining()));

            Update();
        }
        else
        {
            Finish(true);
        }
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Airdrop airdrop = game.GetAirdrop(lAirdropID);

                if(airdrop != null)
                {
                    txtArrivingIn.setText(TextUtilities.GetTimeAmount(airdrop.GetArrivalRemaining()));
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }
}
