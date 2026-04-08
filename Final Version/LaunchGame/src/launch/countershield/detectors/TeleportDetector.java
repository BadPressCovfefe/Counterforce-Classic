package launch.countershield.detectors;

import launch.countershield.model.ScoreEvent;
import launch.game.GeoCoord;

/**
 * Flags "impossible jumps": large distance in a too-short time window. -
 * Primary rule: if seconds < minSeconds AND distance >= jumpMeters ⇒ HIGH
 * score. - Secondary rule: if average speed is absurd (e.g., > maxAvgMps),
 * score too.
 */
public final class TeleportDetector
{
    private final double jumpMeters;   // e.g., 1500 m
    private final double minSeconds;   // e.g., 3 s
    private final double maxAvgMps;    // safety net (e.g., 150 m/s ~= 540 km/h)

    public TeleportDetector(double jumpMeters, double minSeconds)
    {
        this(jumpMeters, minSeconds, 150.0);
    }

    public TeleportDetector(double jumpMeters, double minSeconds, double maxAvgMps)
    {
        this.jumpMeters = Math.max(1.0, jumpMeters);
        this.minSeconds = Math.max(0.001, minSeconds);
        this.maxAvgMps = Math.max(1.0, maxAvgMps);
    }

    /**
     * @return ScoreEvent or null if clean.
     */
    public ScoreEvent check(long nowMs, int pid, GeoCoord prev, GeoCoord curr, double seconds)
    {
        if(prev == null || curr == null)
        {
            return null;
        }
        double meters = prev.DistanceTo(curr);
        if(meters <= 0.0)
        {
            return null;
        }

        // Rule 1: big jump in too little time
        if(seconds < minSeconds && meters >= jumpMeters)
        {
            double over = (meters - jumpMeters) / Math.max(1.0, jumpMeters);
            double score = 25.0 + Math.min(50.0, over * 60.0); // 25..75
            String msg = String.format("Teleport: %.0fm in %.2fs (threshold %.0fm/%.2fs)",
                    meters, seconds, jumpMeters, minSeconds);
            return new ScoreEvent(nowMs, pid, "Teleport", score, ScoreEvent.Severity.HIGH, msg);
        }

        // Rule 2: absurd average speed even over longer windows
        if(seconds > 0.0)
        {
            double mps = meters / seconds;
            if(mps > maxAvgMps)
            {
                double over = (mps - maxAvgMps) / maxAvgMps;
                double score = 18.0 + Math.min(40.0, over * 50.0); // 18..58
                String msg = String.format("Teleport (avg speed): %.1fm/s over cap %.1fm/s; Δ=%.0fm/%.1fs",
                        mps, maxAvgMps, meters, seconds);
                return new ScoreEvent(nowMs, pid, "Teleport", score,
                        over > 1.0 ? ScoreEvent.Severity.HIGH : ScoreEvent.Severity.MEDIUM, msg);
            }
        }
        return null;
    }
}
