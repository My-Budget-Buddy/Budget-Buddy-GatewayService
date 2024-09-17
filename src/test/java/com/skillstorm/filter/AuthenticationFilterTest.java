package com.skillstorm.filter;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestTemplate;

import com.skillstorm.model.JwtValidationDto;

import reactor.core.publisher.Mono;

public class AuthenticationFilterTest {

    
    @Spy
    private RouteValidator routeValidator;

    @Autowired
     private MockRestServiceServer server;

    @Mock
    private RestClient restClient;

    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<JwtValidationDto> responseEntity;

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
    void testSecuredRouteWithValidToken_NoAuthorizationHeader() {
        //test to see if no authorization header is present

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
                .header(HttpHeaders.LINK, "Bearer valid-token")
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
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());


        // assertThatThrownBy(() -> authenticationFilter.apply(new AuthenticationFilter.Config()))
        // .isInstanceOf(MissingAuthorizationHeaderException.class);
    
        // verify(request).getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
        // System.out.print(verify(request).getHeaders().containsKey(HttpHeaders.AUTHORIZATION));


        // Verify interactions
        // doReturn(true).when(routeValidator).isSecured.test(request);
        // System.out.println(routeValidator.isSecured.test(request));
        // verify(routeValidator).isSecured.test(request);
        
        // verify(filterChain).filter(exchange);
    }
    @Test
    // @Disabled
    void testAuthorizationheader_and_BearerJWT_with_exception() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        /*
        Test case when Authorization header is present and Bearer JWT is present
        and the JWT validation fails
        */            

        // Mock the DiscoveryClient
        ServiceInstance authServiceInstance = mock(ServiceInstance.class);
        when(authServiceInstance.getInstanceId()).thenReturn("auth-service");
        when(authServiceInstance.getUri()).thenReturn(URI.create("http://test-url"));
        when(discoveryClient.getInstances("auth-service"))
            .thenReturn(List.of(authServiceInstance));
            
        System.out.println(authServiceInstance.getInstanceId());

        // Create a mock request and exchange
        ServerHttpRequest request = MockServerHttpRequest.get("/secured-path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from((MockServerHttpRequest) request);

        // Mocking restClient
        RestClient restClient = mock(RestClient.class);
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RequestHeadersSpec requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        // Inject the restClient into the authentication filter
        Field restClientField = AuthenticationFilter.class.getDeclaredField("restClient");
        restClientField.setAccessible(true);
        restClientField.set(authenticationFilter, restClient);
        
        // Expected response entity
        // JwtValidationDto expectedDto = new JwtValidationDto();
        // ResponseEntity<JwtValidationDto> responseEntity = new ResponseEntity<>(expectedDto, HttpStatus.UNAUTHORIZED);
        ResponseEntity<JwtValidationDto> responseEntity = mock(ResponseEntity.class);
        JwtValidationDto expectedDto = new JwtValidationDto();
        when(responseEntity.getBody()).thenReturn(expectedDto);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);

        // Mock the restClient GET response
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(JwtValidationDto.class)).thenReturn(responseEntity);
        
        // Mock the GatewayFilterChain
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Apply the authentication filter
        GatewayFilter gatewayFilter = authenticationFilter.apply(new AuthenticationFilter.Config());
        // gatewayFilter.filter(exchange, filterChain).block();
        
        // Assert the response is as expected when the JWT validation fails
        assertThatThrownBy(() -> gatewayFilter.filter(exchange, filterChain).block())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unable to validate Jwt");

    }
    @Disabled
    @Test
    void testNoAuthServiceInstance() {
        // Mock the DiscoveryClient to return null or empty list
        // Mock the DiscoveryClient to return a list with a null service instance URI
        ServiceInstance authServiceInstance = mock(ServiceInstance.class);
        when(authServiceInstance.getInstanceId()).thenReturn("auth-service");
        when(authServiceInstance.getUri()).thenReturn(URI.create("http:///tax-service"));
        when(discoveryClient.getInstances("auth-service"))
            .thenReturn(List.of(authServiceInstance));
            
        when(discoveryClient.getInstances("auth-service")).thenReturn(List.of());

        
        MockServerHttpRequest request = MockServerHttpRequest.get("/secured-path")
            .header(HttpHeaders.AUTHORIZATION, "Bearer some-valid-token")
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Apply the authentication filter
        GatewayFilter gatewayFilter = authenticationFilter.apply(new AuthenticationFilter.Config());
        gatewayFilter.filter(exchange, filterChain).block();


        // Assert the response is as expected when there is no auth service instance
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
    }
    
}