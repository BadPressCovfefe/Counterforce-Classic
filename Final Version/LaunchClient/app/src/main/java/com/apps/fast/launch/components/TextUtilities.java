package com.apps.fast.launch.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.TextView;

import com.apps.fast.launch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.LaunchGame;
import launch.game.EntityPointer.EntityType;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.StoredCargoTruck;
import launch.game.types.TorpedoType;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.entities.Movable.MoveOrders;
import launch.game.entities.conceptuals.StoredDamagable;
import launch.game.entities.conceptuals.StoredTank;
import launch.game.types.InterceptorType;
import launch.game.types.LaunchType;
import launch.game.types.MissileType;

/**
 * Created by tobster on 05/11/15.
 */
public class TextUtilities
{
    private static Context context;

    public enum ShortUnits
    {
        METERS,
        YARDS,
        FEET
    }

    public enum LongUnits
    {
        KILOMETERS,
        STATUTE_MILES,
        NAUTICAL_MILES
    }

    public enum Speeds
    {
        KILOMETERS_PER_HOUR,
        MILES_PER_HOUR,
        KNOTS
    }

    public static final int UNITS_METRIC = 0;
    public static final int UNITS_IMPERIAL = 1;
    public static final int UNITS_MARINE = 2;

    public static final long A_DAY = 86400000;
    public static final long AN_HOUR = 3600000;
    public static final long A_MINUTE = 60000;
    public static final long A_SEC = 1000;

    private static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdfDayAndTime = new SimpleDateFormat("dd/MM HH:mm");
    private static final SimpleDateFormat sdfDayAndFullTime = new SimpleDateFormat("dd/MM HH:mm:ss");
    private static final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yy");

    private static final double COMPASS_DIVISIONS = (Math.PI * 2.0) / 8.0;
    private static final double COMPASS_DIV_OFFSET = COMPASS_DIVISIONS / 2.0;

    private static final float FRACTION_TO_PERCENT = 100.0f;

    private static final float KM_PER_METER = 0.001f;
    private static final float KM_PER_YARD = 0.0009144f;
    private static final float KM_PER_FOOT = 0.0003048f;
    private static final float KM_PER_MILE = 1.60934f;
    private static final float KM_PER_NM = 1.852f;
    private static final float KPH_PER_MPH = 1.60934f;
    private static final float KPH_PER_KNOT = 1.852f;

    private static final String FORMAT_STRING_METERS = "%d m";
    private static final String FORMAT_STRING_YARDS = "%d yd";
    private static final String FORMAT_STRING_FEET = "%d ft";
    private static final String FORMAT_STRING_KILOMETERS = "%.1f km";
    private static final String FORMAT_STRING_STATUTE_MILES = "%.1f mi";
    private static final String FORMAT_STRING_NAUTICAL_MILES = "%.1f nm";
    private static final String FORMAT_STRING_KPH = "%d km/h";
    private static final String FORMAT_STRING_MPH = "%d mph";
    private static final String FORMAT_STRING_KNOTS = "%d kts";
    private static final String FORMAT_STRING_KILOGRAMS = "%d kg";
    private static final String FORMAT_STRING_TONS = "%.0f t";
    private static final String FORMAT_STRING_POUNDS = "%d lbs";

    private static final String FORMAT_STRING_LATENCY = "%d ms";

    private static final int BINARY_THOUSAND = 1024;

    private static ShortUnits shortUnits;
    private static LongUnits longUnits;
    private static Speeds speeds;
    private static String strCurrencySymbol;
    private static boolean bUseLbs;

    public static void Initialise(Context ctx)
    {
        context = ctx;

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(ClientDefs.SETTINGS, Context.MODE_PRIVATE);
        shortUnits = ShortUnits.values()[sharedPreferences.getInt(ClientDefs.SETTINGS_SHORT_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT)];
        longUnits = LongUnits.values()[sharedPreferences.getInt(ClientDefs.SETTINGS_LONG_UNITS, ClientDefs.SETTINGS_UNITS_DEFAULT)];
        speeds = Speeds.values()[sharedPreferences.getInt(ClientDefs.SETTINGS_SPEEDS, ClientDefs.SETTINGS_UNITS_DEFAULT)];
        strCurrencySymbol = sharedPreferences.getString(ClientDefs.SETTINGS_CURRENCY, ClientDefs.SETTINGS_CURRENCY_DEFAULT);
        bUseLbs = sharedPreferences.getBoolean(ClientDefs.SETTINGS_USE_LBS, ClientDefs.SETTINGS_USE_LBS_DEFAULT);
    }

    private static String GetShortDistanceStringFromKM(float fltDistanceKM)
    {
        switch(shortUnits)
        {
            case METERS: return String.format(FORMAT_STRING_METERS, (int)((fltDistanceKM / KM_PER_METER) + 0.5f));
            case YARDS: return String.format(FORMAT_STRING_YARDS, (int)((fltDistanceKM / KM_PER_YARD) + 0.5f));
            case FEET: return String.format(FORMAT_STRING_FEET, (int)((fltDistanceKM / KM_PER_FOOT) + 0.5f));
        }

        return "Short unit error!";
    }

    public static String GetDistanceStringFromKM(float fltDistanceKM)
    {
        switch(longUnits)
        {
            case KILOMETERS:
                if(fltDistanceKM < 1.0f)
                    return GetShortDistanceStringFromKM(fltDistanceKM);
                return String.format(FORMAT_STRING_KILOMETERS, fltDistanceKM);

            case STATUTE_MILES:
                if(fltDistanceKM < KM_PER_MILE)
                    return GetShortDistanceStringFromKM(fltDistanceKM);
                return String.format(FORMAT_STRING_STATUTE_MILES, fltDistanceKM / KM_PER_MILE);

            case NAUTICAL_MILES:
                if(fltDistanceKM < KM_PER_NM)
                    return GetShortDistanceStringFromKM(fltDistanceKM);
                return String.format(FORMAT_STRING_NAUTICAL_MILES, fltDistanceKM / KM_PER_NM);
        }

        return "Long unit error!";
    }

    public static String GetWeightStringFromKG(long kg)
    {
        if(kg >= 1000000)
        {
            return String.format(FORMAT_STRING_TONS, (float)(kg/1000));
        }

        return String.format(FORMAT_STRING_KILOGRAMS, kg);
    }

    public static String GetDistanceStringFromM(float fltDistanceM)
    {
        return GetDistanceStringFromKM(fltDistanceM * KM_PER_METER);
    }

    public static String AssignSpeedFromKPH(float fltSpeed)
    {
        switch(speeds)
        {
            case KILOMETERS_PER_HOUR: return String.format(FORMAT_STRING_KPH, (int)(fltSpeed + 0.5f));
            case MILES_PER_HOUR: return String.format(FORMAT_STRING_MPH, (int)((fltSpeed / KPH_PER_MPH) + 0.5f));
            case KNOTS: return String.format(FORMAT_STRING_KNOTS, (int)((fltSpeed / KPH_PER_KNOT) + 0.5f));
        }

        return "Speed unit error!";
    }

