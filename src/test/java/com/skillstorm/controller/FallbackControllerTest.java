package com.skillstorm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.skillstorm.controllers.FallbackController;
import com.skillstorm.filter.RouteValidator;


public class FallbackControllerTest {

    @InjectMocks
    private FallbackController fallbackController;

    AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        fallbackController = new FallbackController();
    }

    @AfterEach
    public void teardown() throws Exception{
        closeable.close();
    }

    @Test
    public void testCachedUsers() {
        ResponseEntity<String> response = fallbackController.cachedUsers();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("User service is currently unavailable, please try again later.", response.getBody());
    }

    @Test
    public void testCachedTaxes() {
        ResponseEntity<String> response = fallbackController.cachedTaxes();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Tax service is currently unavailable, please try again later.", response.getBody());
    }

    @Test
    public void testCachedAuth() {
        ResponseEntity<String> response = fallbackController.cachedAuth();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Authorization service is currently unavailable, please try again later.", response.getBody());
    }

    @Test
    public void testCachedTransactions() {
        ResponseEntity<String> response = fallbackController.cachedTransactions();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Transaction service is currently unavailable, please try again later.", response.getBody());
    }

    @Test
    public void testCachedAccounts() {
        ResponseEntity<String> response = fallbackController.cachedAccounts();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Account service is currently unavailable, please try again later.", response.getBody());
    }

    @Test
    public void testCachedBudgets() {
        ResponseEntity<String> response = fallbackController.cachedBudgets();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Budget service is currently unavailable, please try again later.", response.getBody());
    }

    @Test
    public void testCachedCredit() {
        ResponseEntity<String> response = fallbackController.cachedCredit();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Credit service is currently unavailable, please try again later.", response.getBody());
    }
}