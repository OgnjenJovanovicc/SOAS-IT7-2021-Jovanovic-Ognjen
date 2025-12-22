package api.dtos;

import java.math.BigDecimal;

public class CryptoWalletDto {


    private String email;
    private BigDecimal btcAmount;
    private BigDecimal ethAmount;
    private BigDecimal usdtAmount;

    public CryptoWalletDto() {}
    
    public CryptoWalletDto(String email,BigDecimal btcAmount, BigDecimal ethAmount, BigDecimal usdAmount) {
    	this.email=email;
    	this.btcAmount=btcAmount;
    	this.ethAmount=ethAmount;
    	this.usdtAmount=usdAmount;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BigDecimal getBtcAmount() {
		return btcAmount;
	}

	public void setBtcAmount(BigDecimal btcAmount) {
		this.btcAmount = btcAmount;
	}

	public BigDecimal getEthAmount() {
		return ethAmount;
	}

	public void setEthAmount(BigDecimal ethAmount) {
		this.ethAmount = ethAmount;
	}

	public BigDecimal getUsdtAmount() {
		return usdtAmount;
	}

	public void setUsdtAmount(BigDecimal usdtAmount) {
		this.usdtAmount = usdtAmount;
	}
    
    
}
