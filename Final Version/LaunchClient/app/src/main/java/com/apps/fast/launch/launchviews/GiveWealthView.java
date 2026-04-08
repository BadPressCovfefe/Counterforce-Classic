package com.apps.fast.launch.launchviews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.ClientDefs;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;
import launch.game.LaunchClientGame;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class GiveWealthView extends LaunchView
{

    private int lAmountToSend = 0;
    private int ID;

    private LinearLayout btnSendWealth;
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
    private TextView btnType;

    private ResourceType typeToSend;

    public GiveWealthView(LaunchClientGame game, MainActivity activity, int lPlayerID)
    {
        super(game, activity, true);

        this.ID = lPlayerID;
        this.typeToSend = ResourceType.WEALTH;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_givewealth, this);

        btnSendWealth = findViewById(R.id.btnSendWealth);
        btnAdd1k = findViewById(R.id.btnAdd1k);
        btnAdd10k = findViewById(R.id.btnAdd10k);
        btnAdd100k = findViewById(R.id.btnAdd100k);
        btnSubtract1k = findViewById(R.id.btnSubtract1k);
        btnSubtract10k = findViewById(R.id.btnSubtract10k);
        btnSubtract100k = findViewById(R.id.btnSubtract100k);
        btnCancel = findViewById(R.id.btnCancel);
        txtAmountToSend = findViewById(R.id.txtAmountToSend);
        btnType = findViewById(R.id.btnType);

        txtAdd1k = findViewById(R.id.txtAdd1k);
        txtAdd10k = findViewById(R.id.txtAdd10k);
        txtAdd100k = findViewById(R.id.txtAdd100k);
        txtSubtract1k = findViewById(R.id.txtSubtract1k);
        txtSubtract10k = findViewById(R.id.txtSubtract10k);
        txtSubtract100k = findViewById(R.id.txtSubtract100k);

        txtSubtract1k.setText(context.getString(R.string.button_subtract, TextUtilities.GetResourceQuantityString(typeToSend, 1000)));
        txtSubtract10k.setText(context.getString(R.string.button_subtract, TextUtilities.GetResourceQuantityString(typeToSend, 10000)));
        txtSubtract100k.setText(context.getString(R.string.button_subtract, TextUtilities.GetResourceQuantityString(typeToSend, 100000)));

        txtSubtract1k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 1000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
        txtSubtract10k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 10000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
        txtSubtract100k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 100000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));

        txtAmountToSend.setText(TextUtilities.GetResourceQuantityString(typeToSend, lAmountToSend));

        btnType.setText(TextUtilities.GetResourceTypeTitleString(typeToSend));

        btnType.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.select_resource_type_to_send));

                ResourceType[] resourceTypes = ResourceType.values();
                String[] resourceTypeNames = new String[resourceTypes.length];
                int checkedItem = -1;

                for (int i = 0; i < resourceTypes.length; i++)
                {
                    resourceTypeNames[i] = TextUtilities.GetResourceTypeTitleString(resourceTypes[i]);

                    if (resourceTypes[i] == typeToSend)
                    {
                        checkedItem = i;
                    }
                }

                builder.setSingleChoiceItems(resourceTypeNames, checkedItem, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        typeToSend = resourceTypes[i];
                        btnType.setText(TextUtilities.GetResourceTypeTitleString(typeToSend));

                        dialogInterface.dismiss();
                        Update();
                    }
                });

                builder.show();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                lAmountToSend = 0;
                activity.InformationMode(false);
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
                lAmountToSend += 100000;
            }
        });

        btnAdd10k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                lAmountToSend += 10000;
            }
        });

        btnAdd1k.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                lAmountToSend += 1000;
            }
        });

        btnSendWealth.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final LaunchDialog launchDialog = new LaunchDialog();
                launchDialog.SetHeaderGiveWealth();
                launchDialog.SetMessage(context.getString(R.string.confirm_give_wealth, TextUtilities.GetResourceQuantityString(typeToSend, lAmountToSend)));
                launchDialog.SetOnClickOk(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        game.GiveWealth(ID, lAmountToSend, typeToSend);
                        launchDialog.dismiss();
                        activity.InformationMode(false);
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

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                txtSubtract1k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 1000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
                txtSubtract10k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 10000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));
                txtSubtract100k.setTextColor(Utilities.ColourFromAttr(context, lAmountToSend - 100000 >= 0 ? R.attr.GoodColour : R.attr.BadColour));

                txtAdd1k.setText(context.getString(R.string.button_add, TextUtilities.GetResourceQuantityString(typeToSend, 1000)));
                txtAdd10k.setText(context.getString(R.string.button_add, TextUtilities.GetResourceQuantityString(typeToSend, 10000)));
                txtAdd100k.setText(context.getString(R.string.button_add, TextUtilities.GetResourceQuantityString(typeToSend, 100000)));
                txtSubtract1k.setText(context.getString(R.string.button_subtract, TextUtilities.GetResourceQuantityString(typeToSend, 1000)));
                txtSubtract10k.setText(context.getString(R.string.button_subtract, TextUtilities.GetResourceQuantityString(typeToSend, 10000)));
                txtSubtract100k.setText(context.getString(R.string.button_subtract, TextUtilities.GetResourceQuantityString(typeToSend, 100000)));

                txtAmountToSend.setText(TextUtilities.GetResourceQuantityString(typeToSend, lAmountToSend));
            }
        });
    }
}