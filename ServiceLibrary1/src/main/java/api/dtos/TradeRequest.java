package api.dtos;
import java.math.BigDecimal;

public class TradeRequest {

    private String from;
    private String to;
    private BigDecimal amount;

    public TradeRequest() {
    	
    }
    
    public TradeRequest(String from, String to, BigDecimal amount) {
		super();
		this.from = from;
		this.to = to;
		this.amount = amount;
	}


	public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
