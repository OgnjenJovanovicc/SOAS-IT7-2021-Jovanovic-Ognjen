package api.dtos;

import java.math.BigDecimal;

public class CurrencyExchangeDto {
	
	private String to;
	private String from;
	private BigDecimal exchangeRate;
	private String port;
	
	
	public CurrencyExchangeDto() {
	}
	
	public CurrencyExchangeDto(String to, String from, BigDecimal exchangeRate) {
		this.to = to;
		this.from = from;
		this.exchangeRate = exchangeRate;
	}
	
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
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
