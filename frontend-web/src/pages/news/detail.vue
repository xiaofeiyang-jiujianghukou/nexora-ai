<template>
  <div class="detail-page">
    <!-- 顶部导航栏 -->
    <div class="detail-topbar">
      <router-link to="/" class="back-link">← {{ $t('common.back') }}</router-link>
      <span class="topbar-spacer" />
      <span v-if="authStore.isAuthenticated" class="fav-btn" @click="toggleFavorite">
        {{ isFav ? '❤️' : '🤍' }}
      </span>
    </div>

    <div v-loading="loading" class="detail-main">
      <template v-if="detail">
        <!-- 标题 -->
        <h1 class="detail-title">{{ detail.title }}</h1>
        <div class="detail-meta">
          <span v-if="detail.sourceName">{{ detail.sourceName }}</span>
          <span>{{ formatDate(detail.publishTime) }}</span>
          <span>👁 {{ detail.viewCount }}</span>
          <span v-if="detail.categoryName">
            <el-tag size="small">{{ detail.categoryName }}</el-tag>
          </span>
        </div>

        <!-- AI 摘要 -->
        <div v-if="detail.aiAnalysis || detail.summary" class="ai-section">
          <h3 class="section-title">🤖 {{ $t('news.summary') }}</h3>
          <p>{{ detail.aiAnalysis?.summary || detail.summary }}</p>
        </div>

        <!-- 核心事实 -->
        <div v-if="detail.summary" class="content-section">
          <h3 class="section-title">📋 {{ $t('news.facts') }}</h3>
          <p>{{ detail.summary }}</p>
        </div>

        <!-- 事件背景 -->
        <div v-if="detail.aiAnalysis?.background" class="content-section">
          <h3 class="section-title">📖 {{ $t('news.background') }}</h3>
          <p>{{ detail.aiAnalysis.background }}</p>
        </div>

        <!-- 影响分析 -->
        <div v-if="detail.aiAnalysis?.impact" class="content-section">
          <h3 class="section-title">📊 {{ $t('news.impact') }}</h3>
          <p>{{ detail.aiAnalysis.impact }}</p>
        </div>

        <!-- 标签 -->
        <div v-if="detail.tags?.length" class="content-section">
          <el-tag v-for="t in detail.tags" :key="t" size="small" class="mr-2">{{ t }}</el-tag>
        </div>

        <!-- 原文 -->
        <div v-if="detail.sourceUrl" class="content-section">
          <a :href="detail.sourceUrl" target="_blank" rel="noopener">
            {{ $t('news.viewOriginal') }} →
          </a>
        </div>

        <!-- 相关新闻 -->
        <div v-if="related.length" class="related-section">
          <h3 class="section-title">{{ $t('news.related') }}</h3>
          <NewsCard v-for="r in related" :key="r.id" :item="r" />
        </div>
      </template>
      <el-empty v-else :description="$t('common.error')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessage } from 'element-plus';
import NewsCard from '@/components/common/NewsCard.vue';
import { useNewsStore } from '@/stores/newsStore';
import { useFavoriteStore } from '@/stores/favoriteStore';
import { useAuthStore } from '@/stores/authStore';
import type { NewsDetail } from '@/stores/newsStore';
import type { NewsItem as NewsItemType } from '@/components/common/NewsCard.vue';

const route = useRoute();
const newsStore = useNewsStore();
const favStore = useFavoriteStore();
const authStore = useAuthStore();
const { t, locale } = useI18n();

const detail = ref<NewsDetail | null>(null);
const related = ref<NewsItemType[]>([]);
const loading = ref(false);
const isFav = ref(false);

function formatDate(d?: string) {
  if (!d) return '';
  return new Date(d).toLocaleString();
}

async function toggleFavorite() {
  if (!detail.value) return;
  try {
    if (isFav.value) {
      await favStore.removeFavorite(detail.value.id);
      isFav.value = false;
      ElMessage.success(t('news.favoriteRemoved'));
    } else {
      await favStore.addFavorite(detail.value.id);
      isFav.value = true;
      ElMessage.success(t('news.favoriteAdded'));
    }
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : t('common.operationFailed');
    ElMessage.error(msg);
  }
}

onMounted(async () => {
  const id = Number(route.params.id);
  loading.value = true;
  try {
    detail.value = await newsStore.getDetail(id);
    related.value = await newsStore.getRelated(id);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.detail-page {
  max-width: 760px;
  margin: 0 auto;
  width: 100%;
}
.detail-topbar {
  display: flex;
  align-items: center;
  padding: 8px 0 16px;
}
.back-link { font-size: 15px; color: var(--accent-color); }
.topbar-spacer { flex: 1; }
.fav-btn { font-size: 22px; cursor: pointer; user-select: none; }
.detail-main { min-height: 400px; }
.detail-title { font-size: 28px; font-weight: 700; line-height: 1.4; margin-bottom: 14px; color: var(--text-primary); }
.detail-meta { display: flex; gap: 14px; font-size: 13px; color: var(--text-secondary); margin-bottom: 28px; flex-wrap: wrap; align-items: center; }
.ai-section { background: linear-gradient(135deg, var(--bg-secondary), var(--bg-card)); padding: 20px; border-radius: 12px; margin-bottom: 24px; border: 1px solid var(--border-color); }
.content-section { margin-bottom: 24px; }
.section-title { font-size: 18px; font-weight: 600; margin-bottom: 12px; color: var(--text-primary); }
.related-section { margin-top: 40px; border-top: 1px solid var(--border-color); padding-top: 24px; }
.mr-2 { margin-right: 8px; }

@media (max-width: 768px) {
  .detail-page { max-width: 100%; }
  .detail-title { font-size: 22px; }
  .detail-meta { gap: 8px; }
}
</style>
