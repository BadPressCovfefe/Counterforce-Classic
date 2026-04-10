package com.apps.fast.launch.launchviews;

import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.activities.MainActivity;

import launch.game.Alliance;
import launch.game.LaunchClientGame;

/**
 * Created by tobster on 09/11/15.
 */
public class RelationshipView extends LaunchView
{
    private Alliance allianceShadow;

    private ImageView imgAvatar;
    private TextView txtName;
    private TextView txtMembers;
    private TextView txtMembersTitle;

    private ImageView imgAtWar;
    private ImageView imgAffiliated;

    public RelationshipView(LaunchClientGame game, MainActivity activity, Alliance alliance)
    {
        super(game, activity, true);
        this.allianceShadow = alliance;

        if(alliance == null)
            Finish(false);
        else
            Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_relationship, this);

        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);

        txtMembers = findViewById(R.id.txtMembers);
        txtMembersTitle = findViewById(R.id.txtMembersTitle);

        imgAtWar = findViewById(R.id.imgAtWar);
        imgAffiliated = findViewById(R.id.imgAffiliated);

        imgAvatar.setImageBitmap(AvatarBitmaps.GetAllianceAvatar(activity, game, allianceShadow));
        txtName.setText(allianceShadow.GetName());
        txtMembers.setText(Integer.toString(game.GetAllianceMemberCount(allianceShadow)));

        switch(game.GetAllegiance(allianceShadow))
        {
            case ALLY:
            case AFFILIATE:
            {
                imgAtWar.setVisibility(GONE);
                imgAffiliated.setVisibility(VISIBLE);
            }
            break;

            case ENEMY:
            {
                imgAtWar.setVisibility(VISIBLE);
                imgAffiliated.setVisibility(GONE);
            }
            break;

            default:
            {
                imgAtWar.setVisibility(GONE);
                imgAffiliated.setVisibility(GONE);
            }
        }
    }

    @Override
    public void Update()
    {

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        //Set title items to same width, to justify text of the values.

        if(txtMembersTitle != null && txtMembers != null)
        {
            int lTitleLargest = txtMembersTitle.getWidth();
            txtMembersTitle.setWidth(lTitleLargest);

            int lValueLargest = txtMembers.getWidth();
            txtMembers.setWidth(lValueLargest);
        }
    }

    @Override
    public void AvatarSaved(int lAvatarID)
    {
        final Alliance alliance = game.GetAlliance(allianceShadow.GetID());

        if(alliance.GetAvatarID() == lAvatarID)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    imgAvatar.setImageBitmap(AvatarBitmaps.GetAllianceAvatar(activity, game, alliance));
                }
            });
        }
    }
}
