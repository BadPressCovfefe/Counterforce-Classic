package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.ChangeTaxRateView;
import com.apps.fast.launch.launchviews.DiplomacyView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.PlayerRankView;
import com.apps.fast.launch.launchviews.RelationshipView;
import com.apps.fast.launch.launchviews.UploadAvatarView;
import com.apps.fast.launch.launchviews.WarView;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.List;

import launch.game.Alliance;
import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.LaunchGame;
import launch.game.entities.Player;

/**
 * Created by tobster on 09/11/15.
 */
public class AllianceControl extends LaunchView
{
    private Alliance allianceShadow;
    private ImageView imgAvatar;
    private TextView txtName;
    private TextView txtNameButton;
    private LinearLayout lytNameEdit;
    private EditText txtNameEdit;
    private LinearLayout btnApplyName;
    private TextView txtDescription;
    private TextView txtDescriptionButton;
    private LinearLayout lytDescriptionEdit;
    private EditText txtDescriptionEdit;
    private LinearLayout btnApplyDescription;
    private TextView txtJoining;
    private TextView txtAtWar;
    private TextView txtAffiliated;
    private TextView txtAffiliationOffered;
    private TextView txtPrisoner;
    private TextView txtMembers;
    private TextView txtWealth;
    private TextView txtTaxRate;
    private TextView txtFounderName;
    private TextView txtFoundedTime;
    private TextView txtWarsWon;
    private TextView txtWarsLost;
    private TextView txtAlliancesDisbanded;
    private TextView txtAffiliationsBroken;
    private TextView txtICBMCount;
    private TextView txtABMCount;
    private LinearLayout btnLeaveAlliance;
    private LinearLayout lytAtWarWith;
    private LinearLayout lytAffiliatedWith;
    private LinearLayout lytMembers;

    //Alliance leader controls.
    private LinearLayout btnJoinAlliance;
    private LinearLayout btnCancelOffer;
    private LinearLayout btnOfferAffiliation;
    private LinearLayout btnDeclareWar;
    private LinearLayout btnSurrenderWar;
    private LinearLayout lytLeaderControls;
    private LinearLayout btnWithdraw1k;
    private LinearLayout btnWithdraw10k;
    private LinearLayout btnWithdraw100k;
    private TextView txtWithdraw1k;
    private TextView txtWithdraw10k;
    private TextView txtWithdraw100k;
    private TextView btnChangeTaxRate;
    private TextView btnPanic;

    public AllianceControl(LaunchClientGame game, MainActivity activity, Alliance alliance)
    {
        super(game, activity, true);
        allianceShadow = alliance;
        Setup();
        Update();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_alliance, this);

        imgAvatar = findViewById(R.id.imgAvatar);

        txtName = findViewById(R.id.txtName);
        txtNameButton = findViewById(R.id.txtNameButton);
        lytNameEdit = findViewById(R.id.lytNameEdit);
        txtNameEdit = findViewById(R.id.txtNameEdit);
        btnApplyName = findViewById(R.id.btnApplyName);

        txtDescription = findViewById(R.id.txtDescription);
        txtDescriptionButton = findViewById(R.id.txtDescriptionButton);
        lytDescriptionEdit = findViewById(R.id.lytDescriptionEdit);
        txtDescriptionEdit = findViewById(R.id.txtDescriptionEdit);
        btnApplyDescription = findViewById(R.id.btnApplyDescription);

        txtJoining = findViewById(R.id.txtJoining);
        txtAtWar = findViewById(R.id.txtAtWar);
        txtAffiliated = findViewById(R.id.txtAffiliated);
        txtAffiliationOffered = findViewById(R.id.txtAffiliationOffered);

        btnJoinAlliance = findViewById(R.id.btnJoinAlliance);
        btnLeaveAlliance = findViewById(R.id.btnLeaveAlliance);
        btnDeclareWar = findViewById(R.id.btnDeclareWar);
        btnSurrenderWar = findViewById(R.id.btnSurrenderWar);
        btnOfferAffiliation = findViewById(R.id.btnOfferAffiliation);
        btnCancelOffer = findViewById(R.id.btnCancelOffer);

        lytAtWarWith = findViewById(R.id.lytAtWarWith);
        lytAffiliatedWith = findViewById(R.id.lytAffiliatedWith);
        lytMembers = findViewById(R.id.lytMembers);

