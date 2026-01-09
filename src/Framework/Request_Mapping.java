package Framework;
import java.lang.reflect.Method;
import java.util.*;

import Controller.Authenticate;
import Controller.Chat_Controller;
import Controller.POST;
import Controller.VALIDATE;
public class Request_Mapping {
    public static Map<String,Method>map = new HashMap<>();
    public final static Chat_Controller annotations = new Chat_Controller();
    public static void main(String[]args){
        Class<?>cls = Chat_Controller.class;
          Method[] methods = cls.getMethods();
          for(Method method : methods){
            if(method.isAnnotationPresent(POST.class)){
                POST post = method.getAnnotation(POST.class);
                String string = post.str();
                map.put("POST"+ "|" +string , method);   
                System.out.println("pushing into queue = "+"POST"+ "|" +string );

            }
            if(method.isAnnotationPresent(VALIDATE.class)){
                VALIDATE validate = method.getAnnotation(VALIDATE.class);
                String string = validate.token();
                map.put("VALIDATE"+ "|" +string , method);  
                System.out.println("pushing into queue = "+"VALIDATE"+ "|" +string );    
            }
            if(method.isAnnotationPresent(Authenticate.class)){
                Authenticate authenticate = method.getAnnotation(Authenticate.class);
                String string = authenticate.credentials();
                map.put("POST"+ "|" +string , method);    
                System.out.println("pushing into queue = "+"POST"+ "|" +string );  
            }
    }
}
        public static Chat_Controller get_controller(){
            return annotations;
        }
        public static Method getMethod(String key){
            return map.get(key);
        }

}
