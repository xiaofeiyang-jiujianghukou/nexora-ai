package com.nexora.crawler.cleaner;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 内容清洗器 — HTML去除、广告过滤、内容标准化
 */
@Component
public class ContentCleaner {

    // 广告/无关内容正则
    private static final Pattern[] AD_PATTERNS = {
            Pattern.compile("(?i)(点击购买|立即抢购|限时优惠|广告|推广|sponsored|advertisement)"),
            Pattern.compile("(?i)(相关推荐|猜你喜欢|热门推荐|you may also like|related articles)"),
            Pattern.compile("(?i)(版权声明|免责声明|all rights reserved|copyright\\s*©)"),
            Pattern.compile("(?i)(分享到|share this|follow us|关注我们|订阅)"),
    };

    // 导航/页脚常见文本
    private static final Pattern[] BOILERPLATE = {
            Pattern.compile("(?i)(首页|关于我们|联系我们|隐私政策|terms of|privacy policy|cookie)"),
    };

    /**
     * 清洗 HTML → 纯文本
     */
    public String cleanHtml(String html) {
        if (html == null || html.isBlank()) return "";
        return Jsoup.clean(html, Safelist.none());
    }

    /**
     * 去除广告文本
     */
    public String removeAds(String text) {
        if (text == null) return "";
        for (Pattern p : AD_PATTERNS) {
            text = p.matcher(text).replaceAll("");
        }
        return text;
    }

    /**
     * 去除页脚/导航文本
     */
    public String removeBoilerplate(String text) {
        if (text == null) return "";
        for (Pattern p : BOILERPLATE) {
            text = p.matcher(text).replaceAll("");
        }
        return text;
    }

    /**
     * 标准化空白字符
     */
    public String normalize(String text) {
        if (text == null) return "";
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * 完整清洗流程
     */
    public String clean(String html) {
        String text = cleanHtml(html);
        text = removeAds(text);
        text = removeBoilerplate(text);
        return normalize(text);
    }
}
