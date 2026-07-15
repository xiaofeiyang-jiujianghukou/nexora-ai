<template>
  <div class="search-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        :placeholder="$t('search.placeholder')"
        size="large"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" size="large" @click="doSearch">
        <el-icon><Search /></el-icon>
        {{ $t('search.results') }}
      </el-button>
    </div>

    <!-- 结果 -->
    <div v-if="searched" class="results-header">
      "{{ searchedKeyword }}" — {{ total }} {{ $t('search.results') }}
    </div>

    <div v-loading="loading" class="results-list">
      <template v-if="results.length">
        <NewsCard v-for="item in results" :key="item.id" :item="item" />
      </template>
      <el-empty v-else-if="searched" :description="$t('search.noResults')" />
      <div v-else class="search-hint">
        <el-icon :size="48" color="var(--text-placeholder)"><Search /></el-icon>
        <p>输入关键词搜索新闻</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import NewsCard from '@/components/common/NewsCard.vue';
import http from '@/utils/http';
import type { NewsItem } from '@/components/common/NewsCard.vue';

const route = useRoute();
const keyword = ref('');
const searchedKeyword = ref('');
const searched = ref(false);
const loading = ref(false);
const results = ref<NewsItem[]>([]);
const total = ref(0);

async function doSearch() {
  if (!keyword.value.trim()) return;
  searchedKeyword.value = keyword.value.trim();
  searched.value = true;
  loading.value = true;
  try {
    const res = await http.get('/api/v1/search/news', {
      params: { q: searchedKeyword.value, page: 1, size: 20 },
    });
    results.value = res.data.list || [];
    total.value = res.data.total || 0;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  const q = (route.query.q as string) || '';
  if (q) {
    keyword.value = q;
    doSearch();
  }
});
</script>

<style scoped>
.search-page {
  max-width: 760px;
  margin: 0 auto;
  width: 100%;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.search-bar .el-input {
  flex: 1;
}
.results-header {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}
.results-list {
  min-height: 300px;
}
.search-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: var(--text-secondary);
}
.search-hint p {
  font-size: 14px;
}

@media (max-width: 768px) {
  .search-page {
    max-width: 100%;
  }
  .search-bar {
    flex-direction: column;
  }
}
</style>
