package com.skillstorm.model;

public class JwtValidationDto {

    private String jwtSubject;
    private String jwtClaim;

    public JwtValidationDto() {
    }

    public JwtValidationDto(String jwtSubject, String jwtClaim) {
        this.jwtSubject = jwtSubject;
        this.jwtClaim = jwtClaim;
    }

    public String getJwtSubject() {
        return jwtSubject;
    }

    public void setJwtSubject(String jwtSubject) {
        this.jwtSubject = jwtSubject;
    }

    public String getJwtClaim() {
        return jwtClaim;
    }

    public void setJwtClaim(String jwtClaim) {
        this.jwtClaim = jwtClaim;
    }

}
