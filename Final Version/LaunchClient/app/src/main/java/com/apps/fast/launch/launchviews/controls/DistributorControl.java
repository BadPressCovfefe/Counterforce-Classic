package com.apps.fast.launch.launchviews.controls;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;

import java.util.Map;
import java.util.Map.Entry;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Distributor;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class DistributorControl extends LaunchView
{
    private int lID;
    private LinearLayout lytIrradiated;
    private LinearLayout lytInputs;
    private LinearLayout lytOutputs;
    private Distributor distributor;

    public DistributorControl(LaunchClientGame game, MainActivity activity, int lDistributorID)
    {
        super(game, activity, true);
        lID = lDistributorID;

        distributor = game.GetDistributor(lDistributorID);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_distributor, this);

        lytIrradiated = findViewById(R.id.lytIrradiated);

        Update();
    }

    @Override
    public void Update()
    {
        Distributor distributor = game.GetDistributor(lID);

        if(game.GetRadioactive(distributor, true))
        {
            lytIrradiated.setVisibility(VISIBLE);
        }
        else
        {
            lytIrradiated.setVisibility(GONE);
        }
    }
}
