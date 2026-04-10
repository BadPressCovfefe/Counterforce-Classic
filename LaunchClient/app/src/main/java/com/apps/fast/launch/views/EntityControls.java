package com.apps.fast.launch.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;

import launch.game.LaunchClientGame;
import launch.game.entities.Player;

/**
 * Created by tobster on 09/11/15.
 */
public class EntityControls extends LinearLayout
{
    private ImageView btnGoTo;
    private ImageView btnZoom;
    private ImageView btnExpand;
    private ImageView btnContract;
    private ImageView btnClose;
    private MainActivity activity;

    public EntityControls(Context context)
    {
        super(context);
        Setup(context);
    }

    public EntityControls(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        Setup(context);
    }

    public EntityControls(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        Setup(context);
    }

    public void Setup(Context context)
    {
        inflate(context, R.layout.view_entitycontrols, this);

        btnGoTo = findViewById(R.id.btnGoTo);
        btnZoom = findViewById(R.id.btnZoom);
        btnExpand = findViewById(R.id.btnExpand);
        btnContract = findViewById(R.id.btnContract);
        btnClose = findViewById(R.id.btnClose);

        btnGoTo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.GoToSelectedEntity(false);
            }
        });

        btnZoom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.GoToSelectedEntity(true);
            }
        });

        btnExpand.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ExpandView();
                btnExpand.setVisibility(GONE);
                btnContract.setVisibility(VISIBLE);
            }
        });

        btnContract.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ContractView();
                btnExpand.setVisibility(VISIBLE);
                btnContract.setVisibility(GONE);
            }
        });

        btnClose.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.ClearGeoTarget();
                activity.ClearSelectedEntity();
                activity.ReturnToMainView();
            }
        });
    }

    public void SetActivity(MainActivity activity)
    {
        this.activity = activity;

        if(activity != null && activity.GetSelectedEntity() instanceof Player)
        {
            btnGoTo.setVisibility(GONE);
            btnZoom.setVisibility(GONE);
        }
    }
}
