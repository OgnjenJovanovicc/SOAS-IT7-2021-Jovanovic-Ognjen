/*package trade_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
                                   @RequestHeader("Authorization") String auth) {
        return service.trade(request, auth);
    }
}*/

package trade_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
}