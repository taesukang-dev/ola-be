package com.example.ola.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class Crawler {
    private Crawler() {}

    public static String imgCrawler(String url){
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        }  catch (IOException e) {
            log.error("none of post {}", e.getMessage());
            return "";
        }
        if (url.contains("naver.com")) {
            return naverBlogCrawling(document);
        }
        Elements meta = document.select("meta[property=og:image]");
        Optional<Element> el = meta.stream().findFirst();
        return el.map(e -> e.attr("content")).orElse("");
    }

    private static String naverBlogCrawling(Document document) {
        Elements iframes = document.select("iframe#mainFrame");
        String src = iframes.attr("src");
        String url2 = "http://blog.naver.com"+ src;
        Document document2 = null;
        try {
            document2 = Jsoup.connect(url2).get();
        } catch (IOException e) {
            log.error("none of post {}", e.getMessage());
            return "";
        }
        Elements meta = document2.select("meta[property=og:image]");
        Optional<Element> el = meta.stream().findFirst();
        return el.map(e -> e.attr("content")).orElse("");
    }
}
