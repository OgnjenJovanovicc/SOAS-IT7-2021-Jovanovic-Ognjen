package apiGateway.routing;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingConfiguration {

    @Bean
    public RouteLocator gatewayRouting(RouteLocatorBuilder builder) {
        return builder.routes()

            .route(p -> p.path("/currency-conversion")
                    .uri("lb://currency-conversion"))
            
            .route(p -> p.path("/crypto-conversion/**")
                    .filters(f -> f.rewritePath("/crypto-conversion/(?<segment>.*)", "/${segment}"))
                    .uri("lb://crypto-conversion"))
            
            .route(p -> p.path("/crypto-wallet/**")
                   // .filters(f -> f.rewritePath("/crypto-wallet/(?<segment>.*)", "/${segment}"))
                    .uri("lb://crypto-wallet"))
            
            .route(p -> p.path("/bank-account/**")
                    //.filters(f -> f.rewritePath("/bank-account/(?<segment>.*)", "/${segment}")
                    //.preserveHostHeader()) 
                    .uri("lb://bank-account"))
            
            .route(p -> p.path("/currency-exchange/**")
                    .uri("lb://currency-exchange"))
            
            .route(p -> p.path("/crypto-exchange/**")
                    .uri("lb://crypto-exchange"))
            
            .route(p -> p.path("/users/**")
                    .uri("lb://users-service"))
            
            .route("trade_service", r -> r
                    .path("/trade/**")
                    .filters(f -> f.rewritePath("/trade-service/(?<segment>.*)", "/${segment}"))
                    .uri("lb://trade-service"))
            
            .build();
    }
}