package api.dtos;

public class CurrencyConversionResponseDto {
    private BankAccountDto account;
    private String transactionMessage;
    
    public CurrencyConversionResponseDto() {}
    
    public CurrencyConversionResponseDto(BankAccountDto account, String transactionMessage) {
        this.account = account;
        this.transactionMessage = transactionMessage;
    }
    
    public BankAccountDto getAccount() {
        return account;
    }
    
    public void setAccount(BankAccountDto account) {
        this.account = account;
    }
    
    public String getTransactionMessage() {
        return transactionMessage;
    }
    
    public void setTransactionMessage(String transactionMessage) {
        this.transactionMessage = transactionMessage;
    }
}