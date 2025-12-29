package trade_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DataLoader implements CommandLineRunner {
    
    private final CurrencyRateRepository currencyRateRepository;
    
    public DataLoader(CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }
    
    @Override
    public void run(String... args) {
        currencyRateRepository.deleteAll();  
        Set<CurrencyRate> uniqueExchanges = new HashSet<>();
        
        addUnique(uniqueExchanges, "RSD", "EUR", "0.0085");
        addUnique(uniqueExchanges, "RSD", "GBP", "0.0072");
        addUnique(uniqueExchanges, "RSD", "CHF", "0.0090");
        addUnique(uniqueExchanges, "RSD", "USD", "0.0092");
        
        addUnique(uniqueExchanges, "EUR", "RSD", "117.65");
        addUnique(uniqueExchanges, "GBP", "RSD", "138.89");
        addUnique(uniqueExchanges, "CHF", "RSD", "111.11");
        addUnique(uniqueExchanges, "USD", "RSD", "108.70");

        addUnique(uniqueExchanges, "EUR", "GBP", "0.85");
        addUnique(uniqueExchanges, "EUR", "CHF", "0.97");
        addUnique(uniqueExchanges, "EUR", "USD", "1.08");
        addUnique(uniqueExchanges, "GBP", "EUR", "1.176");
        addUnique(uniqueExchanges, "GBP", "CHF", "1.14");
        addUnique(uniqueExchanges, "GBP", "USD", "1.27");
        addUnique(uniqueExchanges, "CHF", "EUR", "1.03");
        addUnique(uniqueExchanges, "CHF", "GBP", "0.877");
        addUnique(uniqueExchanges, "CHF", "USD", "1.10");
        addUnique(uniqueExchanges, "USD", "EUR", "0.926");
        addUnique(uniqueExchanges, "USD", "GBP", "0.787");
        addUnique(uniqueExchanges, "USD", "CHF", "0.909");

        addUnique(uniqueExchanges, "USD", "RSD", "108.70");
        addUnique(uniqueExchanges, "EUR", "RSD", "117.65");
        addUnique(uniqueExchanges, "GBP", "RSD", "138.89");
        addUnique(uniqueExchanges, "CHF", "RSD", "111.11");
       
        addUnique(uniqueExchanges, "USD", "BTC", "0.030"); 
        addUnique(uniqueExchanges, "USD", "ETH", "0.050");   
        addUnique(uniqueExchanges, "USD", "USDT", "1.00");     
         
        addUnique(uniqueExchanges, "EUR", "BTC", "0.28"); 
        addUnique(uniqueExchanges, "EUR", "ETH", "0.046");   
        addUnique(uniqueExchanges, "EUR", "USDT", "1.08");    
        
        addUnique(uniqueExchanges, "BTC", "USD", "3333.33");  
        addUnique(uniqueExchanges, "ETH", "USD", "2000.00");   
        addUnique(uniqueExchanges, "USDT", "USD", "1.00");     
        
        addUnique(uniqueExchanges, "BTC", "EUR", "3570.29");  
        addUnique(uniqueExchanges, "ETH", "EUR", "2173.91");   
        addUnique(uniqueExchanges, "USDT", "EUR", "0.9259");   
        
        addUnique(uniqueExchanges, "CHF", "USD", "1.10");
        addUnique(uniqueExchanges, "CHF", "EUR", "1.03");
        addUnique(uniqueExchanges, "GBP", "USD", "1.27");
        addUnique(uniqueExchanges, "GBP", "EUR", "1.18");
        
        addUnique(uniqueExchanges, "USD", "CHF", "0.909");
        addUnique(uniqueExchanges, "EUR", "CHF", "0.97");
        addUnique(uniqueExchanges, "USD", "GBP", "0.787");
        addUnique(uniqueExchanges, "EUR", "GBP", "0.85");
        addUnique(uniqueExchanges, "RSD", "RSD", "1.00");
        addUnique(uniqueExchanges, "USD", "USD", "1.00");
        addUnique(uniqueExchanges, "EUR", "EUR", "1.00");
        addUnique(uniqueExchanges, "GBP", "GBP", "1.00");
        addUnique(uniqueExchanges, "CHF", "CHF", "1.00");
        addUnique(uniqueExchanges, "BTC", "BTC", "1.00");
        addUnique(uniqueExchanges, "ETH", "ETH", "1.00");
        addUnique(uniqueExchanges, "USDT", "USDT", "1.00");
        addUnique(uniqueExchanges, "BTC", "ETH", "16.666");
        addUnique(uniqueExchanges, "ETH", "BTC", "0.06");
        addUnique(uniqueExchanges, "BTC", "USDT", "3333");
        addUnique(uniqueExchanges, "USDT", "BTC", "0.03");
        addUnique(uniqueExchanges, "ETH", "USDT", "2000");
        addUnique(uniqueExchanges, "USDT", "ETH", "0.0005");
        
        List<CurrencyRate> exchangeList = new ArrayList<>(uniqueExchanges);
        currencyRateRepository.saveAll(exchangeList);
        
        System.out.println("✅ Loaded " + exchangeList.size() + " UNIQUE exchange rates");
        
        checkForDuplicates();
    }
    
    private void addUnique(Set<CurrencyRate> set, String from, String to, String rate) {
        String pairKey = from.toUpperCase() + "_" + to.toUpperCase();
        
        boolean exists = set.stream().anyMatch(e -> 
            e.getFromCurrency().equalsIgnoreCase(from) && 
            e.getToCurrency().equalsIgnoreCase(to)
        );
        
        if (!exists) {
            CurrencyRate exchange = new CurrencyRate();
            exchange.setFromCurrency(from.toUpperCase());
            exchange.setToCurrency(to.toUpperCase());
            exchange.setRate(new BigDecimal(rate));
            set.add(exchange);
        } else {
            System.out.println("⚠️ Skipping duplicate: " + pairKey);
        }
    }
    
    private void checkForDuplicates() {
        System.out.println("🔍 Checking database for duplicates...");
        
        List<CurrencyRate> allRates = currencyRateRepository.findAll();
        Map<String, Integer> pairCount = new HashMap<>();
        
        for (CurrencyRate rate : allRates) {
            String pair = rate.getFromCurrency() + "_" + rate.getToCurrency();
            pairCount.put(pair, pairCount.getOrDefault(pair, 0) + 1);
        }
        
        boolean hasDuplicates = false;
        for (Map.Entry<String, Integer> entry : pairCount.entrySet()) {
            if (entry.getValue() > 1) {
                System.out.println("❌ DUPLICATE: " + entry.getKey() + " appears " + entry.getValue() + " times");
                hasDuplicates = true;
            }
        }
        
        if (!hasDuplicates) {
            System.out.println("✅ No duplicates found - database is clean!");
        }
        
        System.out.println("\n📊 Loaded currency pairs:");
        allRates.stream()
                .sorted((a, b) -> {
                    int cmp = a.getFromCurrency().compareTo(b.getFromCurrency());
                    if (cmp == 0) {
                        return a.getToCurrency().compareTo(b.getToCurrency());
                    }
                    return cmp;
                })
                .forEach(rate -> {
                    System.out.printf("  %-5s → %-5s = %-10s%n", 
                        rate.getFromCurrency(), 
                        rate.getToCurrency(), 
                        rate.getRate());
                });
    }
}