        lytLeaderControls = findViewById(R.id.lytLeaderControls);
        btnWithdraw1k = findViewById(R.id.btnWithdraw1k);
        btnWithdraw10k = findViewById(R.id.btnWithdraw10k);
        btnWithdraw100k = findViewById(R.id.btnWithdraw100k);
        txtWithdraw1k = findViewById(R.id.txtWithdraw1k);
        txtWithdraw10k = findViewById(R.id.txtWithdraw10k);
        txtWithdraw100k = findViewById(R.id.txtWithdraw100k);
        btnChangeTaxRate = findViewById(R.id.btnChangeTaxRate);
        btnPanic = findViewById(R.id.btnPanic);

        txtFounderName = findViewById(R.id.txtFounderName);
        txtFoundedTime = findViewById(R.id.txtFoundedTime);
        txtWarsWon = findViewById(R.id.txtWarsWon);
        txtWarsLost = findViewById(R.id.txtWarsLost);
        txtAlliancesDisbanded = findViewById(R.id.txtAlliancesDisbanded);
        txtAffiliationsBroken = findViewById(R.id.txtAffiliationsBroken);
        txtICBMCount = findViewById(R.id.txtICBMCount);
        txtABMCount = findViewById(R.id.txtABMCount);
        txtWealth = findViewById(R.id.txtWealth);
        txtTaxRate = findViewById(R.id.txtTaxRate);
        txtMembers = findViewById(R.id.txtMembers);
        txtPrisoner = findViewById(R.id.txtPrisoner);

        txtWealth.setText(TextUtilities.GetCurrencyString(allianceShadow.GetWealth()));
        TextUtilities.AssignPercentageString(txtTaxRate, allianceShadow.GetTaxRate(), game);
        txtFounderName.setText(allianceShadow.GetFounder());
        txtFoundedTime.setText(TextUtilities.GetTimeAmount(allianceShadow.GetFoundedTime()));
        txtWarsWon.setText(String.valueOf(allianceShadow.GetWarsWon()));
        txtWarsLost.setText(String.valueOf(allianceShadow.GetWarsLost()));
        txtAlliancesDisbanded.setText(String.valueOf(allianceShadow.GetEnemyAllianceDisbands()));
        txtAffiliationsBroken.setText(String.valueOf(allianceShadow.GetAffiliationsBroken()));
        txtICBMCount.setText(String.valueOf(allianceShadow.GetICBMCount()));
        txtABMCount.setText(String.valueOf(allianceShadow.GetABMCount()));

        if(game.GetOurPlayer().GetAllianceMemberID() == allianceShadow.GetID() && game.GetOurPlayer().GetPrisoner())
        {
            txtPrisoner.setVisibility(VISIBLE);
        }
        else
        {
            txtPrisoner.setVisibility(GONE);
        }

