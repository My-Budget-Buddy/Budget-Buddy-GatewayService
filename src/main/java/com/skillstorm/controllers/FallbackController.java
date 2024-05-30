package com.skillstorm.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class FallbackController {

    @GetMapping("/users")
    public ResponseEntity<String> cachedUsers() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("User service is currently unavailable, please try again later.");
    }

    @GetMapping("/taxes")
    public ResponseEntity<String> cachedTaxes() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Tax service is currently unavailable, please try again later.");
    }

    @GetMapping("/auth")
    public ResponseEntity<String> cachedAuth() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Authorization service is currently unavailable, please try again later.");
    }

    @GetMapping("/transactions")
    public ResponseEntity<String> cachedTransactions() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Transaction service is currently unavailable, please try again later.");
    }

    @GetMapping("/accounts")
    public ResponseEntity<String> cachedAccounts() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Account service is currently unavailable, please try again later.");
    }

    @GetMapping("/budgets")
    public ResponseEntity<String> cachedBudgets() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Budget service is currently unavailable, please try again later.");
    }

    @GetMapping("/credit")
    public ResponseEntity<String> cachedCredit() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Credit service is currently unavailable, please try again later.");
    }
}
