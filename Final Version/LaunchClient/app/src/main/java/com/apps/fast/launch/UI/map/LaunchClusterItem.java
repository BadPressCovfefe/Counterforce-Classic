package com.apps.fast.launch.UI.map;

import androidx.annotation.Nullable;

import com.apps.fast.launch.components.Utilities;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import launch.game.entities.LaunchEntity;
import launch.game.entities.MapEntity;

public class LaunchClusterItem implements ClusterItem
{
    private LatLng latLng;
    private int lID;
    private MapEntity entity;

    public LaunchClusterItem(MapEntity entity)
    {
        lID = entity.GetID();
        latLng = Utilities.GetLatLng(entity.GetPosition());
        this.entity = entity;
    }

    public int GetID() { return lID; }

    public MapEntity GetEntity() { return entity; }

    @Override
    public LatLng getPosition()
    {
        return latLng;
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public String getSnippet()
    {
        return null;
    }
}
