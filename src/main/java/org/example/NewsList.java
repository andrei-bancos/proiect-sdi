package org.example;

import java.sql.SQLOutput;
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
    public void printNewsWithId( int id) {
        for (News news : newsList) {
            if( news.getId() == id){
                System.out.println("\n|");
                System.out.println("| Id: " + id);
                System.out.println("| Titlu: "+news.getTitle());
                System.out.println("| Topic: " + news.getTopic());
                System.out.println("| Continut: " + news.getContent());
                System.out.println("\n|");
                break;
            } else
                System.out.println("Nu există nici o știre cu id-ul:"+ id);
        }
    }
    public void deleteNewsById(int id) {
        boolean isDeleted = newsList.removeIf(news -> news.getId() == id);

        if (isDeleted) {
            System.out.println("\n| Știrea cu ID-ul " + id + " a fost ștearsă.");
        } else {
            System.out.println("\n| Nu s-a găsit nicio știre cu ID-ul " + id + ".");
        }
    }


}
