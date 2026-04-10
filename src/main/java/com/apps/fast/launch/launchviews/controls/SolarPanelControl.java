package com.apps.fast.launch.launchviews.controls;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.SolarPanel;

public class SolarPanelControl extends LaunchView
{
    private int lID;
    private TextView txtGenerationRemaining;
    private LinearLayout lytIrradiated;

    public SolarPanelControl(LaunchClientGame game, MainActivity activity, int lOreMineID)
    {
        super(game, activity, true);
        lID = lOreMineID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_solar_panel, this);

        txtGenerationRemaining = findViewById(R.id.txtGenerationRemaining);
        lytIrradiated = findViewById(R.id.lytIrradiated);

        Update();
    }

    @Override
    public void Update()
    {
        SolarPanel solarPanel = game.GetSolarPanel(lID);

        txtGenerationRemaining.setText(TextUtilities.GetTimeAmount(solarPanel.GetGenerateTimeRemaining()));

        if(game.GetRadioactive(solarPanel, true))
        {
            lytIrradiated.setVisibility(VISIBLE);
        }
        else
        {
            lytIrradiated.setVisibility(GONE);
        }
    }
}
