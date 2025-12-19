package api.dtos;


	public class CreateBankAccountDto {
	    private String email;
	    
	    public CreateBankAccountDto() {}
	    
	    public CreateBankAccountDto(String email) {
	        this.email = email;
	    }
	    
	    public String getEmail() { return email; }
	    public void setEmail(String email) { this.email = email; }
	}

