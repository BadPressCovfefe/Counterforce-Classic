package com.apps.fast.launch.launchviews;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.UI.StructureIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.Airplane;
import launch.game.entities.AirplaneInterface;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.ArtilleryInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.Ship;
import launch.game.entities.Tank;
import launch.game.entities.conceptuals.FireOrder;
import launch.game.entities.conceptuals.StoredAirplane;
import launch.game.systems.MissileSystem;

public class ArtilleryQuickSelectView extends LaunchView
{
    private TextView txtArtilleryType;
    private ImageView imgArtilleryType;
    private LinearLayout lytMissiles;
    private TextView txtMissiles;
    private TextView txtArtilleryStatus;
    private MissileSystem missileSystem;
    private TextView txtFlightTime;
    private GeoCoord geoTarget;
    private ArtilleryInterface artillery;
    private TextView txtName;
    private TextView txtHP;

    public ArtilleryQuickSelectView(LaunchClientGame game, MainActivity activity, ArtilleryInterface artillery, GeoCoord geoTarget)
    {
        super(game, activity, true);
        this.artillery = artillery;
        this.geoTarget = geoTarget;

        if(artillery instanceof ArtilleryGun)
        {
            missileSystem = ((ArtilleryGun)artillery).GetMissileSystem();
        }
        else if(artillery instanceof Ship)
        {
            missileSystem = ((Ship)artillery).GetArtillerySystem();
        }
        else if(artillery instanceof Tank)
        {
            missileSystem = ((Tank)artillery).GetMissileSystem();
        }

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_artillery_quickselect, this);

        imgArtilleryType = findViewById(R.id.imgAircraftType);
        txtArtilleryType = findViewById(R.id.txtAircraftType);
        lytMissiles = findViewById(R.id.lytArsenal);
        txtMissiles = findViewById(R.id.txtArmaments);
        txtArtilleryStatus = findViewById(R.id.txtInfantryStatus);
        txtFlightTime = findViewById(R.id.txtTravelTime);
        txtName = findViewById(R.id.txtName);
        txtHP = findViewById(R.id.txtHPTitle);
        txtArtilleryStatus = findViewById(R.id.txtInfantryStatus);
        txtFlightTime.setText(context.getString(R.string.missile_to_target, TextUtilities.GetTimeAmount(game.GetTimeToTarget(((MapEntity)artillery).GetPosition(), geoTarget, Defs.ARTILLERY_SHELL_SPEED))));

