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

    // simple test, no authorization header = missing
   @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() {
        // AuthenticationFilter filter = new AuthenticationFilter(routeValidator, discoveryClient);

         //Mock discoveryClient service instances
         ServiceInstance authServiceInstance = mock(ServiceInstance.class);
         when(authServiceInstance.getInstanceId()).thenReturn("auth-service");
         when(discoveryClient.getInstances("auth-service"))
             .thenReturn(List.of(authServiceInstance));
             
        System.out.println(discoveryClient.getInstances("auth-service"));

        // exchange and request
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        request = MockServerHttpRequest.get("/new-path").header(HttpHeaders.ACCEPT, "Bearer").build();
        MockServerWebExchange exchange = MockServerWebExchange.from((MockServerHttpRequest) request);
        // MockServerWebExchange exchange = MockServerWebExchange.from(
        // MockServerHttpRequest
        //     .get("/new-path")
        //     .header(HttpHeaders.ACCEPT, "Bearer"));

        // when(exchange.getRequest()).thenReturn(request);

        // routeValidator
        // when(routeValidator.isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        // doReturn(true).when(routeValidator.isSecured).test(request);
        System.out.println(routeValidator.isSecured.test(request));
        verify(routeValidator).isSecured.test(request);

        GatewayFilter actualReturn = authenticationFilter.apply(new AuthenticationFilter.Config());
        
    
        
        GatewayFilterChain filterChain = filterExchange -> Mono.empty();
        

        // System.out.println(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));
        // assertFalse(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));

        // assertThatThrownBy(() -> authenticationFilter.apply(new AuthenticationFilter.Config()))
        //     .isInstanceOf(MissingAuthorizationHeaderException.class);
        
        
        
        // Mocking the exchange and request behavior

        
        
        
        // StepVerifier.create(authenticationFilter.apply(new AuthenticationFilter.Config()).filter(exchange, filterChain))
        //     .verifyComplete();

        // HttpHeaders headers = request.getHeaders();
        // when(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);
        
        
        
        // doReturn(false).when(request).getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
        // when(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)).thenReturn(false);

        
        System.out.println("this is filter: "+ actualReturn.toString());

        

        // Test your filter logic
        
        // when(authenticationFilter.apply(new AuthenticationFilter.Config()))
        //     .thenThrow(MissingAuthorizationHeaderException.class);
            
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
                // .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
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
        // verify(request).getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
        // System.out.print(verify(request).getHeaders().containsKey(HttpHeaders.AUTHORIZATION));

        assertThatThrownBy(() -> authenticationFilter.apply(new AuthenticationFilter.Config()))
            .isInstanceOf(MissingAuthorizationHeaderException.class);
        
        // Verify interactions
        // doReturn(true).when(routeValidator).isSecured.test(request);
        // System.out.println(routeValidator.isSecured.test(request));
        // verify(routeValidator).isSecured.test(request);
        
        
        // verify(filterChain).filter(exchange);
    }
}