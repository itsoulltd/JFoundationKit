package com.infoworks.utils.jwt.impl;

import com.infoworks.utils.jwt.TokenProvider;
import com.infoworks.utils.jwt.models.JWTHeader;
import com.infoworks.utils.jwt.models.JWTPayload;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWebToken implements TokenProvider {

    private static Logger LOG = Logger.getLogger(JWebToken.class.getSimpleName());
    private static final String HMAC_ALGO = "HmacSHA256";
    private final JWTHeader header = new JWTHeader().setAlg("HS256").setTyp("JWT");

    @Override
    public Key generateKey(String... args) {
        StringBuilder buffer = new StringBuilder();
        Arrays.stream(args).forEach(str -> buffer.append(str));
        byte[] hash = buffer.toString().getBytes(StandardCharsets.UTF_8);
        Key secretKey = new SecretKeySpec(hash, HMAC_ALGO);
        return secretKey;
    }

    @Override
    public String generateToken(String secret, JWTPayload payload, Calendar timeToLive) throws RuntimeException {
        String encodedHeader = TokenProvider.encode(header.toString());
        if (payload.getExp() <= 0l) payload.setExp(timeToLive.getTimeInMillis());
        String signature = hmacSha256(secret, payload);
        return encodedHeader + "." + TokenProvider.encode(payload.toString()) + "." + signature;
    }

    private String hmacSha256(String secret, JWTPayload payload) {
        try {
            Key secretKey = generateKey(secret);
            Mac sha256Hmac = Mac.getInstance(HMAC_ALGO);
            sha256Hmac.init(secretKey);
            //
            String data = TokenProvider.encode(header.toString()) + "." + TokenProvider.encode(payload.toString());
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return TokenProvider.encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public String refreshToken(String token, Calendar timeToLive) throws RuntimeException {
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
    public void close() throws Exception {
        /**/
    }

    @Override
    public Boolean isValid(String token, String...args) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Token format");
        }
        //
        JWTPayload payload = TokenProvider.parsePayload(token, JWTPayload.class);
        if (payload == null) {
            throw new RuntimeException("Payload is Empty: ");
        }
        if (payload.getExp() <= 0l) {
            throw new RuntimeException("Payload doesn't contain expiry " + payload);
        }
        //Signature & Secret:
        String signature = parts[2];
        String secret = getSecret(TokenProvider.parseHeader(token, JWTHeader.class), args);
        //Check: Validation
        boolean notExpire = payload.getExp() > (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)); //token not expired
        boolean signatureMatched = signature.equals(hmacSha256(secret, payload)); //signature matched
        return notExpire && signatureMatched;
    }

    protected String getSecret(JWTHeader header, String...args) throws RuntimeException{
        StringBuffer buffer = new StringBuffer();
        Arrays.stream(args).forEach(str -> buffer.append(str));
        String secret = buffer.toString();
        if (secret == null || secret.isEmpty())
            throw new RuntimeException("Secret must not null or empty");
        return secret;
    }
}
