package Services;

import Framework.annotations.Service;

@Service
public class Chat_Services {

    public String processChatMessage(String username, String message) {
        System.out.println("[Chat_Services] Processing message from user: " + username + " -> " + message);
        return "PROCESSED: [" + username.toUpperCase() + "]: " + message;
    }

    public boolean validateUserToken(String token) {
        return token != null && !token.trim().isEmpty();
    }
}
