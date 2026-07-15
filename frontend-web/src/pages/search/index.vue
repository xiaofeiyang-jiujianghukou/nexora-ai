<template>
  <div class="search-page">
    <el-container>
      <el-header class="page-header">
        <router-link to="/" class="back-link">← {{ $t('common.back') }}</router-link>
        <el-input
          v-model="keyword"
          :placeholder="$t('search.placeholder')"
          class="search-input-lg"
          clearable
          @keyup.enter="doSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="doSearch">{{ $t('search.results') }}</el-button>
      </el-header>

      <el-main class="page-main">
        <div v-if="searched" class="results-header">
          "{{ searchedKeyword }}" — {{ total }} {{ $t('search.results') }}
        </div>

        <div v-loading="loading" class="results-list">
          <template v-if="results.length">
            <NewsCard v-for="item in results" :key="item.id" :item="item" />
          </template>
          <el-empty v-else-if="searched" :description="$t('search.noResults')" />
        </div>
      </el-main>
    </el-container>
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
.search-page { min-height: 100vh; background: var(--bg-secondary); }
.page-header {
  display: flex; align-items: center; gap: 12px; padding: 0 24px;
  height: 60px; background: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
}
.back-link { font-size: 15px; color: var(--accent-color); flex-shrink: 0; }
.search-input-lg { width: 480px; }
.page-main { max-width: 760px; margin: 0 auto; padding: 24px 16px; }
.results-header { font-size: 15px; color: var(--text-secondary); margin-bottom: 16px; }
.results-list { min-height: 300px; }
</style>
