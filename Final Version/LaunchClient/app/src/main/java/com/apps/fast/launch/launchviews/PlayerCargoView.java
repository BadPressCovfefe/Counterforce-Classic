package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.controls.CargoSystemControl;
import com.apps.fast.launch.launchviews.controls.MissileSystemControl;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.LaunchClientGame;

/**
 * Created by tobster on 11/10/16.
 */
public class PlayerCargoView extends LaunchView
{
    private LinearLayout lytCargo;

    private CargoSystemControl cargoSystemControl;

    public PlayerCargoView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity, true);

        cargoSystemControl = new CargoSystemControl(game, activity, game.GetOurPlayer());

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_player_cargo, this);

        lytCargo = findViewById(R.id.lytCargo);

        lytCargo.removeAllViews();
        lytCargo.addView(cargoSystemControl);
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                cargoSystemControl.Update();
            }
        });
    }
}
