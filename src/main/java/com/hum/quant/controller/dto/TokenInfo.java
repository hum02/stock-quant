package com.hum.quant.controller.dto;

public class TokenInfo {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String acess_token_token_expired;

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public String getAcess_token_token_expired() {
        return acess_token_token_expired;
    }
}
