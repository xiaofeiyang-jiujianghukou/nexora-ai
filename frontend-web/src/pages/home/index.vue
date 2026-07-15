<template>
  <div class="home-page">
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
        {{ cat.name }}
      </el-button>
    </div>

    <!-- 新闻列表 -->
    <div v-loading="store.loading" class="news-feed">
      <template v-if="store.hotList.length">
        <NewsCard
          v-for="item in store.hotList"
          :key="item.id"
          :item="item"
        />
      </template>
      <el-empty v-else :description="$t('common.empty')" />
    </div>
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

onMounted(() => {
  store.fetchCategories();
  store.fetchHotList();
  authStore.fetchProfile();
});
</script>

<style scoped>
.home-page {
  width: 100%;
}
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
}

@media (max-width: 768px) {
  .category-bar {
    gap: 6px;
    margin-bottom: 14px;
  }
  .news-feed {
    max-width: 100%;
  }
}
</style>
