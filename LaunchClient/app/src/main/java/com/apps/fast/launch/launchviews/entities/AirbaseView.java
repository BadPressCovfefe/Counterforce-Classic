package com.apps.fast.launch.launchviews.entities;

import android.util.Log;
import android.view.View;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.controls.AircraftSystemControl;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Airbase;
import launch.game.entities.Structure;
import launch.game.EntityPointer.EntityType;

public class AirbaseView extends StructureView
{
    protected Airbase airbase;

    public AirbaseView(LaunchClientGame game, MainActivity activity, LaunchEntity structure)
    {
        super(game, activity, structure);
    }

    @Override
    protected void Setup()
    {
        systemView = new AircraftSystemControl(game, activity, ((Airbase)structureShadow));

        super.Setup();

        airbase = (Airbase)structureShadow;

        imgLogo.setImageResource(R.drawable.build_airbase);

        lytConfig.addView(systemView);
        Update();
    }

    @Override
    public void Update()
    {
        super.Update();

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Structure structure = GetCurrentStructure();

                if(structure != null)
                {
                    Log.i("LaunchWTF", "We own it: " + Boolean.toString(structureShadow.GetOwnerID() == game.GetOurPlayerID()));
                    Log.i("LaunchWTF", "Not Selling: " + Boolean.toString(!structure.GetSelling()));
                    Log.i("LaunchWTF", "We're functioning: " + Boolean.toString(game.GetOurPlayer().Functioning()));
                }
                if(structure != null)
                {
                    if(!structure.GetSelling() && game.GetOurPlayer().Functioning())
                    {
                        if (!structure.GetSelling())
                            systemView.Update();
                    }
                }
                else
                {
                    Log.i("LaunchWTF", "Structure is null. Finishing...");
                }
            }
        });
    }

    @Override
    public boolean IsSingleStructure()
    {
        return true;
    }

    @Override
    public Structure GetCurrentStructure()
    {
        return game.GetAirbase(structureShadow.GetID());
    }

    @Override
    public List<Structure> GetCurrentStructures()
    {
        return null;
    }

    @Override
    public void SetOnOff(boolean bOnline)
    {
        game.SetStructureOnOff(structureShadow.GetID(), EntityType.AIRBASE, bOnline);
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity.ApparentlyEquals(structureShadow))
        {
            systemView.EntityUpdated(entity);
        }
    }
}
