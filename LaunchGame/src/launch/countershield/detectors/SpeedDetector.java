package launch.countershield.detectors;

import launch.countershield.Provider;
import launch.countershield.model.ScoreEvent;
import launch.game.GeoCoord;

public final class SpeedDetector
{
    private final double maxMpsGps;
    private final double maxMpsNetwork;

    public SpeedDetector(double maxMpsGps, double maxMpsNetwork)
    {
        this.maxMpsGps = maxMpsGps;
        this.maxMpsNetwork = maxMpsNetwork;
    }

    public ScoreEvent check(long nowMs, int pid, GeoCoord prev, GeoCoord curr, double seconds, Provider provider)
    {
        if(prev == null || curr == null || seconds <= 0.0)
        {
            return null;
        }
        double meters = prev.DistanceTo(curr);
        double mps = meters / seconds;
        double cap = (provider == Provider.GPS) ? maxMpsGps : maxMpsNetwork;

        if(mps > cap)
        {
            double over = (mps - cap) / cap;
            double score = 12.0 + Math.min(40.0, over * 60.0);
            String msg = String.format("Speed %.1fm/s over cap %.1fm/s", mps, cap);
            return new ScoreEvent(nowMs, pid, "Speed", score, over > 1.0 ? ScoreEvent.Severity.HIGH : ScoreEvent.Severity.MEDIUM, msg);
        }
        
        return null;
    }
}
