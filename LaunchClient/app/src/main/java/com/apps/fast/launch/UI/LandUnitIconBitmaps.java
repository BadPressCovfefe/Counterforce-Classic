package com.apps.fast.launch.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.apps.fast.launch.R;
import com.apps.fast.launch.components.Utilities;

import launch.game.LaunchClientGame;
import launch.game.LaunchGame.Allegiance;
import launch.game.entities.CargoTruck;
import launch.game.entities.Infantry;
import launch.game.entities.LandUnit;
import launch.game.entities.Movable;
import launch.game.entities.Tank;

/**
 * A utility class that shades structure bitmaps by allegiance, combines the power status, and caches the bitmaps in RAM.
 */
public class LandUnitIconBitmaps
{
    private static final Bitmap[] InfantryBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestInfantryBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] RunningInfantryBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] RunningWestInfantryBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] MBTBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] SPAAGBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] MissileTankBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] SAMTankBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestMissileTankBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestSAMTankBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] HowitzerBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestHowitzerBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestMBTBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestSPAAGBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestCaravanBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] CaravanBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestCargoTruckBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] CargoTruckBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] MiningTruckBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];
    private static final Bitmap[] WestMiningTruckBitmaps = new Bitmap[Allegiance.values().length/* * LandUnitMoveStatus.values().length*/];

    private static void GenerateLandUnitBitmap(Context context, Bitmap[] Container, int lIndex, Allegiance allegiance, int lRes)
    {
        Bitmap icon = LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), lRes), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);

        Bitmap bitmap = Bitmap.createBitmap(icon.getWidth(), icon.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(icon, 0, 0, null);

        Container[lIndex] = bitmap;
    }

    public static Bitmap GetLandUnitBitmap(Context context, LaunchClientGame game, LandUnit unit)
    {
        Allegiance allegiance = game.GetAllegiance(game.GetOurPlayer(), unit);

        int lIndex = allegiance.ordinal(); //(allegiance.ordinal() * LandUnitMoveStatus.values().length) + runStatus.ordinal();

        if(unit.GetOnWater())
        {
            if(unit.GetGeoTarget() == null || DirectionFacesEast(unit.GetPosition().BearingTo(unit.GetGeoTarget())))
            {
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_landing_craft_east), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            }
            else
            {
                return LaunchUICommon.TintBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_landing_craft_west), LaunchUICommon.AllegianceColours[allegiance.ordinal()]);
            }
        }

        if(unit instanceof Tank)
        {
            if(unit.GetGeoTarget() == null || DirectionFacesEast(unit.GetPosition().BearingTo(unit.GetGeoTarget())))
            {
                if(MBTBitmaps[lIndex] == null)
                    GenerateLandUnitBitmap(context, MBTBitmaps, lIndex, allegiance, R.drawable.marker_tank_east);

                return MBTBitmaps[lIndex];
            }
            else
            {
                if(WestMBTBitmaps[lIndex] == null)
                    GenerateLandUnitBitmap(context, WestMBTBitmaps, lIndex, allegiance, R.drawable.marker_tank_west);

                return WestMBTBitmaps[lIndex];
            }
        }

        if(unit instanceof CargoTruck)
        {
            CargoTruck truck = ((CargoTruck)unit);

            if(truck.GetGeoTarget() == null || DirectionFacesEast(truck.GetPosition().BearingTo(truck.GetGeoTarget())))
            {
                if(CargoTruckBitmaps[lIndex] == null)
                    GenerateLandUnitBitmap(context, CargoTruckBitmaps, lIndex, allegiance, R.drawable.marker_truck);

                return CargoTruckBitmaps[lIndex];
            }
            else
            {
                if(WestCargoTruckBitmaps[lIndex] == null)
                    GenerateLandUnitBitmap(context, WestCargoTruckBitmaps, lIndex, allegiance, R.drawable.marker_truck_west);

                return WestCargoTruckBitmaps[lIndex];
            }
        }

        return null;
    }

    public static boolean DirectionFacesEast(double dblBearing)
    {
        if(dblBearing < 0)
        {
            return false;
        }

        return true;
    }
}
