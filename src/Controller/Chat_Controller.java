package Controller;
import Db.username_collection;
import Services.Chat_Services;
import Thread.Reading_Writing;
public class Chat_Controller {
    @POST(str = "Registration")
    public void register(@POST(str="username") String username,
    @POST(str="password") String password,@Req Reading_Writing thread){
            if(username_collection.map.containsKey(username)){
                thread.send("Registration_failed");
                return ;
            }
            Db.username_collection.add_user(username, password);
            thread.send("Registration_Success");
        }
    @POST(str = "Generate_token")
    public void Create_token(@POST(str="username") String username,
    @POST(str="password") String password,@Req Reading_Writing thread){
        
        //System.out.println("username : "+username + " password : "+password);
        int result = username_collection.check_user(username, password);
    
        if(result == 1){
            Chat_Services.add_user(username, thread);
            thread.send("Login_Success|"+username);
            
        }
        else thread.send("Login_Failer|wrong credentials");
    }
    @POST(str="send_msg")
    public void Post_msg( @POST(str="username")String token , @POST(str="message")String message,@Req Reading_Writing thread){
        System.out.println("message want to send to all"+message);
        Chat_Services.send_all(thread, token+" : "+message);
        ;
    }
    @Authenticate(credentials ="validate_user")
    public String validate(String username , String password){
        return null;
    }
    @VALIDATE(token="Registration_validate")
    public boolean registration_validate(String username, String password){
        return true;
    }

}



