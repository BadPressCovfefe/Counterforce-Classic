/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package launch.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import launch.game.entities.Airplane;
import launch.game.entities.Loot;
import launch.game.entities.MapEntity;
import launch.game.entities.Missile;
import launch.game.entities.*;
import java.util.concurrent.*;
import launch.game.EntityPointer.EntityType;

/**
 * Drop‑in replacement for the original Quadtree backed by a sparse spatial hash grid.
 *
 * Why:
 *  - The game world is the real Earth (GPS). A deep tree over the whole globe is wasteful.
 *  - Entities move frequently; repeated remove/insert on a recursive tree is expensive.
 *  - A hash grid gives O(1) bucket updates and queries that scale with *local* density.
 *
 * Keep the public API used by the game code: InsertEntity, RemoveEntity, and the
 * GetAffectedX(center, radius) methods. You can keep using quadtree.Remove/Insert from
 * movement code and ProcessSentries without refactors.
 *
 * Notes:
 *  - Bucket size is in degrees. Default ~0.001° ≈ 111 m vertically at the equator.
 *    You can tune via ctor.
 *  - Radius arguments are interpreted as meters (float), matching the existing usage,
 *    and converted to degrees using the center latitude.
 *  - This class is thread-safe for concurrent reads/writes typical in your server:
 *    buckets live in a ConcurrentHashMap; each bucket uses synchronized lists.
 */
public class Quadtree
{

    // ---- Configuration -----------------------------------------------------
    private static final double DEFAULT_BUCKET_DEG = 0.1;

    // ---- Types --------------------------------------------------------------
    private static final class BucketKey
    {

        final int gx, gy;

        BucketKey(int gx, int gy)
        {
            this.gx = gx;
            this.gy = gy;
        }

        @Override
        public boolean equals(Object o)
        {
            if(this == o)
            {
                return true;
            }
            if(!(o instanceof BucketKey))
            {
                return false;
            }
            BucketKey k = (BucketKey) o;
            return gx == k.gx && gy == k.gy;
        }

        @Override
        public int hashCode()
        {
            return 31 * gx + gy;
        }
    }

    private static final class Bucket
    {
        final List<EntityPointer> structures = Collections.synchronizedList(new ArrayList<>());
        final List<EntityPointer> landUnits = Collections.synchronizedList(new ArrayList<>());
        final List<EntityPointer> aircrafts = Collections.synchronizedList(new ArrayList<>());
        final List<EntityPointer> missiles = Collections.synchronizedList(new ArrayList<>());
        final List<EntityPointer> torpedoes = Collections.synchronizedList(new ArrayList<>());
        final List<EntityPointer> naval = Collections.synchronizedList(new ArrayList<>());
        final List<EntityPointer> loots = Collections.synchronizedList(new ArrayList<>());

        boolean isEmpty()
        {
            return structures.isEmpty()
                    && landUnits.isEmpty()
                    && aircrafts.isEmpty()
                    && missiles.isEmpty()
                    && torpedoes.isEmpty()
                    && naval.isEmpty()
                    && loots.isEmpty();
        }
    }

    // ---- State --------------------------------------------------------------
    private final LaunchGame game;
    private final double bucketDeg; // grid step in degrees for both lat & lon

    // Sparse grid of buckets
    private final ConcurrentHashMap<BucketKey, Bucket> buckets = new ConcurrentHashMap<>();

    // Reverse index so RemoveEntity is O(1)
    private final ConcurrentHashMap<EntityPointer, BucketKey> reverse = new ConcurrentHashMap<>();

    // ---- Ctors --------------------------------------------------------------
    public Quadtree(LaunchGame game)
    {
        this(game, DEFAULT_BUCKET_DEG);
    }

    public Quadtree(LaunchGame game, double bucketDeg)
    {
        this.game = game;
        this.bucketDeg = Math.max(1e-6, bucketDeg);
    }

