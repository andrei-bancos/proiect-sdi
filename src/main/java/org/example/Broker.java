package org.example;

import java.util.List;

public class Broker {
    // Lista URL-urilor de brokeri disponibili
    private List<String> brokerUrls = List.of(
            "tcp://localhost:1883",
            "tcp://localhost:1884",
            "tcp://localhost:1885"
    );
    private int currentBrokerIndex = 0;

    public Broker() {

        System.out.println("Broker-ul rulează pe " + brokerUrls.get(currentBrokerIndex));
    }

    public String getBrokerUrl() {
        return brokerUrls.get(currentBrokerIndex);
    }
    // Trecerea la următorul broker din listă și returnarea noului URL
    public String getNextBrokerUrl() {
        currentBrokerIndex = (currentBrokerIndex + 1) % brokerUrls.size();
        System.out.println("Am schimbat broker-ul pe: " + brokerUrls.get(currentBrokerIndex));
        return brokerUrls.get(currentBrokerIndex);
    }
}
