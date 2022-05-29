package com.nataly.dayofcode.parser;

import com.nataly.dayofcode.Movie;
import com.nataly.dayofcode.interfaces.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ImdbMovieJsonParser implements JsonParser {
    private String json;

    public ImdbMovieJsonParser(String json) {
        this.json = json;
    }

    public List<Movie> parse() {
        String[] moviesArray = parseJsonMovies(json);

        List<String> titles = parseTitles(moviesArray);
        List<String> urlImages = parseUrlImages(moviesArray);
        List<String> ratings = parseRatings(moviesArray);
        List<String> years = parseYears(moviesArray);

        List<Movie> movies = new ArrayList<>(titles.size());

        for (int i = 0; i < titles.size(); i++) {
            movies.add(new Movie(titles.get(i), urlImages.get(i), ratings.get(i), years.get(i)));
        }
        return movies;
    }

    private String[] parseJsonMovies(String json) {
        Matcher matcher = Pattern.compile(".*\\[(.*)\\].*").matcher(json);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("no match in " + json);
        }

        String[] moviesArray = matcher.group(1).split("\\},\\{");
        moviesArray[0] = moviesArray[0].substring(1);
        int last = moviesArray.length - 1;
        String lastString = moviesArray[last];
        moviesArray[last] = lastString.substring(0, lastString.length() - 1);
        return moviesArray;
    }

    private List<String> parseTitles(String[] moviesArray) {
        return parseAttribute(moviesArray, 3);
    }

    private List<String> parseUrlImages(String[] moviesArray) {
        return parseAttribute(moviesArray, 5);
    }

    private List<String> parseRatings(String[] moviesArray) {
        return parseAttribute(moviesArray, 7);
    }

    private List<String> parseYears(String[] moviesArray) {
        return parseAttribute(moviesArray, 4);
    }

    private List<String> parseAttribute(String[] jsonMovies, int pos) {
        return Stream.of(jsonMovies)
                .map(e -> e.split("\",\"")[pos])
                .map(e -> e.split(":\"")[1])
                .map(e -> e.replaceAll("\"", ""))
                .collect(Collectors.toList());
    }
}