        if(artillery instanceof ArtilleryGun)
        {
            ArtilleryGun gun = (ArtilleryGun)artillery;

            txtName.setText(!gun.GetName().isEmpty() ? gun.GetName() : context.getString(R.string.unnamed));
            imgArtilleryType.setImageBitmap(StructureIconBitmaps.GetStructureBitmap(context, game, gun));
            txtArtilleryType.setText(TextUtilities.GetEntityTypeAndName(gun, game));
            TextUtilities.AssignHealthStringAndAppearance(txtHP, gun);

            if(gun.HasFireOrder())
            {
                FireOrder order = gun.GetFireOrder();

                if(geoTarget.DistanceTo(order.GetGeoTarget()) <= order.GetRadius())
                {
                    txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtArtilleryStatus.setText(context.getString(R.string.status_already_shelling));
                }
                else
                {
                    txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtArtilleryStatus.setText(context.getString(R.string.status_shelling));
                }
            }
            else
            {
                TextUtilities.SetStructureState(txtArtilleryStatus, gun);
            }
        }
        else if(artillery instanceof Ship)
        {
            Ship ship = (Ship)artillery;

            txtName.setText(!ship.GetName().isEmpty() ? ship.GetName() : context.getString(R.string.unnamed));
            imgArtilleryType.setImageBitmap(EntityIconBitmaps.GetOwnedNavalBitmap(context, game, ship));
            txtArtilleryType.setText(TextUtilities.GetEntityTypeAndName(ship, game));
            TextUtilities.AssignHealthStringAndAppearance(txtHP, ship);

            if(ship.HasFireOrder())
            {
                FireOrder order = ship.GetFireOrder();

                if(geoTarget.DistanceTo(order.GetGeoTarget()) <= order.GetRadius())
                {
                    txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtArtilleryStatus.setText(context.getString(R.string.status_already_shelling));
                }
                else 
                {
                    txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtArtilleryStatus.setText(context.getString(R.string.status_shelling));
                }
            }
            else 
            {
                txtArtilleryStatus.setText(context.getString(R.string.status_ready));
                txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
        else if(artillery instanceof Tank)
        {
            Tank tank = (Tank)artillery;

            txtName.setText(!tank.GetName().isEmpty() ? tank.GetName() : context.getString(R.string.unnamed));
            imgArtilleryType.setImageBitmap(LandUnitIconBitmaps.GetLandUnitBitmap(context, game, tank));
            txtArtilleryType.setText(TextUtilities.GetEntityTypeAndName(tank, game));
            TextUtilities.AssignHealthStringAndAppearance(txtHP, tank);

            if(tank.HasFireOrder())
            {
                FireOrder order = tank.GetFireOrder();

                if(geoTarget.DistanceTo(order.GetGeoTarget()) <= order.GetRadius())
                {
                    txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtArtilleryStatus.setText(context.getString(R.string.status_already_shelling));
                }
                else
                {
                    txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    txtArtilleryStatus.setText(context.getString(R.string.status_shelling));
                }
            }
            else
            {
                txtArtilleryStatus.setText(context.getString(R.string.status_ready));
                txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }

        /*For setting up the missile/interceptor count readout.*/
        if(missileSystem != null)
        {
            txtMissiles.setText(context.getString(R.string.aircraft_armaments, missileSystem.GetOccupiedSlotCount(), missileSystem.GetSlotCount()));

            int lowMissileThreshold = missileSystem.GetSlotCount()/3;

            if(missileSystem.GetOccupiedSlotCount() == missileSystem.GetSlotCount())
            {
                txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(missileSystem.GetOccupiedSlotCount() < lowMissileThreshold)
            {
                txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
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
                if(artillery instanceof ArtilleryGun)
                {
                    ArtilleryGun gun = game.GetArtilleryGun(((ArtilleryGun) artillery).GetID());

                    txtName.setText(!gun.GetName().isEmpty() ? gun.GetName() : context.getString(R.string.unnamed));
                    imgArtilleryType.setImageBitmap(StructureIconBitmaps.GetStructureBitmap(context, game, gun));
                    txtArtilleryType.setText(TextUtilities.GetEntityTypeAndName(gun, game));
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, gun);

                    if(gun.HasFireOrder())
                    {
                        FireOrder order = gun.GetFireOrder();

                        if(geoTarget.DistanceTo(order.GetGeoTarget()) <= order.GetRadius())
                        {
                            txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            txtArtilleryStatus.setText(context.getString(R.string.status_already_shelling));
                        }
                        else
                        {
                            txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            txtArtilleryStatus.setText(context.getString(R.string.status_shelling));
                        }
                    }
                    else
                    {
                        TextUtilities.SetStructureState(txtArtilleryStatus, gun);
                    }
                }
                else if(artillery instanceof Ship)
                {
                    Ship ship = game.GetShip(((Ship)artillery).GetID());
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, ship);

                    if(ship.HasFireOrder())
                    {
                        FireOrder order = ship.GetFireOrder();

                        if(geoTarget.DistanceTo(order.GetGeoTarget()) <= order.GetRadius())
                        {
                            txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            txtArtilleryStatus.setText(context.getString(R.string.status_already_shelling));
                        }
                        else
                        {
                            txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            txtArtilleryStatus.setText(context.getString(R.string.status_shelling));
                        }
                    }
                    else
                    {
                        txtArtilleryStatus.setText(context.getString(R.string.status_ready));
                        txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                }
                else if(artillery instanceof Tank)
                {
                    Tank tank = game.GetTank(((Tank)artillery).GetID());
                    TextUtilities.AssignHealthStringAndAppearance(txtHP, tank);

                    if(tank.HasFireOrder())
                    {
                        FireOrder order = tank.GetFireOrder();

                        if(geoTarget.DistanceTo(order.GetGeoTarget()) <= order.GetRadius())
                        {
                            txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            txtArtilleryStatus.setText(context.getString(R.string.status_already_shelling));
                        }
                        else
                        {
                            txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                            txtArtilleryStatus.setText(context.getString(R.string.status_shelling));
                        }
                    }
                    else
                    {
                        txtArtilleryStatus.setText(context.getString(R.string.status_ready));
                        txtArtilleryStatus.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                }

                /*For setting up the missile/interceptor count readout.*/
                if(missileSystem != null)
                {
                    txtMissiles.setText(context.getString(R.string.aircraft_armaments, missileSystem.GetOccupiedSlotCount(), missileSystem.GetSlotCount()));

                    int lowMissileThreshold = missileSystem.GetSlotCount()/3;

                    if(missileSystem.GetOccupiedSlotCount() == missileSystem.GetSlotCount())
                    {
                        txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(missileSystem.GetOccupiedSlotCount() < lowMissileThreshold)
                    {
                        txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                    }
                    else
                    {
                        txtMissiles.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                }
            }
        });
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(artillery != null)
        {
            if(entity == artillery)
                bUpdate = true;
        }

        if(bUpdate)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Update();
                }
            });
        }
    }
}
