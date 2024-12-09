package org.example;

public class Broker {
    private String brokerUrl;

    public Broker(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        System.out.println("Broker running at " + brokerUrl);
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }
}
