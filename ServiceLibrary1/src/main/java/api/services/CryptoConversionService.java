package api.services;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import api.dtos.CryptoConversionRequestDto;

public interface CryptoConversionService {

    @PostMapping("/crypto-conversion")
    ResponseEntity<?> convertCrypto(
            @RequestBody CryptoConversionRequestDto request,
            @RequestHeader("Authorization") String authorizationHeader);
    
    @GetMapping("/crypto-conversion")
    ResponseEntity<?> getCryptoConversion(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader);


    @GetMapping("/crypto-conversion-feign")
    ResponseEntity<?> getCryptoConversionFeign(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader);
}
