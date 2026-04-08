package countershield.detectors;

import countershield.model.ScoreEvent;
import countershield.net.NetClassifier;
import countershield.net.NetClassifier.IpProfile;
import countershield.net.NetClassifier.IpKind;
import launch.game.GeoCoord;
import launch.utilities.LaunchLog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DeviceMovementOnHomeIPDetector {
    private final NetClassifier net;
    private final double minMeters; 
    private final double windowSec;   
    private final double jitterMeters; 

    private static final class Track {
        long startMs;
        GeoCoord start;
        String ip;
    }

    private final Map<Integer, Track> tracks = new ConcurrentHashMap<>();

    public DeviceMovementOnHomeIPDetector(NetClassifier net, double minMeters, double windowSec) {
        this(net, minMeters, windowSec, 0.8); // default jitter ~0.8m
    }

    public DeviceMovementOnHomeIPDetector(NetClassifier net, double minMeters, double windowSec, double jitterMeters) {
        this.net = net;
        this.minMeters = Math.max(0.0, minMeters);
        this.windowSec = Math.max(1.0, windowSec);
        this.jitterMeters = Math.max(0.0, jitterMeters);
    }

    /**
     * Flags device movement when the player is on a HOME_LIKELY IP and moved >= minMeters within windowSec.
     * Tracks state per player/IP; resets window on IP change or expiry.
     */
    public ScoreEvent check(long nowMs, int pid, GeoCoord curr, String ip) {
        if (curr == null) return null;

        IpProfile prof = net.note(pid, ip, nowMs);

        // Only evaluate movement when the current IP is classified as HOME_LIKELY
        if (prof.kind != IpKind.HOME_LIKELY) {
            // Optionally keep a rolling track so the first HOME window is ready to measure
            Track t = tracks.get(pid);
            if (t == null || t.ip == null || !prof.ip.equals(t.ip)) {
                Track nt = new Track();
                nt.startMs = nowMs;
                nt.start = curr;
                nt.ip = prof.ip;
                tracks.put(pid, nt);
            }
            /*LaunchLog.ConsoleMessage(String.format(
                "[HomeIPMove] SKIP pid=%d ip=%s kind=%s", pid, prof.ip, prof.kind));*/
            return null;
        }

        Track t = tracks.get(pid);
        boolean newWindow = false;

        if (t == null || t.ip == null || !prof.ip.equals(t.ip)) {
            t = new Track();
            t.startMs = nowMs;
            t.start = curr;
            t.ip = prof.ip;
            tracks.put(pid, t);
            newWindow = true;
        }

        double sinceStartSec = Math.max(0.0, (nowMs - t.startMs) / 1000.0);

        if (sinceStartSec > windowSec || nowMs < t.startMs) {
            t.startMs = nowMs;
            t.start = curr;
            sinceStartSec = 0.0;
            newWindow = true;
        }

        double moved = t.start.DistanceTo(curr);

        // Ignore tiny jitter
        if (moved < jitterMeters) {
            /*LaunchLog.ConsoleMessage(String.format(
                "[HomeIPMove] OK (jitter) pid=%d ip=%s moved=%.2fm win=%.0fs(thresh=%.0fs)",
                pid, prof.ip, moved, sinceStartSec, windowSec));*/
            return null;
        }

        // Not enough movement within the active window
        if (moved < minMeters) {
            /*LaunchLog.ConsoleMessage(String.format(
                "[HomeIPMove] OK pid=%d ip=%s moved=%.2fm<%.0fm win=%.0fs(thresh=%.0fs)%s",
                pid, prof.ip, moved, minMeters, sinceStartSec, windowSec, newWindow ? " (new window)" : ""));*/
            return null;
        }

        // Movement threshold met -> score and roll the window
        double over = moved - minMeters;
        double score = 22.0 + Math.min(40.0, over / 25.0);
        String msg = String.format("Moved %.0fm in ~%.0fs on HOME IP %s",
                                   moved, sinceStartSec, prof.ip);

        /*LaunchLog.ConsoleMessage(String.format(
            "[HomeIPMove] VIOLATION pid=%d ip=%s moved=%.1fm win=%.0fs score=%.2f",
            pid, prof.ip, moved, sinceStartSec, score));*/

        // Roll the window so subsequent movement is measured from here
        t.startMs = nowMs;
        t.start = curr;

        return new ScoreEvent(nowMs, pid, "HomeIPMovement", score, ScoreEvent.Severity.MEDIUM, msg);
    }
}
