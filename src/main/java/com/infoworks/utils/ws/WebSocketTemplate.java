package com.infoworks.utils.ws;

import com.infoworks.objects.Message;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WebSocketTemplate implements SocketTemplate {
    @Override
    public void setAuthorizationHeader(String token) {
        //TODO:
    }

    @Override
    public void setQueryParam(String query, String param) {
        //TODO:
    }

    @Override
    public void connect(String url, long timeoutInSeconds) throws ExecutionException, InterruptedException, TimeoutException {
        //TODO:
    }

    @Override
    public void disconnect() {
        //TODO:
    }

    @Override
    public boolean reconnect() {
        return false;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connectionErrorHandler(Consumer<Throwable> error) {
        //TODO:
    }

    @Override
    public void connectionAcceptedHandler(BiConsumer<Object, Object> afterConnect) {
        //TODO:
    }

    @Override
    public <T extends Message> void subscribe(String topic, Class<T> type, Consumer<T> consumer) {
        //TODO:
    }

    @Override
    public void unsubscribe(String topic) {
        //TODO:
    }

    @Override
    public <T extends Message> void send(String to, T message) {
        //TODO:
    }
}
