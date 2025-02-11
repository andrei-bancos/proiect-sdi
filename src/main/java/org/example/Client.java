package org.example;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Client {
    private String clientId;
    private MQTT mqtt;
    private CallbackConnection connection;
    private NewsList newsList;
    private Gson gson;
    private boolean isConnected = false;
    private boolean processRunning = false;
    private Broker broker; // Referință la broker
    private Set<String> subscribedTopics = new HashSet<>();
    private static final String LOG_FILE = "client_log.txt";

    public Client(String clientId, String brokerUrl, Broker broker) {
        this.clientId = clientId;
        this.broker = broker; // Inițializare broker
        this.newsList = new NewsList();
        this.gson = new Gson();

        setupConnection(brokerUrl);
    }

    private void setupConnection(String brokerUrl) {
        mqtt = new MQTT();
        try {
            mqtt.setHost(brokerUrl);
            mqtt.setClientId(clientId);
            mqtt.setCleanSession(true);
            this.connection = mqtt.callbackConnection();

            this.connection.listener(new Listener() {
                @Override
                public void onConnected() {
                    System.out.println("\n| " + clientId + " conectat.");
                    logToFile(clientId + " s-a conectat la broker.");
                }

                @Override
                public void onDisconnected() {
                    System.out.println("\n| " + clientId + " deconectat.");
                    logToFile(clientId + " s-a deconectat de la broker.");
                    reconnect();
                }

                @Override
                public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                    String message = new String(payload.toByteArray());

                    if (topic.toString().equals("delete_news")) {
                        String id = message.split(":")[1];
                        newsList.deleteNewsById(id);
                        System.out.println("\n| Știrea cu ID " + id + " a fost ștearsă pe acest client.");
                        logToFile(clientId + " a șters știrea cu ID " + id);
                    } else {
                        News news = gson.fromJson(message, News.class);
                        newsList.addNews(news);
                        System.out.println("\n| Stire primită de la topicul " + topic + ": " + news);
                        logToFile(clientId + " a primit stire de la topicul " + topic + ": " + news);
                    }

                    ack.run();
                }



                @Override
                public void onFailure(Throwable value) {
                    System.out.println("\n| " + clientId + " conexiune esuata: " + value.getMessage());
                    logToFile(clientId + " conexiune esuata: " + value.getMessage());
                    reconnect();
                }
            });

            this.connection.connect(new Callback<Void>() {
                @Override
                public void onSuccess(Void value) {
                    System.out.println("\n| " + clientId + " conectat la broker.");
                    isConnected = true;
                    logToFile(clientId + " s-a conectat la broker cu succes.");

                    // Toți clienții se abonează la topicul de ștergere
                    connection.subscribe(new Topic[]{new Topic("delete_news", QoS.EXACTLY_ONCE)}, new Callback<byte[]>() {
                        @Override
                        public void onSuccess(byte[] value) {
                            System.out.println("\n| " + clientId + " s-a abonat la topicul de ștergere.");
                        }

                        @Override
                        public void onFailure(Throwable value) {
                            System.out.println("\n| " + clientId + " eroare la abonarea la topicul de ștergere: " + value.getMessage());
                        }
                    });

                    synchronized (Client.this) {
                        Client.this.notifyAll();
                    }
                }

                @Override
                public void onFailure(Throwable value) {
                    System.out.println("\n| " + clientId + " eroare la conectare: " + value.getMessage());
                    logToFile(clientId + " eroare la conectare: " + value.getMessage());
                    reconnect();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reconnect() {
        isConnected = false;
        synchronized (Client.this) {
            Client.this.notifyAll();
        }

        String nextBrokerUrl = broker.getNextBrokerUrl();
        System.out.println("\n| " + clientId + " reconectare la: " + nextBrokerUrl);
        setupConnection(nextBrokerUrl);

        for (String topic : subscribedTopics) {
            subscribe(topic);
        }
    }

    public void subscribe(String topic) {
        this.connection.subscribe(new Topic[]{new Topic(topic, QoS.EXACTLY_ONCE)}, new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] value) {
                subscribedTopics.add(topic);

                System.out.println("\n|");
                System.out.println("| " + clientId + " abonat la topic: " + topic);
                System.out.println("|\n");

                logToFile(clientId + " s-a abonat la topic: " + topic);

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println("\n| " + clientId + " esuare la abonare: " + value.getMessage());
                logToFile(clientId + " eroare la abonare la topic " + topic + ": " + value.getMessage());

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }
        });
    }

    public void publish(String topic, News news) {
        String message = gson.toJson(news);
        this.connection.publish(topic, message.getBytes(), QoS.EXACTLY_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                System.out.println("\n|");
                System.out.println("| " + clientId + " stire publicata: " + message);
                System.out.println("|\n");

                logToFile(clientId + " a publicat o stire pe topic " + topic + ": " + message);

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println("\n| " + clientId + " eroare la publicare: " + value.getMessage());
                logToFile(clientId + " eroare la publicare pe topic " + topic + ": " + value.getMessage());

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }
        });
    }
    public void deleteNewsById(String id) {
        String message = "DELETE_NEWS:" + id;
        this.connection.publish("delete_news", message.getBytes(), QoS.EXACTLY_ONCE, false, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                System.out.println("\n| Mesaj de ștergere a știrii cu ID " + id + " trimis.");
                logToFile(clientId + " a trimis comanda pentru ștergerea știrii cu ID " + id);
                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println("\n| Eroare la trimiterea comenzii de ștergere: " + value.getMessage());
                logToFile(clientId + " eroare la trimiterea comenzii de ștergere pentru ID " + id + ": " + value.getMessage());
                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }
        });
    }
    public void unsubscribe(String topic) {
        // Convertim String-ul în UTF8Buffer
        UTF8Buffer[] topics = new UTF8Buffer[]{new UTF8Buffer(topic)};

        this.connection.unsubscribe(topics, new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                subscribedTopics.remove(topic);
                System.out.println("\n|");
                System.out.println("| " + clientId + " dezabonat de la topic: " + topic);
                System.out.println("|\n");

                logToFile(clientId + " s-a dezabonat de la topic: " + topic);

                processRunning = false;
                synchronized (Client.this) {
                    Client.this.notifyAll();
                }
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println("\n| " + clientId + " eroare la dezabonare: " + value.getMessage());
                logToFile(clientId + " eroare la dezabonare de la topic " + topic + ": " + value.getMessage());

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
            System.out.println("4. Vizualizare detalii stire");
            System.out.println("5. Ștergere știre");
            System.out.println("6. Colectare știri din SerpAPI");
            System.out.println("7. Exit");


            System.out.print("Alege o optiune: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    processRunning = true;
                    System.out.println("\nSelectează un topic:");
                    System.out.println("1. " + Topics.BLOCKCHAIN);
                    System.out.println("2. " + Topics.AI);
                    System.out.println("3. " + Topics.METAVERSE);
                    System.out.println("4. " + Topics.AUTONOMOUS_CARS);
                    System.out.print("Alegerea ta: ");
                    int topicChoice = scanner.nextInt();
                    scanner.nextLine();

                    String topic = switch (topicChoice) {
                        case 1 -> Topics.BLOCKCHAIN;
                        case 2 -> Topics.AI;
                        case 3 -> Topics.METAVERSE;
                        case 4 -> Topics.AUTONOMOUS_CARS;
                        default -> null;
                    };

                    if (topic != null) {
                        subscribe(topic);
                    } else {
                        System.out.println("Alegere invalidă!");
                    }
                }
                case 2 -> {
                    processRunning = true;
                    System.out.println("\nSelectează un topic:");
                    System.out.println("1. " + Topics.BLOCKCHAIN);
                    System.out.println("2. " + Topics.AI);
                    System.out.println("3. " + Topics.METAVERSE);
                    System.out.println("4. " + Topics.AUTONOMOUS_CARS);
                    System.out.print("Alegerea ta: ");
                    int topicChoice = scanner.nextInt();
                    scanner.nextLine();

                    String topic = switch (topicChoice) {
                        case 1 -> Topics.BLOCKCHAIN;
                        case 2 -> Topics.AI;
                        case 3 -> Topics.METAVERSE;
                        case 4 -> Topics.AUTONOMOUS_CARS;
                        default -> null;
                    };

                    if (topic != null) {
                        System.out.print("Introdu titlu stire: ");
                        String title = scanner.nextLine();
                        System.out.print("Introdu continutul stirii: ");
                        String content = scanner.nextLine();
                        publish(topic, new News(title, content, topic));
                    } else {
                        System.out.println("\n| Alegere invalidă!");
                    }
                }
                case 3 -> {
                    processRunning = true;
                    System.out.println("\nSelectează un topic pentru vizualizarea știrilor:");
                    System.out.println("1. " + Topics.BLOCKCHAIN);
                    System.out.println("2. " + Topics.AI);
                    System.out.println("3. " + Topics.METAVERSE);
                    System.out.println("4. " + Topics.AUTONOMOUS_CARS);
                    System.out.println("5. Vizualizează toate stirile primite.");
                    System.out.print("Alegerea ta: ");
                    int topicChoice = scanner.nextInt();
                    scanner.nextLine();

                    String query = switch (topicChoice) {
                        case 1 -> Topics.BLOCKCHAIN;
                        case 2 -> Topics.AI;
                        case 3 -> Topics.METAVERSE;
                        case 4 -> Topics.AUTONOMOUS_CARS;
                        case 5 -> "ALL";
                        default -> null;
                    };

                    if(Objects.equals(query, "ALL")) {
                        newsList.printAllNews();
                    } else {
                        newsList.printAllNews(query);
                    }

                    processRunning = false;
                }
                case 4 -> {
                    processRunning = true;
                    System.out.print("Introdu id-ul știrii:");
                    String myStringId = scanner.next();
                    newsList.printNewsWithId(myStringId);
                    processRunning = false;
                }
                case 5 -> {
                    processRunning = true;
                    System.out.println("\nȘtergere știre pe baza ID-ului pentru toți clienții.");
                    System.out.print("\nIntroduceți ID-ul știrii: ");
                    String id = scanner.nextLine();
                    deleteNewsById(id);
                }

                case 6 -> {
                    processRunning = true;
                    System.out.println("\nSelectează un topic pentru colectarea știrilor:");
                    System.out.println("1. " + Topics.BLOCKCHAIN);
                    System.out.println("2. " + Topics.AI);
                    System.out.println("3. " + Topics.METAVERSE);
                    System.out.println("4. " + Topics.AUTONOMOUS_CARS);
                    System.out.print("Alegerea ta: ");
                    int topicChoice = scanner.nextInt();
                    scanner.nextLine();

                    String query = switch (topicChoice) {
                        case 1 -> Topics.BLOCKCHAIN;
                        case 2 -> Topics.AI;
                        case 3 -> Topics.METAVERSE;
                        case 4 -> Topics.AUTONOMOUS_CARS;
                        default -> null;
                    };

                    if (query != null) {
                        System.out.print("Introduceți numărul de știri dorit: ");
                        int numResults = scanner.nextInt();
                        scanner.nextLine();

                        List<News> fetchedNews = SerpApiIntegration.fetchNews(query, numResults);
                        for (News news : fetchedNews) {
                            publish(query, news);

                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        System.out.println("\n| Știri colectate cu succes!");
                    } else {
                        System.out.println("\n| Alegere invalidă! Reîncercați.");
                    }
                    processRunning = false;
                }
                case 7 -> {
                    processRunning = true;
                    System.out.println("\nSelectează un topic pentru dezabonare:");
                    System.out.println("1. " + Topics.BLOCKCHAIN);
                    System.out.println("2. " + Topics.AI);
                    System.out.println("3. " + Topics.METAVERSE);
                    System.out.println("4. " + Topics.AUTONOMOUS_CARS);
                    System.out.print("Alegerea ta: ");
                    int topicChoice = scanner.nextInt();
                    scanner.nextLine();

                    String topic = switch (topicChoice) {
                        case 1 -> Topics.BLOCKCHAIN;
                        case 2 -> Topics.AI;
                        case 3 -> Topics.METAVERSE;
                        case 4 -> Topics.AUTONOMOUS_CARS;
                        default -> null;
                    };

                    if (topic != null) {
                        unsubscribe(topic);
                    } else {
                        System.out.println("\n| Alegere invalidă!");
                    }
                }
                case 8 -> {
                    disconnect();
                    System.exit(0);
                }
                default -> System.out.println("\n| Optiuni invalide. Reincearca");

            }
        }
    }

    private void logToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(timestamp + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("\n| Eroare la scrierea în fișierul de log: " + e.getMessage());
        }
    }

    public void disconnect() {
        this.connection.disconnect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                System.out.println("\n| " + clientId + " deconectat.");
            }

            @Override
            public void onFailure(Throwable value) {
                System.out.println("\n| " + clientId + " eroare la deconectare: " + value.getMessage());
            }
        });
    }
}
