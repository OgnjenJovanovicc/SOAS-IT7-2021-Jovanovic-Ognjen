package bank_account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccountEntity,Long> {

	Optional<BankAccountEntity>findByEmail(String email);
	
	boolean existsByEmail(String email);
	 @Transactional
	    @Modifying
	    @Query("DELETE FROM BankAccountEntity b WHERE b.email = :email")
	void deleteByEmail(String email);
	
}
