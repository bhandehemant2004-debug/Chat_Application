package Controller;

import Framework.annotations.Autowired;
import Framework.annotations.PostMapping;
import Framework.annotations.RequestParam;
import Framework.annotations.RestController;
import Framework.annotations.SecurePayload;
import Services.Chat_Services;

@RestController
public class Chat_Controller {

    @Autowired
    private Chat_Services chatServices;

    @PostMapping("chat")
    public String handleChat(@RequestParam("username") String username, @RequestParam("message") String message) {
        System.out.println("[Chat_Controller] Received chat request via IoC injected controller!");
        if (chatServices != null) {
            return chatServices.processChatMessage(username, message);
        }
        return "ERROR: Chat_Services dependency not injected!";
    }

    @PostMapping("secureChat")
    @SecurePayload(verifySignature = true)
    public String handleSecureChat(@RequestParam("username") String username, @RequestParam("message") String message) {
        System.out.println("[Chat_Controller] Processing digitally signed secure payload message.");
        return chatServices.processChatMessage(username, message);
    }
}
