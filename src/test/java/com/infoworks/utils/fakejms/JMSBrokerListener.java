package com.infoworks.utils.fakejms;

public interface JMSBrokerListener {
    void startListener(String message);
    void abortListener(String message);
}
