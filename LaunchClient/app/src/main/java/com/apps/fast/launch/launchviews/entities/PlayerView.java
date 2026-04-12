package com.apps.fast.launch.launchviews.entities;

import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.AvatarBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.GiveWealthView;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.launchviews.SendMessageView;
import com.apps.fast.launch.launchviews.UnitControls;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.Alliance;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.User;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Player;

public class PlayerView extends LaunchView
{
    private int lPlayerID;
    private ImageView imgPlayer;
    private TextView txtPlayerName;
    private TextView txtPlayerAlliance;
    private TextView txtPlayerStatus;
    private TextView txtPlayerMultiplier;
    private ImageView imgLeader;
    private ImageView imgAlly;
    private ImageView imgWar;
    private ImageView imgAffiliate;
    private TextView txtRespawnProtected;
    private TextView txtLastSeen;
    private TextView txtMoney;
    private TextView txtGettingStats;
    private LinearLayout lytStats;
    private TextView txtDamageInflictedTotal;
    private TextView txtOffenceSpendingCost;
    private TextView txtDamageReceivedTotal;
    private TextView txtDefenceSpendingCost;

    private LinearLayout btnPromote;
    private LinearLayout btnKick;
    private LinearLayout btnSendMessage;
    private LinearLayout btnBlacklist;
    private ImageView imgBlacklist;
    private LinearLayout btnGiveWealth;
    private LinearLayout btnObliterate;

    private LinearLayout btnAdminOptions;
    private LinearLayout lytAdminOptions;
    private LinearLayout btnTransferAccount;
    private LinearLayout btnUnban;
    private LinearLayout btnBan;
    private LinearLayout btnPermBanMultiaccount;
    private LinearLayout btnPermBanEmulator;
    private LinearLayout btnResetAvatar;
    private LinearLayout btnResetName;
    private EditText txtBanReason;
    private TextView btnCommonReasonImage;
    private TextView btnCommonReasonName;

    private LinearLayout lytRank;
    private TextView txtRankName;
    private TextView txtExperience;
    private TextView txtWins;
    private TextView txtTotalKills;
    private TextView txtDistanceTraveled;
    private TextView txtTotalDeaths;
    private TextView txtKDR;
    private ProgressBar prgNextRank;
    private LinearLayout lytMoneyOptions;
    private LinearLayout lytKDR;
    boolean bBlacklisting = true;

    public PlayerView(LaunchClientGame game, MainActivity activity, int lPlayerID)
    {
        super(game, activity, true);
        this.lPlayerID = lPlayerID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_player, this);
        ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

        final Player player = game.GetPlayer(lPlayerID);

        imgPlayer = findViewById(R.id.imgPlayer);
        txtPlayerName = findViewById(R.id.txtPlayerName);
        txtPlayerAlliance = findViewById(R.id.txtPlayerAlliance);
        txtPlayerMultiplier = findViewById(R.id.txtPlayerMultiplier);
        imgLeader = findViewById(R.id.imgLeader);
        imgAlly = findViewById(R.id.imgAlly);
        imgWar = findViewById(R.id.imgWar);
        imgAffiliate = findViewById(R.id.imgAffiliate);
        txtRespawnProtected = findViewById(R.id.txtRespawnProtected);
        txtLastSeen = findViewById(R.id.txtLastSeen);
        txtMoney = findViewById(R.id.txtMoney);
        txtWins = findViewById(R.id.txtWins);
        btnTransferAccount = findViewById(R.id.btnTransferAccount);

        txtPlayerStatus = findViewById(R.id.txtPlayerStatus);

        txtGettingStats = findViewById(R.id.txtGettingStats);
        lytStats = findViewById(R.id.lytStats);
        txtDamageInflictedTotal = findViewById(R.id.txtDamageInflictedTotal);
        txtOffenceSpendingCost = findViewById(R.id.txtOffenceSpendingCost);
        txtDamageReceivedTotal = findViewById(R.id.txtDamageReceivedTotal);
        txtDefenceSpendingCost = findViewById(R.id.txtDefenceSpendingCost);
        txtTotalDeaths = findViewById(R.id.txtTotalDeaths);
        txtTotalKills = findViewById(R.id.txtTotalKills);
        txtDistanceTraveled = findViewById(R.id.txtDistanceTraveled);
        txtExperience = findViewById(R.id.txtExperience);
        txtKDR = findViewById(R.id.txtKDR);
        lytKDR = findViewById(R.id.lytKDR);

