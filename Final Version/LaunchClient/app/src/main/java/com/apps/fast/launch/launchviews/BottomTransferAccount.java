package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Player;
import launch.game.entities.Warehouse;

/**
 * Created by tobster on 14/07/16.
 */
public class BottomTransferAccount extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtSelectPlayer;
    private TextView txtTransferAccountHint;
    private LinearLayout btnTransferAccount;

    private Player fromPlayer;
    private Player toPlayer;

    private GoogleMap map;
    private Polyline targetTrajectory;

    public BottomTransferAccount(LaunchClientGame game, MainActivity activity, Player fromPlayer)
    {
        super(game, activity, true);
        this.fromPlayer = fromPlayer;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_transfer_account, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtSelectPlayer = findViewById(R.id.txtSelectPlayer);
        btnTransferAccount = findViewById(R.id.btnTransferAccount);
        txtTransferAccountHint = findViewById(R.id.txtTransferAccountHint);

        txtTransferAccountHint.setText(context.getString(R.string.hint_transfer_account, fromPlayer.GetName()));

        btnTransferAccount.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(toPlayer != null)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.confirm_transfer_account, fromPlayer.GetName(), toPlayer.GetName(), toPlayer.GetName(), fromPlayer.GetName()));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            TransferAccount();
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
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_transfer_account));
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.InformationMode(false);
            }
        });

        Update();
    }

    private void TransferAccount()
    {
        if(toPlayer != null)
        {
            activity.InformationMode(false);

            game.AdminAccountTransfer(fromPlayer.GetID(), toPlayer.GetID());
        }
    }

    public void PlayerSelected(Player toPlayer, Polyline targetTrajectory, GoogleMap map)
    {
        this.toPlayer = toPlayer;
        this.targetTrajectory = targetTrajectory;
        this.map = map;

        Update();
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(toPlayer == null)
                {
                    txtSelectPlayer.setText(context.getString(R.string.must_specify_transfer_account));
                }
                else
                {
                    GeoCoord geoTo = toPlayer.GetPosition();
                    List<LatLng> points = new ArrayList<LatLng>();

                    GeoCoord geoFrom = toPlayer.GetPosition();

                    points.add(Utilities.GetLatLng(geoFrom));
                    points.add(Utilities.GetLatLng(geoTo));
                    targetTrajectory.setPoints(points);

                    txtSelectPlayer.setText(context.getString(R.string.info_transfer_account, fromPlayer.GetName(), toPlayer.GetName(), toPlayer.GetName(), fromPlayer.GetName()));
                }
            }
        });
    }
}
