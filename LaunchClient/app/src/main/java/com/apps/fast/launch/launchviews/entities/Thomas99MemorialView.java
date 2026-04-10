package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.LaunchClientGame;

/**
 * Created by Corbin on 6/8/2025.
 */
public class Thomas99MemorialView extends LaunchView
{
    private ImageView btnClose;

    public Thomas99MemorialView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_thomas99_memorial, this);
        btnClose = (ImageView)findViewById(R.id.btnClose);

        btnClose.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ClearSelectedEntity();
                activity.ReturnToMainView();
            }
        });
    }

    @Override
    public void Update()
    {
    }
}
