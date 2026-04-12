package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.entities.Rubble;
import launch.game.entities.conceptuals.Resource;

/**
 * Created by tobster on 09/11/15.
 */
public class
RubbleView extends LaunchView
{
    private int lRubbleID;

    private TextView txtRubbleTitle;
    private TextView txtExpiresIn;
    private LinearLayout btnRepair;

    public RubbleView(LaunchClientGame game, MainActivity activity, int lRubbleID)
    {
        super(game, activity, true);
        this.lRubbleID = lRubbleID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        Rubble rubble = game.GetRubble(lRubbleID);

        if(rubble != null)
        {
            inflate(context, R.layout.view_rubble, this);
            ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

            txtRubbleTitle = (TextView) findViewById(R.id.txtRubbleTitle);
            txtExpiresIn = (TextView) findViewById(R.id.txtExpiresIn);
            btnRepair = findViewById(R.id.btnRepair);

            txtRubbleTitle.setText(TextUtilities.GetOwnedEntityName(rubble, game));
            txtExpiresIn.setText(TextUtilities.GetTimeAmount(rubble.GetExpiryRemaining()));

            if(game.EntityIsFriendly(rubble, game.GetOurPlayer()))
            {
                btnRepair.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Map<Resource.ResourceType, Long> Costs = game.GetRepairCost(rubble);

                        if(game.GetOurPlayer().GetWealth() >= Costs.get(Resource.ResourceType.WEALTH))
                        {
                            final LaunchDialog launchDialog = new LaunchDialog();
                            launchDialog.SetHeaderPurchase();
                            launchDialog.SetMessage(context.getString(R.string.rebuild_rubble_confirm, TextUtilities.GetCostStatement(Costs)));
                            launchDialog.SetOnClickYes(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                    game.RepairEntity(rubble.GetPointer());
                                }
                            });
                            launchDialog.SetOnClickNo(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    launchDialog.dismiss();
                                }
                            });
                            launchDialog.show(activity.getFragmentManager(), "");
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.insufficient_wealth));
                        }
                    }
                });
            }
            else
            {
                btnRepair.setVisibility(GONE);
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
                Rubble rubble = game.GetRubble(lRubbleID);

                if(rubble != null)
                {
                    txtExpiresIn.setText(context.getString(R.string.expires_in, TextUtilities.GetTimeAmount(rubble.GetExpiryRemaining())));
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }
}
