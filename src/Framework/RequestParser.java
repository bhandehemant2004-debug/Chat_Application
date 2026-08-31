package Framework;

import java.util.HashMap;
import Thread.Reading_Writing;

public class RequestParser {

    public static Request parse(String raw, Reading_Writing thread) {
        Request req = new Request();
        req.thread = thread;
        req.params = new HashMap<>();

        if (raw == null || raw.trim().isEmpty()) {
            return req;
        }

        String[] parts = raw.split("\\|", 3);
        req.method = parts.length > 0 ? parts[0] : "";
        req.path = parts.length > 1 ? parts[1] : "";

        if (parts.length > 2 && parts[2] != null && !parts[2].trim().isEmpty()) {
            String[] kvs = parts[2].split("&");
            for (String kv : kvs) {
                if (kv.trim().isEmpty()) continue;
                String[] p = kv.split("=", 2);
                String key = p[0];
                String val = p.length > 1 ? p[1] : "";
                req.params.put(key, val);
            }
        }
        return req;
    }
}
