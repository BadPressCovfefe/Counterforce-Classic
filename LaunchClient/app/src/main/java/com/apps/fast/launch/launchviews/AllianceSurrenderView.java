package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.Alliance;
import launch.game.LaunchClientGame;

/**
 * Created by tobster on 09/11/15.
 */
public class AllianceSurrenderView extends LaunchView
{
    private Alliance alliance;

    private ImageView imgAlliance;

    private TextView txtName;

    private LinearLayout btnAccept;
    private LinearLayout btnReject;

    private boolean bRedundant = false;

    public AllianceSurrenderView(LaunchClientGame game, MainActivity activity, Alliance alliance)
    {
        super(game, activity, true);
        this.alliance = alliance;
        Setup();
        Update();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_alliance_surrender, this);

        imgAlliance = findViewById(R.id.imgAlliance);
        txtName = findViewById(R.id.txtName);

        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        imgAlliance.setImageBitmap(AvatarBitmaps.GetAllianceAvatar(activity, game, alliance));
        txtName.setText(alliance.GetName());

        btnAccept.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_accept_surrender, alliance.GetName()));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.AcceptSurrender(alliance.GetID());
                    }
                });
                launchDialog.SetOnClickNo(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                    }
                });
                launchDialog.show(activity.getFragmentManager(), "");
            }
        });

        btnReject.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_reject_surrender, alliance.GetName()));
                launchDialog.SetOnClickYes(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.RejectSurrender(alliance.GetID());
                    }
                });
                launchDialog.SetOnClickNo(new OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                    }
                });
                launchDialog.show(activity.getFragmentManager(), "");
            }
        });
    }

    @Override
    public void Update()
    {
        Alliance ourAlliance = game.GetAlliance(game.GetOurPlayer().GetAllianceMemberID());

        if(!game.SurrenderProposed(alliance.GetID(), ourAlliance.GetID()))
        {
            bRedundant = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
    }

    public boolean GetRedundant()
    {
        return bRedundant;
    }
}
