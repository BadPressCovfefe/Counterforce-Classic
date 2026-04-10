package com.apps.fast.launch.launchviews;

import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.UI.LandUnitIconBitmaps;
import com.apps.fast.launch.UI.LaunchUICommon;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import launch.game.EntityPointer;
import launch.game.LaunchClientGame;
import launch.game.entities.CargoTruck;
import launch.game.entities.CargoTruckInterface;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Movable;

public class CargoTruckMaintenanceView extends LaunchView implements LaunchUICommon.CargoTruckInfoProvider
{
    private CargoTruckInterface cargotruckShadow = null;
    private Collection CargoTruckList = null;
    private TextView txtEmptySlots;
    private LinearLayout btnMove;
    private LinearLayout btnCeaseFire;

    /**
     * Initialise for a single structure.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param cargotruck The structure.
     */
    public CargoTruckMaintenanceView(LaunchClientGame game, MainActivity activity, CargoTruckInterface cargotruck)
    {
        super(game, activity, true);
        this.cargotruckShadow = cargotruck;
        Setup();
    }

    /**
     * Initialise for a list of structures which MUST ALL BE THE SAME TYPE.
     * @param game Reference to the game.
     * @param activity Reference to the main activity.
     * @param cargotrucks List of structures WHICH MUST ALL BE THE SAME TYPE.
     */
    public CargoTruckMaintenanceView(LaunchClientGame game, MainActivity activity, Collection cargotrucks)
    {
        super(game, activity, true);
        this.CargoTruckList = cargotrucks;
        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_cargo_truck_maintenance, this);

        TextView txtCount = findViewById(R.id.txtCount);
        ImageView imgCargoTruck = findViewById(R.id.imgCargoTruck);
        TextView txtType = findViewById(R.id.txtTitle);
        txtEmptySlots = findViewById(R.id.txtEmptySlots);
        btnMove = findViewById(R.id.btnMove);
        btnCeaseFire = findViewById(R.id.btnCeaseFire);

        if(cargotruckShadow == null && CargoTruckList.size() == 1)
        {
            cargotruckShadow = (CargoTruckInterface)CargoTruckList.toArray()[0];
            CargoTruckList.clear();
        }

        CargoTruckInterface iconControlCargoTruck = cargotruckShadow == null ? (CargoTruckInterface)CargoTruckList.iterator().next() : cargotruckShadow;

