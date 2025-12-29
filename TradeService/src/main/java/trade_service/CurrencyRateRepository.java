package trade_service;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    Optional<CurrencyRate> findByFromCurrencyAndToCurrency(String from, String to);

}
