package com.nexora.crawler.pipeline;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 新闻去重检测器
 * 第一层: URL Hash 精确匹配
 * 第二层: SimHash 文本指纹 + 汉明距离
 */
@Component
public class DuplicateDetector {

    private static final int SIMHASH_BITS = 64;
    private static final int HAMMING_THRESHOLD = 3; // 汉明距离 ≤ 3 视为重复

    /**
     * 计算 URL 的 SHA-256 Hash
     */
    public String urlHash(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(url.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, digest));
        } catch (Exception e) {
            return Integer.toHexString(url.hashCode());
        }
    }

    /**
     * 计算文本 SimHash 指纹
     */
    public long simHash(String text) {
        if (text == null || text.isBlank()) return 0L;

        int[] bits = new int[SIMHASH_BITS];
        List<String> tokens = tokenize(text);

        for (String token : tokens) {
            long hash = fnv1a64(token);
            for (int i = 0; i < SIMHASH_BITS; i++) {
                if ((hash & (1L << i)) != 0) {
                    bits[i]++;
                } else {
                    bits[i]--;
                }
            }
        }

        long fingerprint = 0L;
        for (int i = 0; i < SIMHASH_BITS; i++) {
            if (bits[i] > 0) {
                fingerprint |= (1L << i);
            }
        }
        return fingerprint;
    }

    /**
     * 计算汉明距离
     */
    public int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    /**
     * 判断两段文本是否相似
     */
    public boolean isDuplicate(long hash1, long hash2) {
        return hammingDistance(hash1, hash2) <= HAMMING_THRESHOLD;
    }

    // ---- 私有方法 ----

    /**
     * 简单分词 (按2-gram切分字符，适合中英文混合)
     */
    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        String cleaned = text.replaceAll("[\\p{P}\\s]+", "");
        for (int i = 0; i < cleaned.length() - 1; i++) {
            tokens.add(cleaned.substring(i, Math.min(i + 2, cleaned.length())));
        }
        return tokens;
    }

    /**
     * FNV-1a 64-bit Hash
     */
    private long fnv1a64(String s) {
        long hash = 0xcbf29ce484222325L;
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            hash ^= (b & 0xff);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
