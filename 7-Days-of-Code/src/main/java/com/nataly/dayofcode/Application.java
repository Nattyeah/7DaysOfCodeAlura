package com.nataly.dayofcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Application {

    public static void main(String[] args) throws Exception {

//        System.out.println("Chamando API Imdb");
//        String apiKey = "apiKey";
//        String json = new ImdbApiClient(apiKey).getBody();

//        System.out.println("Parsing JSON");
//        JsonParser jsonParser = new ImdbMovieJsonParser(json);
//        List<? extends Content> contentList = jsonParser.parse();

        System.out.println("Chamando API Marvel");
        String apiKey = "apiKey";
        String privateKey = "privateKey";
        String jsonSeries = new MarvelApiClient(apiKey, privateKey).getBody();

        System.out.println("Parsing JSON");
        JsonParser jsonParser = new MarvelSerieJsonParser(jsonSeries);
        List<? extends Content> series = jsonParser.parse();
        List<? extends Content> contentList = jsonParser.parse();

        contentList = Stream.of(series, contentList).flatMap(Collection::stream).collect(Collectors.toList());

//        Collections.sort(contentList, Comparator.reverseOrder());
        Collections.sort(contentList, Comparator.comparing(Content::year));

        System.out.println("Gerando HTML");
        PrintWriter writer = new PrintWriter("content.html");
        new HTMLGenerator(writer).generate(contentList);
        writer.close();
    }
}

// Interfaces
interface Content extends Comparable<Content> {
    String title();

    String url();

    String rating();

    String year();
}

interface JsonParser {
    List<? extends Content> parse();
}

interface ApiClient {
    String getBody();
}

// Imdb classes
record Movie(String title, String url, String rating, String year) implements Content {
    @Override
    public int compareTo(Content c) {
        return this.rating().compareTo(c.rating());
    }
}

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

// Marvel classes
record Serie(String title, String url, String rating, String year) implements Content {
    @Override
    public int compareTo(Content c) {
        return this.rating().compareTo(c.rating());
    }
}

class MarvelApiClient implements ApiClient {
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

class HashUtils {
    public static String getHashMd5(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            BigInteger hash = new BigInteger(1, md.digest(value.getBytes()));
            return hash.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

class MarvelSerieJsonParser implements JsonParser {
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

// Gerador de HTML
class HTMLGenerator {

    private final PrintWriter writer;

    public HTMLGenerator(PrintWriter writer) {
        this.writer = writer;
    }

    public void generate(List<? extends Content> contentList) {
        writer.println(
                """
                        <html>
                        	<head>
                        		<meta charset=\"utf-8\">
                        		<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">
                        		<link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/css/bootstrap.min.css\" 
                        					+ "integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">
                        					
                        	</head>
                        	<body>
                        """);

        for (Content content : contentList) {
            String div =
                    """
                            <div class=\"card text-white bg-dark mb-3\" style=\"max-width: 18rem;\">
                                <h4 class=\"card-header\">%s</h4>
                                <div class=\"card-body\">
                                    <img class=\"card-img\" src=\"%s\" alt=\"%s\">
                                    <p class=\"card-text mt-2\">Nota: %s - Ano: %s</p>
                                </div>
                            </div>
                            """;

            writer.println(String.format(div, content.title(), content.url(), content.title(), content.rating(), content.year()));
        }

        writer.println(
                """
                            </body>
                        </html>
                        """);
    }
}