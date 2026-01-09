package com.infoworks.utils.jwt;

import com.infoworks.objects.MessageParser;
import com.infoworks.utils.jwt.impl.JWebToken;
import com.infoworks.utils.jwt.models.JWTPayload;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class JWebTokenTest {

    @Test
    public void tokenTest() {
        JWebToken jwt = new JWebToken();
        String token = jwt.generateToken("What-A-Secret!"
                , new JWTPayload().setIss("Towhid").setSub("m.towhid@gmail.com")
                , TokenProvider.timeToLive(Duration.ofMinutes(5), TimeUnit.MINUTES));
        Assert.assertTrue(token != null);
        System.out.println("JWT:" + token);
        //Test-Validation:
        boolean isValid = jwt.isValid(token, "What-A-Secret!");
        Assert.assertTrue(isValid);
        System.out.println("isValid: " + isValid);
    }

    @Test
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
