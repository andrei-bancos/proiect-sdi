package org.example;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import com.google.gson.Gson;

import java.util.Scanner;

public class Client {
    private String clientId;
    private MQTT mqtt;
    private CallbackConnection connection;
    private NewsList newsList;
    private Gson gson;
    private boolean isConnected = false;
    private boolean processRunning = false;

    public Client(String clientId, String brokerUrl) {
        this.clientId = clientId;
        this.newsList = new NewsList();
        this.gson = new Gson();

        mqtt = new MQTT();
        try {
            mqtt.setHost(brokerUrl);
            mqtt.setClientId(clientId);
            this.connection = mqtt.callbackConnection();

            // Setare listener
            this.connection.listener(new Listener() {
                @Override
                public void onConnected() {
                    System.out.println(clientId + " conectat.");
                }

                @Override
                public void onDisconnected() {
                    System.out.println(clientId + " deconectat.");
                }

                @Override
                public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                    String message = new String(payload.toByteArray());
                    News news = gson.fromJson(message, News.class);
                    newsList.addNews(news);
                    System.out.println(clientId + " stiri primite de la " + topic + ": " + news);
                    ack.run();
                }

                @Override
                public void onFailure(Throwable value) {
                    System.out.println(clientId + " conexiune esuata: " + value.getMessage());
                }
            });

            this.connection.connect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    System.out.println(clientId + " conectat la broker.");
                    isConnected = true;
                    synchronized (Client.this) {
                        Client.this.notifyAll();
                    }
                }

                @Override
                public void onFailure(Throwable value) {
                    System.out.println(clientId + " eroare la conectare: " + value.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        this.connection.subscribe(new Topic[]{new Topic(topic, QoS.AT_LEAST_ONCE)}, new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                System.out.println(clientId + " abonat la topic: " + topic);

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println(clientId + " esuare la abonare: " + value.getMessage());

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }
        });
    }

    public void publish(String topic, News news) {
        String message = gson.toJson(news);
        this.connection.publish(topic, message.getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                System.out.println(clientId + " stire publicata: " + message);

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println(clientId + " eroare la publicare: " + value.getMessage());

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }
        });
    }

    public void runInteractiveConsole() {
        synchronized (this) {
            while (!isConnected) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            synchronized (this) {
                if (processRunning) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("\nOptiuni:");
            System.out.println("1. Abonare la topic");
            System.out.println("2. Publicare stire noua");
            System.out.println("3. Vizualizare stiri primite");
            System.out.println("4. Exit");
            System.out.print("Alege o optiune: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consumă newline

            switch (choice) {
                case 1 -> {
                    processRunning = true;
                    System.out.print("Nume topic: ");
                    String topic = scanner.nextLine();
                    subscribe(topic);
                }
                case 2 -> {
                    processRunning = true;
                    System.out.print("Nume topic: ");
                    String topic = scanner.nextLine();
                    System.out.print("Introdu titlu stire: ");
                    String title = scanner.nextLine();
                    System.out.print("Introdu continutul stirii: ");
                    String content = scanner.nextLine();
                    publish(topic, new News(title, content));
                }
                case 3 -> newsList.printAllNews();
                case 4 -> {
                    disconnect();
                    System.exit(0);
                }
                default -> System.out.println("Optiuni invalide. Reincearca");
            }
        }
    }

    public void disconnect() {
        this.connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                System.out.println(clientId + " deconectat.");
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println(clientId + " eroare la deconectare: " + value.getMessage());
            }
        });
    }
}