        btnPromote = findViewById(R.id.btnPromote);
        btnKick = findViewById(R.id.btnKick);
        lytMoneyOptions = findViewById(R.id.lytMoneyOptions);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnBlacklist = findViewById(R.id.btnBlacklist);
        imgBlacklist = findViewById(R.id.imgBlacklist);
        btnGiveWealth = findViewById(R.id.btnGiveWealth);
        btnObliterate = findViewById(R.id.btnObliterate);

        lytRank = findViewById(R.id.lytRank);
        txtRankName = findViewById(R.id.txtRankName);
        prgNextRank = findViewById(R.id.prgNextRank);

        btnAdminOptions = findViewById(R.id.btnAdminOptions);
        lytAdminOptions = findViewById(R.id.lytAdminOptions);
        btnBan = findViewById(R.id.btnBan);
        btnPermBanMultiaccount = findViewById(R.id.btnPermBanMultiaccount);
        btnPermBanEmulator = findViewById(R.id.btnPermBanEmulators);
        btnResetAvatar = findViewById(R.id.btnResetAvatar);
        btnResetName = findViewById(R.id.btnResetName);
        txtBanReason = findViewById(R.id.txtBanReason);
        btnCommonReasonImage = findViewById(R.id.btnCommonReasonImage);
        btnCommonReasonName = findViewById(R.id.btnCommonReasonName);
        btnUnban = findViewById(R.id.btnUnban);

        lytMoneyOptions.setVisibility(lPlayerID == game.GetOurPlayerID() || !game.GetOurPlayer().Functioning() || !game.GetPlayer(lPlayerID).Functioning() ? GONE : VISIBLE);

