package com.skillstorm.filter;

import com.netflix.discovery.converters.Auto;
import com.skillstorm.model.JwtValidationDto;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Objects;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    // @Autowired
    // private LoadBalancerClient loadBalancerClient;

    private final RouteValidator validator;
    private final DiscoveryClient discoveryClient;
    
    public static class Config {
    }

    public AuthenticationFilter(RouteValidator validator, DiscoveryClient discoveryClient) {
        super(Config.class);
        this.validator = validator;
        this.discoveryClient = discoveryClient;
    }

    // Used to send Http Request to auth server
    RestClient restClient = RestClient.builder()
            // .baseUrl("http://auth-service/auth")
            .build();

    @Override
    public GatewayFilter apply(Config config) {
        // ServiceInstance instance = loadBalancerClient.choose("auth-service");

        List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
        ServiceInstance instance = instances
                .stream().findAny()
                .orElseThrow(() -> new IllegalStateException("No auth-service instances found"));

        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                ServerHttpRequest request = exchange.getRequest();

                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Authorization header is missing");
                }

                // Verify that an Authorization header exists with a Bearer JWT
                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                // Send HttpRequest to Auth server in order to validate JWT
                if (instance == null) {
                    throw new IllegalStateException("No auth-service instance available");
                }

                String serviceUrl = instance.getUri().toString().concat("/auth/validate");
                ResponseEntity<JwtValidationDto> response = restClient.get()
                                    .uri(serviceUrl)
                                    .header("Authorization", "Bearer " + authHeader)
                                    .retrieve()
                                    .toEntity(JwtValidationDto.class);

                if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    throw new RuntimeException("Unable to validate Jwt");
                }

                if (response.getBody() == null) {
                    throw new RuntimeException("Response body empty");
                }

                // Mutate the request to add an ID header that other services will use to authorize requests
                // involving the user ID as a path parameter
                String tokenUserId = response.getBody().getJwtClaim();
                ServerHttpRequest modifiedRequest = exchange.getRequest()
                        .mutate()
                        .header("User-ID", tokenUserId)
                        .build();
                exchange = exchange.mutate().request(modifiedRequest).build();
            }

            return chain.filter(exchange);
        });
    }
}
