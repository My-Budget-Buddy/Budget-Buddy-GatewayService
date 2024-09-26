package com.skillstorm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.skillstorm.controllers.FallbackController;


@WebFluxTest(FallbackController.class)
public class FallbackControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testCachedUsers() {
        webTestClient.get().uri("/cache/users")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503) // Corrected to check for 503 Service Unavailable
            .expectBody(String.class).isEqualTo("User service is currently unavailable, please try again later.");
    }
    @Test
    public void testCachedTaxes() {
        webTestClient.get().uri("/cache/taxes")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503)  // Expect 503 for Service Unavailable
            .expectBody(String.class).isEqualTo("Tax service is currently unavailable, please try again later.");
    }
    @Test
    public void testCachedAuth() {
        webTestClient.get().uri("/cache/auth")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503)  // Expect 503 for Service Unavailable
            .expectBody(String.class).isEqualTo("Authorization service is currently unavailable, please try again later.");
    }
    @Test
    public void testCachedTransactions() {
        webTestClient.get().uri("/cache/transactions")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503)  // Expect 503 for Service Unavailable
            .expectBody(String.class).isEqualTo("Transaction service is currently unavailable, please try again later.");
    }
    @Test
    public void testCachedAccounts() {
        webTestClient.get().uri("/cache/accounts")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503)  // Expect 503 for Service Unavailable
            .expectBody(String.class).isEqualTo("Account service is currently unavailable, please try again later.");
    }
    @Test
    public void testCachedBudgets() {
        webTestClient.get().uri("/cache/budgets")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503)  // Expect 503 for Service Unavailable
            .expectBody(String.class).isEqualTo("Budget service is currently unavailable, please try again later.");
    }
    @Test
    public void testCachedCredit() {
        webTestClient.get().uri("/cache/credit")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(503)  // Expect 503 for Service Unavailable
            .expectBody(String.class).isEqualTo("Credit service is currently unavailable, please try again later.");
    }
}