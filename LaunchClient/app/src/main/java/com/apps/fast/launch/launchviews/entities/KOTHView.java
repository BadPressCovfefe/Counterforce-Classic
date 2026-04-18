package com.apps.fast.launch.launchviews.entities;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.EntityControls;

import launch.game.Alliance;
import launch.game.LaunchClientGame;
import launch.game.entities.KOTH;
import launch.game.entities.Loot;
import launch.game.entities.Player;

/**
 * Created by tobster on 09/11/15.
 */
public class KOTHView extends LaunchView
{
    private KOTH kingOfTheHill;
    private TextView txtOccupied;

    public KOTHView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity, true);
        this.kingOfTheHill = game.GetKOTH();
        Setup();
    }

    @Override
    protected void Setup()
    {
        if(kingOfTheHill != null)
        {
            inflate(context, R.layout.view_koth, this);
            ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

            txtOccupied = findViewById(R.id.txtOccupied);

            if(kingOfTheHill.GetEmpty())
            {
                txtOccupied.setText(context.getString(R.string.koth_empty));
            }
            else if(kingOfTheHill.GetContested())
            {
                txtOccupied.setText(context.getString(R.string.koth_contested));
            }
            else
            {
                if(kingOfTheHill.GetOccupiedByAlliance())
                {
                    Alliance alliance = game.GetAlliance(kingOfTheHill.GetKingID());

                    if(alliance != null)
                    {
                        txtOccupied.setText(context.getString(R.string.koth_occupied, alliance.GetName()));
                    }
                }
                else
                {
                    Player player = game.GetPlayer(kingOfTheHill.GetKingID());

                    if(player != null)
                    {
                        txtOccupied.setText(context.getString(R.string.koth_occupied, player.GetName()));
                    }
                }
            }

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
                kingOfTheHill = game.GetKOTH();

                if(kingOfTheHill != null)
                {
                    if(kingOfTheHill.GetEmpty())
                    {
                        txtOccupied.setText(context.getString(R.string.koth_empty));
                    }
                    else if(kingOfTheHill.GetContested())
                    {
                        txtOccupied.setText(context.getString(R.string.koth_contested));
                    }
                    else
                    {
                        if(kingOfTheHill.GetOccupiedByAlliance())
                        {
                            Alliance alliance = game.GetAlliance(kingOfTheHill.GetKingID());

                            if(alliance != null)
                            {
                                txtOccupied.setText(context.getString(R.string.koth_occupied, alliance.GetName()));
                            }
                        }
                        else
                        {
                            Player player = game.GetPlayer(kingOfTheHill.GetKingID());

                            if(player != null)
                            {
                                txtOccupied.setText(context.getString(R.string.koth_occupied, player.GetName()));
                            }
                        }
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