    public static void AssignMachSpeedFromKph(TextView textView, float fltSpeed)
    {
        float machValue = fltSpeed * 0.000809848f;

        textView.setText(context.getString(R.string.mach, String.format("%.1f", machValue)));

        if(machValue < 4)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
        else if(machValue >= 4 && machValue < 9)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }
        else if(machValue >= 9)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
    }

    public static String GetCurrencyString(int lMoney)
    {
        /*if(lMoney >= 1000000000)
        {
            return strCurrencySymbol + String.format("%.0f", (float)(lMoney/1000000000)) + "t";
        }
        else if(lMoney >= 1000000)
        {
            return strCurrencySymbol + String.format("%.1f", (float)(lMoney/1000000)) + "b";
        }
        else if(lMoney >= 1000)
        {
            return strCurrencySymbol + String.format("%.1f", (float)(lMoney/1000)) + "m";
        }*/

        return strCurrencySymbol + lMoney;
    }

    public static String GetCurrencyString(float fltMoney)
    {
        return strCurrencySymbol + String.format("%.1f", fltMoney);
    }

    public static String GetCurrencyString(long oMoney)
    {
        return strCurrencySymbol + oMoney;
    }

    public static String GetResourceCostString(ResourceType type, long oAmount)
    {
        return ConvertToThousandsWithK(oAmount);

        /*switch(type)
        {
            case OIL: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_oil);
            case IRON: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_iron);
            case COAL: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_coal);
            case CROPS: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_crops);
            case FERTILIZER: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_fertilizer);
            case URANIUM: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_uranium);
            case ELECTRICITY: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_electricity);
            case WEALTH: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_wealth);
            case CONCRETE: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_stone);
            case LUMBER: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_lumber);
            case FOOD: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_food);
            case FUEL: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_fuel);
            case STEEL: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_steel);
            case CONSTRUCTION_SUPPLIES: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_construction_supplies);
            case EXPLOSIVE: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_explosive);
            case NERVE_AGENT: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_nerve_agent);
            case MACHINERY: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_machinery);
            case ELECTRONICS: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_electronics);
            case MEDICINE: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_medicine);
            case ENRICHED_URANIUM: return ConvertToThousandsWithK(lAmount) + " " + context.getString(R.string.resource_title_enriched_uranium);
            default: return "resource type not implemented!";
        }*/
    }

    public static String GetTimeAmount(long oTimespan)
    {
        long oDays = oTimespan / A_DAY;
        long oHours = (oTimespan % A_DAY) / AN_HOUR;
        long oMinutes = ((oTimespan % A_DAY) % AN_HOUR) / A_MINUTE;
        long oSeconds = (((oTimespan % A_DAY) % AN_HOUR) % A_MINUTE) / A_SEC;

        if(oDays > 0)
        {
            return oDays + "days, " + oHours + ":" + String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else if(oHours > 0)
        {
            return oHours + ":" + String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else if(oMinutes > 0)
        {
            return String.format("%02d", oMinutes) + ":" + String.format("%02d", oSeconds);
        }
        else
        {
            return oSeconds + "s";
        }
    }

    public static String GetLatencyString(long oLatency)
    {
        return String.format(FORMAT_STRING_LATENCY, oLatency);
    }

    public static String GetFutureTime(long oInFuture)
    {
        long oTime = System.currentTimeMillis() + oInFuture;

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(oTime);

        if(oInFuture > A_DAY)
        {
            return sdfDayAndTime.format(cal.getTime());
        }

        return sdfTime.format(cal.getTime());
    }

    public static String GetDate(Calendar cal)
    {
        return sdfDate.format(cal.getTime());
    }

    public static String GetTime(Calendar cal)
    {
        return sdfTime.format(cal.getTime());
    }

    public static String GetTime(long oTime)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oTime);

        return sdfTime.format(cal.getTime());
    }

    public static String GetDateAndTime(long oTime)
    {
        if(oTime == 0)
            return "Never";

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oTime);

        return sdfDayAndTime.format(cal.getTime());
    }

    public static String GetDateAndTimeRange(long oStart, long oEnd)
    {
        //TO DO: String(s) from resource, and condense the month/day if same?
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(oStart);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(oEnd);

        return String.format("%s - %s", sdfDayAndTime.format(calStart.getTime()), sdfDayAndTime.format(calEnd.getTime()));
    }

    public static String GetDateAndFullTime(long oTime)
    {
        if(oTime == 0)
            return "Never";

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oTime);

        return sdfDayAndFullTime.format(cal.getTime());
    }

    public static String QualitativeDirectionFromBearing(double dblBearing)
    {
        //Normalise.
        if(dblBearing < 0)
        {
            dblBearing += (2 * Math.PI);
        }

        if(dblBearing > COMPASS_DIV_OFFSET)
        {
            if(dblBearing < COMPASS_DIVISIONS + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.northeast);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 2) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.east);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 3) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.southeast);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 4) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.south);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 5) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.southwest);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 6) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.west);
            }
            else if(dblBearing < (COMPASS_DIVISIONS * 7) + COMPASS_DIV_OFFSET)
            {
                return context.getString(R.string.northwest);
            }
        }

        return context.getString(R.string.north);
    }

    public static String GetEntityTypeAndName(LaunchEntity entity, LaunchGame game)
    {
        if(entity instanceof Player)
        {
            return ((Player)entity).GetName();
        }
        else if(entity instanceof Missile)
        {
            return GetMissileAndType((Missile)entity, game);
        }
        else if(entity instanceof Interceptor)
        {
            return GetInterceptorAndType((Interceptor)entity, game);
        }
        else if(entity instanceof Torpedo)
        {
            return GetTorpedoAndType((Torpedo)entity, game);
        }
        else if(entity instanceof MissileSite)
        {
            MissileSite missileSite = ((MissileSite)entity);
            if(missileSite.CanTakeICBM())
            {
                return "ICBM Silo";
            }
            return context.getString(R.string.missile_site);
        }
        else if(entity instanceof SAMSite)
        {
            SAMSite samSite = ((SAMSite)entity);
            if(samSite.GetIsABMSilo())
            {
                return "ABM Silo";
            }
            return context.getString(R.string.sam_site);
        }
        else if(entity instanceof SentryGun)
        {
            SentryGun gun = (SentryGun)entity;

            if(gun.GetIsWatchTower())
            {
                return context.getString(R.string.artillery_gun);
            }
            else
            {
                return context.getString(R.string.sentry_gun);
            }
        }
        else if(entity instanceof ArtilleryGun)
            return context.getString(R.string.artillery_gun);
        else if(entity instanceof OreMine)
        {
            OreMine oreMine = (OreMine)entity;

            GetExtractorTitle(oreMine.GetType());
        }
        else if(entity instanceof RadarStation)
        {
            return context.getString(R.string.radar_station);
        }
        else if(entity instanceof CommandPost)
        {
            return context.getString(R.string.command_post);
        }
        else if(entity instanceof Bank)
        {
            return context.getString(R.string.bank);
        }
        else if(entity instanceof Warehouse)
        {
            return context.getString(R.string.warehouse);
        }
        else if(entity instanceof MissileFactory)
        {
            return context.getString(R.string.missilefactory);
        }
        else if(entity instanceof Airbase)
        {
            return context.getString(R.string.airbase);
        }
        else if(entity instanceof Armory)
        {
            Armory armory = (Armory) entity;

            if(armory.GetIsBarracks())
                return context.getString(R.string.barracks);
            else
                return context.getString(R.string.armory);
        }
        else if(entity instanceof ScrapYard)
        {
            return context.getString(R.string.scrap_yard);
        }
        else if(entity instanceof Processor)
        {
            return GetProcessorTitle(((Processor)entity).GetType());
        }
        else if(entity instanceof Distributor)
        {
            return GetDistributorTitle(((Distributor)entity).GetType());
        }
        else if(entity instanceof AirplaneInterface)
        {
            AirplaneInterface airplane = (AirplaneInterface)entity;

            switch(airplane.GetAircraftType())
            {
                case FIGHTER: return context.getString(R.string.fighter_title);
                case BOMBER: return context.getString(R.string.bomber_title);
                case STEALTH_FIGHTER: return context.getString(R.string.stealth_fighter_title);
                case STEALTH_BOMBER: return context.getString(R.string.stealth_bomber_title);
                case ATTACK_AIRCRAFT: return context.getString(R.string.attack_aircraft_title);
                case AWACS: return context.getString(R.string.awacs_title);
                case REFUELER: return context.getString(R.string.refueler_title);
                case CARGO_PLANE: return context.getString(R.string.cargo_plane_title);
                case MULTI_ROLE: return context.getString(R.string.multi_role_title);
                case SSB: return context.getString(R.string.ssb_title);
            }
        }
        else if(entity instanceof NavalVessel)
        {
            switch(entity.GetEntityType())
            {
                case FRIGATE: return context.getString(R.string.frigate_title);
                case DESTROYER: return context.getString(R.string.destroyer_title);
                case AMPHIB: return context.getString(R.string.assault_ship_title);
                case CARGO_SHIP: return context.getString(R.string.cargo_ship_title);
                case FLEET_OILER: return context.getString(R.string.fleet_oiler_title);
                case SUPER_CARRIER: return context.getString(R.string.super_carrier_title);
                case ATTACK_SUB: return context.getString(R.string.attack_sub_title);
                case SSBN: return context.getString(R.string.ssbn_title);
            }
        }
        else if(entity instanceof CargoTruckInterface)
        {
            return context.getString(R.string.cargo_truck_title);
        }
        else if(entity instanceof ResourceDeposit)
        {
            return context.getString(R.string.resource_deposit_title);
        }
        else if(entity instanceof Blueprint)
        {
            Blueprint blueprint = (Blueprint)entity;

            switch(blueprint.GetType())
            {
                case NUCLEAR_MISSILE_SITE: return context.getString(R.string.blueprint_title_format, context.getString(R.string.nuke_site_title));
                case WAREHOUSE: return context.getString(R.string.blueprint_title_format, context.getString(R.string.warehouse));
                case RADAR_STATION: return context.getString(R.string.blueprint_title_format, context.getString(R.string.radar_station));
                case MISSILE_FACTORY: return context.getString(R.string.blueprint_title_format, context.getString(R.string.missilefactory));
                case SENTRY_GUN: return context.getString(R.string.blueprint_title_format, context.getString(R.string.sentry_gun));
                case WATCH_TOWER: return context.getString(R.string.blueprint_title_format, context.getString(R.string.artillery_gun));

                case ORE_MINE:
                {
                    String strExtractorType = GetExtractorTitle(blueprint.GetResourceType());

                    return context.getString(R.string.blueprint_title_format, strExtractorType);
                }

                case PROCESSOR: return context.getString(R.string.blueprint_title_format, GetProcessorTitle(blueprint.GetResourceType()));
                case AIRBASE: return context.getString(R.string.blueprint_title_format, context.getString(R.string.airbase));
                case ARMORY: return context.getString(R.string.blueprint_title_format, context.getString(R.string.armory));
                case BARRACKS: return context.getString(R.string.blueprint_title_format, context.getString(R.string.barracks));
                case ABM_SILO: return context.getString(R.string.blueprint_title_format, context.getString(R.string.abm_silo_title));
                case COMMAND_POST: return context.getString(R.string.blueprint_title_format, context.getString(R.string.command_post));
                case BANK: return context.getString(R.string.blueprint_title_format, context.getString(R.string.bank));
                case SCRAP_YARD: return context.getString(R.string.blueprint_title_format, context.getString(R.string.scrap_yard));
                case SAM_SITE: return context.getString(R.string.blueprint_title_format, context.getString(R.string.sam_site));
                case MISSILE_SITE: return context.getString(R.string.blueprint_title_format, context.getString(R.string.missile_site));
                case ARTILLERY_GUN: return context.getString(R.string.blueprint_title_format, context.getString(R.string.artillery_gun));
                case MARKET: return context.getString(R.string.blueprint_title_format, context.getString(R.string.market));
                default: return "NOT IMPLEMENTED! (Blueprint name)";
            }
        }
        else if(entity instanceof Rubble)
        {
            switch(((Rubble)entity).GetStructureType())
            {
                case NUCLEAR_MISSILE_SITE: return context.getString(R.string.rubble_title_format, context.getString(R.string.nuke_site_title));
                case WAREHOUSE: return context.getString(R.string.rubble_title_format, context.getString(R.string.warehouse));
                case RADAR_STATION: return context.getString(R.string.rubble_title_format, context.getString(R.string.radar_station));
                case MISSILE_FACTORY: return context.getString(R.string.rubble_title_format, context.getString(R.string.missilefactory));
                case SENTRY_GUN: return context.getString(R.string.rubble_title_format, context.getString(R.string.sentry_gun));
                case WATCH_TOWER: return context.getString(R.string.rubble_title_format, context.getString(R.string.artillery_gun));
                case ORE_MINE: return context.getString(R.string.rubble_title_format, GetExtractorTitle(((Rubble)entity).GetResourceType()));
                case AIRBASE: return context.getString(R.string.rubble_title_format, context.getString(R.string.airbase));
                case ARMORY: return context.getString(R.string.rubble_title_format, context.getString(R.string.armory));
                case BARRACKS: return context.getString(R.string.rubble_title_format, context.getString(R.string.barracks));
                case ABM_SILO: return context.getString(R.string.rubble_title_format, context.getString(R.string.abm_silo_title));
                case COMMAND_POST: return context.getString(R.string.rubble_title_format, context.getString(R.string.command_post));
                case BANK: return context.getString(R.string.rubble_title_format, context.getString(R.string.bank));
                case SAM_SITE: return context.getString(R.string.rubble_title_format, context.getString(R.string.sam_site));
                case MISSILE_SITE: return context.getString(R.string.rubble_title_format, context.getString(R.string.missile_site));
                case ARTILLERY_GUN: return context.getString(R.string.rubble_title_format, context.getString(R.string.artillery_gun));
                case MARKET: return context.getString(R.string.rubble_title_format, context.getString(R.string.market));
                case DISTRIBUTOR: return context.getString(R.string.rubble_title_format, TextUtilities.GetDistributorTitle(((Rubble)entity).GetResourceType()));
                case PROCESSOR: return context.getString(R.string.rubble_title_format, TextUtilities.GetProcessorTitle(((Rubble)entity).GetResourceType()));
                case SCRAP_YARD: return context.getString(R.string.rubble_title_format, context.getString(R.string.scrap_yard));
                default: return "NOT IMPLEMENTED! (Rubble name)";
            }
        }
        else if(entity instanceof Airdrop)
        {
            return context.getString(R.string.airdrop);
        }
        else if(entity instanceof Shipyard)
        {
            Shipyard shipyard = (Shipyard)entity;

            return shipyard.GetName();
        }
        else if(entity instanceof Loot)
        {
            return GetLootContentString(((Loot)entity), game);
        }
        else if(entity instanceof Infantry)
        {
            return context.getString(R.string.infantry);
        }
        else if(entity instanceof TankInterface)
        {
            TankInterface tank = (TankInterface)entity;

            switch(tank.GetType())
            {
                case MISSILE_TANK:
                {
                    return context.getString(R.string.missile_tank);
                }

                case SAM_TANK:
                {
                    return context.getString(R.string.sam_tank);
                }

                case HOWITZER:
                {
                    return context.getString(R.string.howitzer);
                }

                case MBT:
                {
                    return context.getString(R.string.mbt);
                }

                case SPAAG:
                {
                    return context.getString(R.string.spaag);
                }
            }
        }

        return "NOT IMPLEMENTED! (GetEntityTypeAndName)";
    }

    public static String GetOwnedEntityName(LaunchEntity entity, LaunchGame game)
    {
        Player owner = game.GetOwner(entity);
        String ownerName;

        if(owner != null)
        {
            ownerName = owner.GetName();

            if(entity instanceof Player)
            {
                Player player = (Player)entity;
                return player.GetName();
            }
            if(entity instanceof Missile)
            {
                Missile missile = (Missile)entity;
                MissileType type = game.GetConfig().GetMissileType(missile.GetType());

                if(game.GetPlayer(missile.GetOwnerID()) != null)
                {
                    return context.getString(R.string.owners_entity, ownerName, GetMissileAndType(missile, game));
                }
            }
            else if(entity instanceof Interceptor)
            {
                Interceptor interceptor = (Interceptor)entity;
                return context.getString(R.string.owners_entity, ownerName, GetInterceptorAndType(interceptor, game));
            }
            else if(entity instanceof Torpedo)
            {
                Torpedo torpedo = (Torpedo)entity;
                return context.getString(R.string.owners_entity, ownerName, GetTorpedoAndType(torpedo, game));
            }
            else if(entity instanceof MissileSite)
            {
                if(((MissileSite)entity).CanTakeICBM())
                {
                    MissileSite missileSite = (MissileSite)entity;
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.nuke_site_title));
                }
                else
                {
                    MissileSite missileSite = (MissileSite) entity;
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.missile_site_title));
                }
            }
            else if(entity instanceof Rubble)
            {
                return GetEntityTypeAndName(entity, game);
            }
            else if(entity instanceof SAMSite)
            {
                SAMSite samSite = (SAMSite)entity;

                if(samSite.GetIsABMSilo())
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.abm_silo_title));
                }
                else
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.sam_site_title));
                }
            }
            else if(entity instanceof SentryGun)
            {
                SentryGun gun = (SentryGun)entity;

                if(gun.GetIsWatchTower())
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.artillery_gun_title));
                }
                else
                {
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.sentry_gun_title));
                }
            }
            else if(entity instanceof ArtilleryGun)
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.artillery_gun_title));
            else if(entity instanceof OreMine)
            {
                OreMine oreMine = (OreMine)entity;

                return context.getString(R.string.owners_entity, ownerName, GetExtractorTitle(oreMine.GetType()));
            }
            else if(entity instanceof RadarStation)
            {
                RadarStation radarStation = (RadarStation)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.radar_station_title));
            }
            else if(entity instanceof CommandPost)
            {
                CommandPost bunker = (CommandPost)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.command_post_title));
            }
            else if(entity instanceof Bank)
            {
                Bank bank = (Bank)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.bank_title));
            }
            else if(entity instanceof Processor)
            {
                Processor processor = (Processor)entity;
                return context.getString(R.string.owners_entity, ownerName, GetProcessorName(processor.GetType()));
            }
            else if(entity instanceof Distributor)
            {
                Distributor distributor = (Distributor)entity;
                return context.getString(R.string.owners_entity, ownerName, GetDistributorName(distributor.GetType()));
            }
            else if(entity instanceof Warehouse)
            {
                Warehouse depot = (Warehouse)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.warehouse_title));
            }
            else if(entity instanceof MissileFactory)
            {
                MissileFactory factory = (MissileFactory)entity;
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.missilefactory_title));
            }
            else if(entity instanceof Airbase)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.construct_name_airbase));
            }
            else if(entity instanceof Armory)
            {
                Armory armory = (Armory) entity;

                if(armory.GetIsBarracks())
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.construct_name_barracks));
                else
                    return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.construct_name_armory));
            }
            else if(entity instanceof ScrapYard)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.construct_name_scrap_yard));
            }
            else if(entity instanceof AirplaneInterface)
            {
                AirplaneInterface airplane = (AirplaneInterface)entity;

                switch(airplane.GetAircraftType())
                {
                    case FIGHTER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.fighter_name));
                    case BOMBER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.bomber_name));
                    case STEALTH_FIGHTER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.stealth_fighter_name));
                    case STEALTH_BOMBER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.stealth_bomber_name));
                    case ATTACK_AIRCRAFT: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.attack_aircraft_name));
                    case AWACS: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.awacs_name));
                    case REFUELER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.refueler_name));
                    case CARGO_PLANE: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.cargo_plane_name));
                    case MULTI_ROLE: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.multi_role_name));
                    case SSB: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.ssb_name));
                }
            }
            else if(entity instanceof NavalVessel)
            {
                NavalVessel vessel = (NavalVessel)entity;
                EntityType type = vessel.GetEntityType();

                switch(type)
                {
                    case FRIGATE: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.frigate_name));
                    case DESTROYER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.destroyer_name));
                    case AMPHIB: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.assault_ship_name));
                    case CARGO_SHIP: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.cargo_ship_name));
                    case FLEET_OILER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.fleet_oiler_name));
                    case SUPER_CARRIER: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.super_carrier_name));
                    case ATTACK_SUB: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.attack_sub_name));
                    case SSBN: return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.ssbn_name));
                }
            }
            else if(entity instanceof CargoTruckInterface)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.cargo_truck_construct_name));
            }
            else if(entity instanceof Shipyard)
            {
                Shipyard shipyard = ((Shipyard)entity);
                return shipyard.GetName();
            }
            else if(entity instanceof InfantryInterface)
            {
                return context.getString(R.string.owners_entity, ownerName, context.getString(R.string.title_infantry));
            }
            else if(entity instanceof TankInterface)
            {
                TankInterface tank = ((TankInterface)entity);
                String strName = "";

                switch(tank.GetType())
                {
                    case MISSILE_TANK:
                    {
                        strName = context.getString(R.string.title_missile_tank);
                    }
                    break;

                    case SAM_TANK:
                    {
                        strName = context.getString(R.string.title_sam_tank);
                    }
                    break;

                    case HOWITZER:
                    {
                        strName = context.getString(R.string.title_howitzer);
                    }
                    break;

                    case MBT:
                    {
                        strName = context.getString(R.string.title_mbt);
                    }
                    break;

                    case SPAAG:
                    {
                        strName = context.getString(R.string.title_spaag);
                    }
                    break;
                }

                return context.getString(R.string.owners_entity, ownerName, strName);
            }

            return "NOT IMPLEMENTED! (GetOwnedEntityName)";
        }
        else
        {
            return GetEntityTypeAndName(entity, game);
        }
    }

    public static String GetEntityTypeName(EntityType entityType, ResourceType resourceType)
    {
        switch(entityType)
        {
            case MISSILE_SITE:
            {
                return context.getString(R.string.construct_name_cms);
            }

            case NUCLEAR_MISSILE_SITE:
            {
                return context.getString(R.string.construct_name_ncms);
            }

            case SAM_SITE:
            {
                return context.getString(R.string.construct_name_sam);
            }

            case ABM_SILO:
            {
                return context.getString(R.string.construct_name_abm_silo);
            }

            case SENTRY_GUN:
            {
                return context.getString(R.string.construct_name_sentry_gun);
            }

            case WATCH_TOWER:
            {
                return context.getString(R.string.construct_name_artillery_gun);
            }

            case RADAR_STATION:
            {
                return context.getString(R.string.construct_name_radar_station);
            }

            case COMMAND_POST:
            {
                return context.getString(R.string.construct_name_command_post);
            }

            case ORE_MINE:
            {
                return GetExtractorName(resourceType);
            }

            case DISTRIBUTOR:
            {
                return GetDistributorName(resourceType);
            }

            case AIRBASE:
            {
                return context.getString(R.string.construct_name_airbase);
            }

            case BANK:
            {
                return context.getString(R.string.construct_name_bank);
            }

            case WAREHOUSE:
            {
                return context.getString(R.string.construct_name_warehouse);
            }

            case INFANTRY:
            {
                return context.getString(R.string.infantry_construct_name);
            }

            case ARMORY:
            {
                return context.getString(R.string.construct_name_armory);
            }

            case BARRACKS:
            {
                return context.getString(R.string.construct_name_barracks);
            }

            case CARGO_TRUCK:
            {
                return context.getString(R.string.cargo_truck_construct_name);
            }

            case PROCESSOR:
            {
                switch(resourceType)
                {
                    case ELECTRICITY: return context.getString(R.string.construct_name_power_plant);
                    case FOOD: return context.getString(R.string.construct_name_granary);
                    case FUEL: return context.getString(R.string.construct_name_oil_refinery);
                    case STEEL: return context.getString(R.string.construct_name_foundry);
                    case CONSTRUCTION_SUPPLIES: return context.getString(R.string.construct_name_construction_yard);
                    case MACHINERY: return context.getString(R.string.construct_name_machine_shop);
                    case ELECTRONICS: return context.getString(R.string.construct_name_lithography_plant);
                    case MEDICINE: return context.getString(R.string.construct_name_laboratory);
                    case ENRICHED_URANIUM: return context.getString(R.string.construct_name_enrichment_facility);
                    case NUCLEAR_ELECTRICITY: return context.getString(R.string.construct_name_nuclear_power_plant);
                }
            }

            case ARTILLERY_GUN:
            {
                return context.getString(R.string.construct_name_artillery_gun);
            }

            case SCRAP_YARD:
            {
                return context.getString(R.string.construct_name_scrap_yard);
            }

            case MBT:
            {
                return context.getString(R.string.mbt_construct_name);
            }

            case SPAAG:
            {
                return context.getString(R.string.spaag_construct_name);
            }

            case HOWITZER:
            {
                return context.getString(R.string.howitzer_construct_name);
            }

            case MISSILE_TANK:
            {
                return context.getString(R.string.missile_tank_construct_name);
            }

            case SAM_TANK:
            {
                return context.getString(R.string.sam_tank_construct_name);
            }

            case FRIGATE:
            {
                return context.getString(R.string.frigate_name);
            }

            case DESTROYER:
            {
                return context.getString(R.string.destroyer_name);
            }

            case SUPER_CARRIER:
            {
                return context.getString(R.string.super_carrier_name);
            }

            case CARGO_SHIP:
            {
                return context.getString(R.string.cargo_ship_name);
            }

            case AMPHIB:
            {
                return context.getString(R.string.assault_ship_name);
            }

            case FLEET_OILER:
            {
                return context.getString(R.string.fleet_oiler_name);
            }

            case ATTACK_SUB:
            {
                return context.getString(R.string.attack_sub_name);
            }

            case SSBN:
            {
                return context.getString(R.string.ssbn_name);
            }

            case FIGHTER:
            {
                return context.getString(R.string.fighter_name);
            }

            case BOMBER:
            {
                return context.getString(R.string.bomber_name);
            }

            case STEALTH_FIGHTER:
            {
                return context.getString(R.string.stealth_fighter_name);
            }

            case STEALTH_BOMBER:
            {
                return context.getString(R.string.stealth_bomber_name);
            }

            case ATTACK_AIRCRAFT:
            {
                return context.getString(R.string.attack_aircraft_name);
            }

            case AWACS:
            {
                return context.getString(R.string.awacs_name);
            }

            case REFUELER:
            {
                return context.getString(R.string.refueler_name);
            }

            case CARGO_PLANE:
            {
                return context.getString(R.string.cargo_plane_name);
            }

            case MULTI_ROLE:
            {
                return context.getString(R.string.multi_role_name);
            }

            case SSB:
            {
                return context.getString(R.string.ssb_name);
            }

            default: return "NOT IMPLEMENTED! (GetEntityTypeName)";
        }
    }

    public static String GetMissileAndType(Missile missile, LaunchGame game)
    {
        MissileType type = game.GetConfig().GetMissileType(missile.GetType());

        if(type.GetICBM())
            return context.getString(R.string.missile_type, game.GetConfig().GetMissileType(missile.GetType()).GetName(), context.getString(R.string.icbm));
        else if(type.GetArtillery())
            return context.getString(R.string.shell);
        else
            return context.getString(R.string.missile_type, game.GetConfig().GetMissileType(missile.GetType()).GetName(), context.getString(R.string.missile));
    }

    public static String GetInterceptorAndType(Interceptor interceptor, LaunchGame game)
    {
        return context.getString(R.string.missile_type, game.GetConfig().GetInterceptorType(interceptor.GetType()).GetName(), context.getString(R.string.interceptor));
    }

    public static String GetTorpedoAndType(Torpedo torpedo, LaunchGame game)
    {
        return context.getString(R.string.missile_type, game.GetConfig().GetTorpedoType(torpedo.GetType()).GetName(), context.getString(R.string.torpedo));
    }

    public static void SetStructureState(TextView textView, Structure structure)
    {
        if(structure.GetOnline())
        {
            textView.setText(R.string.state_online_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }

        if(structure.GetBooting())
        {
            textView.setText(R.string.state_booting_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }

        if(structure.GetSelling())
        {
            textView.setText(R.string.state_selling_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }

        if(structure.GetOffline())
        {
            textView.setText(R.string.state_offline_name);
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static String GetPercentStringFromFraction(float fltFraction)
    {
        return String.format("%.0f%%", fltFraction * FRACTION_TO_PERCENT);
    }

    public static String GetLatLongString(float fltLatitude, float fltLongitude)
    {
        return context.getString(R.string.lat_long, String.format("%.6f", fltLatitude), String.format("%.6f", fltLongitude));
    }

    public static void AssignHealthStringAndAppearance (TextView txtHealth, Damagable damagable)
    {
        if(damagable.Destroyed())
        {
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));

            txtHealth.setText(context.getString(R.string.health_destroyed));
        }
        else
        {
            txtHealth.setText(context.getString(R.string.hps, damagable.GetHP(), damagable.GetMaxHP()));

            float fltDamageRatio = (float) damagable.GetHP() / (float) damagable.GetMaxHP();
            if (fltDamageRatio > 0.999f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if (fltDamageRatio > 0.5f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }


    public static void AssignHealthStringAndAppearance(TextView txtHealth, StoredDamagable damagable)
    {
        if(damagable.Destroyed())
        {
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));

            txtHealth.setText(context.getString(R.string.health_destroyed));
        }
        else
        {
            txtHealth.setText(context.getString(R.string.hps, damagable.GetHP(), damagable.GetMaxHP()));

            float fltDamageRatio = (float) damagable.GetHP() / (float) damagable.GetMaxHP();
            if (fltDamageRatio > 0.999f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if (fltDamageRatio > 0.5f)
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignHealthStringAndAppearance (TextView txtHealth, List<Structure> Structures)
    {
        short nMinHealth = Short.MAX_VALUE;
        short nMaxHealth = 0;
        short nMinMaxHealth = Short.MAX_VALUE;
        short nMaxMaxHealth = 0;

        for(Structure structure: Structures)
        {
            if(structure != null)
            {
                nMinHealth = (short)Math.min(nMinHealth, structure.GetHP());
                nMaxHealth = (short)Math.max(nMaxHealth, structure.GetHP());
                nMinMaxHealth = (short)Math.min(nMinMaxHealth, structure.GetMaxHP());
                nMaxMaxHealth = (short)Math.max(nMaxMaxHealth, structure.GetMaxHP());
            }
        }

        String strHealthRangeString = (nMinHealth == nMaxHealth) ? Short.toString(nMinHealth) : String.format("%d-%d", nMinHealth, nMaxHealth);
        String strHealthMaxString = (nMinMaxHealth == nMaxMaxHealth) ? Short.toString(nMinMaxHealth) : String.format("%d-%d", nMinMaxHealth, nMaxMaxHealth);
        String strHealthString = String.format("%s/%s", strHealthRangeString, strHealthMaxString);

        txtHealth.setText(strHealthString);

        float fltDamageRatio = (float) nMinHealth / (float) nMinMaxHealth;
        if (fltDamageRatio > 0.999f)
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if (fltDamageRatio > 0.5f)
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        else
            txtHealth.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
    }

    public static String GetDamageString(int lDamage)
    {
        return context.getString(R.string.hp, lDamage);
    }

    public static String GetMultiplierString(float fltMultiplier)
    {
        return context.getString(R.string.multiplier, String.format("%.2f", fltMultiplier));
    }

    public static String GetOreMineCompetitionString(int lTotal, int lCompeting, int lMaxValue)
    {
        return context.getString(R.string.ore_mine_competing, lCompeting, lTotal, GetCurrencyString(lMaxValue));
    }

    public static String GetConnectionSpeed(int lBytesPerSecond)
    {
        if(lBytesPerSecond < BINARY_THOUSAND)
        {
            //Bytes per second.
            return context.getString(R.string.value_unknown_download, lBytesPerSecond, context.getString(R.string.bytes_per_sec));
        }
        else if(lBytesPerSecond < BINARY_THOUSAND * BINARY_THOUSAND)
        {
            //KB per sec.
            return context.getString(R.string.value_unknown_download, (int)((float)(lBytesPerSecond) / (float)(BINARY_THOUSAND) + 0.5f), context.getString(R.string.kilobytes_per_sec));
        }
        else
        {
            //MB per sec.
            return context.getString(R.string.value_unknown_download, (int)((float)(lBytesPerSecond) / (float)(BINARY_THOUSAND * BINARY_THOUSAND) + 0.5f), context.getString(R.string.megabytes_per_sec));
        }
    }

    /**
     * Return a damage efficiency string, that is units of the player's currency symbol per hit point.
     * @param fltDamageEfficiency The incoming number, will be reduced to 2dp.
     * @return The damage efficiency string, e.g. "£69.69/hp".
     */
    public static String GetDamageEfficiency(float fltDamageEfficiency)
    {
        return context.getString(R.string.hp_efficiency, strCurrencySymbol, String.format("%.2f", fltDamageEfficiency));
    }

    public static String GetAccuracyPercentage(float fltHitChance)
    {
        return context.getString(R.string.interceptor_accuracy, String.format("%.0f%%", (fltHitChance * 100)));
    }

    public static void AssignPercentageString(TextView textView, float fltFloat, LaunchClientGame game)
    {
        textView.setText(String.format("%.0f%%", (fltFloat * 100)));
        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
    }

    public static void AssignAccuracyPercentage(TextView textView, float fltHitChance, LaunchClientGame game)
    {
        textView.setText(GetAccuracyPercentage(fltHitChance));

        if(fltHitChance >= game.GetConfig().GetMaxWeaponSystemAccuracy())
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.75f)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.5f && fltHitChance < 0.75f)
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
        }
        else
        {
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignAccuracyBoostPercentage(TextView textView, float fltHitChance, LaunchClientGame game)
    {
        if(fltHitChance >= game.GetConfig().GetMaxRadarAccuracyBoost())
        {
            textView.setText(context.getString(R.string.high_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.35f)
        {
            textView.setText(context.getString(R.string.high_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        }
        else if(fltHitChance >= 0.15f && fltHitChance < 0.35f)
        {
            textView.setText(context.getString(R.string.medium_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        }
        else
        {
            textView.setText(context.getString(R.string.low_accuracy));
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignFuelPercentageString(TextView textView, FuelableInterface fuelable)
    {
        float lFuelLevel = (float) fuelable.GetCurrentFuel();
        float lFuelCapacity = (float) fuelable.GetMaxFuel();
        float fltPercent = (float)lFuelLevel/lFuelCapacity;

        textView.setText(String.format("%.0f%%", (fltPercent * 100)));

        if(fltPercent >= 1.0)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if(fltPercent < 0.5)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        else
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
    }

    public static void AssignYieldStringAndAppearance(TextView textView, LaunchType launchType, LaunchClientGame game)
    {
        if(launchType instanceof MissileType)
        {
            /**
             * If the type is anti submarine and nuclear, do the yield as normal.
             * If the type is anti submarine and tyetodeploy != LaunchEntity.NONE, get the torpedo type.
             */

            MissileType type = ((MissileType)launchType);

            float yield = type.GetYield();

            if(yield < 1f && type.GetEMPRadius() > 0.0f)
            {
                textView.setText(context.getString(R.string.warhead_emp));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.EMPTextColour));
            }
            else if(type.GetNuclear())
            {
                if(yield >= 1000000000)
                {
                    yield /= 1000000000;
                    textView.setText(String.format("%.1f teratons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000000)
                {
                    yield /= 1000000;
                    textView.setText(String.format("%.1f gigatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000)
                {
                    yield /= 1000;
                    textView.setText(String.format("%.1f megatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    textView.setText(String.format("%.0f kilotons", yield));

                    if(yield > 250)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(yield > 50)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                    else
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
            else if(type.GetAntiSubmarine() && type.GetTorpedoType() != LaunchEntity.ID_NONE)
            {
                TorpedoType torpedoType = game.GetConfig().GetTorpedoType(type.GetTorpedoType());

                if(torpedoType != null)
                {
                    textView.setText(torpedoType.GetName());
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
            }
            else if(yield < 0.01f)
            {
                textView.setText(context.getString(R.string.warhead_kinetic));
            }
            else
            {
                if(bUseLbs)
                {
                    yield *= 2.205;
                    textView.setText(String.format(FORMAT_STRING_POUNDS, (int)yield));
                }
                else
                    textView.setText(String.format(FORMAT_STRING_KILOGRAMS, (int)yield));

                if(yield > 500)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield > 250)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
                else
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
        }
        else if(launchType instanceof InterceptorType)
        {
            InterceptorType type = ((InterceptorType)launchType);

            float yield = type.GetYield();

            if(type.GetNuclear())
            {
                if(yield >= 1000000000)
                {
                    yield /= 1000000000;
                    textView.setText(String.format("%.1f teratons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000000)
                {
                    yield /= 1000000;
                    textView.setText(String.format("%.1f gigatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000)
                {
                    yield /= 1000;
                    textView.setText(String.format("%.1f megatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    textView.setText(String.format("%.0f kilotons", yield));

                    if(yield > 250)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(yield > 50)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                    else
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
        }
        if(launchType instanceof TorpedoType)
        {
            TorpedoType type = ((TorpedoType)launchType);

            float yield = type.GetYield();

            if(type.GetNuclear())
            {
                if(yield >= 1000000000)
                {
                    yield /= 1000000000;
                    textView.setText(String.format("%.1f teratons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000000)
                {
                    yield /= 1000000;
                    textView.setText(String.format("%.1f gigatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield >= 1000)
                {
                    yield /= 1000;
                    textView.setText(String.format("%.1f megatons", yield));
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else
                {
                    textView.setText(String.format("%.0f kilotons", yield));

                    if(yield > 250)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    }
                    else if(yield > 50)
                    {
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                    }
                    else
                        textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
                }
            }
            else
            {
                if(bUseLbs)
                {
                    yield *= 2.205;
                    textView.setText(String.format(FORMAT_STRING_POUNDS, (int)yield));
                }
                else
                    textView.setText(String.format(FORMAT_STRING_KILOGRAMS, (int)yield));

                if(yield > 500)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                }
                else if(yield > 250)
                {
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
                }
                else
                    textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
        }
    }

    public static void AssignMissileRangeAppearance(TextView textView, float range, boolean bMissile)
    {
        if(bMissile)
        {
            if(range > 5000)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if(range > 1500)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
        else
        {
            if(range > 550)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            else if(range > 250)
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            else
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
        }
    }

    public static void AssignTorpedoRangeAppearance(TextView textView, float range)
    {
        if(range > 14)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if(range > 8)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        else
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
    }

    public static void AssignBuildTimeAppearance(TextView textView, long buildTime)
    {
        if(buildTime < Defs.MS_PER_DAY)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
        else if(buildTime < Defs.MS_PER_DAY * 3)
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
        else
            textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
    }

    public static String GetMachFromKPH(float KPH)
    {
        float fltMach = KPH/Defs.KPH_PER_MACH;

        return String.format("%.1f", fltMach);
    }

    public static String GetMetricTonsFromKG(float lKG)
    {
        float fltTons = lKG/Defs.KG_PER_TON;

        return String.format("%.0f", fltTons);
    }

    public static void AssignAircraftStatusString(TextView textView, AirplaneInterface aircraft)
    {
        if(aircraft.Flying())
        {
            Movable flyingAircraft = (Movable)aircraft;

            if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.RETURN)
            {
                textView.setText(context.getString(R.string.status_returning));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.ATTACK)
            {
                textView.setText(context.getString(R.string.status_attacking));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.SEEK_FUEL || flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.PROVIDE_FUEL)
            {
                textView.setText(context.getString(R.string.status_refueling));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else if(flyingAircraft.GetMoveOrders() == Airplane.MoveOrders.WAIT)
            {
                textView.setText(context.getString(R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
        }
        else
        {
            StoredDamagable storedAircraft = ((StoredDamagable)aircraft);

            if(!storedAircraft.DoneBuilding())
            {
                textView.setText(context.getString(R.string.unit_constructing, TextUtilities.GetTimeAmount(storedAircraft.GetPrepRemaining())));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(((FuelableInterface)storedAircraft).GetFuelDeficit() > 0)
            {
                textView.setText(context.getString(R.string.status_refueling));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else if(!storedAircraft.AtFullHealth())
            {
                textView.setText(context.getString(R.string.status_damaged));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static void AssignInfantryStatusString(TextView textView, InfantryInterface infantry)
    {
        if(infantry.Deployed())
        {
            Infantry deployedInfantry = ((Infantry)infantry);

            if(deployedInfantry.GetMoveOrders() == MoveOrders.ATTACK)
            {
                textView.setText(context.getString(R.string.status_attacking));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(deployedInfantry.GetMoveOrders() == MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(deployedInfantry.GetMoveOrders() == MoveOrders.CAPTURE)
            {
                textView.setText(context.getString(R.string.status_capturing));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else if(deployedInfantry.GetMoveOrders() == MoveOrders.LIBERATE)
            {
                textView.setText(context.getString(R.string.status_liberating));
                textView.setTextColor(Color.MAGENTA);
            }
            else if(deployedInfantry.GetMoveOrders() == MoveOrders.DEFEND)
            {
                textView.setText(context.getString(R.string.status_defending));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(deployedInfantry.GetMoveOrders() == MoveOrders.WAIT)
            {
                textView.setText(context.getString(R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
        else
        {
            if(infantry.GetHP() < infantry.GetMaxHP())
            {
                textView.setText(context.getString(R.string.status_healing));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.WarningColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static void AssignTankStatusString(TextView textView, TankInterface tankInterface)
    {
        if(tankInterface instanceof Tank)
        {
            Tank tank = (Tank)tankInterface;

            if(tank.GetMoveOrders() == MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(tank.GetMoveOrders() == MoveOrders.WAIT)
            {
                textView.setText(context.getString(tank.IsASPAAG() ? R.string.status_defending : R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
        else if(tankInterface instanceof StoredTank)
        {
            StoredTank tank = (StoredTank)tankInterface;

            if(tank.GetHP() < tank.GetMaxHP()/1.5f) //HP is less than 2/3.
            {
                textView.setText(context.getString(R.string.status_damaged));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static void AssignCargoTruckStatusString(TextView textView, CargoTruckInterface truckInterface)
    {
        if(truckInterface instanceof CargoTruck)
        {
            CargoTruck truck = (CargoTruck)truckInterface;

            if(truck.GetMoveOrders() == MoveOrders.MOVE)
            {
                textView.setText(context.getString(R.string.status_moving));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
            else if(truck.GetMoveOrders() == MoveOrders.WAIT)
            {
                textView.setText(context.getString(R.string.status_waiting));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.InfoColour));
            }
        }
        else if(truckInterface instanceof StoredCargoTruck)
        {
            StoredCargoTruck truck = (StoredCargoTruck)truckInterface;

            if(truck.GetHP() < truck.GetMaxHP()/1.5f) //HP is less than 2/3.
            {
                textView.setText(context.getString(R.string.status_damaged));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.BadColour));
            }
            else
            {
                textView.setText(context.getString(R.string.status_ready));
                textView.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
            }
        }
    }

    public static String GetFuelUsageString(LaunchGame game, FuelableInterface fuelable, GeoCoord geoTarget)
    {
        GeoCoord geoStart = game.GetPosition((LaunchEntity)fuelable);
        float fltDistanceTravelled = geoStart.DistanceTo(geoTarget);

        float fltMaxRange = Defs.CARGO_TRUCK_RANGE;

        if(fuelable instanceof NavalVessel)
            fltMaxRange = Defs.NAVAL_RANGE;
        else if(fuelable instanceof AirplaneInterface)
            fltMaxRange = Defs.GetAircraftRange(((AirplaneInterface)fuelable).GetAircraftType());
        else if(fuelable instanceof Tank)
            fltMaxRange = Defs.TANK_RANGE;

        float fuelPercent = fltDistanceTravelled/game.GetFuelableRange(fuelable.GetMaxFuel(), fltMaxRange);

        return GetPercentStringFromFraction(fuelPercent);
    }

    public static String GetFuelUsageString(LaunchGame game, FuelableInterface fuelable, float fltDistance)
    {
        float fltMaxRange = Defs.CARGO_TRUCK_RANGE;

        if(fuelable instanceof NavalVessel)
            fltMaxRange = Defs.NAVAL_RANGE;
        else if(fuelable instanceof AirplaneInterface)
            fltMaxRange = Defs.GetAircraftRange(((AirplaneInterface)fuelable).GetAircraftType());
        else if(fuelable instanceof Tank)
            fltMaxRange = Defs.TANK_RANGE;

        GeoCoord geoStart = game.GetPosition((LaunchEntity)fuelable);
        float fuelPercent = fltDistance/game.GetFuelableRange(fuelable.GetMaxFuel(), fltMaxRange);

        return GetPercentStringFromFraction(fuelPercent);
    }

    public static String GetShipyardOccupiedString(LaunchGame game, Shipyard shipyard)
    {
        if(shipyard.GetPortOnly())
        {
            if(shipyard.IsCaptured())
            {
                Player player = game.GetOwner(shipyard);

                return context.getString(R.string.port_occupied, player.GetName());
            }
            else if(shipyard.GetContested())
            {
                return context.getString(R.string.port_contested);
            }
            else
            {
                return context.getString(R.string.port_unoccupied);
            }
        }
        else
        {
            if(shipyard.IsCaptured())
            {
                Player player = game.GetOwner(shipyard);

                return context.getString(R.string.shipyard_occupied, player.GetName());
            }
            else if(shipyard.GetContested())
            {
                return context.getString(R.string.shipyard_contested);
            }
            else
            {
                return context.getString(R.string.shipyard_unoccupied);
            }
        }
    }

    public static String GetResourceTypeString(ResourceType type)
    {
        switch(type)
        {
            case OIL: return context.getString(R.string.resource_name_oil);
            case IRON: return context.getString(R.string.resource_name_iron);
            case COAL: return context.getString(R.string.resource_name_coal);
            case CROPS: return context.getString(R.string.resource_name_crops);
            case URANIUM: return context.getString(R.string.resource_name_uranium);
            case GOLD: return context.getString(R.string.resource_name_gold);
            case ELECTRICITY: return context.getString(R.string.resource_name_electricity);
            case WEALTH: return context.getString(R.string.resource_name_wealth);
            case CONCRETE: return context.getString(R.string.resource_name_stone);
            case LUMBER: return context.getString(R.string.resource_name_lumber);
            case FOOD: return context.getString(R.string.resource_name_food);
            case FUEL: return context.getString(R.string.resource_name_fuel);
            case STEEL: return context.getString(R.string.resource_name_steel);
            case CONSTRUCTION_SUPPLIES: return context.getString(R.string.resource_name_construction_supplies);
            case MACHINERY: return context.getString(R.string.resource_name_machinery);
            case ELECTRONICS: return context.getString(R.string.resource_name_electronics);
            case MEDICINE: return context.getString(R.string.resource_name_medicine);
            case ENRICHED_URANIUM: return context.getString(R.string.resource_name_enriched_uranium);
            default: return context.getString(R.string.resource_name_not_found);
        }
    }

    public static String GetResourceTypeTitleString(ResourceType type)
    {
        switch(type)
        {
            case OIL: return context.getString(R.string.resource_title_oil);
            case IRON: return context.getString(R.string.resource_title_iron);
            case COAL: return context.getString(R.string.resource_title_coal);
            case CROPS: return context.getString(R.string.resource_title_crops);
            case URANIUM: return context.getString(R.string.resource_title_uranium);
            case GOLD: return context.getString(R.string.resource_title_gold);
            case ELECTRICITY: return context.getString(R.string.resource_title_electricity);
            case WEALTH: return context.getString(R.string.resource_title_wealth);
            case CONCRETE: return context.getString(R.string.resource_title_stone);
            case LUMBER: return context.getString(R.string.resource_title_lumber);
            case FOOD: return context.getString(R.string.resource_title_food);
            case FUEL: return context.getString(R.string.resource_title_fuel);
            case STEEL: return context.getString(R.string.resource_title_steel);
            case CONSTRUCTION_SUPPLIES: return context.getString(R.string.resource_title_construction_supplies);
            case MACHINERY: return context.getString(R.string.resource_title_machinery);
            case ELECTRONICS: return context.getString(R.string.resource_title_electronics);
            case MEDICINE: return context.getString(R.string.resource_title_medicine);
            case ENRICHED_URANIUM: return context.getString(R.string.resource_title_enriched_uranium);
            default: return "Feet";
        }
    }

    public static String GetResourceDepositTitle(ResourceDeposit deposit)
    {
        switch(deposit.GetType())
        {
            case OIL: return context.getString(R.string.deposit_name_oil);
            case IRON: return context.getString(R.string.deposit_name_iron);
            case COAL: return context.getString(R.string.deposit_name_coal);
            case CROPS: return context.getString(R.string.deposit_name_crops);
            case URANIUM: return context.getString(R.string.deposit_name_uranium);
            case GOLD: return context.getString(R.string.deposit_name_gold);
            case CONCRETE: return context.getString(R.string.deposit_name_stone);
            case LUMBER: return context.getString(R.string.deposit_name_lumber);
            case ELECTRICITY: return context.getString(R.string.deposit_name_electricity);
            case STEEL: return context.getString(R.string.deposit_name_steel);
            case FUEL: return context.getString(R.string.deposit_name_fuel);
            case FOOD: return context.getString(R.string.deposit_name_food);
            case ELECTRONICS: return context.getString(R.string.deposit_name_electronics);
            case MACHINERY: return context.getString(R.string.deposit_name_machinery);
            case MEDICINE: return context.getString(R.string.deposit_name_medicine);
            case ENRICHED_URANIUM: return context.getString(R.string.deposit_name_enriched_uranium);
            default: return "UNKNOWN DEPOSIT TYPE";
        }
    }

    public static String GetProcessorTitle(ResourceType type)
    {
        switch(type)
        {
            case FOOD: return context.getString(R.string.title_granary);
            case STEEL: return context.getString(R.string.title_foundry);
            case FUEL: return context.getString(R.string.title_oil_refinery);
            case CONSTRUCTION_SUPPLIES: return context.getString(R.string.title_construction_yard);
            case ELECTRICITY: return context.getString(R.string.title_power_plant);
            case NUCLEAR_ELECTRICITY: return context.getString(R.string.title_nuclear_power_plant);
            case MACHINERY: return context.getString(R.string.title_machine_shop);
            case MEDICINE: return context.getString(R.string.title_laboratory);
            case ELECTRONICS: return context.getString(R.string.title_lithography_plant);
            case ENRICHED_URANIUM: return context.getString(R.string.title_enrichment_facility);
            case WEALTH: return context.getString(R.string.title_coin_mint);
            default: return "[UNKNOWN PROCESSOR TYPE FOR TITLE]";
        }
    }

    public static String GetProcessorName(ResourceType type)
    {
        switch(type)
        {
            case FOOD: return context.getString(R.string.construct_name_granary);
            case STEEL: return context.getString(R.string.construct_name_foundry);
            case FUEL: return context.getString(R.string.construct_name_oil_refinery);
            case CONSTRUCTION_SUPPLIES: return context.getString(R.string.construct_name_construction_yard);
            case ELECTRICITY: return context.getString(R.string.construct_name_power_plant);
            case NUCLEAR_ELECTRICITY: return context.getString(R.string.construct_name_nuclear_power_plant);
            case MACHINERY: return context.getString(R.string.construct_name_machine_shop);
            case MEDICINE: return context.getString(R.string.construct_name_laboratory);
            case ELECTRONICS: return context.getString(R.string.construct_name_lithography_plant);
            case ENRICHED_URANIUM: return context.getString(R.string.construct_name_enrichment_facility);
            case WEALTH: return context.getString(R.string.construct_name_coin_mint);
            default: return "[UNKNOWN PROCESSOR TYPE FOR NAME]";
        }
    }

    public static String GetDistributorTitle(ResourceType type)
    {
        switch(type)
        {
            case OIL: return context.getString(R.string.title_distributor_oil);
            case IRON: return context.getString(R.string.title_distributor_iron);
            case COAL: return context.getString(R.string.title_distributor_coal);
            case CROPS: return context.getString(R.string.title_distributor_crop);
            case URANIUM: return context.getString(R.string.title_distributor_uranium);
            case ELECTRICITY: return context.getString(R.string.title_distributor_electricity);
            case WEALTH: return context.getString(R.string.title_distributor_wealth);
            case CONCRETE: return context.getString(R.string.title_distributor_concrete);
            case LUMBER: return context.getString(R.string.title_distributor_lumber);
            case FOOD: return context.getString(R.string.title_distributor_food);
            case FUEL: return context.getString(R.string.title_distributor_fuel);
            case STEEL: return context.getString(R.string.title_distributor_steel);
            case CONSTRUCTION_SUPPLIES: return context.getString(R.string.title_distributor_construction_supplies);
            case MACHINERY: return context.getString(R.string.title_distributor_machinery);
            case ELECTRONICS: return context.getString(R.string.title_distributor_electronics);
            case MEDICINE: return context.getString(R.string.title_distributor_medicine);
            case ENRICHED_URANIUM: return context.getString(R.string.title_distributor_enriched_uranium);
            default: return "resource type not implemented!";
        }
    }

    public static String GetDistributorName(ResourceType type)
    {
        switch(type)
        {
            case OIL: return context.getString(R.string.construct_name_distributor_oil);
            case IRON: return context.getString(R.string.construct_name_distributor_iron);
            case COAL: return context.getString(R.string.construct_name_distributor_coal);
            case CROPS: return context.getString(R.string.construct_name_distributor_crop);
            case URANIUM: return context.getString(R.string.construct_name_distributor_uranium);
            case ELECTRICITY: return context.getString(R.string.construct_name_distributor_electricity);
            case CONCRETE: return context.getString(R.string.construct_name_distributor_concrete);
            case LUMBER: return context.getString(R.string.construct_name_distributor_lumber);
            case FOOD: return context.getString(R.string.construct_name_distributor_food);
            case FUEL: return context.getString(R.string.construct_name_distributor_fuel);
            case STEEL: return context.getString(R.string.construct_name_distributor_steel);
            case CONSTRUCTION_SUPPLIES: return context.getString(R.string.construct_name_distributor_construction_supplies);
            case MACHINERY: return context.getString(R.string.construct_name_distributor_machinery);
            case ELECTRONICS: return context.getString(R.string.construct_name_distributor_electronics);
            case MEDICINE: return context.getString(R.string.construct_name_distributor_medicine);
            case ENRICHED_URANIUM: return context.getString(R.string.construct_name_distributor_enriched_uranium);
            default: return "resource type not implemented!";
        }
    }

    public static String GetExtractorTitle(ResourceType type)
    {
        switch(type)
        {
            case OIL: return context.getString(R.string.oil_well_title);
            case IRON: return context.getString(R.string.iron_mine_title);
            case COAL: return context.getString(R.string.coal_mine_title);
            case CROPS: return context.getString(R.string.farm_title);
            case URANIUM: return context.getString(R.string.uranium_mine_title);
            case GOLD: return context.getString(R.string.gold_mine_title);
            case ELECTRICITY: return context.getString(R.string.power_recovery_unit_title);
            case CONCRETE: return context.getString(R.string.quarry_title);
            case LUMBER: return context.getString(R.string.lumber_mill_title);
            case STEEL: return context.getString(R.string.metal_recycling_team_title);
            case MACHINERY: return context.getString(R.string.industrial_salvage_unit_title);
            case FUEL: return context.getString(R.string.fuel_recovery_team_title);
            case FOOD: return context.getString(R.string.ration_recovery_unit_title);
            case ELECTRONICS: return context.getString(R.string.tech_salvage_unit_title);
            case MEDICINE: return context.getString(R.string.medical_recovery_unit_title);
            case ENRICHED_URANIUM: return context.getString(R.string.nuclear_recovery_unit_title);
            default: return "extractor type not implemented! [GetExtractorTitle]";
        }
    }

    public static String GetExtractorName(ResourceType type)
    {
        switch(type)
        {
            case OIL: return context.getString(R.string.construct_name_oil_well);
            case IRON: return context.getString(R.string.construct_name_iron_mine);
            case COAL: return context.getString(R.string.construct_name_coal_mine);
            case CROPS: return context.getString(R.string.construct_name_farm);
            case URANIUM: return context.getString(R.string.construct_name_uranium_mine);
            case GOLD: return context.getString(R.string.construct_name_gold_mine);
            case ELECTRICITY: return context.getString(R.string.construct_name_power_recovery_unit);
            case CONCRETE: return context.getString(R.string.construct_name_quarry);
            case LUMBER: return context.getString(R.string.construct_name_lumber_mill);
            case STEEL: return context.getString(R.string.construct_name_metal_recycling_team);
            case MACHINERY: return context.getString(R.string.construct_name_industrial_salvage_unit);
            case FUEL: return context.getString(R.string.construct_name_fuel_recovery_team);
            case FOOD: return context.getString(R.string.construct_name_ration_recovery_unit);
            case ELECTRONICS: return context.getString(R.string.construct_name_tech_salvage_unit);
            case MEDICINE: return context.getString(R.string.construct_name_medical_recovery_unit);
            case ENRICHED_URANIUM: return context.getString(R.string.construct_name_nuclear_recovery_unit);
            default: return "extractor type not implemented! [GetExtractorName]";
        }
    }

    public static String GetCostStatement(Map<ResourceType, Long> costs)
    {
        if(costs.isEmpty())
            return context.getString(R.string.free).toLowerCase();

        List<String> parts = new ArrayList<>();

        for(Map.Entry<ResourceType, Long> cost : costs.entrySet())
        {
            parts.add(GetResourceQuantityStringWithName(cost.getKey(), cost.getValue()));
        }

        if(parts.isEmpty())
        {
            return "";
        }
        else if(parts.size() == 1)
        {
            return parts.get(0);
        }
        else if(parts.size() == 2)
        {
            return parts.get(0) + " and " + parts.get(1);
        }
        else
        {
            String allButLast = String.join(", ", parts.subList(0, parts.size() - 1));
            String last = parts.get(parts.size() - 1);
            return allButLast + ", and " + last;
        }
    }

    public static String ConvertToThousandsWithK(long oThousands)
    {
        return oThousands/1000 + "k";
    }

    public static String GetTonsString(int lTonnage)
    {
        return context.getString(R.string.tonnes, lTonnage);
    }

    public static String GetResourceQuantityString(ResourceType type, long oQuantity)
    {
        switch(type)
        {
            case ELECTRICITY:
            {
                if(oQuantity >= 1000)
                {
                    return String.format("%.0f", (float)(oQuantity/1000)) + " " + context.getString(R.string.megawatt);
                }
                else
                    return oQuantity + " " + context.getString(R.string.kilowatt);
            }

            case WEALTH:
            {
                return GetCurrencyString(oQuantity);
            }

            default:
            {
                return GetWeightStringFromKG(oQuantity);
            }
        }
    }

    public static String GetResourceQuantityStringWithName(ResourceType type, long oQuantity)
    {
        return oQuantity + " " + GetResourceTypeString(type);
    }

    public static String GetLootContentString(Loot loot, LaunchGame game)
    {
        switch(loot.GetLootType())
        {
            case MISSILES:
            {
                MissileType type = game.GetConfig().GetMissileType(loot.GetCargoID());

                if(type != null)
                {
                    return "x" + loot.GetQuantity() + " " + type.GetName();
                }
            }
            break;

            case INTERCEPTORS:
            {
                InterceptorType type = game.GetConfig().GetInterceptorType(loot.GetCargoID());

                if(type != null)
                {
                    return "x" + loot.GetQuantity() + " " + type.GetName();
                }
            }
            break;

            case RESOURCES:
            {
                Resource resource = new Resource(ResourceType.values()[loot.GetCargoID()], loot.GetQuantity());
                return GetResourceQuantityString(resource.GetResourceType(), resource.GetQuantity()) + " " + GetResourceTypeString(resource.GetResourceType());
            }
        }

        return "[NOT IMPLEMENTED! (GetLootContentString)]";
    }
}
