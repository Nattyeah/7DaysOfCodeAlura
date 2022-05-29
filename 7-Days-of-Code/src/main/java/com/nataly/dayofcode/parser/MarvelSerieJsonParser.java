package com.nataly.dayofcode.parser;

import com.nataly.dayofcode.Serie;
import com.nataly.dayofcode.interfaces.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarvelSerieJsonParser implements JsonParser {
    private String json;

    public MarvelSerieJsonParser(String json) {
        this.json = json;
    }

    public List<Serie> parse() {
        String[] seriesArray = parseJsonSeries(this.json);

        List<Serie> series = new ArrayList<>();
        for (int i = 0; i < seriesArray.length; i++) {
            String title = parseAttribute(seriesArray[i], "title");
            String ratings = parseAttribute(seriesArray[i], "rating");
            String years = parseAttribute(seriesArray[i], "startYear");

            if (ratings.isEmpty()) {
                ratings = "Sem";
            }
            String thumbnailValue = parseThumbnailAttribute(seriesArray[i]);
            series.add(new Serie(title, thumbnailValue, ratings, years));
        }
        return series;
    }

    private String parseThumbnailAttribute(String jsonSeries) {
        Pattern pattern = Pattern.compile("\"thumbnail\":\\{\"path\":\"");
        Matcher matcher = pattern.matcher(jsonSeries);

        if (!matcher.find()) {
            throw new IllegalStateException("Thumbnail não encontrado");
        }

        int posIniAttribute = matcher.end();
        String thumbnail_ext = jsonSeries.substring(posIniAttribute);

        pattern = Pattern.compile("\",\"extension\":\"");
        matcher = pattern.matcher(thumbnail_ext);

        if (!matcher.find()) {
            throw new IllegalStateException("Thumbnail extension não encontrado");
        }

        posIniAttribute = matcher.start();
        String thumbnail = thumbnail_ext.substring(0, posIniAttribute);

        String ext = thumbnail_ext.substring(matcher.end(), matcher.end() + 3);

        return cleanUp(thumbnail) + "." + ext;
    }

    String parseAttribute(String jsonSeries, String attributeName) {
        int posIniAttribute = findInitialPositionOfAttribute(jsonSeries, attributeName);
        jsonSeries = jsonSeries.substring(posIniAttribute);

        int posEndAttribute = findFinalPositionOfAttribute(jsonSeries, attributeName);
        String attributeValue = jsonSeries.substring(0, posEndAttribute);

        String value = cleanUp(attributeValue);

        return value;
    }

    private int findFinalPositionOfAttribute(String jsonSeries, String attributeName) {
        Pattern endPattern = Pattern.compile(",");
        Matcher endMatcher = endPattern.matcher(jsonSeries);

        if (!endMatcher.find()) {
            throw new IllegalStateException(attributeName + " não encontrado");
        }

        int posEndAttribute = endMatcher.start();
        return posEndAttribute;
    }

    private int findInitialPositionOfAttribute(String jsonSeries, String attributeName) {
        Pattern beginPattern = Pattern.compile("\"" + attributeName + "\":");
        Matcher beginMatcher = beginPattern.matcher(jsonSeries);

        if (!beginMatcher.find()) {
            throw new IllegalStateException(attributeName + " não encontrado");
        }

        int posIniAttribute = beginMatcher.end();
        return posIniAttribute;
    }

    private static String cleanUp(String attributeValue) {
        if (attributeValue.startsWith("\"")) {
            attributeValue = attributeValue.substring(1);
        }
        if (attributeValue.endsWith(",")) {
            attributeValue = attributeValue.substring(0, attributeValue.length() - 1);
        }
        if (attributeValue.endsWith("\"")) {
            attributeValue = attributeValue.substring(0, attributeValue.length() - 1);
        }
        return attributeValue.trim();
    }

    static Pattern BEGIN_ARRAY = Pattern.compile(".*\"results\":");
    static Pattern END_ARRAY = Pattern.compile(".*\\]}}");

    private String[] parseJsonSeries(String body) {
        Matcher matcher = BEGIN_ARRAY.matcher(body);
        matcher.find();
        int begin = matcher.end();

        matcher = END_ARRAY.matcher(body);
        matcher.find();
        int end = matcher.end();

        String jsonStringSeries = body.substring(begin, end);

        String[] jsonMovies = jsonStringSeries.split("\\},\\{\"id\"");
        return jsonMovies;
    }
}
