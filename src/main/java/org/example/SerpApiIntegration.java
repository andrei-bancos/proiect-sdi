package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SerpApiIntegration {
    private static final String API_KEY = "api_key";

    public static List<News> fetchNews(String query, int maxResults) {
        List<News> newsList = new ArrayList<>();
        int currentCount = 0;
        int start = 0;

        try {
            while (currentCount < maxResults) {
                // Construim URL-ul API
                String url = "https://serpapi.com/search.json?q=" + query +
                        "&tbm=nws&start=" + start + "&api_key=" + API_KEY;

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Parsăm răspunsul JSON
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray results = jsonObject.getAsJsonArray("news_results");

                for (int i = 0; i < results.size() && currentCount < maxResults; i++) {
                    JsonObject article = results.get(i).getAsJsonObject();
                    String title = article.get("title").getAsString();
                    String content = article.has("snippet") ? article.get("snippet").getAsString() : "No content";
                    String topic = query; // Setăm topic-ul ca fiind termenul de căutare

                    newsList.add(new News(title, content, topic));
                    currentCount++;
                }

                // Incrementăm pagina
                start += results.size();

                // Dacă nu mai sunt rezultate, ieșim
                if (results.size() == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Eroare la colectarea știrilor: " + e.getMessage());
        }

        return newsList;
    }
}