        if(player.GetIsAnAdmin())
        {
            txtPlayerStatus.setText(context.getString(R.string.player_status_admin));
        }
        else if(player.GetChampion())
        {
            txtPlayerStatus.setText(context.getString(R.string.player_status_champion));
        }
        else if(player.IsAMember())
        {
            txtPlayerStatus.setText(context.getString(R.string.player_status_member));
        }
        else if(player.GetVeteran())
        {
            txtPlayerStatus.setText(context.getString(R.string.player_status_veteran));
        }
        else
        {
            txtPlayerStatus.setVisibility(GONE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player.GetRank() != 29)
        {
            int lExperience = player.GetExperience();

            if(lExperience < 0)
            {
                prgNextRank.setVisibility(GONE);
            }
            else
            {
                prgNextRank.setMin(game.GetLastXPThreshold(player));
                prgNextRank.setMax(game.GetNextRankThreshold(player.GetRank()));
                prgNextRank.setProgress(player.GetExperience());
            }
        }
        else
        {
            prgNextRank.setVisibility(GONE);
        }

        if(lPlayerID != game.GetOurPlayerID())
        {
            if(game.GetOurPlayer().Functioning() && player.Functioning())
            {
                lytMoneyOptions.setVisibility(VISIBLE);
            }
        }
        else
        {
            lytMoneyOptions.setVisibility(GONE);
        }

        btnSendMessage.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.SetView(new SendMessageView(game, activity, player.GetID()));
            }
        });

        if(game.GetOurPlayer().Blacklisted(lPlayerID))
        {
            //The blacklistee is already blacklisted, so we are whitelisting them now.
            bBlacklisting = false;
            imgBlacklist.setImageResource(R.drawable.button_whitelist);
        }
        else
        {
            imgBlacklist.setImageResource(R.drawable.button_blacklist);
        }

        if(!bBlacklisting || (!game.EntityIsFriendly(player, game.GetOurPlayer()) && !game.GetAttackIsBullying(game.GetOurPlayer(), player)))
        {
            btnBlacklist.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderDiplomacy();
                    launchDialog.SetMessage(context.getString(bBlacklisting ? R.string.confirm_blacklist : R.string.confirm_whitelist, player.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.Blacklist(lPlayerID);
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
        }
        else
        {
            btnBlacklist.setVisibility(GONE);
        }

        btnPromote.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Alliance alliance = game.GetAlliance(player.GetAllianceMemberID());

                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderDiplomacy();
                launchDialog.SetMessage(context.getString(R.string.confirm_promote, player.GetName(), alliance.GetName()));
                launchDialog.SetOnClickYes(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        launchDialog.dismiss();
                        game.Promote(lPlayerID);
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

        btnKick.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.InBattle(player))
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.alliance_kick_engaged, player.GetName()));
                }
                else
                {
                    Alliance alliance = game.GetAlliance(player.GetAllianceMemberID());

                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderDiplomacy();
                    launchDialog.SetMessage(context.getString(R.string.confirm_kick, player.GetName(), alliance.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.Kick(lPlayerID);
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

        float fltMultiplier = game.GetNetWorthMultiplier(game.GetOurPlayer(), player);

        txtPlayerMultiplier.setText(TextUtilities.GetMultiplierString(fltMultiplier));

        if(fltMultiplier < Defs.NOOB_WARNING)
            txtPlayerMultiplier.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        else if(fltMultiplier > Defs.ELITE_WARNING)
            txtPlayerMultiplier.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));

        game.GetPlayerStats(player);

        RefreshUI();

        if(game.GetOurPlayer().GetIsAnAdmin())
        {
            btnAdminOptions.setVisibility(VISIBLE);

            btnAdminOptions.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.admin_longpress));
                }
            });

            btnAdminOptions.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    lytAdminOptions.setVisibility(VISIBLE);
                    btnAdminOptions.setVisibility(GONE);

                    //Must be off the main thread as it does network stuff.
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            game.GetUserData(player);
                        }
                    }).start();

                    return true;
                }
            });

            btnBan.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.ban_longpress));
                }
            });

            btnUnban.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.unban_longpress));
                }
            });

            btnPermBanMultiaccount.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.ban_longpress));
                }
            });

            btnPermBanEmulator.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.ban_longpress));
                }
            });

            btnGiveWealth.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.SetView(new GiveWealthView(game, activity, lPlayerID));
                }
            });

            btnObliterate.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderDiplomacy();
                    launchDialog.SetMessage(context.getString(R.string.confirm_obliterate, player.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.Obliterate(lPlayerID);
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

            btnBan.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    if(txtBanReason.getText().length() > 0)
                    {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.ban_confirm, player.GetName()));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                game.Ban(txtBanReason.getText().toString(), lPlayerID, false);
                                launchDialog.dismiss();
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
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.ban_reason_specify));
                    }

                    return true;
                }
            });

            btnUnban.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderLaunch();
                        launchDialog.SetMessage(context.getString(R.string.unban_confirm, player.GetName()));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                game.Unban(player.GetID());
                                launchDialog.dismiss();
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

                    return true;
                }
            });

            btnPermBanMultiaccount.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.ban_confirm, player.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.Ban("Multiple accounts", lPlayerID, true);
                            launchDialog.dismiss();
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

                    return true;
                }
            });

            btnPermBanEmulator.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.ban_confirm, player.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.Ban("Emulators/GPS Spoofers/Other cheating", lPlayerID, true);
                            launchDialog.dismiss();
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

                    return true;
                }
            });

            btnCommonReasonImage.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    txtBanReason.setText(context.getText(R.string.common_reason_image));
                }
            });

            btnCommonReasonName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    txtBanReason.setText(context.getText(R.string.common_reason_name));
                }
            });

            btnResetAvatar.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.avreset_longpress));
                }
            });

            btnResetAvatar.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.avreset_confirm, player.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.ResetAvatar(lPlayerID);
                            launchDialog.dismiss();
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

                    return true;
                }
            });

            btnResetName.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.namereset_longpress));
                }
            });

            btnResetName.setOnLongClickListener(new OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.namereset_confirm, player.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.ResetName(lPlayerID);
                            launchDialog.dismiss();
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

                    return true;
                }
            });

            btnTransferAccount.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    activity.TransferAccountMode(player);
                }
            });
        }
    }

    private void RefreshUI()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Player player = game.GetPlayer(lPlayerID);

                if(player != null)
                {
                    if(game.GetOurPlayer() != null)
                    {
                        btnPromote.setVisibility(game.GetOurPlayer().GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED &&
                                game.GetOurPlayer().GetAllianceMemberID() == player.GetAllianceMemberID() &&
                                game.GetOurPlayer().GetIsAnMP() &&
                                !player.GetIsAnMP() ? VISIBLE : GONE);

                        btnKick.setVisibility(game.GetOurPlayer().GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED &&
                                game.GetOurPlayer().GetAllianceMemberID() == player.GetAllianceMemberID() &&
                                game.GetOurPlayer().GetIsAnMP() &&
                                !player.GetIsAnMP() ? VISIBLE : GONE);

                        lytMoneyOptions.setVisibility(lPlayerID == game.GetOurPlayerID() || !game.GetOurPlayer().Functioning() || !game.GetPlayer(lPlayerID).Functioning() ? GONE : VISIBLE);

                        imgPlayer.setImageBitmap(AvatarBitmaps.GetPlayerAvatar(activity, game, player));
                        txtPlayerName.setText(player.GetName());

                        if(player.GetAllianceMemberID() != Alliance.ALLIANCE_ID_UNAFFILIATED)
                        {
                            if(game.GetAlliance(player.GetAllianceMemberID()) != null)
                                txtPlayerAlliance.setText(game.GetAlliance(player.GetAllianceMemberID()).GetName());

                            txtPlayerAlliance.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                        }
                        else
                        {
                            txtPlayerAlliance.setText(context.getString(R.string.unaffiliated));
                            txtPlayerAlliance.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                        }

                        if(player.GetIsAnMP())
                        {
                            imgLeader.setVisibility(VISIBLE);
                            imgLeader.setImageResource(R.drawable.icon_leader);
                        }
                        else
                        {
                            imgLeader.setVisibility(GONE);
                        }

                        switch(game.GetAllegiance(player))
                        {
                            case ALLY:
                            {
                                imgAlly.setVisibility(VISIBLE);
                                imgWar.setVisibility(GONE);
                                imgAffiliate.setVisibility(GONE);
                            }
                            break;

                            case AFFILIATE:
                            {
                                imgAlly.setVisibility(GONE);
                                imgWar.setVisibility(GONE);
                                imgAffiliate.setVisibility(VISIBLE);
                            }
                            break;

                            case ENEMY:
                            {
                                imgAlly.setVisibility(GONE);
                                imgWar.setVisibility(VISIBLE);
                                imgAffiliate.setVisibility(GONE);
                            }
                            break;

                            default:
                            {
                                imgAlly.setVisibility(GONE);
                                imgWar.setVisibility(GONE);
                                imgAffiliate.setVisibility(GONE);
                            }
                        }

                        if(game.GetPlayerOnline(player))
                        {
                            txtLastSeen.setText(context.getString(R.string.online_now));
                        }
                        else
                        {
                            txtLastSeen.setText(context.getString(R.string.player_seen, TextUtilities.GetDateAndFullTime(player.GetLastSeen())));
                        }

                        if(player.GetRespawnProtected())
                        {
                            txtRespawnProtected.setVisibility(VISIBLE);
                            txtRespawnProtected.setText(context.getString(R.string.player_respawn_protected, TextUtilities.GetFutureTime(player.GetStateTimeRemaining())));
                        }
                        else
                        {
                            txtRespawnProtected.setVisibility(GONE);
                        }

                        txtMoney.setText(TextUtilities.GetCurrencyString(player.GetWealth()));

                        if(player.GetHasFullStats())
                        {

                            txtGettingStats.setVisibility(GONE);
                            lytStats.setVisibility(VISIBLE);
                            lytRank.setVisibility(VISIBLE);

                            if(player.GetTotalDeaths() > 0 && player.GetTotalKills() > 0)
                            {
                                lytKDR.setVisibility(VISIBLE);
                                txtKDR.setText(String.format("%.2f", player.GetKDR()));
                            }
                            else
                            {
                                lytKDR.setVisibility(GONE);
                            }

                            txtRankName.setText(String.valueOf(player.GetRank()));
                            txtExperience.setText(Integer.toString(player.GetExperience()));

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player.GetRank() != 29)
                            {
                                if(player.GetExperience() < 0)
                                {
                                    prgNextRank.setVisibility(GONE);
                                }
                                else
                                {
                                    prgNextRank.setMin(game.GetLastXPThreshold(player));
                                    prgNextRank.setMax(game.GetNextRankThreshold(player.GetRank()));
                                    prgNextRank.setProgress(player.GetExperience());
                                }
                            }
                            else
                            {
                                prgNextRank.setVisibility(GONE);
                            }

                            txtWins.setText(Integer.toString(player.GetChampionCount()));
                            txtTotalKills.setText(Integer.toString(player.GetTotalKills()));
                            txtDistanceTraveled.setText(TextUtilities.GetDistanceStringFromKM(player.GetDistanceTraveled()));
                            txtTotalDeaths.setText(Integer.toString(player.GetTotalDeaths()));
                            txtDamageInflictedTotal.setText(TextUtilities.GetDamageString(player.GetDamageInflicted()));
                            txtOffenceSpendingCost.setText(TextUtilities.GetCurrencyString(player.GetOffenceSpending()));
                            txtDamageReceivedTotal.setText(TextUtilities.GetDamageString(player.GetDamageReceived()));
                            txtDefenceSpendingCost.setText(TextUtilities.GetCurrencyString(player.GetDefenceSpending()));
                        }
                    }
                    else
                    {
                        Finish(true);
                    }
                }
                else
                {
                    Finish(true);
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        if(entity instanceof Player)
        {
            if (entity.GetID() == lPlayerID)
            {
                RefreshUI();
            }
        }
    }

    @Override
    public void EntityRemoved(LaunchEntity entity)
    {
        if(entity instanceof Player)
        {
            if(entity.GetID() == lPlayerID)
                Finish(true);
        }
    }

    public void UserReceived(final User user)
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                findViewById(R.id.txtLoadingUserInfo).setVisibility(GONE);
                findViewById(R.id.lytUserInfo).setVisibility(VISIBLE);

                ((TextView)findViewById(R.id.txtPlayerID)).setText(String.format("Player ID: %d", user.GetPlayerID()));
                ((TextView)findViewById(R.id.txtLastIP)).setText(user.GetLastIP());
                ((TextView)findViewById(R.id.txtLastConnectionType)).setText(user.GetLastTypeMobile() ? "Cellular" : "WiFi");
                ((TextView)findViewById(R.id.txtLastChecked)).setText(String.format("Last check %s", TextUtilities.GetDateAndTime(user.GetDeviceCheckedDate())));
                ((TextView)findViewById(R.id.txtChecksFailed)).setText(String.format(user.GetLastDeviceCheckFailed() ? "Checks not passed" : "Checks passed"));
                ((TextView)findViewById(R.id.txtCheckAPIFailed)).setText(String.format(user.GetDeviceChecksAPIFailed() ? "Check API fail" : "Checks API OK"));
                ((TextView)findViewById(R.id.txtProscribed)).setText(String.format(user.GetProscribed() ? "PROSCRIBED" : "Not Proscribed"));
                ((TextView)findViewById(R.id.txtFailCode)).setText(String.format("Fail code: %d", user.GetDeviceChecksFailCode()));
                ((TextView)findViewById(R.id.txtProfileMatch)).setText(String.format(user.GetProfileMatch() ? "Profile OK" : "Profile fail"));
                ((TextView)findViewById(R.id.txtBasicIntegrity)).setText(String.format(user.GetBasicIntegrity() ? "Integrity OK" : "Integrity fail"));
                ((TextView)findViewById(R.id.txtManuallyApproved)).setText(String.format(user.GetApproved() ? "Manually approved" : "Not manually approved"));
                ((TextView)findViewById(R.id.txtDeviceHash)).setText(String.format("Device ID %s", user.GetDeviceID()));
                ((TextView)findViewById(R.id.txtAppListHash)).setText(String.format("Apps hash %s", user.GetAppListShortHash()));

                switch(user.GetBanState())
                {
                    case NOT: ((TextView)findViewById(R.id.txtBanState)).setText("Not banned"); break;
                    case TIME_BANNED: ((TextView)findViewById(R.id.txtBanState)).setText("Temp banned"); break;
                    case TIME_BANNED_ACK: ((TextView)findViewById(R.id.txtBanState)).setText("Temp banned (ack)"); break;
                    case PERMABANNED: ((TextView)findViewById(R.id.txtBanState)).setText("Permabanned"); break;
                }

                ((TextView)findViewById(R.id.txtNextBanTime)).setText(String.format("Next ban: %s", TextUtilities.GetTimeAmount(user.GetNextBanTime())));
                ((TextView)findViewById(R.id.txtBanDuration)).setText(String.format("Ban left: %s", TextUtilities.GetTimeAmount(user.GetBanDurationRemaining())));
                ((TextView)findViewById(R.id.txtUserBanReason)).setText(String.format("Reason: %s", user.GetBanReason()));
                ((TextView)findViewById(R.id.txtExpired)).setText(String.format("Expired: %s", TextUtilities.GetDateAndTime(user.GetExpiredOn())));

                if(user.GetUnderAttack())
                    ((TextView)findViewById(R.id.txtUnderAttack)).setText("Attack notif");
                else
                    findViewById(R.id.txtUnderAttack).setVisibility(GONE);

                if(user.GetAllyUnderAttack())
                    ((TextView)findViewById(R.id.txtAllyUnderAttack)).setText("Ally notif");
                else
                    findViewById(R.id.txtAllyUnderAttack).setVisibility(GONE);

                if(user.GetNuclearEscalation())
                    ((TextView)findViewById(R.id.txtNukeEscalation)).setText("Nuclear notif");
                else
                    findViewById(R.id.txtNukeEscalation).setVisibility(GONE);
            }
        });
    }
}
