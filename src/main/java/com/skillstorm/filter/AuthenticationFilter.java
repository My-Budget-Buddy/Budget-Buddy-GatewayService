package com.skillstorm.filter;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    // Used to send Http Request to auth server
    RestClient restClient = RestClient.builder()
                            .baseUrl("http://auth-service/auth")
                            .build();
    
    public static class Config {
    }

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return ((exchange, chain) -> {

            if (validator.isSecured.test(exchange.getRequest())) {

                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Authorization header is missing");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

            
                // Send HttpRequest to Auth server in order to validate JWT
                ResponseEntity<Object> response = restClient.get()
                                        .uri("/validate")
                                        .header("Authorization", "Bearer " + authHeader)
                                        .retrieve()
                                        .toEntity(Object.class);

                if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new RuntimeException("Unable to validate Jwt");
                }
            }
            return chain.filter(exchange);
        });
    }
}