    // ---- Public API (drop-in) ----------------------------------------------
    public void InsertEntity(EntityPointer pointer)
    {
        MapEntity entity = pointer.GetMapEntity(game);
        
        if(entity == null)
        {
            return;
        }

        BucketKey key = KeyFor(entity.GetPosition());
        Bucket b = buckets.computeIfAbsent(key, k -> new Bucket());
        AddToTypedList(b, entity, pointer);
        reverse.put(pointer, key);
    }

    public void RemoveEntity(EntityPointer pointer)
    {
        BucketKey key = reverse.remove(pointer);
        if(key == null)
        {
            return;
        } // wasn't indexed

        Bucket b = buckets.get(key);
        if(b == null)
        {
            return;
        }

        MapEntity entity = pointer.GetMapEntity(game);
        if(entity != null)
        {
            RemoveFromTypedList(b, entity, pointer);
        }

        // Optional cleanup to keep the grid tidy
        if(b.isEmpty())
        {
            buckets.remove(key, b);
        }
    }

    // Optional helper to avoid remove/insert every tick: call this when an entity may have moved.
    public void UpdateEntityCell(EntityPointer pointer)
    {
        MapEntity entity = pointer.GetMapEntity(game);
        if(entity == null)
        {
            RemoveEntity(pointer);
            return;
        }

        BucketKey newKey = KeyFor(entity.GetPosition());
        BucketKey oldKey = reverse.get(pointer);
        if(oldKey != null && oldKey.equals(newKey))
        {
            return; // still in same cell
        }

        // moved across a cell boundary
        RemoveEntity(pointer);
        InsertEntity(pointer);
    }

    // ---- Query helpers -----------------------------------------------------
    public List<EntityPointer> GetAffectedLandUnits(GeoCoord center, float radiusKM)
    {
        return Query(center, radiusKM, b -> b.landUnits);
    }

    public List<EntityPointer> GetAffectedInfantries(GeoCoord center, float radiusKM)
    {
        List<EntityPointer> result = new ArrayList<>();
        for(EntityPointer pointer : Query(center, radiusKM, b -> b.landUnits))
        {
            if(pointer.GetType() == EntityType.INFANTRY)
            {
                result.add(pointer);
            }
        }
        return result;
    }

    public List<EntityPointer> GetAffectedDetectables(GeoCoord center, float radiusKM)
    {
        List<EntityPointer> result = new ArrayList<>();

        // Inline expansion of your 6 queries to avoid list creation/merging
        ForEachBucketOverlapping(center, radiusKM, b ->
        {
            synchronized(b.landUnits)
            {
                result.addAll(b.landUnits);
            }
            synchronized(b.missiles)
            {
                result.addAll(b.missiles);
            }
            synchronized(b.aircrafts)
            {
                result.addAll(b.aircrafts);
            }
            synchronized(b.naval)
            {
                result.addAll(b.naval);
            }
        });

        return result;
    }

    public List<EntityPointer> GetAffectedMissiles(GeoCoord center, float radiusKM)
    {
        return Query(center, radiusKM, b -> b.missiles);
    }

    public List<EntityPointer> GetAffectedAircrafts(GeoCoord center, float radiusKM)
    {
        return Query(center, radiusKM, b -> b.aircrafts);
    }

    public List<EntityPointer> GetAffectedStructures(GeoCoord center, float radiusKM)
    {
        return Query(center, radiusKM, b -> b.structures);
    }

    public List<EntityPointer> GetAffectedNavals(GeoCoord center, float radiusKM)
    {
        return Query(center, radiusKM, b -> b.naval);
    }

    public List<EntityPointer> GetAffectedLoots(GeoCoord center, float radiusKM)
    {
        return Query(center, radiusKM, b -> b.loots);
    }

