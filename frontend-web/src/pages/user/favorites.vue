<template>
  <div class="fav-page">
    <div class="page-topbar">
      <router-link to="/" class="back-link">← {{ $t('common.back') }}</router-link>
      <h2 class="page-title">{{ $t('nav.favorites') }}</h2>
    </div>

    <div v-loading="favStore.loading" class="page-main">
      <template v-if="favStore.favorites.length">
        <NewsCard
          v-for="item in favStore.favorites"
          :key="item.id"
          :item="item"
        />
      </template>
      <el-empty v-else :description="$t('common.empty')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import NewsCard from '@/components/common/NewsCard.vue';
import { useFavoriteStore } from '@/stores/favoriteStore';

const favStore = useFavoriteStore();

onMounted(() => favStore.fetchFavorites());
</script>

<style scoped>
.fav-page {
  max-width: 760px;
  margin: 0 auto;
  width: 100%;
}
.page-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0 16px;
}
.back-link { font-size: 15px; color: var(--accent-color); }
.page-title { font-size: 20px; font-weight: 600; margin: 0; color: var(--text-primary); }
.page-main { min-height: 400px; }

@media (max-width: 768px) {
  .fav-page { max-width: 100%; }
}
</style>
