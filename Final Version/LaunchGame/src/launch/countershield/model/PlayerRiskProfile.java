package launch.countershield.model;

import java.util.ArrayDeque;
import java.util.Deque;

public final class PlayerRiskProfile
{
    public enum Action
    {
        NONE, WARN, SHADOW_LIMIT, TEMP_SUSPEND, BAN
    }

    private static final int MAX_EVENTS = 128;
    private static final double DECAY_HALFLIFE_SEC = 900.0;

    private final Deque<ScoreEvent> recent = new ArrayDeque<>();

    public void add(ScoreEvent evt)
    {
        if(evt == null)
        {
            return;
        }
        recent.addLast(evt);
        if(recent.size() > MAX_EVENTS)
        {
            recent.removeFirst();
        }
    }

    public double score(long nowMs)
    {
        double s = 0.0;
        for(ScoreEvent e : recent)
        {
            double ageSec = (nowMs - e.ts) / 1000.0;
            double w = Math.pow(0.5, ageSec / DECAY_HALFLIFE_SEC);
            s += e.score * w;
        }
        return s;
    }

    public Action actionFor(double score)
    {
        if(score >= 100)
        {
            return Action.BAN;
        }
        if(score >= 60)
        {
            return Action.TEMP_SUSPEND;
        }
        if(score >= 35)
        {
            return Action.SHADOW_LIMIT;
        }
        if(score >= 20)
        {
            return Action.WARN;
        }
        return Action.NONE;
    }
}