    /**
     * All entities across all types overlapping the radius.
     */
    public List<EntityPointer> GetAllAffectedEntities(GeoCoord center, float radiusKM)
    {
        ArrayList<EntityPointer> out = new ArrayList<>();

        ForEachBucketOverlapping(center, radiusKM, b ->
        {
            synchronized(b.structures)
            {
                out.addAll(b.structures);
            }
            synchronized(b.landUnits)
            {
                out.addAll(b.landUnits);
            }
            synchronized(b.aircrafts)
            {
                out.addAll(b.aircrafts);
            }
            synchronized(b.missiles)
            {
                out.addAll(b.missiles);
            }
            synchronized(b.torpedoes)
            {
                out.addAll(b.torpedoes);
            }
            synchronized(b.naval)
            {
                out.addAll(b.naval);
            }
            synchronized(b.loots)
            {
                out.addAll(b.loots);
            }
        });

        return out;
    }

    // ---- Internals ---------------------------------------------------------
    private interface BucketListSelector
    {

        List<EntityPointer> list(Bucket b);
    }

    private List<EntityPointer> Query(GeoCoord center, float radiusKM, BucketListSelector sel)
    {
        ArrayList<EntityPointer> out = new ArrayList<>();

        ForEachBucketOverlapping(center, radiusKM, b ->
        {
            List<EntityPointer> src = sel.list(b);
            synchronized(src)
            {
                out.addAll(src);
            }
        });

        return out; // fresh copy
    }

    private void ForEachBucketOverlapping(GeoCoord center, float radiusKM, java.util.function.Consumer<Bucket> consumer)
    {
        float radiusMeters = radiusKM * Defs.METRES_PER_KM;

        // Convert meters to degrees span
        double latRad = Math.toRadians(center.GetLatitude());
        double latDegPerMeter = 1.0 / 111_320.0;
        double lonDegPerMeter = 1.0 / (111_320.0 * Math.max(0.0001, Math.cos(latRad)));

        double dLatDeg = radiusMeters * latDegPerMeter;
        double dLonDeg = radiusMeters * lonDegPerMeter;

        double minLat = center.GetLatitude() - dLatDeg;
        double maxLat = center.GetLatitude() + dLatDeg;
        double minLon = center.GetLongitude() - dLonDeg;
        double maxLon = center.GetLongitude() + dLonDeg;

        // Convert lat/lon bounds into bucket index ranges
        int gxMin = (int)Math.floor(minLon / bucketDeg);
        int gxMax = (int)Math.floor(maxLon / bucketDeg);
        int gyMin = (int)Math.floor(minLat / bucketDeg);
        int gyMax = (int)Math.floor(maxLat / bucketDeg);

        for(int gy = gyMin; gy <= gyMax; gy++)
        {
            for(int gx = gxMin; gx <= gxMax; gx++)
            {
                Bucket b = buckets.get(new BucketKey(gx, gy));
                if(b == null) 
                    continue;

                // Optional: fine filter with bounding-box vs. query circle if you want to skip more
                consumer.accept(b);
            }
        }
    }

