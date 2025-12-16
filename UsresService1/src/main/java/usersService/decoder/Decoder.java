package usersService.decoder;

import java.util.Base64;

import org.springframework.stereotype.Component;
@Component
public class Decoder {

	
	  public String decodeHeader(String authorization) {
	        if (authorization != null && authorization.startsWith("Basic ")) {
	            String base64Credentials = authorization.substring("Basic ".length());
	            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
	            String credentials = new String(decodedBytes);	   
	            return credentials.split(":", 2)[0]; 
	        }
	        return null;
	    }
}
