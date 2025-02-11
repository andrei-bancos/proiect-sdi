package org.example;
public class App {
    public static void main(String[] args) {
        Broker broker = new Broker();
        Client client = new Client("Client1", broker.getBrokerUrl(), broker);
        client.runInteractiveConsole();
    }
}
