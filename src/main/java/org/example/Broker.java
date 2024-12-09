package org.example;

public class Broker {
    private String brokerUrl = "tcp://localhost:1883";

    public Broker() {
        System.out.println("Broker running at " + brokerUrl);
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }
}