        if(game.GetOurPlayer().GetIsAnMP() && game.GetOurPlayer().GetAllianceMemberID() == allianceShadow.GetID())
        {
            txtName.setVisibility(GONE);
            lytLeaderControls.setVisibility(VISIBLE);
            txtWithdraw1k.setText(TextUtilities.GetCurrencyString(1000));
            txtWithdraw10k.setText(TextUtilities.GetCurrencyString(10000));
            txtWithdraw100k.setText(TextUtilities.GetCurrencyString(100000));

            btnWithdraw1k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(game.AllianceInBattle(allianceShadow))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.alliance_in_battle));
                    }
                    else if(game.AtWar(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.at_war_cant_do_thing));
                    }
                    else if(game.InBattle(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_do_thing));
                    }
                    else
                    {
                        game.AllianceWithdraw(1000);
                    }
                }
            });

            btnWithdraw10k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(game.AllianceInBattle(allianceShadow))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.alliance_in_battle));
                    }
                    else if(game.AtWar(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.at_war_cant_do_thing));
                    }
                    else if(game.InBattle(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_do_thing));
                    }
                    else
                    {
                        game.AllianceWithdraw(10000);
                    }
                }
            });

            btnWithdraw100k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(game.AllianceInBattle(allianceShadow))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.alliance_in_battle));
                    }
                    else if(game.AtWar(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.at_war_cant_do_thing));
                    }
                    else if(game.InBattle(game.GetOurPlayer()))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.in_battle_cant_do_thing));
                    }
                    else
                    {
                        game.AllianceWithdraw(100000);
                    }
                }
            });

            btnChangeTaxRate.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    ChangeTaxRateView taxView = new ChangeTaxRateView();
                    taxView.SetGame(activity, game, allianceShadow.GetID());
                    taxView.show(activity.getFragmentManager(), "");
                }
            });

            btnPanic.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderDiplomacy();
                    launchDialog.SetMessage(context.getString(R.string.alliance_panic_confirm));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.AlliancePanic(allianceShadow.GetID());
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
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

            txtNameButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    txtNameButton.setVisibility(GONE);
                    lytNameEdit.setVisibility(VISIBLE);
                }
            });

            txtNameEdit.setText(allianceShadow.GetName());

            btnApplyName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetAllianceName(txtNameEdit.getText().toString());

                    txtNameButton.setVisibility(VISIBLE);
                    lytNameEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtNameEdit);
                }
            });

            txtDescription.setVisibility(GONE);

            txtDescriptionButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    txtDescriptionButton.setVisibility(GONE);
                    lytDescriptionEdit.setVisibility(VISIBLE);
                }
            });

            txtDescriptionEdit.setText(allianceShadow.GetDescription());

            btnApplyDescription.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetAllianceDescription(txtDescriptionEdit.getText().toString());

                    txtDescriptionButton.setVisibility(VISIBLE);
                    lytDescriptionEdit.setVisibility(GONE);
                    Utilities.DismissKeyboard(activity, txtDescriptionEdit);
                }
            });

            imgAvatar.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    {
                        activity.SetView(new UploadAvatarView(game, activity, LaunchUICommon.AvatarPurpose.ALLIANCE));
                    }
                }
            });
        }
        else
        {
            txtNameButton.setVisibility(GONE);
            txtDescriptionButton.setVisibility(GONE);
            lytLeaderControls.setVisibility(GONE);
        }

        btnJoinAlliance.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.InBattle(game.GetOurPlayer()))
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.alliance_join_engaged));
                }
                else if(!game.GetOurPlayer().GetAllianceCooloffExpired())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.cannot_ally, TextUtilities.GetTimeAmount(game.GetOurPlayer().GetAllianceCooloffRemaining())));
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderDiplomacy();

                    if(game.GetOurPlayer().GetRequestingToJoinAlliance() && game.GetAlliance(game.GetOurPlayer().GetAllianceJoiningID()) != null)
                    {
                        Alliance allianceOther = game.GetAlliance(game.GetOurPlayer().GetAllianceJoiningID());
                        launchDialog.SetMessage(context.getString(R.string.confirm_join_alliance_cancel_other, allianceShadow.GetName(), allianceOther.GetName()));
                    }
                    else
                    {
                        launchDialog.SetMessage(context.getString(R.string.confirm_join_alliance, allianceShadow.GetName()));
                    }

                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.JoinAlliance(allianceShadow.GetID());
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
            }
        });

        btnLeaveAlliance.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.InBattle(game.GetOurPlayer()))
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.alliance_leave_engaged));
                }
                else if(game.GetOurPlayer().GetPrisoner())
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_leave_alliance_prisoner));
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderDiplomacy();
                    launchDialog.SetMessage(context.getString(game.IAmTheOnlyLeader() ? R.string.confirm_disband_alliance : R.string.confirm_leave_alliance,
                    allianceShadow.GetName(),
                    TextUtilities.GetTimeAmount(game.GetConfig().GetAllianceCooloffTime())));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.LeaveAlliance();
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
            }
        });

        btnDeclareWar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_declare_war, allianceShadow.GetName()));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.DeclareWar(allianceShadow.GetID());
                    }
                });
                launchDialog.SetOnClickNo(new View.OnClickListener()
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

        btnSurrenderWar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_surrender_war, allianceShadow.GetName()));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.SurrenderWar(allianceShadow.GetID());
                    }
                });
                launchDialog.SetOnClickNo(new View.OnClickListener()
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

        btnCancelOffer.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_cancel_affiliation_offer, allianceShadow.GetName()));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.CancelAffiliation(allianceShadow.GetID());
                    }
                });
                launchDialog.SetOnClickNo(new View.OnClickListener()
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

        btnOfferAffiliation.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_offer_affiliation, allianceShadow.GetName()));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.OfferAffiliation(allianceShadow.GetID());
                    }
                });
                launchDialog.SetOnClickNo(new View.OnClickListener()
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

        final List<Alliance> Enemies = game.GetEnemies(allianceShadow);
        final List<Alliance> Friends = game.GetAffiliates(allianceShadow);
        final List<Player> Members = game.GetMembers(allianceShadow);

        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(Enemies.size() > 0)
                {
                    lytAtWarWith.setVisibility(VISIBLE);
                }

                for(final Alliance alliance : Enemies)
                {
                    RelationshipView view = new RelationshipView(game, activity, alliance);

                    view.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetView(new WarView(game, activity, game.GetWar(allianceShadow.GetID(), alliance.GetID())));
                        }
                    });

                    lytAtWarWith.addView(view);
                }

                if(Friends.size() > 0)
                {
                    lytAffiliatedWith.setVisibility(VISIBLE);
                }

                for(final Alliance alliance : Friends)
                {
                    RelationshipView view = new RelationshipView(game, activity, alliance);

                    view.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.SetView(new AllianceControl(game, activity, alliance));
                        }
                    });

                    lytAffiliatedWith.addView(view);
                }

                for(final Player player : Members)
                {
                    PlayerRankView view = new PlayerRankView(game, activity, player);

                    view.setOnClickListener(new OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            activity.ReturnToMainView();
                            activity.SelectEntity(player);
                        }
                    });

                    lytMembers.addView(view);
                }
            }
        });
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Player ourPlayer = game.GetOurPlayer();
                Alliance alliance = game.GetAlliance(allianceShadow.GetID());

                if(alliance != null)
                {
                    txtWealth.setText(TextUtilities.GetCurrencyString(allianceShadow.GetWealth()));
                    TextUtilities.AssignPercentageString(txtTaxRate, allianceShadow.GetTaxRate(), game);
                    txtFounderName.setText(allianceShadow.GetFounder());
                    txtFoundedTime.setText(TextUtilities.GetDateAndFullTime(allianceShadow.GetFoundedTime()));
                    txtWarsWon.setText(String.valueOf(allianceShadow.GetWarsWon()));
                    txtWarsLost.setText(String.valueOf(allianceShadow.GetWarsLost()));
                    txtAlliancesDisbanded.setText(String.valueOf(allianceShadow.GetEnemyAllianceDisbands()));
                    txtAffiliationsBroken.setText(String.valueOf(allianceShadow.GetAffiliationsBroken()));
                    txtICBMCount.setText(String.valueOf(allianceShadow.GetICBMCount()));
                    txtABMCount.setText(String.valueOf(allianceShadow.GetABMCount()));

                    imgAvatar.setImageBitmap(AvatarBitmaps.GetAllianceAvatar(activity, game, alliance));

                    if(ourPlayer.GetIsAnMP() && ourPlayer.GetAllianceMemberID() == alliance.GetID())
                    {
                        lytLeaderControls.setVisibility(VISIBLE);
                        imgAvatar.setBackground(getResources().getDrawable(R.drawable.text_button_normal));
                    }
                    else
                    {
                        lytLeaderControls.setVisibility(GONE);
                    }

                    txtName.setText(alliance.GetName());
                    txtNameButton.setText(alliance.GetName());
                    txtDescription.setText(alliance.GetDescription());
                    txtDescriptionButton.setText(alliance.GetDescription());

                    txtMembers.setText(String.valueOf(game.GetAllianceMemberCount(alliance)));

                    btnJoinAlliance.setVisibility(ourPlayer.GetAllianceMemberID() == Alliance.ALLIANCE_ID_UNAFFILIATED ? VISIBLE : GONE); //TO DO: And player is not in cool-off period from leaving an alliance.
                    btnLeaveAlliance.setVisibility(ourPlayer.GetAllianceMemberID() == alliance.GetID() ? VISIBLE : GONE);

                    boolean bShowDeclareWar = false;
                    boolean bShowOfferAffiliation = false;
                    boolean bShowSurrenderWar = false;
                    boolean bShowCancelOffer = false;

                    if(ourPlayer.GetIsAnMP() && ourPlayer.GetAllianceMemberID() != alliance.GetID())
                    {
                        if(game.CanDeclareWar(ourPlayer.GetAllianceMemberID(), alliance.GetID()))
                            bShowDeclareWar = true;

                        if(game.CanProposeAffiliation(ourPlayer.GetAllianceMemberID(), alliance.GetID()))
                            bShowOfferAffiliation = true;

                        if(game.CanProposeSurrender(ourPlayer.GetAllianceMemberID(), alliance.GetID()))
                            bShowSurrenderWar = true;

                        if(game.GetAllegiance(ourPlayer, alliance) == LaunchGame.Allegiance.PENDING_TREATY)
                            bShowCancelOffer = true;
                    }

                    btnDeclareWar.setVisibility(bShowDeclareWar ? VISIBLE : GONE);
                    btnOfferAffiliation.setVisibility(bShowOfferAffiliation ? VISIBLE : GONE);
                    btnSurrenderWar.setVisibility(bShowSurrenderWar ? VISIBLE : GONE);
                    btnCancelOffer.setVisibility(bShowCancelOffer ? VISIBLE : GONE);

                    txtJoining.setVisibility(game.GetOurPlayer().GetAllianceJoiningID() == allianceShadow.GetID() ? VISIBLE : GONE);

                    if(game.GetOurPlayer().GetAllianceMemberID() == alliance.GetID() && game.GetOurPlayer().GetPrisoner())
                    {
                        txtPrisoner.setVisibility(VISIBLE);
                    }
                    else
                    {
                        txtPrisoner.setVisibility(GONE);
                    }

                    switch(game.GetAllegiance(alliance))
                    {
                        case ENEMY:
                        {
                            txtAtWar.setVisibility(VISIBLE);
                            txtAffiliated.setVisibility(GONE);
                            txtAffiliationOffered.setVisibility(GONE);
                        }
                        break;

                        case PENDING_TREATY:
                        {
                            txtAtWar.setVisibility(GONE);
                            txtAffiliated.setVisibility(GONE);
                            txtAffiliationOffered.setVisibility(VISIBLE);
                        }
                        break;

                        case AFFILIATE:
                        {
                            txtAtWar.setVisibility(GONE);
                            txtAffiliated.setVisibility(VISIBLE);
                            txtAffiliationOffered.setVisibility(GONE);
                        }
                        break;

                        default:
                        {
                            txtAtWar.setVisibility(GONE);
                            txtAffiliated.setVisibility(GONE);
                            txtAffiliationOffered.setVisibility(GONE);
                        }
                    }
                }
                else
                {
                    activity.SetView(new DiplomacyView(game, activity));
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        //NOT NECESSARY UNLESS MORE ITEMS APPEAR IN FUTURE. EXAMPLE BELOW.
        /*int lTitleLargest = txtMembersTitle.getWidth();
        txtMembersTitle.setWidth(lTitleLargest);

        int lValueLargest = txtMembers.getWidth();
        txtMembers.setWidth(lValueLargest);*/

        //Set title items to same width, to justify text of the values.
        /*int lTitleLargest = Math.max(Math.max(txtRankTitle.getWidth(), txtScoreTitle.getWidth()), txtMembersTitle.getWidth());
        txtRankTitle.setWidth(lTitleLargest);
        txtScoreTitle.setWidth(lTitleLargest);
        txtMembersTitle.setWidth(lTitleLargest);

        int lValueLargest = Math.max(Math.max(txtRank.getWidth(), txtScore.getWidth()), txtMembers.getWidth());
        txtRank.setWidth(lValueLargest);
        txtScore.setWidth(lValueLargest);
        txtMembers.setWidth(lValueLargest);*/
    }

    @Override
    public void AvatarSaved(int lAvatarID)
    {
        for(int i = 0; i < lytAtWarWith.getChildCount(); i++)
        {
            View view = lytAtWarWith.getChildAt(i);

            if(view instanceof RelationshipView)
            {
                ((RelationshipView)view).AvatarSaved(lAvatarID);
            }
        }

        for(int i = 0; i < lytAffiliatedWith.getChildCount(); i++)
        {
            View view = lytAffiliatedWith.getChildAt(i);

            if(view instanceof RelationshipView)
            {
                ((RelationshipView)view).AvatarSaved(lAvatarID);
            }
        }
    }
}
