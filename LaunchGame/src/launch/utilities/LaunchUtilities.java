/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package launch.utilities;

import launch.game.entities.conceptuals.StoredAirplane;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.*;
import launch.game.entities.conceptuals.Resource;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.game.systems.CargoSystem.LootType;

/**
 *
 * @author tobster
 */
public class LaunchUtilities
{
    private static final String SANCTIFIED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static Random random = new Random();

    public static boolean NEUTRAL_VISIBLE = true;
    public static boolean FRIENDLY_VISIBLE = true;
    public static boolean ENEMY_VISIBLE = true;

    public static boolean PLAYERS_VISIBLE = true;
    public static boolean DEAD_PLAYERS_VISIBLE = false;
    public static boolean MISSILE_SITES_VISIBLE = true;
    public static boolean SAM_SITES_VISIBLE = true;
    public static boolean MISSILES_VISIBLE = true;
    public static boolean INTERCEPTORS_VISIBLE = true;
    public static boolean TORPEDOES_VISIBLE = true;
    public static boolean SENTRY_GUNS_VISIBLE = true;
    public static boolean ORE_MINES_VISIBLE = true;
    public static boolean RADAR_STATIONS_VISIBLE = true;
    public static boolean SOLAR_PANELS_VISIBLE = true;
    public static boolean FARMS_VISIBLE = true;
    public static boolean COMMAND_POSTS_VISIBLE = true;
    public static boolean AIRBASES_VISIBLE = true;
    public static boolean BARRACKS_VISIBLE = true;
    public static boolean BANKS_VISIBLE = true;
    public static boolean MISSILE_FACTORYS_VISIBLE = true;
    public static boolean WAREHOUSES_VISIBLE = true;
    public static boolean AIRCRAFTS_VISIBLE = true;
    public static boolean LOOTS_VISIBLE = true;
    public static boolean RADIATIONS_VISIBLE = true;
    public static boolean CHEMICALS_VISIBLE = true;
    public static boolean BLUEPRINTS_VISIBLE = true;
    public static boolean INFANTRIES_VISIBLE = true;
    public static boolean TANKS_VISIBLE = true;
    public static boolean SPAAGS_VISIBLE = true;
    public static boolean CITIES_VISIBLE = true;
    public static boolean SHIPYARDS_VISIBLE = true;
    public static boolean MARKETS_VISIBLE = true;
    public static boolean CARGO_TRUCKS_VISIBLE = true;
    public static boolean DEPOSITS_VISIBLE = true;
    public static boolean PROCESSORS_VISIBLE = true;
    public static boolean SHIPS_VISIBLE = true;
    public static boolean ARTILLERY_GUNS_VISIBLE = true;
    public static boolean SCRAP_YARDS_VISIBLE = true;
    public static boolean DISTRIBUTORS_VISIBLE = true;
    public static boolean RUBBLES_VISIBLE = true;
    public static boolean ONLINE_VISIBLE = true;
    public static boolean BOOTING_VISIBLE = true;
    public static boolean OFFLINE_VISIBLE = true;
    public static boolean ARMORIES_VISIBLE = true;
    public static boolean SUBMARINES_VISIBLE = true;
    public static boolean PORTS_VISIBLE = true;
    public static boolean MONUMENTS_VISIBLE = true;
    public static boolean IRON_DEPOSITS_VISIBLE = true;
    public static boolean COAL_DEPOSITS_VISIBLE = true;
    public static boolean OIL_DEPOSITS_VISIBLE = true;
    public static boolean CROP_DEPOSITS_VISIBLE = true;
    public static boolean CONCRETE_DEPOSITS_VISIBLE = true;
    public static boolean URANIUM_DEPOSITS_VISIBLE = true;
    public static boolean GOLD_DEPOSITS_VISIBLE = true;
    public static boolean ELECTRICITY_DEPOSITS_VISIBLE = true;
    public static boolean LUMBER_DEPOSITS_VISIBLE = true;
    public static boolean FERTILIZER_DEPOSITS_VISIBLE = true;
    public static boolean IRON_LOOT_VISIBLE = true;
    public static boolean COAL_LOOT_VISIBLE = true;
    public static boolean OIL_LOOT_VISIBLE = true;
    public static boolean CROP_LOOT_VISIBLE = true;
    public static boolean URANIUM_LOOT_VISIBLE = true;
    public static boolean GOLD_LOOT_VISIBLE = true;
    public static boolean LUMBER_LOOT_VISIBLE = true;
    public static boolean FERTILIZER_LOOT_VISIBLE = true;
    public static boolean WEALTH_LOOT_VISIBLE = true;
    public static boolean ELECTRICITY_LOOT_VISIBLE = true;
    public static boolean CONCRETE_LOOT_VISIBLE = true;
    public static boolean FUEL_LOOT_VISIBLE = true;
    public static boolean FOOD_LOOT_VISIBLE = true;
    public static boolean STEEL_LOOT_VISIBLE = true;
    public static boolean CONSTRUCTION_SUPPLIES_LOOT_VISIBLE = true;
    public static boolean EXPLOSIVES_LOOT_VISIBLE = true;
    public static boolean NERVE_AGENT_LOOT_VISIBLE = true;
    public static boolean MACHINERY_LOOT_VISIBLE = true;
    public static boolean ELECTRONICS_LOOT_VISIBLE = true;
    public static boolean MEDICINE_LOOT_VISIBLE = true;
    public static boolean FISSILE_LOOT_VISIBLE = true;
    public static boolean LABOR_LOOT_VISIBLE = true;
    public static boolean KNOWLEDGE_LOOT_VISIBLE = true;
    public static boolean ANTIMATTER_LOOT_VISIBLE = true;
    public static boolean HELICOPTERS_VISIBLE = true;
    
