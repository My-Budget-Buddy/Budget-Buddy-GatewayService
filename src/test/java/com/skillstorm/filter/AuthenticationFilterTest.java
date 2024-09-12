package com.skillstorm.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;

import org.apache.hc.core5.http.HttpHeaders;

import com.skillstorm.exceptions.MissingAuthorizationHeaderException;
import com.skillstorm.filter.AuthenticationFilter;
import com.skillstorm.filter.RouteValidator;
import com.skillstorm.model.JwtValidationDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Predicate;

public class AuthenticationFilterTest {

    
    //Inject mocks into object we are testing
    @InjectMocks
    AuthenticationFilter authenticationFilter;


    //mock up dependencies
    @Mock
    private RouteValidator routeValidator;

    @Mock
    private Predicate<ServerHttpRequest> isSecured;

    @Mock
    private DiscoveryClient discoveryClient;

    @Mock
    private GatewayFilterChain filterChain;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private HttpHeaders headers;



    @BeforeEach
    public void setup() {
        //needed to use mock injection
        MockitoAnnotations.openMocks(this);
    }

    //simple test, no authorization header = missing
   @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() {
        // Mocking RouteValidator's predicate behavior
        when(routeValidator.isSecured).thenReturn(isSecured);
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);

        // Mocking the exchange and request behavior
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(headers.containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);

        // Test your filter logic
        AuthenticationFilter filter = new AuthenticationFilter(routeValidator, discoveryClient);

        StepVerifier.create(filter.apply(new AuthenticationFilter.Config()).filter(exchange, chain))
            .expectError(MissingAuthorizationHeaderException.class)
            .verify();
    }
}
