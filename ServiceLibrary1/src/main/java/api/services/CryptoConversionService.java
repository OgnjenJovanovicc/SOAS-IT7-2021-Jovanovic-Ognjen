package api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import api.dtos.CryptoConversionRequestDto;

public interface CryptoConversionService {

    @PostMapping("/crypto-conversion")
    ResponseEntity<?> convertCrypto(
            @RequestBody CryptoConversionRequestDto request,
            @RequestHeader("Authorization") String authorizationHeader);
}
