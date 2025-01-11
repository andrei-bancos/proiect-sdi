package org.example;

public class News {
    private String title;
    private String content;
    private String topic;

    public News(String title, String content, String topic) {
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

    @Override
    public String toString() {
        return "Title: " + title + ", Topic: " + topic + ", Content: " + content;
    }
}
