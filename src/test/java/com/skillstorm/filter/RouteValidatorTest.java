package com.skillstorm.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.server.reactive.ServerHttpRequest;

class RouteValidatorTest {

    private RouteValidator routeValidator;
    private ServerHttpRequest mockRequest;

    @BeforeEach
    void setUp() {
        routeValidator = new RouteValidator();
        mockRequest = mock(ServerHttpRequest.class);
    }


    @Test
    void testIsSecuredForOpenApiEndpoints() {
        //Check to see path is in list
        when(mockRequest.getURI()).thenReturn(URI.create("/auth/login"));
        boolean result = routeValidator.isSecured.test(mockRequest);
        assertFalse(result, "Secured");

        // Test fake path
        when(mockRequest.getURI()).thenReturn(URI.create("/auth/fake"));
        result = routeValidator.isSecured.test(mockRequest);
        assertTrue(result, "Not secured");
    }
}