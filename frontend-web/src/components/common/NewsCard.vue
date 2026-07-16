<template>
  <div class="news-card" @click="goDetail">
    <div class="card-header">
      <h3 class="card-title">{{ displayTitle }}</h3>
      <span v-if="item.hotScore && item.hotScore > 50" class="hot-badge">🔥</span>
    </div>

    <p v-if="summaryText" class="card-summary">{{ summaryText }}</p>

    <div class="card-meta">
      <span v-if="item.sourceName" class="meta-source">{{ item.sourceName }}</span>
      <span class="meta-time">{{ formatTime(item.publishTime) }}</span>
      <span v-if="item.categoryName" class="meta-category">
        <el-tag size="small" type="info">{{ categoryLabel }}</el-tag>
      </span>
    </div>

    <div v-if="item.tags && item.tags.length" class="card-tags">
      <el-tag
        v-for="tag in item.tags.slice(0, 3)"
        :key="tag"
        size="small"
        class="tag-item"
      >
        #{{ tag }}
      </el-tag>
    </div>

    <div class="card-actions">
      <span class="action-views">👁 {{ item.viewCount || 0 }}</span>
      <span class="action-hot" v-if="item.hotScore">🔥 {{ item.hotScore?.toFixed(0) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { localeToLang, getLocalizedContent } from '@/locales/config';

export interface NewsItem {
  id: number;
  title: string;
  summary?: string;
  sourceName?: string;
  categoryName?: string;
  categoryCode?: string;
  hotScore?: number;
  viewCount?: number;
  tags?: string[];
  publishTime?: string;
  /** 多语言 AI 分析结果: { zh: {summary, facts, background, impact}, en: {...}, ja: {...}, ... } */
  aiResult?: Record<string, any>;
}

const props = defineProps<{ item: NewsItem }>();
const router = useRouter();
const { t, locale } = useI18n();

/** 当前语言 key（'zh-CN' → 'zh', 'en-US' → 'en'） */
const lang = computed(() => localeToLang(locale.value));

/** 双语标题：优先取 aiResult[lang].title，回退到原始 title */
const displayTitle = computed(() => {
  const localized = getLocalizedContent<Record<string, any>>(props.item.aiResult, lang.value);
  if (localized?.title) return localized.title as string;
  return props.item.title;
});

/** 分类标签：优先用 code 翻译，回退到原始名称 */
const categoryLabel = computed(() => {
  if (props.item.categoryCode) {
    return t('category.' + props.item.categoryCode, props.item.categoryName || '');
  }
  return props.item.categoryName || '';
});

const summaryText = computed(() => {
  const localized = getLocalizedContent<Record<string, any>>(props.item.aiResult, lang.value);
  if (localized?.summary) return localized.summary as string;
  return props.item.summary || '';
});

function goDetail() {
  router.push(`/news/${props.item.id}`);
}

function formatTime(time?: string): string {
  if (!time) return '';
  const d = new Date(time);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  if (diff < 3600000) return Math.floor(diff / 60000) + t('common.minAgo');
  if (diff < 86400000) return Math.floor(diff / 3600000) + t('common.hourAgo');
  return d.toLocaleDateString(locale.value);
}
</script>

<style scoped>
.news-card {
  padding: 20px;
  background: var(--bg-card);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--border-color);
  margin-bottom: 12px;
}
.news-card:hover {
  box-shadow: 0 4px 16px var(--shadow-color);
  transform: translateY(-1px);
}
.card-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  flex: 1;
}
.hot-badge {
  font-size: 16px;
  flex-shrink: 0;
}
.card-summary {
  margin-top: 10px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-meta {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}
.card-tags {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.tag-item {
  font-size: 11px;
}
.card-actions {
  margin-top: 10px;
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
