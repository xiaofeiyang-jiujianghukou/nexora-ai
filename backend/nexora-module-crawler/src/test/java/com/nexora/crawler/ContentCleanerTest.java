package com.nexora.crawler;

import com.nexora.crawler.cleaner.ContentCleaner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentCleanerTest {

    private final ContentCleaner cleaner = new ContentCleaner();

    @Test
    void shouldRemoveHtmlTags() {
        String html = "<p>新闻内容</p><div>详细信息</div>";
        String result = cleaner.cleanHtml(html);
        assertTrue(result.contains("新闻内容"));
        assertFalse(result.contains("<p>"));
    }

    @Test
    void shouldRemoveAdText() {
        String text = "这是一条重要新闻 点击购买 限时优惠 更多内容";
        String result = cleaner.removeAds(text);
        assertTrue(result.contains("这是一条重要新闻"));
        assertTrue(result.contains("更多内容"));
        assertFalse(result.contains("点击购买"));
    }

    @Test
    void shouldRemoveBoilerplate() {
        String text = "新闻正文内容 关于我们 联系我们 隐私政策";
        String result = cleaner.removeBoilerplate(text);
        assertTrue(result.contains("新闻正文内容"));
        assertFalse(result.contains("关于我们"));
    }

    @Test
    void shouldNormalizeWhitespace() {
        String text = "多个    空格      测试";
        assertEquals("多个 空格 测试", cleaner.normalize(text));
    }

    @Test
    void shouldHandleNullInput() {
        assertEquals("", cleaner.clean(null));
        assertEquals("", cleaner.cleanHtml(null));
        assertEquals("", cleaner.removeAds(null));
    }

    @Test
    void shouldCleanCompleteFlow() {
        String html = "<html><body><p>OpenAI发布GPT-6新模型</p>" +
                "<div class='ad'>点击购买立即抢购</div>" +
                "<footer>关于我们 隐私政策</footer></body></html>";
        String result = cleaner.clean(html);
        assertTrue(result.contains("OpenAI"));
        assertTrue(result.contains("GPT-6"));
        assertFalse(result.contains("点击购买"));
        assertFalse(result.contains("关于我们"));
    }
}
