package countershield.model;

public final class ScoreEvent {
    public enum Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }
    public final long ts;
    public final int playerId;
    public final String detector;
    public final double score;
    public final Severity severity;
    public final String detail;

    public ScoreEvent(long ts, int playerId, String detector, double score, Severity severity, String detail) {
        this.ts = ts;
        this.playerId = playerId;
        this.detector = detector;
        this.score = score;
        this.severity = severity;
        this.detail = detail;
    }
}
