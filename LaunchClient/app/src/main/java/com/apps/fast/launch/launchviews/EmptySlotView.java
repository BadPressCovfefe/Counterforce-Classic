package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.controls.SlotListener;

import launch.game.LaunchClientGame;

/**
 * Created by tobster on 19/10/16.
 */
public class EmptySlotView extends LaunchView
{
    private TextView txtType;
    private TextView txtStatus;
    private ImageView imgAdd;
    private ImageView imgOccupied;

    public EmptySlotView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity, true);
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_missile_slot, this);

        txtType = (TextView)findViewById(R.id.txtType);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        imgAdd = (ImageView)findViewById(R.id.imgAdd);
        imgOccupied = (ImageView)findViewById(R.id.imgOccupied);

        txtType.setText(context.getString(R.string.empty));
        txtStatus.setText(context.getString(R.string.purchase_aircraft));
        txtStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        imgOccupied.setVisibility(GONE);

    }

    @Override
    public void Update()
    {

    }
}
