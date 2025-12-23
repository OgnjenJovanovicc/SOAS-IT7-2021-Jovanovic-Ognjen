package api.dtos;

import java.math.BigDecimal;

public class CryptoExchangeDto {

	
	private String from;
    private String to;
    private BigDecimal exchangeRate;
    private String port;
    
    public CryptoExchangeDto() {
    	
    }

    public CryptoExchangeDto(String from, String to, BigDecimal rate) {
        this.from = from;
        this.to = to;
        this.exchangeRate = rate;
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

	public BigDecimal getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(BigDecimal exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
    
    

}
