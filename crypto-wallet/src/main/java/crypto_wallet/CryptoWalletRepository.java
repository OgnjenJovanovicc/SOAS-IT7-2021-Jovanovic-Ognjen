package crypto_wallet;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface CryptoWalletRepository extends JpaRepository<CryptoWalletEntity,Long> {

	
	Optional<CryptoWalletEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CryptoWalletEntity w WHERE w.email = :email")
    void deleteByEmail(String email);
}
