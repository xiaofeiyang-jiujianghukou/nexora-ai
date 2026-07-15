<template>
  <div class="fav-page">
    <el-container>
      <el-header class="page-header">
        <router-link to="/" class="back-link">← {{ $t('common.back') }}</router-link>
        <h2>{{ $t('nav.favorites') }}</h2>
      </el-header>

      <el-main v-loading="favStore.loading" class="page-main">
        <template v-if="favStore.favorites.length">
          <NewsCard
            v-for="item in favStore.favorites"
            :key="item.id"
            :item="item"
          />
        </template>
        <el-empty v-else :description="$t('common.empty')" />
      </el-main>
    </el-container>
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
.fav-page { min-height: 100vh; background: var(--bg-secondary); }
.page-header {
  display: flex; align-items: center; gap: 16px; padding: 0 24px;
  height: 56px; background: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
}
.back-link { font-size: 15px; color: var(--accent-color); }
.page-main { max-width: 760px; margin: 0 auto; padding: 24px 16px; }
</style>
