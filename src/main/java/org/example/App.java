package org.example;
public class App {
    public static void main(String[] args) {
        Broker broker= new Broker();
        Client client = new Client("ConsoleClient", broker.getBrokerUrl());
        client.runInteractiveConsole();
    }
}