    //public static List<String> BadWords = LoadBadWords();
    
    private static final int STRING_LENGTH_PREFIX_SIZE = 2;
    
    public static int GetStringDataSize(String str)
    {
        return STRING_LENGTH_PREFIX_SIZE + str.getBytes().length;
    }
    
    public static byte[] GetStringData(String str)
    {
        ByteBuffer bb = ByteBuffer.allocate(GetStringDataSize(str));
        byte[] cString = str.getBytes();
        bb.putShort((short)cString.length);
        bb.put(cString);
        return bb.array();
    }
    
    public static String StringFromData(ByteBuffer bb)
    {
        byte[] cString = new byte[bb.getShort()];
        bb.get(cString, 0, cString.length);
        return new String(cString);
    }
    
    public static List<Integer> IntListFromData(ByteBuffer bb)
    {
        List<Integer> Result = new ArrayList();
        
        short nCount = bb.getShort();

        for(int i = 0; i < nCount; i++)
        {
            Result.add(bb.getInt());
        }
        
        return Result;
    }
    
    public static byte[] GetIntListData(List<Integer> IntList)
    {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES + (IntList.size() * Integer.BYTES));
        bb.putShort((short)IntList.size());
        
        for(Integer integer : IntList)
        {
            bb.putInt(integer);
        }
        
