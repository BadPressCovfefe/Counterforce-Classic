package com.apps.fast.launch.launchviews;

import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;

import launch.game.Alliance;
import launch.game.LaunchClientGame;

/**
 * Created by tobster on 09/11/15.
 */
public class AllianceView extends LaunchView
{
    private Alliance allianceShadow;

    private ImageView imgAvatar;
    private TextView txtName;
    private TextView txtMembers;
    private TextView txtTotal;
    private TextView txtEconomy;
    private TextView txtOffences;
    private TextView txtDefences;
    private TextView txtMembersTitle;
    private TextView txtTotalTitle;
    private TextView txtEconomyTitle;
    private TextView txtOffencesTitle;
    private TextView txtDefencesTitle;

    private ImageView imgAtWar;
    private ImageView imgAffiliated;

    public AllianceView(LaunchClientGame game, MainActivity activity, Alliance alliance)
    {
        super(game, activity, true);
        this.allianceShadow = alliance;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_alliance, this);

        imgAvatar = findViewById(R.id.imgAvatar);
        txtName = findViewById(R.id.txtName);

        txtMembers = findViewById(R.id.txtMembers);
        txtTotal = findViewById(R.id.txtTotal);
        txtEconomy = findViewById(R.id.txtEconomy);
        txtOffences = findViewById(R.id.txtOffences);
        txtDefences = findViewById(R.id.txtDefences);
        txtMembersTitle = findViewById(R.id.txtMembersTitle);
        txtTotalTitle = findViewById(R.id.txtTotalTitle);
        txtEconomyTitle = findViewById(R.id.txtEconomyTitle);
        txtOffencesTitle = findViewById(R.id.txtOffencesTitle);
        txtDefencesTitle = findViewById(R.id.txtDefencesTitle);

        imgAtWar = findViewById(R.id.imgAtWar);
        imgAffiliated = findViewById(R.id.imgAffiliated);

        if(game.GetOurPlayer() != null && allianceShadow != null)
            imgAvatar.setImageBitmap(AvatarBitmaps.GetAllianceAvatar(activity, game, allianceShadow));

        txtName.setText(allianceShadow.GetName());
        txtMembers.setText(Integer.toString(game.GetAllianceMemberCount(allianceShadow)));
        txtTotal.setText(TextUtilities.GetCurrencyString(game.GetAllianceTotalValue(allianceShadow)));
        txtEconomy.setText(TextUtilities.GetCurrencyString(game.GetAllianceNeutralValue(allianceShadow)));
        txtOffences.setText(TextUtilities.GetCurrencyString(game.GetAllianceOffenseValue(allianceShadow)));
        txtDefences.setText(TextUtilities.GetCurrencyString(game.GetAllianceDefenseValue(allianceShadow)));

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
        int lTitleLargest = Math.max(Math.max(Math.max(Math.max(txtMembersTitle.getWidth(), txtEconomyTitle.getWidth()), txtOffencesTitle.getWidth()), txtDefencesTitle.getWidth()), txtTotalTitle.getWidth());
        txtMembersTitle.setWidth(lTitleLargest);
        txtTotalTitle.setWidth(lTitleLargest);
        txtEconomyTitle.setWidth(lTitleLargest);
        txtOffencesTitle.setWidth(lTitleLargest);
        txtDefencesTitle.setWidth(lTitleLargest);

        int lValueLargest = Math.max(Math.max(Math.max(Math.max(txtMembers.getWidth(), txtEconomy.getWidth()), txtOffences.getWidth()), txtDefences.getWidth()), txtTotal.getWidth());
        txtMembers.setWidth(lValueLargest);
        txtTotal.setWidth(lValueLargest);
        txtEconomy.setWidth(lValueLargest);
        txtOffences.setWidth(lValueLargest);
        txtDefences.setWidth(lValueLargest);
    }

    @Override
    public void AvatarSaved(int lAvatarID)
    {
        final Alliance alliance = game.GetAlliance(allianceShadow.GetID());

        if(lAvatarID == alliance.GetAvatarID())
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    imgAvatar.setImageBitmap(AvatarBitmaps.GetAllianceAvatar(activity, game, allianceShadow));
                }
            });
        }
    }
}
