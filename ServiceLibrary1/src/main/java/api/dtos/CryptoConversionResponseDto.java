package api.dtos;

import java.util.Map;

public class CryptoConversionResponseDto {

    private Map<String, Object> walletState;
    private String message;

    public CryptoConversionResponseDto() {}

    public CryptoConversionResponseDto(
    	Map<String, Object> walletState,
        String message) {
        this.walletState = walletState;
        this.message = message;
    }

    public Map<String, Object> getWalletState() {
        return walletState;
    }

    public void setWalletState(Map<String, Object> walletState) {
        this.walletState = walletState;
    }

    public String getMessage() {
        return message;
    }
}
