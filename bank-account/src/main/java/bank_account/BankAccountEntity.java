package bank_account;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name="bank_account", 
uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class BankAccountEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false, unique=true)
	private String email;
	
	@Column(precision=19, scale=4)
	private BigDecimal usdAmount=BigDecimal.ZERO;
	
	@Column(precision=19, scale=4)
	private BigDecimal eurAmount=BigDecimal.ZERO;
	
	@Column(precision=19, scale=4)
	private BigDecimal gbpAmount=BigDecimal.ZERO;
	
	@Column(precision=19, scale=4)
	private BigDecimal chfAmount=BigDecimal.ZERO;
	
	@Column(precision=19, scale=4)
	private BigDecimal rsdAmount=BigDecimal.ZERO;
	
	public BankAccountEntity() {
		
	}

	public BankAccountEntity(Long id, String email, BigDecimal usdAmount, BigDecimal eurAmount, BigDecimal gbpAmount,
			BigDecimal chfAmount, BigDecimal rsdAmount) {
		this.id = id;
		this.email = email;
		this.usdAmount = usdAmount;
		this.eurAmount = eurAmount;
		this.gbpAmount = gbpAmount;
		this.chfAmount = chfAmount;
		this.rsdAmount = rsdAmount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	
	 public BigDecimal getAmountByCurrency(String currency) {
	        return switch (currency.toUpperCase()) {
	            case "USD" -> usdAmount;
	            case "EUR" -> eurAmount;
	            case "GBP" -> gbpAmount;
	            case "CHF" -> chfAmount;
	            case "RSD" -> rsdAmount;
	            default -> BigDecimal.ZERO;
	        };
	    }
	 
	 public void setAmountByCurrency(String currency, BigDecimal amount) {
	        if (amount == null) amount = BigDecimal.ZERO;
	        
	        switch (currency.toUpperCase()) {
	            case "USD" -> usdAmount = amount;
	            case "EUR" -> eurAmount = amount;
	            case "GBP" -> gbpAmount = amount;
	            case "CHF" -> chfAmount = amount;
	            case "RSD" -> rsdAmount = amount;
	        }
	    }
	}
	

