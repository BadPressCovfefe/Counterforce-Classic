package tobcomm.api;

import java.util.Collection;
import java.util.Map;

// Custom JSON serializor for the API so I dont need to use dependacies. Exact copy in tobcomm.api. Just makes things easier to manage.
final class Json {
    private Json() {}

    static String serialize(Object o) {
        if (o == null) return "null";
        if (o instanceof String s) return quote(s);
        if (o instanceof Number || o instanceof Boolean) return String.valueOf(o);
        if (o instanceof Map<?, ?> m) return map(m);
        if (o instanceof Collection<?> c) return array(c);
        if (o.getClass().isArray()) return array(java.util.Arrays.asList((Object[]) o));
        // Fallback: toString in quotes
        return quote(String.valueOf(o));
    }

    private static String map(Map<?, ?> m) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> e : m.entrySet()) {
            if (!first) sb.append(',');
            sb.append(quote(String.valueOf(e.getKey()))).append(':').append(serialize(e.getValue()));
            first = false;
        }
        return sb.append('}').toString();
    }

    private static String array(Collection<?> c) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object o : c) {
            if (!first) sb.append(',');
            sb.append(serialize(o));
            first = false;
        }
        return sb.append(']').toString();
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2).append('"');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        return sb.append('"').toString();
    }
}
