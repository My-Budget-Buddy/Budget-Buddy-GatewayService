package com.skillstorm.filter;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.skillstorm.service.TokenService;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private TokenService tokenService;

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

                // Decode Jwt and check the user Id against the header id
                Jwt decodedJwt = tokenService.decodeJwt(authHeader);
                String tokenUserId = decodedJwt.getClaim("userId");

                if (!exchange.getRequest().getHeaders().containsKey("ID")) {
                    throw new RuntimeException("Id header is missing");
                }

                String idHeader = exchange.getRequest().getHeaders().get("ID").get(0);

                if (idHeader != tokenUserId) {
                    throw new RuntimeException("Token id does not match provided id");
                }

            }
            return chain.filter(exchange);
        });
    }
}
