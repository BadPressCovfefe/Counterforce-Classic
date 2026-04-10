package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.entities.AirplaneView;
import com.apps.fast.launch.views.LaunchDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.HaulerInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.LauncherInterface;
import launch.game.entities.MapEntity;
import launch.game.entities.ResourceInterface;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.systems.CargoSystem.LootType;
import launch.game.entities.Loot;

public class BottomLoadLoot extends LaunchView
{
    private LinearLayout btnCancel;
    private TextView txtSelectLoot;
    private TextView txtLoadLootHint;
    private LinearLayout btnLoadLoot;
    private LaunchEntity lootReceiver;
    private Loot loot;
    private boolean bLoadAsCargo = true;
    private Polyline targetTrajectory;
    private float fltLoadDistance;

    public BottomLoadLoot(LaunchClientGame game, MainActivity activity, EntityPointer pointerReceiver)
    {
        super(game, activity, true);
        this.lootReceiver = pointerReceiver.GetEntity(game);

        fltLoadDistance = Defs.LOAD_DISTANCE;

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.bottom_load_loot, this);

        btnCancel = findViewById(R.id.btnCancel);
        txtSelectLoot = findViewById(R.id.txtSelectLoot);
        txtLoadLootHint = findViewById(R.id.txtLoadLootHint);
        btnLoadLoot = findViewById(R.id.btnLoadLoot);

        txtLoadLootHint.setText(context.getString(R.string.hint_load_loot, TextUtilities.GetEntityTypeAndName(lootReceiver, game)));

        btnLoadLoot.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(loot != null)
                {
                    if(loot.GetPosition().DistanceTo(game.GetPosition(lootReceiver)) > fltLoadDistance)
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.loot_out_of_range_info));
                    }
                    else if(!game.LoadHaulableValid(loot, lootReceiver, null))
                    {
                        activity.ShowBasicOKDialog(context.getString(R.string.loot_invalid_info));
                    }
                    else if(loot.GetLootType() == LootType.INTERCEPTORS || loot.GetLootType() == LootType.MISSILES && lootReceiver instanceof LauncherInterface && lootReceiver instanceof HaulerInterface)
                    {
                        //The player is transferring launchables and the receiver has cargo and a missile system. Give the player the option of which one they want to load it into.
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderSAMControl();
                        launchDialog.SetMessage(context.getString(R.string.cargo_transfer_option_launchables));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                bLoadAsCargo = true;
                                LoadLoot();
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                bLoadAsCargo = false;
                                LoadLoot();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                    else if(loot.GetLootType() == LootType.RESOURCES && (lootReceiver instanceof ResourceInterface && ((ResourceInterface)lootReceiver).GetResourceSystem().CanHoldType(Resource.ResourceType.values()[loot.GetCargoID()])) && lootReceiver instanceof HaulerInterface)
                    {
                        //The player is transferring launchables and the receiver has cargo and a missile system. Give the player the option of which one they want to load it into.
                        final LaunchDialog launchDialog = new LaunchDialog();
                        launchDialog.SetHeaderSAMControl();
                        launchDialog.SetMessage(context.getString(R.string.cargo_transfer_option_resources));
                        launchDialog.SetOnClickYes(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                bLoadAsCargo = true;
                                LoadLoot();
                            }
                        });
                        launchDialog.SetOnClickNo(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                launchDialog.dismiss();
                                bLoadAsCargo = false;
                                LoadLoot();
                            }
                        });
                        launchDialog.show(activity.getFragmentManager(), "");
                    }
                    else
                    {
                        LoadLoot();
                    }
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.must_specify_loot));
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

    private void LoadLoot()
    {
        if(loot != null)
        {
            game.TransferCargo(loot.GetPointer(), lootReceiver.GetPointer(), null, 0, 0, bLoadAsCargo, true);
        }

        activity.ResetInteractionMode();
        activity.RemoveTargettingMapUI();

        if(lootReceiver instanceof MapEntity)
        {
            activity.SelectEntity((MapEntity)lootReceiver);
        }
        else if(lootReceiver instanceof StoredAirplane)
        {
            MainNormalView mainView = new MainNormalView(game, activity);
            activity.SetView(mainView);
            mainView.BottomLayoutShowView(new AirplaneView(game, activity, (AirplaneInterface)lootReceiver, true));
        }
    }

    public void EntitySelected(MapEntity entity, Polyline targetTrajectory, GoogleMap map)
    {
        this.targetTrajectory = targetTrajectory;

        if(entity instanceof Loot)
        {
            loot = (Loot)entity;
        }

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
                if(loot == null)
                {
                    txtLoadLootHint.setVisibility(VISIBLE);
                }
                else
                {
                    txtLoadLootHint.setVisibility(GONE);

                    if(game.GetPosition(lootReceiver).DistanceTo(loot.GetPosition()) > fltLoadDistance)
                    {
                        txtSelectLoot.setText(context.getString(R.string.loot_out_of_range));
                        txtSelectLoot.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    }
                    else if(!game.LoadHaulableValid(loot, lootReceiver, null))
                    {
                        txtSelectLoot.setText(context.getString(R.string.selected_loot_invalid, TextUtilities.GetLootContentString(loot, game)));
                        txtSelectLoot.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    }
                    else
                    {
                        txtSelectLoot.setText(context.getString(R.string.selected_loot, TextUtilities.GetLootContentString(loot, game)));
                        txtSelectLoot.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }

                    GeoCoord geoLoot = loot.GetPosition();
                    List<LatLng> points = new ArrayList<LatLng>();

                    GeoCoord geoFrom = game.GetPosition(lootReceiver);

                    points.add(Utilities.GetLatLng(geoFrom));
                    points.add(Utilities.GetLatLng(geoLoot));
                    targetTrajectory.setPoints(points);
                }
            }
        });
    }
}
