package Framework;
import java.util.HashMap;

import Thread.Reading_Writing;
public class RequestParser {

    public static Request parse(String raw,Reading_Writing thread) {
        //System.out.println("parsing a request");
        String[] parts = raw.split("\\|");

        Request req = new Request();
        req.thread = thread;
        req.method = parts[0];
        req.path = parts[1];
        //System.out.println("half request parsed");
        req.params = new HashMap<>();

        if (parts.length > 2) {
            //System.out.println("have a length > 2");
            //System.out.println("message is " + parts[2]);
            String[] kvs = parts[2].split("&");
            for (String kv : kvs) {
                
                String[] p = kv.split("=");
                req.params.put(p[0], p[1]);
                //System.out.println("putting a "+p[0]+" as a : "+p[1]);
            }
        }
        //System.out.println("returning a parse request");
        return req;
    }
}


