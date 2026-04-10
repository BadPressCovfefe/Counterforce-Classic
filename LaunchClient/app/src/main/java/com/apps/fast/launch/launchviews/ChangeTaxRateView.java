package com.apps.fast.launch.launchviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;

import launch.game.Alliance;
import launch.game.Defs;
import launch.game.LaunchClientGame;

/**
 * Created by tobster on 03/09/16.
 */
public class ChangeTaxRateView extends DialogFragment
{
    private LinearLayout lytContent;
    private TextView btnOk;
    private TextView btnCancel;
    private TextView txtCurrentTaxRate;
    private ImageView btnDecreaseTax;
    private ImageView btnIncreaseTax;

    private MainActivity activity;
    private LaunchClientGame game;
    private Alliance alliance;
    private float fltTaxRate;

    private final float fltTaxIncrement = 0.05f;

    public void SetGame(MainActivity activity, LaunchClientGame game, int lAllianceID)
    {
        this.activity = activity;
        this.game = game;
        this.alliance = game.GetAlliance(lAllianceID);

        if(alliance != null)
            this.fltTaxRate = alliance.GetTaxRate();
        else
            dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if(alliance != null && game != null)
        {
            View us = getActivity().getLayoutInflater().inflate(R.layout.view_change_tax_rate, null);

            lytContent = (LinearLayout)us.findViewById(R.id.lytContent);

            btnOk = (TextView)us.findViewById(R.id.btnOk);
            btnCancel = (TextView)us.findViewById(R.id.btnCancel);
            btnIncreaseTax = us.findViewById(R.id.btnIncreaseTax);
            btnDecreaseTax = us.findViewById(R.id.btnDecreaseTax);
            txtCurrentTaxRate = us.findViewById(R.id.txtCurrentTaxRate);

            TextUtilities.AssignPercentageString(txtCurrentTaxRate, fltTaxRate, game);

            btnIncreaseTax.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(fltTaxRate <= (Defs.MAX_TAX_RATE - fltTaxIncrement))
                    {
                        fltTaxRate += fltTaxIncrement;
                        TextUtilities.AssignPercentageString(txtCurrentTaxRate, fltTaxRate, game);
                    }
                }
            });

            btnDecreaseTax.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(fltTaxRate >= (Defs.MIN_TAX_RATE + fltTaxIncrement))
                    {
                        fltTaxRate -= fltTaxIncrement;
                        TextUtilities.AssignPercentageString(txtCurrentTaxRate, fltTaxRate, game);
                    }
                }
            });

            btnOk.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    game.SetAllianceTaxRate(fltTaxRate);
                    dismiss();
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    dismiss();
                }
            });

            builder.setView(us);
        }
        else
            dismiss();

        return builder.create();
    }

    public void AddContent(View view)
    {
        lytContent.addView(view);
    }
}
