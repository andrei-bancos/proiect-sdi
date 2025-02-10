package org.example;

public class News {
    private int id;
    private static int idCounter = 0;
    private String title;
    private String content;
    private String topic;



    public News(String title, String content, String topic) {
        this.id = ++idCounter;
        this.title = title;
        this.content = content;
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTopic() {
        return topic;
    }
    public int getId() { return id; }



    @Override
    public String toString() {
        return "ID: "+ id + " Title: " + title + ", Topic: " + topic + ", Content: " + content;
    }
}
