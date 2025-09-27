package com.infoworks.utils.ws;

import com.infoworks.utils.ws.spring.SpringSocketTemplate;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class SpringWebSocketTest {

    @Test
    public void basic() {
        SocketTemplate socket = new SpringSocketTemplate(SocketType.Standard);
        socket.setAuthorizationHeader("TOKEN");
        socket.setQueryParam("UserId", "user_name");
        socket.setQueryParam("secret", "app_secret");
        try {
            socket.connect("ws://localhost:8080/process", 0);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            socket.disconnect();
        }
    }

}