package gym.util;

import java.util.Map;
import java.util.List;

/**
 * Minimal JSON builder – no external dependencies.
 */
public class JsonUtil {

    public static String ok(String message) {
        return "{\"status\":\"success\",\"message\":" + quote(message) + "}";
    }

    public static String ok(String message, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder("{\"status\":\"success\",\"message\":");
        sb.append(quote(message)).append(",\"data\":{");
        boolean first = true;
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (!first) sb.append(",");
            sb.append(quote(e.getKey())).append(":").append(toJson(e.getValue()));
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    public static String okList(String message, List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("{\"status\":\"success\",\"message\":");
        sb.append(quote(message)).append(",\"data\":[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : list.get(i).entrySet()) {
                if (!first) sb.append(",");
                sb.append(quote(e.getKey())).append(":").append(toJson(e.getValue()));
                first = false;
            }
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public static String error(String message) {
        return "{\"status\":\"error\",\"message\":" + quote(message) + "}";
    }

    // ── internals ─────────────────────────────────────────────

    private static String toJson(Object val) {
        if (val == null) return "null";
        if (val instanceof Number || val instanceof Boolean) return val.toString();
        return quote(val.toString());
    }

    private static String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r") + "\"";
    }
}
