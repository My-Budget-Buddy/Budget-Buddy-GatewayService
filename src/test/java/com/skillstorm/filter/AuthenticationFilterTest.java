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
import static org.mockito.Mockito.verify;
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

    private ServiceInstance authServiceInstance;

    @BeforeEach
    public void setup() {
        //needed to use mock injection
        closeable = MockitoAnnotations.openMocks(this);

        // Mock the DiscoveryClient
        authServiceInstance = mock(ServiceInstance.class);
        when(authServiceInstance.getInstanceId()).thenReturn("auth-service");
        when(authServiceInstance.getUri()).thenReturn(URI.create("http://test-url"));
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
    void testNoAuthorizationHeader() {
         /*
        Test case when Authorization header is not present
        */      

        // Mock the DiscoveryClient
        when(discoveryClient.getInstances("auth-service"))
            .thenReturn(List.of(authServiceInstance));      
       System.out.println(authServiceInstance.getInstanceId());

       
        // Create a mock request (no authorization header) and exchange
        ServerHttpRequest request = MockServerHttpRequest.get("/secured-path")
                .header(HttpHeaders.LINK, "Bearer valid-token")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from((MockServerHttpRequest) request);
        System.out.println(request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION));

        // Mock the GatewayFilterChain
        GatewayFilterChain filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Apply the authentication filter
        GatewayFilter gatewayFilter = authenticationFilter.apply(new AuthenticationFilter.Config());
        gatewayFilter.filter(exchange, filterChain).block();
        

        // Get buffer message from exchange response
        Mono<String> responseBody = exchange.getResponse().getBodyAsString();
        // Assert the response is as expected when the Authorization header is missing
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("Unauthorized: Missing Authorization Header.", responseBody.block());

    }
    @Test
    // @Disabled
    void testAuthorizationHeader_and_InvalidJWTHeader() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        /*
        Test case when Authorization header is present and Bearer JWT is present
        and the JWT validation fails
        */            

        // Mock the DiscoveryClient
        when(discoveryClient.getInstances("auth-service"))
            .thenReturn(List.of(authServiceInstance));
        System.out.println(authServiceInstance.getInstanceId());

        // Create a mock request (with authorization header) and exchange
        ServerHttpRequest request = MockServerHttpRequest.get("/secured-path")
                .header(HttpHeaders.AUTHORIZATION, "valid-token")
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
        JwtValidationDto expectedDto = mock(JwtValidationDto.class);
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
    @Test
    // @Disabled
    void testAuthorizationHeader_and_EmptyResponseBody() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        /*
        Test case when Authorization header is present and the response does not contain
        a JWTValidationDto object
        */            

        // Mock the DiscoveryClient
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
        JwtValidationDto expectedDto = mock(JwtValidationDto.class);
        when(responseEntity.getBody()).thenReturn(null);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

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
        
        // Assert the response is as expected when the response body is empty
        assertThatThrownBy(() -> gatewayFilter.filter(exchange, filterChain).block())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Response body empty");

    }
    @Test
    // @Disabled
    void testAuthorizationHeader_and_MutateRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        /*
        Test case when Authorization header is present and request was mutated to include 
        JWT claim in the header
        */       
        
        // Mock DiscoveryClient  
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
        JwtValidationDto expectedDto = mock(JwtValidationDto.class);
        when(expectedDto.getJwtClaim()).thenReturn("user-id-123");
        when(responseEntity.getBody()).thenReturn(expectedDto);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

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
        gatewayFilter.filter(exchange, filterChain).block();
        
        // Verify that the JwtClaim tokenUserId was executed
        verify(responseEntity.getBody()).getJwtClaim();
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