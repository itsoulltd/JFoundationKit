package com.infoworks.utils.jwt;

import com.infoworks.objects.MessageParser;
import com.infoworks.utils.jwt.impl.JWebToken;
import com.infoworks.utils.jwt.models.JWTPayload;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JWebTokenTest {

    @Test
    public void tokenTest() {
        JWebToken jwt = new JWebToken();
        JWTPayload payload = new JWTPayload()
                .setIat(Instant.now().toEpochMilli())
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com");
        String secret = UUID.randomUUID().toString();
        String token = jwt.generateToken(secret
                , payload
                , TokenProvider.timeToLive(Duration.ofMinutes(5), TimeUnit.MINUTES));
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, secret);
        Assert.assertTrue(isValid);
        System.out.println("isValid: " + isValid);
    }

    @Test
    public void tokenExpireTest() throws InterruptedException {
        JWebToken jwt = new JWebToken();
        JWTPayload payload = new JWTPayload()
                .setIat(Instant.now().toEpochMilli())
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com")
                .setExp(Instant.now().toEpochMilli());
        String token = jwt.generateToken("What-A-Secret!", payload, null);
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Wait- 2 sec
        Thread.sleep(5000);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, "What-A-Secret!");
        Assert.assertTrue(isValid == false);
        System.out.println("isValid: " + isValid);
    }

    @Test
    public void tokenSecretTest() throws InterruptedException {
        JWebToken jwt = new JWebToken();
        JWTPayload payload = new JWTPayload()
                .setIat(Instant.now().toEpochMilli())
                .setIss("Towhid")
                .setSub("m.towhid@gmail.com");
        String token = jwt.generateToken("What-A-Secret!", payload, TokenProvider.timeToLive(Duration.ofMinutes(5), TimeUnit.MINUTES));
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, "Ohh-A-Secret!");
        Assert.assertTrue(isValid == false);
        System.out.println("isValid: " + isValid);
    }

    //@Test
    public void jwtPayloadTest() throws IOException {
        JWTPayload payload = new JWTPayload().setIss("Towhid").setSub("m.towhid@gmail.com");
        //
        System.out.println(payload.toString());
        System.out.println(MessageParser.marshal(payload));
        //
        System.out.println(MessageParser.getJsonSerializer().writeValueAsString(payload));
        System.out.println(MessageParser.getJsonSerializer().writeValueAsString(payload.toString()));
    }
}
