package trade_service;

import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class AuthDecoder {

    public String decodeEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) return null;
        try {
            String decoded = new String(Base64.getDecoder()
                    .decode(authHeader.substring(6)));
            return decoded.split(":", 2)[0];
        } catch (Exception e) {
            return null;
        }
    }
}
