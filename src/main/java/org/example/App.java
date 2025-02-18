package org.example;
public class App {
    public static void main(String[] args) {
        Broker broker = new Broker();
        // Crearea unui client cu un nume, URL-ul brokerului și referința la broker
        Client client = new Client("Razvan", broker.getBrokerUrl(), broker);
        client.runInteractiveConsole();
    }
}