        imgCargoTruck.setImageBitmap(LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_truck), LaunchUICommon.AllegianceColours[game.GetAllegiance(game.GetOurPlayer(), (LaunchEntity)iconControlCargoTruck).ordinal()]));

        if(cargotruckShadow != null)
        {
            txtCount.setVisibility(GONE);
            txtType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)iconControlCargoTruck, game));
        }

        if(CargoTruckList != null)
        {
            //Group of structures. Bin the name and state labels and set the count.
            txtCount.setText(Integer.toString(CargoTruckList.size()));
            txtEmptySlots.setVisibility(GONE);
            txtType.setText(TextUtilities.GetEntityTypeAndName((LaunchEntity)iconControlCargoTruck, game));
        }

        btnMove.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(cargotruckShadow != null)
                    activity.MoveOrderMode(((LaunchEntity)cargotruckShadow).GetPointer(), null);
                else if(CargoTruckList != null)
                {
                    List<EntityPointer> MovablePointers = new ArrayList<>();

                    for(Object objCargoTruck : CargoTruckList.toArray())
                    {
                        CargoTruckInterface cargotruck = (CargoTruckInterface)objCargoTruck;
                        MovablePointers.add(((LaunchEntity)cargotruck).GetPointer());
                    }

                    activity.MoveOrderMode(null, MovablePointers);
                }
            }
        });

        boolean bShowCeaseFire = false;

        if(cargotruckShadow != null)
        {
            if(cargotruckShadow instanceof CargoTruck)
            {
                CargoTruck cargoTruck = (CargoTruck)cargotruckShadow;

                if(cargoTruck.GetMoveOrders() != Movable.MoveOrders.WAIT && cargoTruck.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;
            }
        }
        else if(CargoTruckList != null)
        {
            for(Object objCargoTruck : CargoTruckList)
            {
                if(objCargoTruck instanceof CargoTruck)
                {
                    CargoTruck cargoTruck = (CargoTruck)objCargoTruck;

                    if(cargoTruck.GetMoveOrders() != Movable.MoveOrders.WAIT && cargoTruck.GetMoveOrders() != Movable.MoveOrders.DEFEND)
                        bShowCeaseFire = true;
                }
            }
        }

        if(bShowCeaseFire)
        {
            btnCeaseFire.setVisibility(VISIBLE);

            btnCeaseFire.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.cease_fire_confirm));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();

                            if(cargotruckShadow != null)
                            {
                                game.CeaseFire(Collections.singletonList(((LaunchEntity)cargotruckShadow).GetPointer()));
                            }
                            else if(CargoTruckList != null)
                            {
                                List<EntityPointer> SitePointers = new ArrayList<>();

                                for(Object objEntity : CargoTruckList.toArray())
                                {
                                    LaunchEntity entity = (LaunchEntity)objEntity;
                                    SitePointers.add(entity.GetPointer());
                                }

                                game.CeaseFire(SitePointers);
                            }
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

        Update();
    }

    @Override
    public void Update()
    {
        boolean bShowCeaseFire = false;

        if(cargotruckShadow != null)
        {
            CargoTruckInterface cargotruck = GetCurrentCargoTruck();

            if(cargotruck instanceof CargoTruck)
            {
                if(((CargoTruck)cargotruck).GetMoveOrders() != Movable.MoveOrders.WAIT && ((CargoTruck)cargotruck).GetMoveOrders() != Movable.MoveOrders.DEFEND)
                    bShowCeaseFire = true;
            }
        }
        else if(CargoTruckList != null)
        {
            for(Object objCargoTruck : CargoTruckList)
            {
                if(objCargoTruck instanceof CargoTruck)
                {
                    if(((CargoTruck)objCargoTruck).GetMoveOrders() != Movable.MoveOrders.WAIT && ((CargoTruck)objCargoTruck).GetMoveOrders() != Movable.MoveOrders.DEFEND)
                        bShowCeaseFire = true;
                }
            }

            txtEmptySlots.setVisibility(GONE);
        }

        if(bShowCeaseFire)
        {
            btnCeaseFire.setVisibility(VISIBLE);
        }
        else
        {
            btnCeaseFire.setVisibility(GONE);
        }
    }

    @Override
    public boolean IsSingleCargoTruck()
    {
        return cargotruckShadow != null;
    }

    @Override
    public CargoTruckInterface GetCurrentCargoTruck()
    {
        return (CargoTruckInterface)(((LaunchEntity)cargotruckShadow).GetPointer().GetEntity(game));
    }

    @Override
    public List<CargoTruckInterface> GetCurrentCargoTrucks()
    {
        CargoTruckInterface controlCargoTruck = (CargoTruckInterface)CargoTruckList.iterator().next();
        List<CargoTruckInterface> CurrentCargoTrucks = new ArrayList<>();

        for(Object object : CargoTruckList)
        {
            CargoTruckInterface cargotruck = (CargoTruckInterface)object;
            CurrentCargoTrucks.add(cargotruck);
        }

        return CurrentCargoTrucks;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        boolean bUpdate = false;

        if(cargotruckShadow != null)
        {
            if(entity.ApparentlyEquals((LaunchEntity)cargotruckShadow))
                bUpdate = true;
        }

        if(CargoTruckList != null)
        {
            for(Object object : CargoTruckList)
            {
                CargoTruckInterface cargotruck = (CargoTruckInterface)object;

                if(entity.ApparentlyEquals((LaunchEntity)cargotruck))
                {
                    bUpdate = true;
                    break;
                }
            }
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
