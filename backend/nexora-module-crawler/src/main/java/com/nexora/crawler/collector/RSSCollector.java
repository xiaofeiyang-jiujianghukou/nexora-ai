package com.nexora.crawler.collector;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * RSS 采集器 — 通用 RSS/Atom Feed 采集
 */
@Slf4j
public class RSSCollector implements NewsCollector {

    private final String name;
    private final String feedUrl;

    public RSSCollector(String name, String feedUrl) {
        this.name = name;
        this.feedUrl = feedUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Map<String, String>> collect() {
        List<Map<String, String>> items = new ArrayList<>();
        try {
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));

            for (SyndEntry entry : feed.getEntries()) {
                Map<String, String> item = new HashMap<>();
                item.put("title", entry.getTitle() != null ? entry.getTitle() : "");
                item.put("content", entry.getDescription() != null
                        ? entry.getDescription().getValue() : "");
                item.put("url", entry.getLink() != null ? entry.getLink() : "");
                item.put("author", entry.getAuthor() != null ? entry.getAuthor() : "");
                if (entry.getPublishedDate() != null) {
                    item.put("publishTime", LocalDateTime.ofInstant(
                            entry.getPublishedDate().toInstant(), ZoneId.systemDefault()).toString());
                }
                items.add(item);
            }
            log.info("RSS采集完成: source={}, count={}", name, items.size());
        } catch (Exception e) {
            log.error("RSS采集失败: source={}, url={}", name, feedUrl, e);
        }
        return items;
    }
}
