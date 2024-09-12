package com.skillstorm.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof JwtValidationDto)) {
            return false;
        }
        JwtValidationDto jwtDto = (JwtValidationDto) o;
        return Objects.equals(jwtSubject, jwtDto.jwtSubject) && Objects.equals(jwtClaim, jwtDto.jwtClaim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jwtSubject,jwtClaim);
    }

    @Override
    public String toString() {
        return "{" +
                " subject='" + getJwtSubject() + "'" +
                ", claim='" + getJwtClaim() + "'" ;
    }
}
