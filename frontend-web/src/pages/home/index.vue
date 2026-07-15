<template>
  <div class="home-page">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <h1 class="logo">Nexora AI</h1>
          <span class="slogan">全球智能信息平台</span>
        </div>
        <div class="header-right">
          <el-input
            v-model="searchQuery"
            :placeholder="$t('search.placeholder')"
            class="search-input"
            @keyup.enter="goSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <template v-if="authStore.isAuthenticated">
            <router-link to="/user/profile" class="nav-link">{{ authStore.user?.nickname }}</router-link>
            <router-link to="/user/favorites" class="nav-link">{{ $t('nav.favorites') }}</router-link>
            <el-button text @click="authStore.logout()">{{ $t('nav.logout') }}</el-button>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">{{ $t('nav.login') }}</router-link>
          </template>
        </div>
      </el-header>

      <el-main class="app-main">
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
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import NewsCard from '@/components/common/NewsCard.vue';
import { useNewsStore } from '@/stores/newsStore';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const store = useNewsStore();
const authStore = useAuthStore();

const searchQuery = ref('');
const activeCategory = ref<number | null>(null);

function selectCategory(catId: number | null) {
  activeCategory.value = catId;
  store.fetchHotList(catId ?? undefined);
}

function goSearch() {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`);
  }
}

onMounted(() => {
  store.fetchCategories();
  store.fetchHotList();
  authStore.fetchProfile();
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--bg-secondary);
}
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 60px;
  background: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent-color);
}
.slogan {
  font-size: 13px;
  color: var(--text-secondary);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.search-input {
  width: 320px;
}
.nav-link {
  font-size: 14px;
  color: var(--text-primary);
}
.app-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 16px;
}
.category-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.news-feed {
  min-height: 400px;
}
</style>
