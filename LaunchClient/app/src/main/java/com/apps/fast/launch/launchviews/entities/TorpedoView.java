package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.DistancedEntityView;
import com.apps.fast.launch.views.EntityControls;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.Damagable;
import launch.game.entities.Missile;
import launch.game.entities.Player;
import launch.game.entities.Torpedo;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;

/**
 * Created by tobster on 09/11/15.
 */
public class TorpedoView extends LaunchView
{
    private int lTorpedoID;

    private TextView txtState;
    private TextView txtSpeed;
    private TextView txtRange;
    private TextView txtYield;

    public TorpedoView(LaunchClientGame game, MainActivity activity, int lTorpedoID)
    {
        super(game, activity, true);
        this.lTorpedoID = lTorpedoID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        final Torpedo torpedo = game.GetTorpedo(lTorpedoID);

        if(torpedo != null)
        {
            TorpedoType torpedoType = game.GetConfig().GetTorpedoType(torpedo.GetType());

            if(torpedoType != null)
            {
                inflate(context, R.layout.view_torpedo, this);
                ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

                ((TextView) findViewById(R.id.txtTorpedoTitle)).setText(TextUtilities.GetEntityTypeAndName(torpedo, game));

                txtState = findViewById(R.id.txtState);
                txtSpeed = findViewById(R.id.txtSpeed);
                txtRange = findViewById(R.id.txtRange);
                txtYield = findViewById(R.id.txtYield);

                TextUtilities.AssignYieldStringAndAppearance(txtYield, torpedoType, game);
                txtSpeed.setText(TextUtilities.AssignSpeedFromKPH(torpedoType.GetTorpedoSpeed()));
                txtRange.setText(TextUtilities.GetDistanceStringFromKM(torpedo.GetRange()));

                switch(torpedo.GetState())
                {
                    case TRAVELLING:
                    {
                        txtState.setText(context.getString(R.string.torpedo_state_traveling));
                    }
                    break;

                    case SEEKING:
                    {
                        txtState.setText(context.getString(R.string.torpedo_state_seeking));
                    }
                    break;

                    case HOMING:
                    {
                        txtState.setText(context.getString(R.string.torpedo_state_homing));
                    }
                    break;
                }

                Update();
            }
            else
            {
                Finish(true);
            }
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
                Torpedo torpedo = game.GetTorpedo(lTorpedoID);

                if(torpedo != null)
                {
                    TorpedoType torpedoType = game.GetConfig().GetTorpedoType(torpedo.GetType());

                    if(torpedoType != null)
                    {
                        switch(torpedo.GetState())
                        {
                            case TRAVELLING:
                            {
                                txtState.setText(context.getString(R.string.torpedo_state_traveling));
                            }
                            break;

                            case SEEKING:
                            {
                                txtState.setText(context.getString(R.string.torpedo_state_seeking));
                            }
                            break;

                            case HOMING:
                            {
                                txtState.setText(context.getString(R.string.torpedo_state_homing));
                            }
                            break;
                        }
                    }
                    else
                    {
                        Finish(true);
                    }
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }
}
