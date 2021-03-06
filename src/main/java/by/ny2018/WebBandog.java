package by.ny2018;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.google.common.collect.Sets;

public class WebBandog {

    private String basePage;
    private String domain;

    public WebBandog(String startPage) {
        if (!startPage.matches("^https?://.*")) {
            throw new IllegalArgumentException();
        }
        this.basePage = startPage;
        int beginIndex = startPage.indexOf("/") + 2;
        int endIndex = startPage.indexOf("/", beginIndex);
        this.domain = startPage.substring(beginIndex, endIndex == -1 ? startPage.length() - 1 : endIndex);
    }

    public int findWords(String word) {
        Set<String> visited = Sets.newConcurrentHashSet();
        return find(basePage, word, visited);
    }

    private int find(String pageURL, String word, Set<String> visited) {
        if (visited.contains(pageURL))
            return 0;
        else
            visited.add(pageURL);

        System.out.println("Thread: " + Thread.currentThread().getName() + " - Open page: " + pageURL);
        Document doc = getDocument(pageURL);
        if (doc == null)
            return 0;

        // get all links
        CompletableFuture<Integer> onPageLinksFound = CompletableFuture.supplyAsync(() -> getLinks(doc))
                .thenApply(links ->
                        links.stream()
                                .map(l -> l.attr("abs:href"))
                                .filter(l -> l.matches("^https?://" + domain + ".*"))
                                .filter(l -> !l.matches(".*\\.(img|png|jpg)$"))
                                .map(l -> find(l, word, visited))
                                .mapToInt(Integer::valueOf)
                                .sum()
                );

        CompletableFuture<Integer> onPageFound = CompletableFuture.supplyAsync(() -> matcherCount(doc, word));

        CompletableFuture.allOf(onPageLinksFound, onPageFound);

        int total;
        try {
            total = onPageFound.get() + onPageLinksFound.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Thread: " + Thread.currentThread().getName() + " - Total: " + total);
        return total;
    }


    private int matcherCount(Document doc, String word) {
        String fullPage = doc.outerHtml();
//        fullPage = fullPage.replaceAll("[^A-Za-z0-9]", ""); // in case "moJA VAriable". Not found while testing.

        Matcher matcher = Pattern.compile(word, Pattern.CASE_INSENSITIVE).matcher(fullPage);

        int i = 0;
        while (matcher.find()) {
            i++;
        }

        return i;
    }

    private Elements getLinks(Document doc) {return doc.select("a[href]");}

    private Document getDocument(String pageURL) {
        String userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";

        try {
            return Jsoup.connect(URLDecoder.decode(pageURL, "UTF-8")).userAgent(userAgent).get();
        } catch (IOException ignored) {
            System.out.println(ignored.getMessage() + " while getting [" + pageURL + "]");
        }
        return null;
    }
}
