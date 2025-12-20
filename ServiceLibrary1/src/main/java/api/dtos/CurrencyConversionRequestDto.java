package api.dtos;

import java.math.BigDecimal;

public class CurrencyConversionRequestDto {
    private String from;
    private String to;
    private BigDecimal quantity;
    
    public CurrencyConversionRequestDto() {}
    
    public CurrencyConversionRequestDto(String from, String to, BigDecimal quantity) {
        this.from = from;
        this.to = to;
        this.quantity = quantity;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}