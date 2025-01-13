package org.example;

import java.util.ArrayList;
import java.util.List;

public class NewsList {
    private List<News> newsList;

    public NewsList() {
        this.newsList = new ArrayList<>();
    }

    public void addNews(News news) {
        newsList.add(news);
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void printAllNews(String topic) {
        System.out.println("\n|");
        System.out.println("| Listă știri:");
        for (News news : newsList) {
            if(news.getTopic().equals(topic)) {
                System.out.println("| " + news);
            }
        }
        System.out.println("|\n");
    }
}