        return bb.array();
    }
    
    public static List<Byte> ByteListFromData(ByteBuffer bb)
    {
        List<Byte> Result = new ArrayList();
        
        short nCount = bb.getShort();

        for(int i = 0; i < nCount; i++)
        {
            Result.add(bb.get());
        }
        
        return Result;
    }
    
    public static byte[] GetByteListData(List<Byte> byteList)
    {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES + (byteList.size() * Byte.BYTES));
        bb.putShort((short)byteList.size());
        
        for(Byte cByte : byteList)
        {
            bb.put(cByte);
        }
        
        return bb.array();
    }
    
    // ===== AIRPLANES =====

    public static Map<Integer, StoredAirplane> StoredAircraftListFromData(ByteBuffer bb)
    {
        Map<Integer, StoredAirplane> Result = new ConcurrentHashMap<>();

        try
        {
            // Read unsigned short
            int nCount = bb.getShort() & 0xFFFF;

            for (int i = 0; i < nCount; i++)
            {
                StoredAirplane aircraft = new StoredAirplane(bb);
                Result.put(aircraft.GetID(), aircraft);
            }
        }
        catch(Exception ex)
        {
            throw new RuntimeException();
        }
        
        return Result;
    }

    public static byte[] BBFromStoredAircraft(Collection<StoredAirplane> StoredAircrafts)
    {
        // Compute total payload size once, as int
        int totalSize = GetStoredAircraftListDataSize(StoredAircrafts);
        ByteBuffer bb = ByteBuffer.allocate(totalSize);

        // Write count as unsigned short (validate first)
        int count = StoredAircrafts.size();
        if (count > 0xFFFF) {
            throw new IllegalStateException("Too many airplanes: " + count);
        }
        bb.putShort((short) (count & 0xFFFF));

        for (StoredAirplane aircraft : StoredAircrafts)
        {
            // Assumes GetData returns a complete, self-contained blob
            byte[] data = aircraft.GetData(aircraft.GetOwnerID());
            bb.put(data);
        }
        return bb.array();
    }

    // IMPORTANT: return int, not short
    public static int GetStoredAircraftListDataSize(Collection<StoredAirplane> StoredAircrafts)
    {
        int total = 0;

        // 2 bytes for the unsigned short count prefix
        total = Math.addExact(total, Short.BYTES);

        // Sum item sizes safely
        for (StoredAirplane aircraft : StoredAircrafts)
        {
            byte[] data = aircraft.GetData(aircraft.GetOwnerID());
            if (data == null) {
                throw new IllegalStateException("StoredAirplane GetData() returned null");
            }
            total = Math.addExact(total, data.length);
        }

        if (total < 0) {
            throw new IllegalStateException("Airplane list total size went negative (overflow)");
        }
        return total;
    }
    
    public static List<GeoCoord> GeoCoordListFromData(ByteBuffer bb)
    {
        List<GeoCoord> Result = new ArrayList<>();
        
        short nCount = bb.getShort();

        for(int i = 0; i < nCount; i++)
        {
            GeoCoord coord = new GeoCoord(bb.getFloat(), bb.getFloat());
            Result.add(coord);
        }
        
        return Result;
    }
    
    public static byte[] BBFromGeoCoords(List<GeoCoord> GeoCoords)
    {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES + (GeoCoords.size() * 8)); //* 8 because 1 geocoord is made of 2 doubles, which are both worth 4 bytes.)
        bb.putShort((short)GeoCoords.size());
        
        for(GeoCoord coord : GeoCoords)
        {
            bb.putFloat(coord.GetLatitude());
            bb.putFloat(coord.GetLongitude());
        }
        
        return bb.array();
    }
    
    public static Map<Integer, GeoCoord> GeoCoordMapFromData(ByteBuffer bb)
    {
        Map<Integer, GeoCoord> Result = new HashMap<>();
        
        short nCount = bb.getShort();

        for(int i = 0; i < nCount; i++)
        {
            int lOrder = bb.getInt();
            GeoCoord coord = new GeoCoord(bb.getFloat(), bb.getFloat());
            Result.put(lOrder, coord);
        }
        
        return Result;
    }
    
    public static byte[] BBFromGeoCoords(Map<Integer, GeoCoord> GeoCoords)
    {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES + (GeoCoords.size() * 12));
        bb.putShort((short)GeoCoords.size());
        
        for(Entry<Integer, GeoCoord> coordinate : GeoCoords.entrySet())
        {
            bb.putInt(coordinate.getKey());
            bb.putFloat(coordinate.getValue().GetLatitude());
            bb.putFloat(coordinate.getValue().GetLongitude());
        }
        
        return bb.array();
    }
    
    public static List<EntityPointer> PointerListFromData(ByteBuffer bb)
    {
        List<EntityPointer> Result = new ArrayList<>();
        
        short nCount = bb.getShort();

        for(int i = 0; i < nCount; i++)
        {
            EntityPointer pointer = new EntityPointer(bb);
            Result.add(pointer);
        }
        
        return Result;
    }
    
    public static byte[] BBFromPointers(List<EntityPointer> Pointers)
    {
        ByteBuffer bb = ByteBuffer.allocate(Short.BYTES + (Pointers.size() * 5));
        bb.putShort((short)Pointers.size());
        
        for(EntityPointer pointer : Pointers)
        {
            bb.put(pointer.GetData());
        }
        
        return bb.array();
    }

    public static long GetNextHour()
    {
        //Determine the ms until the next hour.
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        
        return cal.getTimeInMillis() - System.currentTimeMillis();
    }

    public static boolean GetEntityVisibility(LaunchClientGame game, MapEntity entity)
    {
        boolean bEntityTypeVisible = false; //"bEntityTypeVisible" is only for entities that can be considered friend/foe etc. 
      
        if(entity instanceof Player)
        {
            Player player = (Player)entity;
            
            if(player.Functioning())
                bEntityTypeVisible = PLAYERS_VISIBLE;
            else 
                bEntityTypeVisible = DEAD_PLAYERS_VISIBLE;
        }
        else if(entity instanceof KOTH)
            return true;
        else if(entity instanceof MissileSite)
            bEntityTypeVisible = MISSILE_SITES_VISIBLE;
        else if(entity instanceof SAMSite)
            bEntityTypeVisible = SAM_SITES_VISIBLE;
        else if(entity instanceof SentryGun)
            bEntityTypeVisible = SENTRY_GUNS_VISIBLE;
        else if(entity instanceof OreMine)
            bEntityTypeVisible = ORE_MINES_VISIBLE;
        else if(entity instanceof RadarStation)
            bEntityTypeVisible = RADAR_STATIONS_VISIBLE;
        else if(entity instanceof CommandPost)
            bEntityTypeVisible = COMMAND_POSTS_VISIBLE;
        else if (entity instanceof Airbase)
            bEntityTypeVisible = AIRBASES_VISIBLE;
        else if (entity instanceof Armory)
            bEntityTypeVisible = ARMORIES_VISIBLE;
        else if (entity instanceof Bank)
            bEntityTypeVisible = BANKS_VISIBLE;
        else if (entity instanceof ArtilleryGun)
            bEntityTypeVisible = ARTILLERY_GUNS_VISIBLE;
        else if (entity instanceof Warehouse)
            bEntityTypeVisible = WAREHOUSES_VISIBLE;
        else if (entity instanceof MissileFactory)
            bEntityTypeVisible = MISSILE_FACTORYS_VISIBLE;
        else if (entity instanceof Airplane)
            bEntityTypeVisible = AIRCRAFTS_VISIBLE;
        else if(entity instanceof Missile)
            bEntityTypeVisible = MISSILES_VISIBLE;
        else if(entity instanceof Interceptor)
            bEntityTypeVisible = INTERCEPTORS_VISIBLE;
        else if(entity instanceof Torpedo)
            bEntityTypeVisible = TORPEDOES_VISIBLE;
        else if(entity instanceof Blueprint)
            bEntityTypeVisible = BLUEPRINTS_VISIBLE;
        else if(entity instanceof Infantry)
            bEntityTypeVisible = INFANTRIES_VISIBLE;
        else if(entity instanceof Tank)
        {
            Tank tank = (Tank)entity;
            
            if(tank.IsAnMBT() || tank.IsArtillery() || tank.IsMissiles() || tank.IsInterceptors())
                bEntityTypeVisible = TANKS_VISIBLE;
            else if(tank.IsASPAAG())
                bEntityTypeVisible = SPAAGS_VISIBLE;
        }
        else if(entity instanceof Shipyard)
            bEntityTypeVisible = SHIPYARDS_VISIBLE;
        else if(entity instanceof ScrapYard)
            bEntityTypeVisible = SCRAP_YARDS_VISIBLE;
        else if(entity instanceof Ship)
            bEntityTypeVisible = SHIPS_VISIBLE;
        else if(entity instanceof Submarine)
            bEntityTypeVisible = SUBMARINES_VISIBLE;
        else if(entity instanceof CargoTruck)
            bEntityTypeVisible = CARGO_TRUCKS_VISIBLE;
        else if(entity instanceof ResourceDeposit)
            return DEPOSITS_VISIBLE;
        else if(entity instanceof Processor)
            bEntityTypeVisible = PROCESSORS_VISIBLE;
        else if(entity instanceof Distributor)
            bEntityTypeVisible = DISTRIBUTORS_VISIBLE;
        else if(entity instanceof Radiation)
            return RADIATIONS_VISIBLE;
        else if(entity instanceof Rubble)
            return RUBBLES_VISIBLE;
        else if(entity instanceof Loot)
        {
            Loot loot = (Loot)entity;
            
            if(LOOTS_VISIBLE)
            {
                if(loot.GetLootType() == LootType.RESOURCES)
                {
                    ResourceType type = ResourceType.values()[loot.GetCargoID()];

                    switch(type)
                    {
                        case IRON: return IRON_LOOT_VISIBLE;
                        case COAL: return COAL_LOOT_VISIBLE;
                        case OIL: return OIL_LOOT_VISIBLE;
                        case CROPS: return CROP_LOOT_VISIBLE;
                        case URANIUM: return URANIUM_LOOT_VISIBLE;
                        case GOLD: return GOLD_LOOT_VISIBLE;
                        case LUMBER: return LUMBER_LOOT_VISIBLE;
                        case WEALTH: return WEALTH_LOOT_VISIBLE;
                        case NUCLEAR_ELECTRICITY:
                        case ELECTRICITY: return ELECTRICITY_LOOT_VISIBLE;
                        case CONCRETE: return CONCRETE_LOOT_VISIBLE;
                        case FUEL: return FUEL_LOOT_VISIBLE;
                        case FOOD: return FOOD_LOOT_VISIBLE;
                        case STEEL: return STEEL_LOOT_VISIBLE;
                        case CONSTRUCTION_SUPPLIES: return CONSTRUCTION_SUPPLIES_LOOT_VISIBLE;
                        case MACHINERY: return MACHINERY_LOOT_VISIBLE;
                        case ELECTRONICS: return ELECTRONICS_LOOT_VISIBLE;
                        case MEDICINE: return MEDICINE_LOOT_VISIBLE;
                        case ENRICHED_URANIUM: return FISSILE_LOOT_VISIBLE;
                        default: return true;
                    }
                }
            }
        }
        else if(entity instanceof Airdrop)
            return true;

        if(entity instanceof Structure)
        {
            Structure structure = (Structure)entity;
            
            if(structure.GetOnline())
                bEntityTypeVisible = ONLINE_VISIBLE;
            else if(structure.GetBooting())
                bEntityTypeVisible = BOOTING_VISIBLE;
            else
                bEntityTypeVisible = OFFLINE_VISIBLE;
        }
        
        if(bEntityTypeVisible)
        {
            switch(game.GetAllegiance(game.GetOurPlayer(), entity))
            {
                case YOU:
                case AFFILIATE:
                case ALLY:
                case PENDING_TREATY:
                {
                    return FRIENDLY_VISIBLE;
                }

                case ENEMY: return ENEMY_VISIBLE;

                case UNAFFILIATED:
                case NEUTRAL: return NEUTRAL_VISIBLE;
            }
        }
        else
        {
            return false;
        }

        //Everything else is visible.
        return true;
        
    }
    
    /**
     * Returns a string of the format [Three random Latin letters and numbers].
     * @return A sanctified string.
     */
    public static String GetRandomSanctifiedString()
    {
        return "[" +
        SANCTIFIED_CHARACTERS.charAt(random.nextInt(SANCTIFIED_CHARACTERS.length())) +
        SANCTIFIED_CHARACTERS.charAt(random.nextInt(SANCTIFIED_CHARACTERS.length())) +
        SANCTIFIED_CHARACTERS.charAt(random.nextInt(SANCTIFIED_CHARACTERS.length())) +
        "] ";
    }
    
    /**
     * Remove dangerous characters and wicked phrases from text.
     * @param strText Text to clean if applicable.
     * @param bDangerous Remove dangerous characters. Note: Alliance descriptions should be cut slack on this to allow URLs, unless this feature is separated out in future.
     * @param bProfane Remove profane terms.
     * @return A clean text string.
     */
    public static String SanitiseText(String strText, boolean bDangerous, boolean bProfane)
    {
        //Dangerous.
        if(bDangerous)
        {
            strText = strText.replace("\\", "|");
            strText = strText.replace("/", "|");
            strText = strText.replace("\"", "|");
            strText = strText.replace("@", "|");
        }
        
        //Profane.
        if(bProfane)
        {
            strText = strText.replaceAll("(?i)adolf", "*****");
            strText = strText.replaceAll("(?i)hitler", "******");
            strText = strText.replaceAll("(?i)fuck", "****");
            strText = strText.replaceAll("(?i)phone", "*****");
            strText = strText.replaceAll("(?i)address", "*******");
            strText = strText.replaceAll("(?i)adress", "******");
            strText = strText.replaceAll("(?i)vagina", "******");
            strText = strText.replaceAll("(?i)asshole", "*******");
            strText = strText.replaceAll("(?i)ass hole", "********");
            strText = strText.replaceAll("(?i)how old", "*******");
            strText = strText.replaceAll("(?i)@", "");
        }
        
        return strText;
    }
    
    /**
    public static boolean 
     * Ensures a name is greppable with Latin alphanumeric + numeric characters. Forces it if not. While we're here, deal with some other unwanted name artefacts.
     * @param strName The player or alliance name to bless.
     * @return A blessed, greppable name.
     */
    public static String BlessName(String strName)
    {
        strName = strName.trim();
        
        if(strName.length() > Defs.MAX_PLAYER_NAME_LENGTH)
            strName = GetRandomSanctifiedString() + "My name was too long";
        else
        {
            strName = SanitiseText(strName, true, true);

            if(!strName.matches("^.*[a-zA-Z0-9][a-zA-Z0-9][a-zA-Z0-9].*$"))
            {
                strName = GetRandomSanctifiedString() + strName;
            }
        }
        
        return strName;
    }
    
    public static boolean DaylightAtLocation(GeoCoord geoPosition)
    {
        float timeZoneOffset = geoPosition.GetLongitude()/15;
        String strTimeZoneID = "GMT" + (timeZoneOffset < 0 ? String.valueOf(timeZoneOffset) : "+" + String.valueOf(timeZoneOffset));
        int lCurrentHour = Calendar.getInstance(TimeZone.getTimeZone(strTimeZoneID)).get(Calendar.HOUR_OF_DAY);
        
        return lCurrentHour >= 6 && lCurrentHour < 19;
    }
    
    public static int GetRandomIntInBounds(int from, int to) 
    {
        Random rand = new Random();
        int lResult = (int)(rand.nextFloat() * (to - from) + from);
        return lResult;
    }
    
    public static float GetRandomFloatInBounds(float from, float to) 
    {
        Random rand = new Random();
        return rand.nextFloat() * (to - from) + from;
    }
    
    public static String GetTimeAmount(long oTimespan)
    {
        long oDays = oTimespan / Defs.MS_PER_DAY;
        long oHours = (oTimespan % Defs.MS_PER_DAY) / Defs.MS_PER_HOUR;
        long oMinutes = ((oTimespan % Defs.MS_PER_DAY) % Defs.MS_PER_HOUR) / Defs.MS_PER_MIN;
        long oSeconds = (((oTimespan % Defs.MS_PER_DAY) % Defs.MS_PER_HOUR) % Defs.MS_PER_MIN) / Defs.MS_PER_SEC;

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
    
    /**
     * Ensure that a unit/structure name is appropriate.
     * @param strName
     * @return 
     */
    public static boolean ValidateUnitName(String strName)
    {
        //Make sure the name meets the minimum character count and max character count.
        //Remove all periods, spaces, commas, apostraphes, paranthesis, hyphens, brackets, squigly brackets, question marks, dashes, and slashes. 
        //See if the remaining text contains any choice words.
        return true;
    }
    
    public static boolean TextIsClean(String strText)
    {
        //We need a list of bad words loaded. Once loaded, 
        //remove all underscores and punctuation from strText, 
        //then check each word on the list to see if strText 
        //contains it.
        
        return true;
    }
    
    public static int GetSmallestIntInSet(Set<Integer> Set)
    {
        int lSmallestKey = Integer.MAX_VALUE;

        for(int l : Set)
        {
            lSmallestKey = Math.min(lSmallestKey, l);
        }

        return lSmallestKey;
    }
    
    public static int GetLargestIntInSet(Set<Integer> Set)
    {
        int lLargestKey = Integer.MIN_VALUE;

        for(int l : Set)
        {
            lLargestKey = Math.max(lLargestKey, l);
        }

        return lLargestKey;
    }
    
    public static float GetTotalTravelDistance(GeoCoord geoStart, Map<Integer, GeoCoord> Coordinates)
    {
        if(geoStart != null && Coordinates != null && !Coordinates.isEmpty())
        {
            float fltDistance = 0;
            
            //start i with the smallest int in the coordinate map. The integer key is the order of the coordinate. Every time a movable hits the next coordinate, the map changes size as one entry is removed, but the integer keys stay the same. Therefore, we cannot handle the map by the mere size.
            for(int i = LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()); i <= LaunchUtilities.GetLargestIntInSet(Coordinates.keySet()); i++)
            {
                GeoCoord geoCoord = Coordinates.get(i);
                
                if(geoCoord != null)
                {
                    //If we are handling the "first" entry in the map, measure distance from geoStart. Otherwise, measure the distances between each coordinate in the map.
                    if(i > LaunchUtilities.GetSmallestIntInSet(Coordinates.keySet()))
                    {
                        GeoCoord geoLast = Coordinates.get(i - 1);
                        
                        if(geoLast != null)
                        {
                            fltDistance += geoCoord.DistanceTo(geoLast);
                        }
                    }
                    else
                    {
                        fltDistance += geoStart.DistanceTo(geoCoord);
                    }
                }
            }
            
            return fltDistance;
        }
        
        return 0;
    }
    
    public static void AddResourceMapsTogether(Map<ResourceType, Long> MainMap, Map<ResourceType, Long> MapToAdd)
    {
        if(MainMap != null && MapToAdd != null)
        {
            for(Entry<ResourceType, Long> entry : MapToAdd.entrySet())
            {
                if(MainMap.containsKey(entry.getKey()))
                {
                    MainMap.put(entry.getKey(), MainMap.get(entry.getKey()) + entry.getValue());
                }
                else
                {
                    if(entry.getValue() > 0)
                        MainMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    public static Map<ResourceType, Long> ScaleResourceMap(Map<ResourceType, Long> map, float fltScaler)
    {
        if(map == null || map.isEmpty())
        {
            return Collections.emptyMap();
        }

        Map<ResourceType, Long> scaled = new HashMap<>(map.size());
        
        for(Entry<ResourceType, Long> entry : map.entrySet())
        {
            long oOldValue = entry.getValue();
            scaled.put(entry.getKey(), (long)(oOldValue * fltScaler));
        }

        return scaled;
    }
    
    public static String GetCostStatement(Map<ResourceType, Long> costs)
    {
        List<String> parts = new ArrayList<>();

        for(Map.Entry<ResourceType, Long> cost : costs.entrySet())
        {
            parts.add(cost.getValue() + " " + Resource.GetTypeName(cost.getKey()));
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
    
    public static int GetTotalResourceCost(Map<ResourceType, Long> costs)
    {
        int lCost = 0;
        
        for(Map.Entry<ResourceType, Long> cost : costs.entrySet())
        {
            lCost += cost.getValue();
        }
        
        return lCost;
    }
    
    public static byte[] GetResourceData(Map<ResourceType, Long> resources)
    {
        ByteBuffer bb = ByteBuffer.allocate(Byte.BYTES + (resources.size() * Resource.DATA_SIZE));
        
        bb.put((byte)resources.size()); //ResourceTypes are counted as bytes, so there can only be 126 different types of resources. 
        
        for(Entry<ResourceType, Long> entry : resources.entrySet())
        {
            bb.put((byte)entry.getKey().ordinal());
            bb.putLong(entry.getValue());
        }
        
        return bb.array();
    }
    
    public static Map<ResourceType, Long> ResourcesFromData(ByteBuffer bb)
    {
        Map<ResourceType, Long> result = new ConcurrentHashMap<>();
        
        byte cResourceCount = bb.get();
        
        for(int i = 0; i < cResourceCount; i++)
        {
            result.put(ResourceType.values()[bb.get()], bb.getLong());
        }
        
        return result;
    }
}
