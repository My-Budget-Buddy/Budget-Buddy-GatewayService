package com.skillstorm.filter;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.skillstorm.exceptions.MissingAuthorizationHeaderException;

import reactor.core.publisher.Mono;

public class AuthenticationFilterTest {

    
    @Spy
    private RouteValidator routeValidator;

    @Mock
    private DiscoveryClient discoveryClient;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;


    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        //needed to use mock injection
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception{
        closeable.close();
    }

    @Test
    void noAuthServiceInstance(){
        when(discoveryClient.getInstances("auth-service"))
            .thenReturn(List.of());

        assertThatThrownBy(() -> authenticationFilter.apply(new AuthenticationFilter.Config()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("No auth-service instances found");
    }
    @Test
    void testSecuredRouteWithValidToken() {
        // Mock the DiscoveryClient
        ServiceInstance authServiceInstance = mock(ServiceInstance.class);
        when(authServiceInstance.getInstanceId()).thenReturn("auth-service");
        when(authServiceInstance.getUri()).thenReturn(URI.create("http://test-url"));
        when(discoveryClient.getInstances("auth-service"))
            .thenReturn(List.of(authServiceInstance));
            
       System.out.println(authServiceInstance.getInstanceId());

        // Mock the RouteValidator
        // when(routeValidator.isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);

        // Create a mock request and exchange
        ServerHttpRequest request = MockServerHttpRequest.get("/secured-path")
                .header(HttpHeaders.ACCEPT, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from((MockServerHttpRequest) request);

        System.out.println(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));

        //mockRestServiceServer.expect(requestTo("/endpoint")).andRespond(...)
        // mockRestServiceServer.expect(requestTo("http://test-url")).andRespond(...);

        // Mock the GatewayFilterChain
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Apply the authentication filter
        GatewayFilter gatewayFilter = authenticationFilter.apply(new AuthenticationFilter.Config());
        gatewayFilter.filter(exchange, filterChain).block();
        verify (request).getHeaders();
        System.out.print(verify(request).getHeaders().containsKey(HttpHeaders.AUTHORIZATION));

        assertThatThrownBy(() -> authenticationFilter.apply(new AuthenticationFilter.Config()))
            .isInstanceOf(MissingAuthorizationHeaderException.class);
        
        // Verify interactions
        // doReturn(true).when(routeValidator).isSecured.test(request);
        System.out.println(routeValidator.isSecured.test(request));
        verify(routeValidator).isSecured.test(request);
        
        
        // verify(filterChain).filter(exchange);
    }
}