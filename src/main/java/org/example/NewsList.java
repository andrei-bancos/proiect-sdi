package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        long countNewsPerTopic = newsList.stream().filter(news -> Objects.equals(news.getTopic(), topic)).count();

        System.out.println("\n|");
        System.out.println("| Listă știri - " + topic + " [ " + countNewsPerTopic + " ]");

        for (News news : newsList) {
            if(news.getTopic().equals(topic)) {
                System.out.println("| " + news);
            }
        }
        System.out.println("|\n");
    }

    public void printAllNews() {
        System.out.println("\n|");
        System.out.println("| Listă știri [ " + newsList.size() +" ]");
        for (News news : newsList) {
            System.out.println("| " + news);
        }
        System.out.println("|\n");
    }

    public void printNewsWithId(String id) {
        Optional<News> newsOptional = newsList.stream()
                .filter(news -> Objects.equals(news.getId(), id))
                .findAny();

        if (newsOptional.isPresent()) {
            System.out.println("\n|");
                System.out.println("| Id: " + id);
                System.out.println("| Titlu: " + newsOptional.get().getTitle());
                System.out.println("| Topic: " + newsOptional.get().getTopic());
                System.out.println("| Continut: " + newsOptional.get().getContent());
                System.out.println("|\n");
        } else {
            System.out.println("Nu s-a găsit nicio știre cu acest ID.");
        }
    }

    public void deleteNewsById(String id) {
        boolean isDeleted = newsList.removeIf(news -> Objects.equals(news.getId(), id));

        if (isDeleted) {
            System.out.println("\n| Știrea cu ID-ul " + id + " a fost ștearsă.");
        } else {
            System.out.println("\n| Nu s-a găsit nicio știre cu ID-ul " + id + ".");
        }
    }
}
