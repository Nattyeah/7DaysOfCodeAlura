package com.nataly.dayofcode.client;

import com.nataly.dayofcode.interfaces.ApiClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class ImdbApiClient implements ApiClient {
    private String apiKey;

    public ImdbApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBody() {
        try {
            URI apiIMDB = URI.create("https://imdb-api.com/en/API/Top250TVs/" + this.apiKey);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(apiIMDB).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
