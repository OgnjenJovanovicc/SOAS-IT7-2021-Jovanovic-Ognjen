package crypto_wallet;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "crypto-wallet",
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
public class CryptoWalletEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private BigDecimal btcAmount = BigDecimal.ZERO;
    private BigDecimal ethAmount = BigDecimal.ZERO;
    private BigDecimal usdtAmount = BigDecimal.ZERO;

    public CryptoWalletEntity() {}

    public CryptoWalletEntity(String email) {
        this.email = email;
        this.btcAmount = BigDecimal.ZERO;
        this.ethAmount = BigDecimal.ZERO;
        this.usdtAmount = BigDecimal.ZERO;
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
