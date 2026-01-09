package Db;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import Thread.Reading_Writing;
public class username_collection {
    public static Map<String,String>map = new HashMap<>();
    public static ConcurrentHashMap<String,Reading_Writing> mpp= new ConcurrentHashMap<>();
    public static int add_user(String username , String password){
        if(map.containsKey(username)){
            return 0;
        }
        map.put(username, password);
        return 1;
    }
    public static int check_user(String username,String password){
        if(map.containsKey(username)){
            if(map.get(username).equals(password)){
                return 1;
            }
            else return 0;
        }
        return 0;
    }
}
