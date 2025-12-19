package api.dtos;

import java.math.BigDecimal;

public class BankAccountDto {

	
	 private String email;
	    private BigDecimal usdAmount;
	    private BigDecimal eurAmount;
	    private BigDecimal gbpAmount;
	    private BigDecimal chfAmount;
	    private BigDecimal rsdAmount;
	    
	    public BankAccountDto() {}
	    
	    public BankAccountDto(String email, BigDecimal usdAmount, BigDecimal eurAmount, 
	                         BigDecimal gbpAmount, BigDecimal chfAmount, BigDecimal rsdAmount) {
	        this.email = email;
	        this.usdAmount = usdAmount;
	        this.eurAmount = eurAmount;
	        this.gbpAmount = gbpAmount;
	        this.chfAmount = chfAmount;
	        this.rsdAmount = rsdAmount;
	    }

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public BigDecimal getUsdAmount() {
			return usdAmount;
		}

		public void setUsdAmount(BigDecimal usdAmount) {
			this.usdAmount = usdAmount;
		}

		public BigDecimal getEurAmount() {
			return eurAmount;
		}

		public void setEurAmount(BigDecimal eurAmount) {
			this.eurAmount = eurAmount;
		}

		public BigDecimal getGbpAmount() {
			return gbpAmount;
		}

		public void setGbpAmount(BigDecimal gbpAmount) {
			this.gbpAmount = gbpAmount;
		}

		public BigDecimal getChfAmount() {
			return chfAmount;
		}

		public void setChfAmount(BigDecimal chfAmount) {
			this.chfAmount = chfAmount;
		}

		public BigDecimal getRsdAmount() {
			return rsdAmount;
		}

		public void setRsdAmount(BigDecimal rsdAmount) {
			this.rsdAmount = rsdAmount;
		}
	    
	    
	    
}
