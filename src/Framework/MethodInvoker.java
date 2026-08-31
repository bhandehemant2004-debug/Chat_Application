package Framework;

import Framework.annotations.RequestParam;
import Framework.annotations.SecurePayload;
import Controller.POST;
import Controller.Req;
import Controller.Authenticate;
import Controller.VALIDATE;
import Security.IdentityManager;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodInvoker {

    private static IdentityManager identityManager;

    static {
        try {
            identityManager = new IdentityManager();
        } catch (Exception e) {
            System.err.println("[MethodInvoker] Could not load IdentityManager for security checks: " + e.getMessage());
        }
    }

    public static Object invoke(Method method, Request request) throws Exception {
        Object controller = Request_Mapping.getControllerInstance(method);
        if (controller == null) {
            throw new IllegalStateException("No IoC controller instance registered for method: " + method.getName());
        }

        // Security check for @SecurePayload annotated controller actions
        if (method.isAnnotationPresent(SecurePayload.class)) {
            SecurePayload securePayload = method.getAnnotation(SecurePayload.class);
            if (securePayload.verifySignature()) {
                String signature = request.params.get("signature");
                String senderPublicKey = request.params.get("publicKey");
                String message = request.params.get("message");

                if (signature == null || senderPublicKey == null || message == null) {
                    return "SECURITY_ERROR: Missing cryptographic signature, public key, or message payload!";
                }

                if (identityManager != null) {
                    boolean valid = identityManager.verifySignature(message, signature, senderPublicKey);
                    if (!valid) {
                        return "SECURITY_ERROR: Cryptographic RSA Signature Verification Failed!";
                    }
                    System.out.println("[Security Filter] RSA Signature verified successfully for method: " + method.getName());
                }
            }
        }

        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];

            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam reqParam = param.getAnnotation(RequestParam.class);
                args[i] = request.params.get(reqParam.value());
            } else if (param.isAnnotationPresent(POST.class)) {
                POST p = param.getAnnotation(POST.class);
                args[i] = request.params.get(p.str());
            } else if (param.isAnnotationPresent(Authenticate.class)) {
                Authenticate p = param.getAnnotation(Authenticate.class);
                args[i] = request.params.get(p.credentials());
            } else if (param.isAnnotationPresent(VALIDATE.class)) {
                VALIDATE p = param.getAnnotation(VALIDATE.class);
                args[i] = request.params.get(p.token());
            } else if (param.isAnnotationPresent(Req.class)) {
                args[i] = request.thread;
            } else if (param.getType().equals(Request.class)) {
                args[i] = request;
            }
        }

        return method.invoke(controller, args);
    }
}
