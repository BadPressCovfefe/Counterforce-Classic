package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.entities.PlayerView;
import com.apps.fast.launch.views.LaunchDialog;
import com.apps.fast.launch.views.LaunchableSelectionView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import launch.game.Config;
import launch.game.LaunchClientGame;
import launch.game.entities.MissileSite;
import launch.game.entities.SAMSite;
import launch.game.systems.MissileSystem;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.utilities.LaunchEvent;

/**
 * Created by tobster on 16/10/15.
 */
public class RaiseBountyView extends LaunchView
{

    private int lAmountToSend = 0;
    private int ID;

    private LinearLayout btnRaiseBounty;
    private LinearLayout btnAdd1k;
    private LinearLayout btnAdd10k;
    private LinearLayout btnAdd100k;
    private LinearLayout btnSubtract1k;
    private LinearLayout btnSubtract10k;
    private LinearLayout btnSubtract100k;
    private TextView btnCancel;
    private TextView txtAdd1k;
    private TextView txtAdd10k;
    private TextView txtAdd100k;
    private TextView txtSubtract1k;
    private TextView txtSubtract10k;
    private TextView txtSubtract100k;
    private TextView txtAmountToSend;

    public RaiseBountyView(LaunchClientGame game, MainActivity activity, int lPlayerID)
    {
        super(game, activity, true);

        this.ID = lPlayerID;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_raisebounty, this);

        btnRaiseBounty = findViewById(R.id.btnRaiseBounty);
        btnAdd1k = findViewById(R.id.btnAdd1k);
        btnAdd10k = findViewById(R.id.btnAdd10k);
        btnAdd100k = findViewById(R.id.btnAdd100k);
        btnSubtract1k = findViewById(R.id.btnSubtract1k);
        btnSubtract10k = findViewById(R.id.btnSubtract10k);
        btnSubtract100k = findViewById(R.id.btnSubtract100k);
        btnCancel = findViewById(R.id.btnCancel);
        txtAmountToSend = findViewById(R.id.txtAmountToSend);

        txtAdd1k = findViewById(R.id.txtAdd1k);
        txtAdd10k = findViewById(R.id.txtAdd10k);
        txtAdd100k = findViewById(R.id.txtAdd100k);
        txtSubtract1k = findViewById(R.id.txtSubtract1k);
        txtSubtract10k = findViewById(R.id.txtSubtract10k);
        txtSubtract100k = findViewById(R.id.txtSubtract100k);

        txtAdd1k.setText(context.getString(R.string.button_add, TextUtilities.GetCurrencyString(1000)));
        txtAdd10k.setText(context.getString(R.string.button_add, TextUtilities.GetCurrencyString(10000)));
        txtAdd100k.setText(context.getString(R.string.button_add, TextUtilities.GetCurrencyString(100000)));
        txtSubtract1k.setText(context.getString(R.string.button_subtract, TextUtilities.GetCurrencyString(1000)));
        txtSubtract10k.setText(context.getString(R.string.button_subtract, TextUtilities.GetCurrencyString(10000)));
        txtSubtract100k.setText(context.getString(R.string.button_subtract, TextUtilities.GetCurrencyString(100000)));

        txtAdd1k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend + 1000 ? R.attr.GoodColour : R.attr.BadColour));
        txtAdd10k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend + 10000 ? R.attr.GoodColour : R.attr.BadColour));
        txtAdd100k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend + 100000 ? R.attr.GoodColour : R.attr.BadColour));

        txtSubtract1k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 1000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
        txtSubtract10k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 10000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
        txtSubtract100k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 100000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));

        txtAmountToSend.setText(TextUtilities.GetCurrencyString(lAmountToSend));
        txtAmountToSend.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend ? R.attr.GoodColour : R.attr.BadColour));

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                lAmountToSend = 0;
                ReturnToParentView();
            }
        });

        btnSubtract100k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(lAmountToSend - 100000 >= 0)
                {
                    lAmountToSend -= 100000;
                }
            }
        });

        btnSubtract10k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(lAmountToSend - 10000 >= 0)
                {
                    lAmountToSend -= 10000;
                }
            }
        });

        btnSubtract1k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(lAmountToSend - 1000 >= 0)
                {
                    lAmountToSend -= 1000;
                }
            }
        });

        btnAdd100k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= lAmountToSend + 100000)
                {
                    lAmountToSend += 100000;
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.cant_add_more));
                    launchDialog.SetOnClickOk(new View.OnClickListener()
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

        btnAdd10k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= lAmountToSend + 10000)
                {
                    lAmountToSend += 10000;
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.cant_add_more));
                    launchDialog.SetOnClickOk(new View.OnClickListener()
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

        btnAdd1k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= lAmountToSend + 1000)
                {
                    lAmountToSend += 1000;
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.cant_add_more));
                    launchDialog.SetOnClickOk(new View.OnClickListener()
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

        btnRaiseBounty.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.GetOurPlayer().GetWealth() >= lAmountToSend && lAmountToSend > 0)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderAddBounty();
                    launchDialog.SetMessage(context.getString(R.string.confirm_raise_bounty, TextUtilities.GetCurrencyString(lAmountToSend)));
                    launchDialog.SetOnClickOk(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            game.AddBounty(ID, lAmountToSend);
                            launchDialog.dismiss();
                            ReturnToParentView();
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
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.cant_raise_bounty));
                    launchDialog.SetOnClickOk(new View.OnClickListener()
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
    }

    @Override
    public void Update()
    {
        txtAmountToSend.setText(TextUtilities.GetCurrencyString(lAmountToSend));
        txtAmountToSend.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend ? R.attr.GoodColour : R.attr.BadColour));

        txtAdd1k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend + 1000 ? R.attr.GoodColour : R.attr.BadColour));
        txtAdd10k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend + 10000 ? R.attr.GoodColour : R.attr.BadColour));
        txtAdd100k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= lAmountToSend + 100000 ? R.attr.GoodColour : R.attr.BadColour));

        txtSubtract1k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 1000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
        txtSubtract10k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 10000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
        txtSubtract100k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 100000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
    }

    private void ReturnToParentView()
    {
        activity.SelectEntity(game.GetPlayer(ID));
    }
}