package api.dtos;

import java.math.BigDecimal;

public class CryptoConversionRequestDto {

    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String calculation;

    public CryptoConversionRequestDto() {}
    
    
  

	public CryptoConversionRequestDto(String from, String to, BigDecimal amount, BigDecimal exchangeRate,
			BigDecimal convertedAmount, String calculation) {
		super();
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.exchangeRate = exchangeRate;
		this.convertedAmount = convertedAmount;
		this.calculation = calculation;
	}




	public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public BigDecimal getConvertedAmount() {
		return convertedAmount;
	}

	public void setConvertedAmount(BigDecimal convertedAmount) {
		this.convertedAmount = convertedAmount;
	}
	public String getCalculation() {
		return calculation;
	}

	public void setCalculation(String calculation) {
		this.calculation = calculation;
	}
       
}
