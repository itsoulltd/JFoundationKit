package com.infoworks.utils.jwt;

import com.infoworks.utils.jwt.impl.JJWTokenProvider;
import com.infoworks.utils.jwt.models.JWTHeader;
import com.infoworks.utils.jwt.models.JWTPayload;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

public class JJWTokenProviderTest {

    @Test
    public void tokenTest() {
        JJWTokenProvider jwt = new JJWTokenProvider();
        JWTHeader header = new JWTHeader().setAlg("HS256").setTyp("JWT");
        JWTPayload payload = new JWTPayload()
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com")
                .setIat(Instant.now().toEpochMilli());
        String secret = UUID.randomUUID().toString();
        String token = jwt.generateToken(secret, header, payload);
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, secret);
        Assert.assertTrue(isValid);
        System.out.println("isValid: " + isValid);
    }

    @Test
    public void tokenExpireTest() throws InterruptedException {
        JJWTokenProvider jwt = new JJWTokenProvider();
        JWTHeader header = new JWTHeader().setAlg("HS256").setTyp("JWT");
        JWTPayload payload = new JWTPayload()
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com")
                .setIat(Instant.now().toEpochMilli())
                .setExp(Instant.now().toEpochMilli());
        String token = jwt.generateToken("my-super-secret-key-that-is-at-least-32-bytes-long", header, payload);
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Wait- 2 sec
        Thread.sleep(5000);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, "my-super-secret-key-that-is-at-least-32-bytes-long");
        Assert.assertTrue(isValid == false);
        System.out.println("isValid: " + isValid);
    }

    @Test
    public void tokenSecretTest() throws InterruptedException {
        JJWTokenProvider jwt = new JJWTokenProvider();
        JWTHeader header = new JWTHeader().setAlg("HS256").setTyp("JWT");
        JWTPayload payload = new JWTPayload()
                .setIat(Instant.now().toEpochMilli())
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com");
        String token = jwt.generateToken("my-super-secret-key-that-is-at-least-32-bytes-long", header, payload);
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, "Ohh-my-super-secret-key-that-is-at-least-32-bytes-long");
        Assert.assertTrue(isValid == false);
        System.out.println("isValid: " + isValid);
    }

    @Test
    public void tokenSimpleTest() throws InterruptedException {
        JJWTokenProvider jwt = new JJWTokenProvider();
        //
        String algo = jwt.getSigAlgo().name();
        Assert.assertEquals("HS256", algo);
        //
        JWTHeader header = new JWTHeader().setAlg("HS256-xyz").setTyp("JWT").setKid("1");
        JWTPayload payload = new JWTPayload()
                .setIat(Instant.now().toEpochMilli())
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com");
        String token = jwt.generateToken("my-super-secret-key-that-is-at-least-32-bytes-long", header, payload);
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, "my-super-secret-key-that-is-at-least-32-bytes-long");
        Assert.assertTrue(isValid);
        System.out.println("isValid: " + isValid);
    }

}