import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '@/utils/http';
import type { NewsItem } from '@/components/common/NewsCard.vue';

export interface NewsDetail {
  id: number;
  title: string;
  content: string;
  summary: string;
  sourceName: string;
  sourceUrl: string;
  categoryName: string;
  hotScore: number;
  viewCount: number;
  likeCount: number;
  tags?: string[];
  publishTime: string;
  aiAnalysis?: {
    summary: string;
    background: string;
    impact: string;
    keywords: string[];
    sentiment: string;
  };
}

export interface Category {
  id: number;
  name: string;
  code: string;
  parentId: number;
}

export const useNewsStore = defineStore('news', () => {
  const hotList = ref<NewsItem[]>([]);
  const categories = ref<Category[]>([]);
  const loading = ref(false);

  async function fetchHotList(categoryId?: number) {
    loading.value = true;
    try {
      const res = await http.get('/api/v1/news/list', {
        params: { page: 1, size: 20, categoryId },
      });
      hotList.value = res.data.list || [];
    } finally {
      loading.value = false;
    }
  }

  async function fetchCategories() {
    const res = await http.get('/api/v1/news/categories');
    categories.value = res.data || [];
  }

  async function getDetail(id: number): Promise<NewsDetail> {
    const res = await http.get(`/api/v1/news/${id}`);
    return res.data;
  }

  async function getRelated(id: number): Promise<NewsItem[]> {
    const res = await http.get(`/api/v1/news/${id}/related`);
    return res.data || [];
  }

  return { hotList, categories, loading, fetchHotList, fetchCategories, getDetail, getRelated };
});
