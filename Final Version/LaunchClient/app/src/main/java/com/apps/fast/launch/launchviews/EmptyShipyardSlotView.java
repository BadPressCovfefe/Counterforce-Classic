package com.apps.fast.launch.launchviews;

import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;

import launch.game.LaunchClientGame;

/**
 * Created by tobster on 19/10/16.
 */
public class EmptyShipyardSlotView extends LaunchView
{
    public EmptyShipyardSlotView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_shipyard_slot, this);
    }

    @Override
    public void Update()
    {

    }
}
