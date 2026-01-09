package Services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Thread.Reading_Writing;
public class Chat_Services {

    public static ConcurrentHashMap<String,Reading_Writing> map= new ConcurrentHashMap<>();

    public static void add_user(String token ,Reading_Writing client){
        System.out.println("one more user added to the list");
        map.put(token, client);
    }
    public static void remove_user(String token){
        map.remove(token);
    }
    public static boolean is_token_available(String token){
        return map.containsKey(token);
    }
    public static void send_all(Reading_Writing thread ,String msg) {
        System.out.println("in the main function to send_all");
        for (Map.Entry<String, Reading_Writing> entry : map.entrySet()) {
            Reading_Writing c = entry.getValue();
            if(c==thread)continue;
            try {
                if (c.socket.isClosed()) {
                    map.remove(entry.getKey());
                    continue;
                }
                c.send(msg);
            } catch (Exception e) {
                map.remove(entry.getKey());
            }
        }
    }
    

    public static void send_to(String from , String to , String msg){
        try{
            map.get(to).out.writeUTF(msg);
        }
        catch(Exception e){
            System.out.println("error to send a personal mail to the user : "+to);
        }
        
    }
    
}
