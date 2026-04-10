package com.apps.fast.launch.launchviews.controls;

import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.LaunchClientGame;
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

        imgType.setImageBitmap(EntityIconBitmaps.GetNavalBitmap(context, order.GetTypeUnderConstruction()));

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
