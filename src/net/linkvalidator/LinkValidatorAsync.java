package net.linkvalidator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkValidatorAsync {

    private static HttpClient client;

    public static void main(String[] args) throws IOException {
        client = HttpClient.newHttpClient();

        var futures = Files.lines(Path.of("urls.txt"))
                .map(LinkValidatorAsync::validateLink)
                .collect(Collectors.toList());

        futures.stream()
                .map(CompletableFuture::join)
                .forEach(System.out::println);
    }

    private static CompletableFuture<String> validateLink(String link){
        HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(LinkValidatorAsync::responseToString)
                .exceptionally( throwable -> String.format("%s -> %s", link, false));
    }

    private static String responseToString(HttpResponse<Void> response){
        int status = response.statusCode();
        boolean success = status >= 200 && status <=299;

        return String.format("%s -> %s (status: %s)", response.uri(), success, status);
    }
}