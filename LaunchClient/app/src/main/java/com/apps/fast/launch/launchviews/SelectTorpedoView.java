package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchableSelectionView;

import java.util.ArrayList;
import java.util.List;

import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;
import launch.game.entities.Player;
import launch.game.entities.Ship;
import launch.game.entities.Submarine;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.systems.MissileSystem;
import launch.game.types.TorpedoType;

/**
 * Created by tobster on 16/10/15.
 */
public class SelectTorpedoView extends LaunchView
{
    private GeoCoord geoTarget;
    private LinearLayout lytExistingSites;
    private String strTargetName;
    private MapEntity target;

    private TextView txtNoTorpedoes;

    public SelectTorpedoView(LaunchClientGame game, MainActivity activity, GeoCoord geoTarget, MapEntity target, String strTargetName)
    {
        super(game, activity, true);

        this.geoTarget = geoTarget;
        this.strTargetName = strTargetName;
        this.target = target;

        Setup();
        Update();
        RebuildList();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_select_torpedo, this);

        ((TextView)findViewById(R.id.txtAttacking)).setText(context.getString(R.string.attacking, strTargetName));

        txtNoTorpedoes = (TextView)findViewById(R.id.txtNoTorpedoes);

        //Existing torpedo sites.
        lytExistingSites = (LinearLayout)findViewById(R.id.lytAvailableTorpedoes);
    }

    @Override
    public void Update()
    {
        //RebuildList();
    }

    private void RebuildList()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                lytExistingSites.removeAllViews();

                boolean bTorpedoesAvailable = false;

                Player ourPlayer = game.GetOurPlayer();

                for(Ship ship : game.GetShips())
                {
                    if(ship.GetOwnerID() == ourPlayer.GetID() && ship.HasTorpedoes() && ship.GetMissileSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(ship.GetPosition(), ship.GetMissileSystem(), false, ship.GetID(), SystemType.SHIP_TORPEDOES))
                        {
                            bTorpedoesAvailable = true;
                        }
                    }
                }

                for(Submarine submarine : game.GetSubmarines())
                {
                    if(submarine.GetOwnerID() == ourPlayer.GetID() && submarine.HasTorpedoes() && submarine.GetMissileSystem().ReadyToFire())
                    {
                        if(AddUIForSystem(submarine.GetPosition(), submarine.GetMissileSystem(), false, submarine.GetID(), SystemType.SUBMARINE_TORPEDO))
                        {
                            bTorpedoesAvailable = true;
                        }
                    }
                }

                txtNoTorpedoes.setVisibility(bTorpedoesAvailable ? GONE : VISIBLE);
            }
        });
    }

    private boolean AddUIForSystem(GeoCoord geoFrom, MissileSystem pepeepoopoo, final boolean bIsPlayer, final int lMissileSiteID, SystemType systemType)
    {
        TextView txtSiteName = new TextView(context);
        MissileSystem system = null;

        switch(systemType)
        {
            case SHIP_TORPEDOES:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetShip(lMissileSiteID)));
                system = game.GetShip(lMissileSiteID).GetTorpedoSystem();
            }
            break;

            case SUBMARINE_TORPEDO:
            {
                txtSiteName.setText(Utilities.GetEntityName(context, game.GetShip(lMissileSiteID)));
                system = game.GetSubmarine(lMissileSiteID).GetTorpedoSystem();
            }
            break;
        }

        lytExistingSites.addView(txtSiteName);

        List<Integer> SuitableTypes = new ArrayList<>();

        boolean bTorpedoesAvailable = false;

        if(system != null)
        {
            for(int c = 0; c < system.GetSlotCount(); c++)
            {
                if(system.GetSlotReadyToFire(c))
                {
                    int lType = system.GetSlotMissileType(c);

                    if(!SuitableTypes.contains(lType))
                    {
                        SuitableTypes.add(lType);

                        TorpedoType type = game.GetConfig().GetTorpedoType(system.GetSlotMissileType(c));

                        //Check it's in range.
                        if(geoFrom.DistanceTo(geoTarget) <= type.GetTorpedoRange())
                        {
                            bTorpedoesAvailable = true;
                            LaunchableSelectionView launchableSelectionView = new LaunchableSelectionView(activity, game, type, true);

                            //Count number of this type that are ready.
                            int lNumber = 0;
                            for(int lSlot = 0; lSlot < system.GetSlotCount(); lSlot++)
                            {
                                if(system.GetSlotReadyToFire(lSlot))
                                    if(system.GetSlotMissileType(lSlot) == lType)
                                        lNumber++;
                            }
                            launchableSelectionView.SetNumber(lNumber);

                            final int lSlotNo = c;

                            launchableSelectionView.setOnClickListener(new OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    activity.DesignateTorpedoTarget(lMissileSiteID, lSlotNo, geoTarget, target, systemType);
                                }
                            });

                            lytExistingSites.addView(launchableSelectionView);
                        }
                    }
                }
            }
        }

        if(!bTorpedoesAvailable)
        {
            lytExistingSites.removeView(txtSiteName);
            /*TextView textNone = new TextView(context);
            textNone.setText(context.getString(R.string.none));
            textNone.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            lytExistingSites.addView(textNone);*/
        }

        return bTorpedoesAvailable;
    }

    @Override
    public void EntityUpdated(LaunchEntity entity)
    {
        //I'm not sure if this auto-updating list feature is good. In testing it seems more like a nuisance.
        /*if(entity.GetOwnedBy(game.GetOurPlayerID()) && (entity.ApparentlyEquals(game.GetOurPlayer()) || entity instanceof MissileSite || (entity instanceof Airplane && ((Airplane)entity).CanHaveMissiles())))
        {
            RebuildList();
        }*/
    }
}
