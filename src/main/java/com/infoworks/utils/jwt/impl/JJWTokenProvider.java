package com.infoworks.utils.jwt.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infoworks.objects.MessageParser;
import com.infoworks.utils.jwt.TokenProvider;
import com.infoworks.utils.jwt.models.JWTHeader;
import com.infoworks.utils.jwt.models.JWTPayload;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class JJWTokenProvider implements TokenProvider {

    protected static Logger LOG = Logger.getLogger(JJWTokenProvider.class.getSimpleName());
    private SignatureAlgorithm sigAlgo = SignatureAlgorithm.HS256;

    public SignatureAlgorithm getSigAlgo() {
        return sigAlgo;
    }

    public JJWTokenProvider setSigAlgo(SignatureAlgorithm sigAlgo) {
        this.sigAlgo = sigAlgo;
        return this;
    }

    protected void updateHeaderAlgo(JWTHeader header) {
        if (header != null && getSigAlgo() != null) {
            header.setAlg(getSigAlgo().name());
        }
    }

    @Override
    public Key generateKey(String... args) {
        StringBuilder buffer = new StringBuilder();
        Arrays.stream(args).forEach(str -> buffer.append(str));
        byte[] hash = buffer.toString().getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = Keys.hmacShaKeyFor(hash); //returning javax.crypto.SecretKey
        return secretKey;
    }

    @Override
    public String generateToken(String secret, JWTHeader header, JWTPayload payload) throws RuntimeException {
        try {
            updateHeaderAlgo(header);
            if (payload.getExp() <= 0l) {
                Calendar timeToLive = TokenProvider.timeToLive(Duration.ofHours(1), TimeUnit.HOURS);
                payload.setExp(timeToLive.getTimeInMillis());
            }
            String jwtToken = generateJWToken(secret, header, payload);
            return jwtToken;
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String refreshToken(String secret, String token, Calendar timeToLive) throws RuntimeException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Token format");
        }
        //Payload:
        JWTPayload payload = TokenProvider.parsePayload(token, JWTPayload.class);
        if (payload == null) {
            throw new RuntimeException("Payload is Empty: ");
        }
        if (payload.getExp() <= 0l) {
            throw new RuntimeException("Payload doesn't contain expiry " + payload);
        }
        //Header:
        JWTHeader header = TokenProvider.parseHeader(token, JWTHeader.class);
        try {
            payload.setExp(timeToLive.getTimeInMillis());
            String jwtToken = generateJWToken(secret, header, payload);
            return jwtToken;
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected final String generateJWToken(String secret, JWTHeader header, JWTPayload payload) {
        try {
            Key key = generateKey(secret);
            JwtBuilder builder = Jwts.builder().signWith(getSigAlgo(), key);
            //
            if(header != null) builder.setHeaderParams(header.marshalling(true));
            if(payload != null) {
                if (payload.getIat() <= 0l) {
                    payload.setIat(new Date().getTime());
                }
                builder.setPayload(convert(payload));
            } else {
                Calendar timeToLive = TokenProvider.timeToLive(Duration.ofHours(1), TimeUnit.HOURS);
                builder.setIssuedAt(new Date())
                        .setExpiration(timeToLive.getTime());
            }
            //
            return builder.compact();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
        return null;
    }

    private String convert(JWTPayload payload) {
        if (payload != null){
            try {
                return MessageParser.getJsonSerializer().writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                LOG.warning(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void makeExpire() throws RuntimeException {
        /**/
    }

    @Override
    public void dispose() {
        /**/
    }

    @Override
    public Boolean isValid(String token, String... args) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Token format");
        }
        //Signature & Secret:
        JWTHeader header = TokenProvider.parseHeader(token, JWTHeader.class);
        String signature = parts[2];
        String secret = getSecret(header, args);
        //Check: Validation
        try {
            SecretKey key = (SecretKey) generateKey(secret);
            //Version till: 0.12.x
            /*Jws<Claims> cl = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            Claims claims =  cl.getBody();*/
            //Version from: 0.13.x
            Jws<Claims> cl = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            Claims claims =  cl.getPayload();
            LOG.info("JWT Claims: " + claims.toString());
            boolean isExpire = hasExpire(header, claims);
            return isExpire;
        } catch (Exception e) {
            LOG.warning(e.getMessage());
        }
        return false;
    }

    protected String getSecret(JWTHeader header, String...args) throws RuntimeException{
        StringBuffer buffer = new StringBuffer();
        Arrays.stream(args).forEach(str -> buffer.append(str));
        String secret = buffer.toString();
        if (secret == null || secret.isEmpty())
            throw new RuntimeException("Secret must not null or empty");
        return secret;
    }

    protected Boolean hasExpire(JWTHeader header, Claims claims) {
        //boolean notExpire = claims.getExpiration().getTime() > (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)); //token not expired
        long expiration = claims.getExpiration().getTime() / 1000L;
        boolean notExpire = expiration > (Instant.now().toEpochMilli()); //token not expired
        return notExpire;
    }

    @Override
    public void close() throws Exception {
        /**/
    }
}
