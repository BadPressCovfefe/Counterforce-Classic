package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.EntityControls;

import launch.game.LaunchClientGame;
import launch.game.entities.ResourceDeposit;

public class ResourceDepositView extends LaunchView
{
    private int lDepositID;

    private TextView txtReserves;
    private TextView txtResourceDepositTitle;
    private ImageView imgType;

    private LinearLayout btnMissileTarget;

    public ResourceDepositView(LaunchClientGame game, MainActivity activity, int lDepositID)
    {
        super(game, activity, true);
        this.lDepositID = lDepositID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        ResourceDeposit deposit = game.GetResourceDeposit(lDepositID);

        if(deposit != null)
        {
            inflate(context, R.layout.view_resource_deposit, this);
            ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

            txtReserves = findViewById(R.id.txtReserves);
            txtResourceDepositTitle = findViewById(R.id.txtResourceDepositTitle);
            imgType = findViewById(R.id.imgType);
            btnMissileTarget = findViewById(R.id.btnMissileTarget);

            //txtReserves.setText(context.getString(R.string.reserves_remaining, TextUtilities.GetResourceQuantityString(deposit.GetType(), deposit.GetReserves())));
            txtResourceDepositTitle.setText(TextUtilities.GetResourceDepositTitle(deposit));
            imgType.setImageBitmap(EntityIconBitmaps.GetResourceTypeBitmap(activity, deposit.GetType()));

            Update();

            btnMissileTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!game.GetInteractionReady())
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.waiting_for_data));
                    }
                    else
                    {
                        activity.MissileSelectForTarget(deposit.GetPosition(), deposit, TextUtilities.GetEntityTypeAndName(deposit, game));
                    }
                }
            });
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
                ResourceDeposit deposit = game.GetResourceDeposit(lDepositID);

                if(deposit != null)
                {
                    txtReserves.setText(context.getString(R.string.reserves_remaining, TextUtilities.GetResourceQuantityString(deposit.GetType(), deposit.GetReserves())));
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }
}
