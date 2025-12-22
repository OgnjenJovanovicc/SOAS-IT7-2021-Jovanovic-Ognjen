package crypto_wallet;

import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class AuthDecoder {

    public String decodeEmailFromAuthHeader(String header) {
        if (header == null || !header.startsWith("Basic ")) return null;

        try {
            String base64 = header.substring(6);
            String decoded = new String(Base64.getDecoder().decode(base64));
            return decoded.split(":", 2)[0];
        } catch (Exception e) {
            return null;
        }
    }
}
