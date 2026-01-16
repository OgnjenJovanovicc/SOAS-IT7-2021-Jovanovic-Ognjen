package trade_service;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.TradeRequest;

@RestController
@RequestMapping("/trade")
public class TradeController {

    private final TradeService service;

    public TradeController(TradeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> trade(@RequestBody TradeRequest request,
                                   @RequestHeader("Authorization") String authHeader) {
        return service.trade(request, authHeader);
    }
    @GetMapping
    public ResponseEntity<?> tradeGet(
            @RequestParam(value="from") String from,
            @RequestParam (value="to")String to,
            @RequestParam (value="amount")BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        return service.tradeGet(from, to, amount, authorizationHeader);
    }
}