    private void AddToTypedList(Bucket b, MapEntity e, EntityPointer p)
    {
        if(e instanceof Structure)
        {
            b.structures.add(p);
        }
        else
        {
            if(e instanceof LandUnit)
            {
                b.landUnits.add(p);
            }
            else
            {
                if(e instanceof Airplane)
                {
                    b.aircrafts.add(p);
                }
                else
                {
                    if(e instanceof Missile)
                    {
                        b.missiles.add(p);
                    }
                    else
                    {
                        if(e instanceof Torpedo)
                        {
                            b.torpedoes.add(p);
                        }
                        else
                        {
                            if(e instanceof NavalVessel)
                            {
                                b.naval.add(p);
                            }
                            else
                            {
                                if(e instanceof Loot)
                                {
                                    b.loots.add(p);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void RemoveFromTypedList(Bucket b, MapEntity e, EntityPointer p)
    {
        if(e instanceof Structure)
        {
            b.structures.remove(p);
        }
        else
        {
            if(e instanceof LandUnit)
            {
                b.landUnits.remove(p);
            }
            else
            {
                if(e instanceof Airplane)
                {
                    b.aircrafts.remove(p);
                }
                else
                {
                    if(e instanceof Missile)
                    {
                        b.missiles.remove(p);
                    }
                    else
                    {
                        if(e instanceof Torpedo)
                        {
                            b.torpedoes.remove(p);
                        }
                        else
                        {
                            if(e instanceof NavalVessel)
                            {
                                b.naval.remove(p);
                            }
                            else
                            {
                                if(e instanceof Loot)
                                {
                                    b.loots.remove(p);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private BucketKey KeyFor(GeoCoord pos)
    {
        return new BucketKey(GridX(pos.GetLongitude()), GridY(pos.GetLatitude()));
    }

    private int GridX(double lon)
    {
        // normalize to [-180, 180] before bucketing
        double nlon = NormalizeLon(lon);
        return (int) Math.floor(nlon / bucketDeg);
    }

    private int GridY(double lat)
    {
        // clamp to [-90,90]
        double nlat = Math.max(-90.0, Math.min(90.0, lat));
        return (int) Math.floor(nlat / bucketDeg);
    }

    private static double NormalizeLon(double lon)
    {
        double x = lon;
        while(x < -180.0)
        {
            x += 360.0;
        }
        while(x > 180.0)
        {
            x -= 360.0;
        }
        return x;
    }

    /**
     * Returns all entities of the specified type within radiusKM of the center
     * position. If sel is null, returns all entity types.
     */
    public List<EntityPointer> QueryRadius(GeoCoord center, float radiusKM, BucketListSelector sel)
    {
        ArrayList<EntityPointer> out = new ArrayList<>();

        ForEachBucketOverlapping(center, radiusKM, b ->
        {
            List<EntityPointer> src;

            if(sel != null)
            {
                src = sel.list(b);
            }
            else
            {
                // Combine all lists if no selector
                src = new ArrayList<>();
                synchronized(b.structures)
                {
                    src.addAll(b.structures);
                }
                synchronized(b.landUnits)
                {
                    src.addAll(b.landUnits);
                }
                synchronized(b.aircrafts)
                {
                    src.addAll(b.aircrafts);
                }
                synchronized(b.missiles)
                {
                    src.addAll(b.missiles);
                }
                synchronized(b.torpedoes)
                {
                    src.addAll(b.torpedoes);
                }
                synchronized(b.naval)
                {
                    src.addAll(b.naval);
                }
                synchronized(b.loots)
                {
                    src.addAll(b.loots);
                }
            }

            synchronized(src)
            {
                for(EntityPointer pointer : src)
                {
                    MapEntity entity = pointer.GetMapEntity(game);
                    if(entity != null
                            && entity.GetPosition().DistanceTo(center) <= radiusKM * Defs.METRES_PER_KM)
                    {
                        out.add(pointer);
                    }
                }
            }
        });

        return out;
    }

    /**
     * Returns all entities of the specified type within radiusKM of the center
     * position. If sel is null, returns all entity types.
     */
    public List<EntityPointer> QueryRadiusDamagables(GeoCoord center, float radiusKM)
    {
        ArrayList<EntityPointer> out = new ArrayList<>();

        ForEachBucketOverlapping(center, radiusKM, b ->
        {
            List<EntityPointer> src = new ArrayList<>();
            synchronized(b.structures)
            {
                src.addAll(b.structures);
            }
            synchronized(b.landUnits)
            {
                src.addAll(b.landUnits);
            }
            synchronized(b.naval)
            {
                src.addAll(b.naval);
            }

            synchronized(src)
            {
                for(EntityPointer pointer : src)
                {
                    MapEntity entity = pointer.GetMapEntity(game);
                    if(entity != null
                            && entity.GetPosition().DistanceTo(center) <= radiusKM * Defs.METRES_PER_KM)
                    {
                        out.add(pointer);
                    }
                }
            }
        });

        return out;
    }
}
