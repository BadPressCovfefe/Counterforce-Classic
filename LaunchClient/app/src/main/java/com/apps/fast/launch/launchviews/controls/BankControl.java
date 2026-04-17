package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.LaunchClientGame;
import launch.game.entities.Bank;
import launch.game.entities.Warehouse;

public class BankControl extends LaunchView
{
    private int lID;

    private TextView txtMoneyStats;
    private LinearLayout lytBankAction;
    private LinearLayout btnWithdraw1k;
    private LinearLayout btnWithdraw10k;
    private LinearLayout btnWithdraw100k;
    private LinearLayout btnDeposit1k;
    private LinearLayout btnDeposit10k;
    private LinearLayout btnDeposit100k;
    private TextView txtWithdraw1k;
    private TextView txtWithdraw10k;
    private TextView txtWithdraw100k;
    private TextView txtDeposit1k;
    private TextView txtDeposit10k;
    private TextView txtDeposit100k;

    private boolean bOurStructure;
    private Warehouse bank;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public BankControl(LaunchClientGame game, MainActivity activity, int lBankID)
    {
        super(game, activity, true);
        lID = lBankID;

        if(game.GetWarehouse(lID) != null)
        {
            bOurStructure = (game.GetWarehouse(lID).GetOwnerID() == game.GetOurPlayerID());
            bank = game.GetWarehouse(lID);
        }
        else
        {
            bOurStructure = false;
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_bank, this);

        txtMoneyStats = findViewById(R.id.txtMoneyStats);
        lytBankAction = findViewById(R.id.lytBankAction);
        btnWithdraw1k = findViewById(R.id.btnWithdraw1k);
        btnWithdraw10k = findViewById(R.id.btnWithdraw10k);
        btnWithdraw100k = findViewById(R.id.btnWithdraw100k);
        btnDeposit1k = findViewById(R.id.btnDeposit1k);
        btnDeposit10k = findViewById(R.id.btnDeposit10k);
        btnDeposit100k = findViewById(R.id.btnDeposit100k);
        txtWithdraw1k = findViewById(R.id.txtWithdraw1k);
        txtWithdraw10k = findViewById(R.id.txtWithdraw10k);
        txtWithdraw100k = findViewById(R.id.txtWithdraw100k);
        txtDeposit1k = findViewById(R.id.txtDeposit1k);
        txtDeposit10k = findViewById(R.id.txtDeposit10k);
        txtDeposit100k = findViewById(R.id.txtDeposit100k);

        if(bOurStructure)
        {
            txtWithdraw1k.setText(TextUtilities.GetCurrencyString(1000));
            txtWithdraw10k.setText(TextUtilities.GetCurrencyString(10000));
            txtWithdraw100k.setText(TextUtilities.GetCurrencyString(100000));
            txtDeposit1k.setText(TextUtilities.GetCurrencyString(1000));
            txtDeposit10k.setText(TextUtilities.GetCurrencyString(10000));
            txtDeposit100k.setText(TextUtilities.GetCurrencyString(100000));

            txtMoneyStats.setText(context.getString(R.string.bank_money_stats, TextUtilities.GetCurrencyString(bank.GetWealth()), TextUtilities.GetCurrencyString(bank.GetMaxCapacity())));

            btnWithdraw1k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bank.GetOnline())
                    {
                        game.BankAction(lID, 1000, true);
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });

            btnWithdraw10k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bank.GetOnline())
                    {
                        game.BankAction(lID, 10000, true);
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });

            btnWithdraw100k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bank.GetOnline())
                    {
                        game.BankAction(lID, 100000, true);
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });

            btnDeposit1k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bank.GetOnline())
                    {
                        if(bank.GetRemainingCapacity() >= 1000)
                        {
                            game.BankAction(lID, 1000, false);
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.bank_full));
                        }
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });

            btnDeposit10k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bank.GetOnline())
                    {
                        if(bank.GetRemainingCapacity() >= 10000)
                        {
                            game.BankAction(lID, 10000, false);
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.bank_full));
                        }
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });

            btnDeposit100k.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(bank.GetOnline())
                    {
                        if(bank.GetRemainingCapacity() >= 100000)
                        {
                            game.BankAction(lID, 100000, false);
                        }
                        else
                        {
                            activity.ShowBasicOKDialog(context.getString(R.string.bank_full));
                        }
                    }
                    else
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.bank_offline));
                    }
                }
            });
        }
        else
        {
            txtMoneyStats.setVisibility(GONE);
            lytBankAction.setVisibility(GONE);
        }

        Update();
    }

    @Override
    public void Update()
    {
         Warehouse bank = game.GetWarehouse(lID);

        if(bOurStructure)
        {
            txtWithdraw1k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= 1000 ? R.attr.GoodColour : R.attr.BadColour));
            txtWithdraw10k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= 10000 ? R.attr.GoodColour : R.attr.BadColour));
            txtWithdraw100k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= 100000 ? R.attr.GoodColour : R.attr.BadColour));
            txtDeposit1k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= 1000 ? R.attr.GoodColour : R.attr.BadColour));
            txtDeposit10k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= 10000 ? R.attr.GoodColour : R.attr.BadColour));
            txtDeposit100k.setTextColor(Utilities.ColourFromAttr(context, game.GetOurPlayer().GetWealth() >= 100000 ? R.attr.GoodColour : R.attr.BadColour));

            txtMoneyStats.setText(context.getString(R.string.bank_money_stats, TextUtilities.GetCurrencyString(bank.GetWealth()), TextUtilities.GetCurrencyString(bank.GetMaxCapacity())));
        }
        else
        {
            lytBankAction.setVisibility(GONE);
        }
    }
}
