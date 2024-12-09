package org.example;
public class App {
    public static void main(String[] args) {
        String brokerUrl = "tcp://localhost:1883";
        Client client = new Client("ConsoleClient", brokerUrl);
        client.runInteractiveConsole();
    }
}
