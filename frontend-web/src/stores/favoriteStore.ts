import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '@/utils/http';
import type { NewsItem } from '@/components/common/NewsCard.vue';

export const useFavoriteStore = defineStore('favorite', () => {
  const favorites = ref<NewsItem[]>([]);
  const loading = ref(false);

  async function fetchFavorites(page = 1, size = 20) {
    loading.value = true;
    try {
      const res = await http.get('/api/v1/favorites', { params: { page, size } });
      favorites.value = res.data.list || [];
    } finally {
      loading.value = false;
    }
  }

  async function addFavorite(newsId: number) {
    await http.post(`/api/v1/news/${newsId}/favorite`);
  }

  async function removeFavorite(newsId: number) {
    await http.delete(`/api/v1/news/${newsId}/favorite`);
    favorites.value = favorites.value.filter(f => f.id !== newsId);
  }

  return { favorites, loading, fetchFavorites, addFavorite, removeFavorite };
});
