package org.example;
public class App {
    public static void main(String[] args) {
        Broker broker = new Broker();
        Client client = new Client("Client", broker.getBrokerUrl(), broker);
        client.runInteractiveConsole();
    }
}
