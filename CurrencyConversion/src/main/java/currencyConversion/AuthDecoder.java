package currencyConversion;

import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class AuthDecoder {
    public String decodeEmailFromAuthHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            return null;
        }
        try {
            String base64Credentials = authorizationHeader.substring("Basic ".length());
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(decodedBytes);
            return credentials.split(":", 2)[0];
        } catch (Exception e) {
            return null;
        }
    }
}