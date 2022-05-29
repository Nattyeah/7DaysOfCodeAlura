package com.nataly.dayofcode.client;

import com.nataly.dayofcode.utils.HashUtils;
import com.nataly.dayofcode.interfaces.ApiClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MarvelApiClient implements ApiClient {
    private final String endpoint;

    public MarvelApiClient(String apiKey, String privateKey) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String hash = HashUtils.getHashMd5(timestamp + privateKey + apiKey);
        this.endpoint = String.format("https://gateway.marvel.com:443/v1/public/series?ts=%s&hash=%s&apikey=%s",
                timestamp, hash, apiKey);
    }

    @Override
    public String getBody() {
        String json = executeRequest();
        return json;
    }

    private String executeRequest() {
        try {
            URI apiIMDB = URI.create(this.endpoint);

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
