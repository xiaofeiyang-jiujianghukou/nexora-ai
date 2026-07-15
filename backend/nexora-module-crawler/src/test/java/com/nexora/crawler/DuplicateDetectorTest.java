package com.nexora.crawler;

import com.nexora.crawler.pipeline.DuplicateDetector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateDetectorTest {

    private final DuplicateDetector detector = new DuplicateDetector();

    @Test
    void shouldHashSameUrlIdentically() {
        String h1 = detector.urlHash("https://reuters.com/article/123");
        String h2 = detector.urlHash("https://reuters.com/article/123");
        assertEquals(h1, h2);
    }

    @Test
    void shouldHashDifferentUrlsDifferently() {
        String h1 = detector.urlHash("https://reuters.com/article/1");
        String h2 = detector.urlHash("https://reuters.com/article/2");
        assertNotEquals(h1, h2);
    }

    @Test
    void shouldDetectSimilarTextAsDuplicate() {
        // 使用较长文本，确保 SimHash 对小幅修改有容忍度
        String base = "OpenAI于今日正式发布GPT6新一代大语言模型该模型在推理能力和多模态理解方面取得重大突破引发全球AI行业广泛关注各大科技公司纷纷发表评论";
        String text1 = base + "业界专家认为这将改变AI产业格局推动新一轮技术革新浪潮";
        String text2 = base + "业内专家认为这将改变AI产业格局推动新一轮技术革命浪潮";

        long h1 = detector.simHash(text1);
        long h2 = detector.simHash(text2);

        int dist = detector.hammingDistance(h1, h2);
        assertTrue(dist <= 3,
                "长文本小幅修改应被检测为重复 (汉明距离=" + dist + ")");
    }

    @Test
    void shouldDetectDifferentTextAsNotDuplicate() {
        String text1 = "OpenAI发布GPT-6新模型 引发行业震动";
        String text2 = "苹果公司发布新款iPhone 16 Pro Max 搭载A18芯片";

        long h1 = detector.simHash(text1);
        long h2 = detector.simHash(text2);

        assertFalse(detector.isDuplicate(h1, h2),
                "完全不同文本不应被判为重复 (汉明距离=" + detector.hammingDistance(h1, h2) + ")");
    }

    @Test
    void shouldHandleEmptyText() {
        assertEquals(0L, detector.simHash(""));
        assertEquals(0L, detector.simHash(null));
    }

    @Test
    void shouldReturnZeroHammingForIdenticalHash() {
        long h = detector.simHash("测试文本内容");
        assertEquals(0, detector.hammingDistance(h, h));
    }
}
