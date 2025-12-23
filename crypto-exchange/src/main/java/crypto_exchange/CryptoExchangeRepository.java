package crypto_exchange;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CryptoExchangeRepository  extends JpaRepository<CryptoExchangeModel, Long> {

    CryptoExchangeModel findByFromAndTo(
            String from, String to);
    
    @Query(value = """
    		SELECT DISTINCT currency_from as currency from crypto_exchange
    		UNION
    		SELECT DISTINCT currency_to as currency from crypto_exchange
    		"""
    		,nativeQuery=true)
    
        List<String> findAllDistinctCryptos();
}
