package Framework;
import java.util.*;

import Thread.Reading_Writing;
public class Request {
    public String method;
    public String path;
    public Map<String, String> params;
    public Reading_Writing thread;
}
