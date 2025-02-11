package org.example;

import java.util.UUID;

public class News {
    private String id;
    private String title;
    private String content;
    private String topic;



    public News(String title, String content, String topic) {
        this.id = UUID.randomUUID().toString();
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

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: "+ id + " Title: " + title + ", Topic: " + topic + ", Content: " + content;
    }
}
