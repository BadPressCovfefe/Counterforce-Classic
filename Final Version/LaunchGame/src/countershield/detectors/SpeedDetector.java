package countershield.detectors;

import countershield.Provider;
import countershield.model.ScoreEvent;
import launch.game.GeoCoord;
import launch.utilities.LaunchLog;

public final class SpeedDetector {
    private final double maxMpsGps;
    private final double maxMpsNetwork;

    private static final double MAX_SECONDS_BETWEEN_FIXES = 60 * 60; 
    private static final double MIN_JITTER_METERS = 0.5;            

    public SpeedDetector(double maxMpsGps, double maxMpsNetwork) {
        this.maxMpsGps = maxMpsGps;
        this.maxMpsNetwork = maxMpsNetwork;
    }

    public ScoreEvent check(long nowMs, int pid, GeoCoord prev, GeoCoord curr,
                            double seconds, Provider provider) {
        if (prev == null || curr == null) return null;

        if (seconds <= 0.0 || seconds > MAX_SECONDS_BETWEEN_FIXES) {
            /*LaunchLog.ConsoleMessage(String.format(
                "[SpeedDetector] SKIP pid=%d reason=bad_seconds seconds=%.2f prev=%s curr=%s",
                pid, seconds, String.valueOf(prev), String.valueOf(curr)
            ));*/
            return null;
        }

        double meters = prev.DistanceTo(curr);

        if (meters < MIN_JITTER_METERS) {
            /*LaunchLog.ConsoleMessage(String.format(
                "[SpeedDetector] OK pid=%d provider=%s meters=%.2f seconds=%.2f (jitter)",
                pid, provider, meters, seconds
            ));*/
            return null;
        }

        double mps = meters / seconds;
        double cap = (provider == Provider.GPS) ? maxMpsGps : maxMpsNetwork;

        if (mps > cap) {
            double over = (mps - cap) / cap;
            double score = 12.0 + Math.min(40.0, over * 60.0);
            ScoreEvent.Severity sev = (over > 1.0)
                    ? ScoreEvent.Severity.HIGH
                    : ScoreEvent.Severity.MEDIUM;

            /*LaunchLog.ConsoleMessage(String.format(
                "[SpeedDetector] VIOLATION pid=%d provider=%s meters=%.2f seconds=%.2f mps=%.2f cap=%.2f over=%.3f score=%.2f severity=%s prev=%s curr=%s",
                pid, provider, meters, seconds, mps, cap, over, score, sev, String.valueOf(prev), String.valueOf(curr)
            ));*/

            String msg = String.format("Speed %.1fm/s over cap %.1fm/s", mps, cap);
            return new ScoreEvent(nowMs, pid, "Speed", score, sev, msg);
        }

        /*LaunchLog.ConsoleMessage(String.format(
            "[SpeedDetector] OK pid=%d provider=%s meters=%.2f seconds=%.2f mps=%.2f cap=%.2f prev=%s curr=%s",
            pid, provider, meters, seconds, mps, cap, String.valueOf(prev), String.valueOf(curr)
        ));*/

        return null;
    }
}
