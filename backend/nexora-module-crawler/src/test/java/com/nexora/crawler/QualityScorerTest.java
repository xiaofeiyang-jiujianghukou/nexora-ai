package com.nexora.crawler;

import com.nexora.crawler.pipeline.QualityScorer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualityScorerTest {

    private final QualityScorer scorer = new QualityScorer();

    @Test
    void shouldGiveHighScoreToReuters() {
        int score = scorer.score("Reuters",
                "Long article about global economy. ".repeat(30),
                "https://www.reuters.com/world/article1");
        assertTrue(score > 70, "Reuters high-quality article should score > 70, got " + score);
    }

    @Test
    void shouldGiveLowScoreToShortText() {
        int score = scorer.score("Blog",
                "Short",
                "https://blog.example.com/post");
        assertTrue(score < 50, "Short text should score < 50, got " + score);
    }

    @Test
    void shouldGiveHighScoreToGovDomain() {
        int score = scorer.score("Unknown",
                "Government research publication. ".repeat(20),
                "https://research.gov/publication/123");
        assertTrue(score >= 55, "Gov domain should boost score, got " + score);
    }

    @Test
    void shouldReturnKnownSourceWeight() {
        assertEquals(95, scorer.getSourceWeight("Reuters"));
        assertEquals(90, scorer.getSourceWeight("BBC"));
        assertEquals(90, scorer.getSourceWeight("新华社"));
        assertEquals(40, scorer.getSourceWeight("Unknown"));
    }

    @Test
    void shouldScoreContentLength() {
        String longText = "A".repeat(2000);
        String mediumText = "B".repeat(500);
        String shortText = "C".repeat(100);

        assertTrue(scorer.getLengthScore(longText) > scorer.getLengthScore(mediumText));
        assertTrue(scorer.getLengthScore(mediumText) > scorer.getLengthScore(shortText));
    }

    @Test
    void shouldScoreAuthorityByDomain() {
        assertTrue(scorer.getAuthorityScore("https://example.gov/doc") > 80);
        assertTrue(scorer.getAuthorityScore("https://reuters.com/news") > 70);
        assertEquals(30, scorer.getAuthorityScore(null));
    }
}
