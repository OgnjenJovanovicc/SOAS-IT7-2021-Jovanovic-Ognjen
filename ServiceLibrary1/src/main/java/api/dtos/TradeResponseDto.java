package api.dtos;

import java.util.Map;

public class TradeResponseDto {
    private Map<String, Object> walletState; 
    private String message;
    
    public TradeResponseDto() {}
    
    public TradeResponseDto(Map<String, Object> walletState, String message) {
        this.walletState = walletState;
        this.message = message;
    }
    public Map<String, Object> getWalletState() { return walletState; }
    public void setWalletState(Map<String, Object> walletState) { this.walletState = walletState; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}