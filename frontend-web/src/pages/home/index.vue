<template>
  <div class="home-page">
    <!-- 推荐区域 -->
    <section v-if="store.recommendations.length" class="recommend-section">
      <div class="recommend-header">
        <h2 class="recommend-title">{{ $t('home.forYou') }}</h2>
        <span class="recommend-hint">{{ $t('home.basedOnInterests') }}</span>
      </div>
      <div class="recommend-scroll">
        <NewsCard
          v-for="item in store.recommendations"
          :key="'rec-' + item.id"
          :item="item"
          class="recommend-card"
        />
      </div>
    </section>

    <!-- 分类导航 -->
    <div class="category-bar">
      <el-button
        :type="activeCategory === null ? 'primary' : 'default'"
        size="small"
        @click="selectCategory(null)"
      >
        {{ $t('news.hot') }}
      </el-button>
      <el-button
        v-for="cat in store.categories"
        :key="cat.id"
        :type="activeCategory === cat.id ? 'primary' : 'default'"
        size="small"
        @click="selectCategory(cat.id)"
      >
        {{ $t('category.' + cat.code, cat.name) }}
      </el-button>
    </div>

    <!-- 新闻列表 -->
    <el-scrollbar height="calc(100vh - 180px)" class="news-scrollbar">
      <div v-loading="store.loading" class="news-feed"
           v-infinite-scroll="onLoadMore"
           :infinite-scroll-disabled="!store.hasMore"
           infinite-scroll-distance="100">
        <template v-if="store.hotList.length">
          <NewsCard
            v-for="item in store.hotList"
            :key="item.id"
            :item="item"
          />
          <div v-if="store.loadingMore" class="load-more-hint">{{ $t('common.loading') }}</div>
        </template>
        <el-empty v-else :description="$t('common.empty')" />
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import NewsCard from '@/components/common/NewsCard.vue';
import { useNewsStore } from '@/stores/newsStore';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const store = useNewsStore();
const authStore = useAuthStore();

const activeCategory = ref<number | null>(null);

function selectCategory(catId: number | null) {
  activeCategory.value = catId;
  store.fetchHotList(catId ?? undefined);
}

function onLoadMore() {
  store.loadMore();
}

onMounted(() => {
  store.fetchRecommendations();
  store.fetchCategories();
  store.fetchHotList();
  authStore.fetchProfile();
});
</script>

<style scoped>
.home-page {
  width: 100%;
}

/* ---- 推荐区域 ---- */
.recommend-section {
  max-width: 820px;
  margin: 0 auto 24px;
  padding: 0 4px;
}
.recommend-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
}
.recommend-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}
.recommend-hint {
  font-size: 12px;
  color: var(--text-secondary);
}
.recommend-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
}
.recommend-scroll::-webkit-scrollbar {
  height: 4px;
}
.recommend-scroll::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}
.recommend-card {
  flex: 0 0 260px;
  scroll-snap-align: start;
  margin-bottom: 0;
}

/* ---- 分类导航 ---- */
.category-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  justify-content: center;
}
.news-feed {
  max-width: 800px;
  margin: 0 auto;
  min-height: 400px;
  padding: 0 4px;
}
.news-scrollbar {
  max-width: 820px;
  margin: 0 auto;
}
.load-more-hint {
  text-align: center;
  padding: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}

@media (max-width: 768px) {
  .recommend-section {
    max-width: 100%;
    margin-bottom: 18px;
  }
  .recommend-card {
    flex: 0 0 220px;
  }
  .category-bar {
    gap: 6px;
    margin-bottom: 14px;
  }
  .news-feed {
    max-width: 100%;
  }
}
</style>
