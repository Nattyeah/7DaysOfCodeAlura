package com.nataly.dayofcode;

import com.nataly.dayofcode.client.MarvelApiClient;
import com.nataly.dayofcode.interfaces.Content;
import com.nataly.dayofcode.interfaces.JsonParser;
import com.nataly.dayofcode.parser.MarvelSerieJsonParser;

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Application {

    public static void main(String[] args) throws Exception {

//        System.out.println("Chamando API Imdb");
//        String apiKey = "k_6vden298";
//        String json = new ImdbApiClient(apiKey).getBody();

//        System.out.println("Parsing JSON");
//        JsonParser jsonParser = new ImdbMovieJsonParser(json);
//        List<? extends Content> contentList = jsonParser.parse();

        System.out.println("Chamando API Marvel");
        String apiKey = "5aa3a9b87bdb1467821a2fdcb729f06b";
        String privateKey = "eda4b449c63359399125fcdf6ead797f1870dd4e";
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