package countershield.detectors;

import countershield.model.ScoreEvent;

public final class BannedAppDetector {
    public ScoreEvent check(long nowMs, int pid, String sig, boolean matched) {
        if (!matched) return null;
        double score = 50.0;
        return new ScoreEvent(nowMs, pid, "BannedApp", score, ScoreEvent.Severity.CRITICAL,
                "Banned app detected: " + sig);
    }
}
