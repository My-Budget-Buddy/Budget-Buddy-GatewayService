package com.skillstorm.filter;

import java.util.List;

import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.skillstorm.exceptions.AuthServiceUnavailableException;
import com.skillstorm.exceptions.MissingAuthorizationHeaderException;
import com.skillstorm.model.JwtValidationDto;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

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
    RestClient restClient = RestClient.builder().build();

    @Override
    public GatewayFilter apply(Config config) {
        List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
        ServiceInstance instance = instances
                .stream().findAny()
                .orElseThrow(() -> new IllegalStateException("No auth-service instances found"));

        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {

                try {
                    ServerHttpRequest request = exchange.getRequest();

                    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        throw new MissingAuthorizationHeaderException();
                    }

                    // Verify that an Authorization header exists with a Bearer JWT
                    String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        authHeader = authHeader.substring(7);
                    }

                    // Send HttpRequest to Auth server in order to validate JWT
                    if (instance == null) {
                        throw new AuthServiceUnavailableException();
                    }
                    
                    System.out.println("Now attempting to get JWT validation from auth server");
                    String serviceUrl = instance.getUri().toString().concat("/auth/validate");

                    System.out.println("concat the string now time to use the rest client");
                    ResponseEntity<JwtValidationDto> response = restClient.get()
                            .uri(serviceUrl)
                            .header("Authorization", "Bearer " + authHeader)
                            .retrieve()
                            .toEntity(JwtValidationDto.class);
                    System.out.println("made the response, let's see what happens");
                    if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        throw new RuntimeException("Unable to validate Jwt");
                    }

                    if (response.getBody() == null) {
                        throw new RuntimeException("Response body empty");
                    }

                    // Mutate the request to add an ID header that other services will use to
                    // authorize requests
                    // involving the user ID as a path parameter
                    String tokenUserId = response.getBody().getJwtClaim();
                    ServerHttpRequest modifiedRequest = exchange.getRequest()
                            .mutate()
                            .header("User-ID", tokenUserId)
                            .build();
                    exchange = exchange.mutate().request(modifiedRequest).build();

                } catch (HttpClientErrorException.Unauthorized e) {
                    // Return a 401 Unauthorized if the auth service returns 401, previously
                    // returned 500 server error
                    // I'm also not sure if there is a cleaner way to modify the response text
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                            .wrap("Unauthorized: Invalid JWT".getBytes());

                    return exchange.getResponse().writeWith(Mono.just(buffer));
                } catch (MissingAuthorizationHeaderException e) {
                    // Return a 401 Unauthorized no JWT was sent, previously returned 500 server err
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                            .wrap("Unauthorized: Missing Authorization Header.".getBytes());

                    return exchange.getResponse().writeWith(Mono.just(buffer));
                }

            }

            return chain.filter(exchange);
        });
    }
}
