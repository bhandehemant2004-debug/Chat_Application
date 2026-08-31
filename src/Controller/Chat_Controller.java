package Controller;

import Framework.annotations.Autowired;
import Framework.annotations.PostMapping;
import Framework.annotations.RequestParam;
import Framework.annotations.RestController;
import Framework.annotations.SecurePayload;
import Controller.Req;
import Services.Chat_Services;
import Thread.Reading_Writing;
import Db.username_collection;
import Framework.session.WebSocketSessionRegistry;

@RestController
public class Chat_Controller {

    @Autowired
    private Chat_Services chatServices;

    @PostMapping("Registration")
    public String handleRegistration(@RequestParam("username") String username, @RequestParam("password") String password) {
        System.out.println("[Chat_Controller] Registration Request for user: " + username);
        boolean registered = username_collection.add(username, password);
        if (registered) {
            return "Registration_Success";
        } else {
            return "Registration_Failed";
        }
    }

    @PostMapping("Generate_token")
    public String handleLogin(@RequestParam("username") String username, @RequestParam("password") String password, @Req Reading_Writing thread) {
        System.out.println("[Chat_Controller] Login Request for user: " + username);
        boolean valid = username_collection.checkPassword(username, password);
        if (valid) {
            if (thread != null) {
                thread.username = username;
            }
            return "Login_Success|" + username;
        } else {
            return "Login_Failed";
        }
    }

    @PostMapping("send_msg")
    public String handleMessage(@RequestParam("username") String username, @RequestParam("message") String message) {
        System.out.println("[Chat_Controller] HTTP Chat Message received from: " + username);
        String formatted = chatServices.processChatMessage(username, message);
        WebSocketSessionRegistry.broadcast(username, message);
        return formatted;
    }

    @PostMapping("secureChat")
    @SecurePayload(verifySignature = true)
    public String handleSecureChat(@RequestParam("username") String username, @RequestParam("message") String message) {
        System.out.println("[Chat_Controller] Secure Digitally Signed Message received from: " + username);
        String secureMsg = "[RSA Verified 🔒] " + message;
        WebSocketSessionRegistry.broadcast(username, secureMsg);
        return "SECURE_SUCCESS|" + secureMsg;
    }
}
