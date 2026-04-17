package com.apps.fast.launch.launchviews.controls;

import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.ShipProductionOrder;

/**
 * Created by tobster on 19/10/16.
 */
public class NavalProductionOrderView extends LaunchView
{
    private TextView txtBuildTime;
    private ImageView imgType;

    private ShipProductionOrder order;

    public NavalProductionOrderView(LaunchClientGame game, MainActivity activity, ShipProductionOrder order)
    {
        super(game, activity, true);
        this.order = order;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_naval_production, this);

        imgType = findViewById(R.id.imgType);
        txtBuildTime = findViewById(R.id.txtBuildTime);

        txtBuildTime.setText(TextUtilities.GetTimeAmount(order.GetConstructionTimeRemaining()));

        switch(order.GetTypeUnderConstruction())
        {
            case FRIGATE:
            {
                imgType.setImageResource(R.drawable.build_frigate);
            }
            break;

            case DESTROYER:
            {
                imgType.setImageResource(R.drawable.build_destroyer);
            }
            break;

            case SUPER_CARRIER:
            {
                imgType.setImageResource(R.drawable.build_super_carrier);
            }
            break;

            case ATTACK_SUB:
            {
                imgType.setImageResource(R.drawable.build_attack_sub);
            }
            break;

            case SSBN:
            {
                imgType.setImageResource(R.drawable.build_ssbn_2);
            }
            break;
        }

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
                txtBuildTime.setText(TextUtilities.GetTimeAmount(order.GetConstructionTimeRemaining()));
            }
        });
    }
}
