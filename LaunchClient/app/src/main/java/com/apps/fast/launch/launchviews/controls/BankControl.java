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
import launch.game.entities.CommandPost;

public class BankControl extends LaunchView
{
    private int lID;

    private LinearLayout btnUpgradeBankCapacity;
    private TextView txtBankCapacityUpgrade;
    private TextView txtBankCapacityUpgradeCost;

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
    private Bank bank;

    private static boolean bUpgradeConfirmHasBeenShown = false;

    public BankControl(LaunchClientGame game, MainActivity activity, int lBankID)
    {
        super(game, activity, true);
        lID = lBankID;

        if(game.GetBank(lID) != null)
        {
            bOurStructure = (game.GetBank(lID).GetOwnerID() == game.GetOurPlayerID());
            bank = game.GetBank(lID);
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

        btnUpgradeBankCapacity = findViewById(R.id.btnUpgradeBankCapacity);
        txtBankCapacityUpgrade = findViewById(R.id.txtBankCapacityUpgrade);
        txtBankCapacityUpgradeCost = findViewById(R.id.txtBankCapacityUpgradeCost);
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
         Bank bank = game.GetBank(lID);

        if(bOurStructure)
        {

        }
        else
        {
            lytBankAction.setVisibility(GONE);
            btnUpgradeBankCapacity.setVisibility(GONE);
        }
    }
}
