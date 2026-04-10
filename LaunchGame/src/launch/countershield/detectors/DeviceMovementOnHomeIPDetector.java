package launch.countershield.detectors;

import launch.countershield.model.ScoreEvent;
import launch.countershield.net.NetClassifier;
import launch.countershield.net.NetClassifier.IpProfile;
import launch.countershield.net.NetClassifier.IpKind;
import launch.game.GeoCoord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DeviceMovementOnHomeIPDetector
{
    private final NetClassifier net;
    private final double minMeters;
    private final double windowSec;

    private static final class Track
    {
        long startMs;
        GeoCoord start;
        String ip;
    }
    private final Map<Integer, Track> tracks = new ConcurrentHashMap<>();

    public DeviceMovementOnHomeIPDetector(NetClassifier net, double minMeters, double windowSec)
    {
        this.net = net;
        this.minMeters = minMeters;
        this.windowSec = windowSec;
    }

    public ScoreEvent check(long nowMs, int pid, GeoCoord curr, String ip)
    {
        IpProfile prof = net.note(pid, ip, nowMs);
        if(prof.kind != IpKind.HOME_LIKELY)
        {
            return null;
        }

        Track t = tracks.get(pid);
        
        if(t == null || !prof.ip.equals(t.ip) || (nowMs - t.startMs) / 1000.0 > windowSec)
        {
            t = new Track();
            t.startMs = nowMs;
            t.start = curr;
            t.ip = prof.ip;
            tracks.put(pid, t);
            return null;
        }
        
        double moved = t.start.DistanceTo(curr);
        
        if(moved >= minMeters)
        {
            double score = 22.0 + Math.min(40.0, (moved - minMeters) / 25.0);
            String msg = String.format("Moved %.0fm in ~%.0fs on HOME IP %s",
                    moved, (nowMs - t.startMs) / 1000.0, prof.ip);
            t.startMs = nowMs;
            t.start = curr;
            return new ScoreEvent(nowMs, pid, "HomeIPMovement", score, ScoreEvent.Severity.MEDIUM, msg);
        }
        
        return null;
    }
}
