package org.oppexai.dto.response;

public class AuthResponse {

    private String token;
    private String email;
    private Boolean isVerified;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, Boolean isVerified, String message) {
        this.token = token;
        this.email = email;
        this.isVerified = isVerified;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "email='" + email + '\'' +
                ", isVerified=" + isVerified +
                ", message='" + message + '\'' +
                ", token='[PRESENT]'" +
                '}';
    }
}