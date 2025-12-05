package util.exceptions;

import java.util.List;

public class NotDataFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	List<String> currencies;
	
	public NotDataFoundException() {
		
	}
	
	public NotDataFoundException(String message,List<String> currencies) {
		super(message);
		this.currencies=currencies;
	}

	public List<String> getCurrencies() {
		return currencies;
	}

	public void setCurrencies(List<String> currencies) {
		this.currencies = currencies;
	}
	
	
	

}
