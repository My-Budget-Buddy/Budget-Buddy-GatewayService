package com.skillstorm.filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {
    
    // List of all api endpoints that the Gateway Filter will ignore. These endpoints will not need an access token passed in the header of the request
    public static final List<String> openApiEndpoints = List.of(
        "/auth/register", // How will we add new user to both auth and user dbs?
        "/auth/login",
        "/auth/login/oauth2",
        "/auth/validate"
    );

    public Predicate<ServerHttpRequest> isSecured = 
        request -> openApiEndpoints
                .stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
