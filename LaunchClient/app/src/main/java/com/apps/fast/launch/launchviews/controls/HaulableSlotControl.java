package com.apps.fast.launch.launchviews.controls;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Haulable;
import launch.game.entities.Tank;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.entities.conceptuals.StoredInfantry;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.systems.CargoSystem;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 19/10/16.
 */
public class HaulableSlotControl extends LaunchView
{
    private TextView txtType;
    private TextView txtQuantity;
    private ImageView imgHaulable;

    private Haulable haulable;

    public HaulableSlotControl(LaunchClientGame game, MainActivity activity, Haulable haulable)
    {
        super(game, activity, true);
        this.haulable = haulable;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_haulable_slot, this);

        txtType = (TextView)findViewById(R.id.txtType);
        txtQuantity = (TextView)findViewById(R.id.txtQuantity);
        imgHaulable = (ImageView)findViewById(R.id.imgHaulable);

        switch(haulable.GetLootType())
        {
            case RESOURCES:
            {
                Resource resource = new Resource(ResourceType.values()[haulable.GetCargoID()], haulable.GetQuantity());
                txtType.setText(TextUtilities.GetResourceTypeTitleString(resource.GetResourceType()));
                imgHaulable.setImageBitmap(EntityIconBitmaps.GetResourceTypeBitmap(context, resource.GetResourceType()));

                txtQuantity.setText(TextUtilities.GetResourceQuantityString(resource.GetResourceType(), resource.GetQuantity()));
            }
            break;

            case MISSILES:
            {
                MissileType type = game.GetConfig().GetMissileType(haulable.GetCargoID());
                txtType.setText(type.GetName());
                txtQuantity.setText(String.valueOf(haulable.GetQuantity()));
                imgHaulable.setImageResource(R.drawable.button_missile);
            }
            break;

            case INTERCEPTORS:
            {
                InterceptorType type = game.GetConfig().GetInterceptorType(haulable.GetCargoID());
                txtType.setText(type.GetName());
                txtQuantity.setText(String.valueOf(haulable.GetQuantity()));
                imgHaulable.setImageResource(R.drawable.button_interceptor);
            }
            break;

            case STORED_CARGO_TRUCK:
            {
                StoredCargoTruck truck = (StoredCargoTruck)haulable;

                txtType.setText(truck.GetName().isEmpty() ? context.getString(R.string.cargo_truck_title) : truck.GetName());
                txtQuantity.setVisibility(GONE);
                imgHaulable.setImageResource(R.drawable.marker_truck);
            }
            break;

            case STORED_INFANTRY:
            {
                StoredInfantry infantry = (StoredInfantry)haulable;
                txtType.setText(infantry.GetName().isEmpty() ? context.getString(R.string.title_infantry) : infantry.GetName());
                txtQuantity.setVisibility(GONE);
                imgHaulable.setImageResource(R.drawable.marker_infantry);
            }
            break;

            case STORED_TANK:
            {
                StoredTank tank = ((StoredTank)haulable);
                txtQuantity.setVisibility(GONE);

                switch(tank.GetType())
                {
                    case MISSILE_TANK:
                    {
                        txtType.setText(tank.GetName().isEmpty() ? context.getString(R.string.title_missile_tank) : tank.GetName());
                        imgHaulable.setImageResource(R.drawable.marker_missile_tank);
                    }
                    break;

                    case SAM_TANK:
                    {
                        txtType.setText(tank.GetName().isEmpty() ? context.getString(R.string.title_sam_tank) : tank.GetName());
                        imgHaulable.setImageResource(R.drawable.marker_sam_tank);
                    }
                    break;

                    case HOWITZER:
                    {
                        txtType.setText(tank.GetName().isEmpty() ? context.getString(R.string.title_howitzer) : tank.GetName());
                        imgHaulable.setImageResource(R.drawable.marker_howitzer);
                    }
                    break;

                    case MBT:
                    {
                        txtType.setText(tank.GetName().isEmpty() ? context.getString(R.string.title_mbt) : tank.GetName());
                        imgHaulable.setImageResource(R.drawable.marker_tank_east);
                    }
                    break;

                    case SPAAG:
                    {
                        txtType.setText(tank.GetName().isEmpty() ? context.getString(R.string.title_spaag) : tank.GetName());
                        imgHaulable.setImageResource(R.drawable.marker_aa_gun_east);
                    }
                    break;
                }
            }
            break;
        }

        Update();
    }

    @Override
    public void Update()
    {
        switch(haulable.GetLootType())
        {
            case RESOURCES:
            {
                Resource resource = new Resource(ResourceType.values()[haulable.GetCargoID()], haulable.GetQuantity());

                txtQuantity.setText(TextUtilities.GetResourceQuantityString(resource.GetResourceType(), resource.GetQuantity()));
            }
            break;

            case MISSILES:
            case INTERCEPTORS:
            {
                txtQuantity.setText(String.valueOf(haulable.GetQuantity()));
            }
            break;
        }
    }
}
