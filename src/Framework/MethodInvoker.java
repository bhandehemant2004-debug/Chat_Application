package Framework;


import java.lang.reflect.*;

import java.lang.reflect.Method;


import Controller.*;

public class MethodInvoker {

    public static Object invoke(Method method, Request request)
            throws Exception {

        Object controller = Request_Mapping.get_controller();
        
        Parameter[] params = method.getParameters();
        //System.out.println("correct requred  parameters are all follows ");
        for(int i = 0 ;i< params.length ;i++){
            System.out.print(" "+params[i]);
        }
        //System.out.println("correct available  parameters are all follows ");
        //for(String key:request.params.keySet()){
          //  System.out.print(" "+request.params.get(key));
        //}
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {

            if (params[i].isAnnotationPresent(POST.class)) {
                POST p = params[i].getAnnotation(POST.class);
                
                args[i] = request.params.get(p.str());
                //System.out.println(p.str()+"get the value of : "+ args[i].toString());
            }
            if (params[i].isAnnotationPresent(Authenticate.class)) {
                Authenticate p = params[i].getAnnotation(Authenticate.class);
                args[i] = request.params.get(p.credentials());
            }
            if (params[i].isAnnotationPresent(VALIDATE.class)) {
                VALIDATE p = params[i].getAnnotation(VALIDATE.class);
                args[i] = request.params.get(p.token());
            }
            if (params[i].isAnnotationPresent(Req.class)) {
                //Req p = params[i].getAnnotation(Req.class);
                
                args[i] = request.thread;
                //System.out.println(p.str()+"get the value of : "+ args[i].toString());
            }
        }

        return method.invoke(controller, args);
    }
}
