package com.apps.fast.launch.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.apps.fast.launch.R;

import launch.game.LaunchClientGame;
import launch.game.entities.Bunker;
import launch.game.entities.MissileSite;
import launch.game.entities.OreMine;
import launch.game.entities.RadarStation;
import launch.game.entities.SAMSite;
import launch.game.entities.SentryGun;
import launch.game.entities.SolarPanel;
import launch.game.entities.Structure;

import launch.game.LaunchGame.Allegiance;

/**
 * A utility class that shades structure bitmaps by allegiance, combines the power status, and caches the bitmaps in RAM.
 */
public class StructureIconBitmaps
{
    private enum StructureIndexRunStatus
    {
        ONLINE,
        OFFLINE,
        BOOTING,
        SELLING
    }

    private static final Bitmap[] MissileSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] NuclearMissileSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] SAMSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] SentryGunBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] OreMineBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] RadarStationBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];
    private static final Bitmap[] ABMSiteBitmaps = new Bitmap[Allegiance.values().length * StructureIndexRunStatus.values().length];

    private static void GenerateStructureBitmap(Context context, Bitmap[] Container, int lIndex, Allegiance allegiance, StructureIndexRunStatus runStatus, int lRes)
    {
        Bitmap icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), lRes), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        Bitmap runOverlay = null;

        switch(runStatus)
        {
            case SELLING: runOverlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_selling); break;
            case OFFLINE: runOverlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_offline); break;
            case BOOTING: runOverlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_booting); break;
            case ONLINE: runOverlay = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_online); break;
        }

        Bitmap bitmap = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), icon.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(icon, new Matrix(), null);
        canvas.drawBitmap(runOverlay, 0, 0, null);

        Container[lIndex] = bitmap;
    }

    public static Bitmap GetStructureBitmap(Context context, LaunchClientGame game, Structure structure, int mipMapLevel)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), structure);
        Bitmap icon = null;

        if(structure instanceof MissileSite)
        {
            MissileSite missileSite = ((MissileSite)structure);

            if(missileSite.CanTakeICBM())
            {
                if(missileSite.GetOffline())
                {
                    if(mipMapLevel == 1)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 2)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_offline_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 3)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_offline_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 4)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_offline), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                }
                else if(missileSite.GetBooting())
                {
                    if(mipMapLevel == 1)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 2)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_online_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 3)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_online_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 4)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_booting), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                }
                else
                {
                    if(mipMapLevel == 1)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 2)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_online_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 3)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke_online_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 4)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesitenuke), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                }
            }
            else
            {
                if(mipMapLevel == 1)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                else if(mipMapLevel == 2)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesite_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                else if(mipMapLevel == 3)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesite_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                else if(mipMapLevel == 4)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_missilesite), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            }
            //Clustered missile site markers.
            //CreateClusterUI(entity, MissileSiteMarkers, ClusterManagerMissileSites);
        }
        else if(structure instanceof SAMSite)
        {
            SAMSite samSite = ((SAMSite)structure);
            if(samSite.GetIsABMSilo())
            {
                if(samSite.GetOffline())
                {
                    if(mipMapLevel == 1)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 2)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_offline_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 3)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_offline_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 4)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_offline), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                }
                else
                {
                    if(mipMapLevel == 1)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 2)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_online_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 3)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_online_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                    else if(mipMapLevel == 4)
                        icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_abmsite_online), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                }
            }
            else
            {
                if(mipMapLevel == 1)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                else if(mipMapLevel == 2)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_samsite_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                else if(mipMapLevel == 3)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_samsite_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
                else if(mipMapLevel == 4)
                    icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_samsite), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            }
            //Clustered SAM site markers.
            //CreateClusterUI(entity, SAMSiteMarkers, ClusterManagerSAMSites);
        }
        else if(structure instanceof SentryGun)
        {
            if(mipMapLevel == 1)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 2)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sentry_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 3)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sentry_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 4)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_sentry), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            //Clustered sentry gun markers.
            //CreateClusterUI(entity, SentryGunMarkers, ClusterManagerSentryGuns);
        }
        else if(structure instanceof OreMine)
        {
            if(mipMapLevel == 1)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 2)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_oremine_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 3)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_oremine_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 4)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_oremine), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            //Clustered ore mine markers.
            //CreateClusterUI(entity, OreMineMarkers, ClusterManagerOreMines);
        }
        else if(structure instanceof RadarStation)
        {
            if(mipMapLevel == 1)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 2)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_radarstation_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 3)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_radarstation_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 4)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_radarstation), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            //Clustered radar station markers.
            //CreateClusterUI(entity, RadarStationMarkers, ClusterManagerRadarStations);
        }
        else if(structure instanceof SolarPanel)
        {
            if(mipMapLevel == 1)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 2)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_solarpanel_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 3)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_solarpanel_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 4)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_solarpanel), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        }
        else if(structure instanceof Bunker)
        {
            /*if(mipMapLevel == 1)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_smallest_mipmap), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 2)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_solarpanel_16), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 3)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_solarpanel_32), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            else if(mipMapLevel == 4)
                icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_solarpanel), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);*/

            icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_bunker), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
        }

        Bitmap runBackdrop = null;

        if(mipMapLevel == 4)
        {
            if(structure.GetSelling())
            {
                runBackdrop = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_selling);
            }
            else if(structure.GetOffline())
            {
                runBackdrop = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_offline);
            }
            else if(structure.GetBooting())
            {
                runBackdrop = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_booting);
            }
            else if(structure.GetOnline())
            {
                runBackdrop = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_online);
            }
        }

        Bitmap bitMap = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), icon.getConfig());
        Canvas canvas = new Canvas(bitMap);
        canvas.drawBitmap(icon, new Matrix(), null);

        if(runBackdrop != null)
            canvas.drawBitmap(runBackdrop, 0, 0, null);

        return bitMap;

        /*StructureIndexRunStatus runStatus;

        if(structure.GetSelling())
            runStatus = StructureIndexRunStatus.SELLING;
        else if(structure.GetOffline())
            runStatus = StructureIndexRunStatus.OFFLINE;
        else if(structure.GetBooting())
            runStatus = StructureIndexRunStatus.BOOTING;
        else
            runStatus = StructureIndexRunStatus.ONLINE;

        int lIndex = (allegiance.ordinal() * StructureIndexRunStatus.values().length) + runStatus.ordinal();

        if(structure instanceof MissileSite)
        {
            if(((MissileSite)structure).CanTakeICBM() && structure.GetOffline())
            {
                if (NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesitenuke_offline);
                }

                return NuclearMissileSiteBitmaps[lIndex];
            }
            else if(((MissileSite)structure).CanTakeICBM() && structure.GetBooting())
            {
                if (NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesitenuke_booting);
                }

                return NuclearMissileSiteBitmaps[lIndex];
            }
            else if(((MissileSite)structure).CanTakeICBM())
            {
                if (NuclearMissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, NuclearMissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesitenuke);
                }

                return NuclearMissileSiteBitmaps[lIndex];
            }
            else
            {
                if (MissileSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, MissileSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_missilesite);
                }

                return MissileSiteBitmaps[lIndex];
            }
        }

        if(structure instanceof SAMSite)
        {
            if(((SAMSite)structure).GetIsABMSilo() && structure.GetOffline())
            {
                if (ABMSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, ABMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_abmsite_offline);
                }

                return ABMSiteBitmaps[lIndex];
            }
            else if(((SAMSite)structure).GetIsABMSilo())
            {
                if (ABMSiteBitmaps[lIndex] == null) {
                    GenerateStructureBitmap(context, ABMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_abmsite_online);
                }

                return ABMSiteBitmaps[lIndex];
            }
            else
            {
                if (SAMSiteBitmaps[lIndex] == null)
                {
                    GenerateStructureBitmap(context, SAMSiteBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_samsite);
                }

                return SAMSiteBitmaps[lIndex];
            }
        }

        if(structure instanceof SentryGun)
        {
            if(SentryGunBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, SentryGunBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_sentry);
            }

            return SentryGunBitmaps[lIndex];
        }

        if(structure instanceof OreMine)
        {
            if(OreMineBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, OreMineBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_oremine);
            }

            return OreMineBitmaps[lIndex];
        }

        if(structure instanceof RadarStation)
        {
            if(RadarStationBitmaps[lIndex] == null)
            {
                GenerateStructureBitmap(context, RadarStationBitmaps, lIndex, allegiance, runStatus, R.drawable.marker_radarstation);
            }

            return RadarStationBitmaps[lIndex];
        }*/
    }
}
