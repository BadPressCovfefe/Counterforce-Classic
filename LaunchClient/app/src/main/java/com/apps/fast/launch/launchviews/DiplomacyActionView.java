package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;

import launch.game.Alliance;
import launch.game.LaunchClientGame;
import launch.game.entities.Player;

/**
 * Created by tobster on 16/10/15.
 */
public class DiplomacyActionView extends LaunchView
{
    private LinearLayout lytPlayers;
    private LinearLayout lytAlliances;
    private LinearLayout lytSurrenderingAlliances;

    private TextView txtNoPlayers;
    private TextView txtNoAlliances;
    private TextView txtNoSurrenders;

    public DiplomacyActionView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_diplomacy_action, this);

        txtNoPlayers = findViewById(R.id.txtNoPlayers);
        txtNoAlliances = findViewById(R.id.txtNoAlliances);
        txtNoSurrenders = findViewById(R.id.txtNoSurrenders);

        ((TextView)findViewById(R.id.txtPlayerJoins)).setText(context.getString(R.string.players_join_title, game.GetAlliance(game.GetOurPlayer().GetAllianceMemberID()).GetName()));
        ((TextView)findViewById(R.id.txtAllianceAffiliates)).setText(context.getString(R.string.alliances_affiliate_title, game.GetAlliance(game.GetOurPlayer().GetAllianceMemberID()).GetName()));
        ((TextView)findViewById(R.id.txtAllianceSurrenders)).setText(context.getString(R.string.alliances_surrender_title, game.GetAlliance(game.GetOurPlayer().GetAllianceMemberID()).GetName()));

        lytPlayers = findViewById(R.id.lytPlayers);
        lytAlliances = findViewById(R.id.lytAlliances);
        lytSurrenderingAlliances = findViewById(R.id.lytSurrenderingAlliances);

        findViewById(R.id.btnContinue).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new DiplomacyView(game, activity));
            }
        });

        DrawList();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean bNoneToUpdate = true;

                //Remove redundant views.
                for(int i = 0; i < lytPlayers.getChildCount(); i++)
                {
                    View view = lytPlayers.getChildAt(i);

                    if(view instanceof PlayerJoinView)
                    {
                        PlayerJoinView joinView = (PlayerJoinView) view;

                        bNoneToUpdate = false;
                        joinView.Update();

                        if(joinView.GetRedundant())
                        {
                            lytPlayers.removeViewAt(i);
                            i--;
                        }
                    }
                }

                for(int i = 0; i < lytAlliances.getChildCount(); i++)
                {
                    View view = lytAlliances.getChildAt(i);

                    if(view instanceof AllianceAffiliateView)
                    {
                        AllianceAffiliateView affiliateView = (AllianceAffiliateView) view;

                        bNoneToUpdate = false;
                        affiliateView.Update();

                        if(affiliateView.GetRedundant())
                        {
                            lytAlliances.removeViewAt(i);
                            i--;
                        }
                    }
                }

                for(int i = 0; i < lytSurrenderingAlliances.getChildCount(); i++)
                {
                    View view = lytSurrenderingAlliances.getChildAt(i);

                    if(view instanceof AllianceSurrenderView)
                    {
                        AllianceSurrenderView surrenderView = (AllianceSurrenderView) view;

                        bNoneToUpdate = false;
                        surrenderView.Update();

                        if(surrenderView.GetRedundant())
                        {
                            lytSurrenderingAlliances.removeViewAt(i);
                            i--;
                        }
                    }
                }

                //Auto-proceed when all requests dealt with.
                if(bNoneToUpdate)
                {
                    activity.SetView(new DiplomacyView(game, activity));
                }
            }
        });
    }

    private void DrawList()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytPlayers.removeAllViews();
                lytAlliances.removeAllViews();
                lytSurrenderingAlliances.removeAllViews();

                boolean bPlayersAdded = false;
                boolean bAlliancesAdded = false;
                boolean bSurrendersAdded = false;

                //Create listing.
                for(final Player player : game.GetPlayers())
                {
                    if(player.GetAllianceJoiningID() == game.GetOurPlayer().GetAllianceMemberID())
                    {
                        PlayerJoinView playerView = new PlayerJoinView(game, activity, player);

                        if (!player.GetAWOL())
                        {
                            playerView.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    activity.SelectEntity(player);
                                }
                            });
                        }

                        lytPlayers.addView(playerView);

                        bPlayersAdded = true;
                    }
                }

                for(final Alliance alliance : game.GetAlliances())
                {
                    if(game.AffiliationOffered(alliance.GetID(), game.GetOurPlayer().GetAllianceMemberID()))
                    {
                        AllianceAffiliateView affiliateView = new AllianceAffiliateView(game, activity, alliance);

                        lytAlliances.addView(affiliateView);

                        bAlliancesAdded = true;
                    }

                    if(game.SurrenderProposed(alliance.GetID(), game.GetOurPlayer().GetAllianceMemberID()))
                    {
                        AllianceSurrenderView surrenderView = new AllianceSurrenderView(game, activity, alliance);

                        lytSurrenderingAlliances.addView(surrenderView);

                        bSurrendersAdded = true;
                    }
                }

                txtNoPlayers.setVisibility(bPlayersAdded ? GONE : VISIBLE);
                txtNoAlliances.setVisibility(bAlliancesAdded ? GONE : VISIBLE);
                txtNoSurrenders.setVisibility(bSurrendersAdded ? GONE : VISIBLE);
            }
        });
    }
}
