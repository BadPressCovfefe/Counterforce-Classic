package launch.countershield.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NetClassifier
{
    public enum IpKind
    {
        UNKNOWN, HOME_LIKELY, MOBILE_LIKELY, VPN_LIKELY
    }

    public static final class IpProfile
    {
        public final String ip;
        public long firstSeenMs;
        public long lastSeenMs;
        public int seenCount;
        public IpKind kind = IpKind.UNKNOWN;

        public IpProfile(String ip, long ts)
        {
            this.ip = ip;
            this.firstSeenMs = ts;
            this.lastSeenMs = ts;
            this.seenCount = 1;
        }

        public long stabilityMs()
        {
            return lastSeenMs - firstSeenMs;
        }
    }

    private final Map<Integer, Map<String, IpProfile>> store = new ConcurrentHashMap<>();

    public IpProfile note(int playerId, String ip, long nowMs)
    {
        if(ip == null)
        {
            ip = "0.0.0.0";
        }
        Map<String, IpProfile> map = store.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        IpProfile p = map.get(ip);
        if(p == null)
        {
            p = new IpProfile(ip, nowMs);
            map.put(ip, p);
        }
        else
        {
            p.lastSeenMs = nowMs;
            p.seenCount++;
        }
        if(p.stabilityMs() > 86_400_000L && p.seenCount >= 6)
        {
            p.kind = IpKind.HOME_LIKELY;
        }
        return p;
    }

    public IpKind classify(int playerId, String currentIp)
    {
        Map<String, IpProfile> map = store.get(playerId);
        if(map == null)
        {
            return IpKind.UNKNOWN;
        }
        IpProfile p = map.get(currentIp);
        return p == null ? IpKind.UNKNOWN : p.kind;
    }
}
