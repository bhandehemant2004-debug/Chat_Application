package Security;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.Cipher;
import java.util.Base64;

public class IdentityManager {

    private static final String INFO_FILE = "info.txt";
    private static final String PARAM_FILE = "params.txt";

    private Properties info = new Properties();
    private Properties params = new Properties();

    /* ================== INIT ================== */

    public IdentityManager() throws Exception {
        loadParams();
        loadInfo();
        if (!info.containsKey("publicKey")) {
            generateKeyPair();
            saveInfo();
        }
    }

    /* ================== FILE HANDLING ================== */

    private void loadParams() throws IOException {
        try (FileInputStream fis = new FileInputStream(PARAM_FILE)) {
            params.load(fis);
        }
    }

    private void loadInfo() throws IOException {
        if (Files.exists(Path.of(INFO_FILE))) {
            try (FileInputStream fis = new FileInputStream(INFO_FILE)) {
                info.load(fis);
            }
        }
    }

    private void saveInfo() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(INFO_FILE)) {
            info.store(fos, "User Identity Info");
        }
    }

    /* ================== IDENTITY ================== */

    public void setUser(String username, String jwt, String bio, String address) throws IOException {
        info.setProperty("username", username);
        info.setProperty("jwt", jwt);
        info.setProperty("bio", bio);
        info.setProperty("address", address);
        saveInfo();
    }
    public void set_username(String username){
        try{
            info.setProperty("username", username);
            saveInfo();
        }
        catch(Exception e){

        }
        
    }

    public String getUsername() {
        return info.getProperty("username");
    }

    public String getPublicKey() {
        return info.getProperty("publicKey");
    }

    /* ================== KEY GENERATION ================== */

    private void generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(params.getProperty("key.algorithm"));
        gen.initialize(Integer.parseInt(params.getProperty("key.size")));

        KeyPair pair = gen.generateKeyPair();

        info.setProperty("publicKey",
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        info.setProperty("privateKey",
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
    }

    private PublicKey loadPublicKey(String key) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private PrivateKey loadPrivateKey() throws Exception {
        byte[] bytes = Base64.getDecoder().decode(info.getProperty("privateKey"));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /* ================== ENCRYPT / DECRYPT ================== */

    // Encrypt for receiver (confidential)
    public String encryptWithPublicKey(String message, String receiverPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey(receiverPublicKey));
        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
    }

    public String decryptWithPrivateKey(String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, loadPrivateKey());
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }

    /* ================== SIGN / VERIFY ================== */

    public String signMessage(String message) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(loadPrivateKey());
        sig.update(message.getBytes());
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public boolean verifySignature(String message, String signature, String senderPublicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(loadPublicKey(senderPublicKey));
        sig.update(message.getBytes());
        return sig.verify(Base64.getDecoder().decode(signature));
    }

    /* ================== SERVER INFO ================== */

    public String getServerIP() {
        return params.getProperty("server.ip");
    }
    public int getPort() {
        String port = params.getProperty("server_port");
        return Integer.parseInt(port);
    }
}

