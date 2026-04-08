package launch.countershield.detectors;

import launch.countershield.model.ScoreEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CommandRateDetector
{
    private final int burst;
    private final long windowMs;
    private final Map<String, Integer> counts = new ConcurrentHashMap<>();

    public CommandRateDetector(int burst, long windowMs)
    {
        this.burst = burst;
        this.windowMs = windowMs;
    }

    public ScoreEvent check(long nowMs, int pid, String cmd)
    {
        String key = pid + "|" + cmd + "|" + (nowMs / windowMs);
        int n = counts.merge(key, 1, Integer::sum);
        if(n > burst)
        {
            double score = 5.0 + (n - burst) * 1.5;
            return new ScoreEvent(nowMs, pid, "CommandRate", score, ScoreEvent.Severity.LOW,
                    "Command '" + cmd + "' burst " + n);
        }
        return null;
    }
}
