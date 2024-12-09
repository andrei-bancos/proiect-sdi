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

    public void printAllNews() {
        System.out.println("Listă news:");
        for (News news : newsList) {
            System.out.println(news);
        }
    }
}