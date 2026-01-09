package com.infoworks.utils.jwt.impl;

import com.infoworks.utils.jwt.TokenProvider;

import java.security.Key;
import java.util.Calendar;

public class JWebToken implements TokenProvider {

    @Override
    public Key generateKey(String... args) {
        return null;
    }

    @Override
    public String generateToken(Calendar timeToLive) throws RuntimeException {
        return null;
    }

    @Override
    public String refreshToken(String token, Calendar timeToLive) throws RuntimeException {
        return null;
    }

    @Override
    public void makeExpire() throws RuntimeException {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public Boolean isValid(String token, String...args) {
        return null;
    }
}
