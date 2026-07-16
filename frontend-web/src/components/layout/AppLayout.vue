<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="header-inner">
        <!-- Logo -->
        <div class="header-left">
          <router-link to="/" class="logo-link">
            <h1 class="logo">Nexora AI</h1>
            <span class="slogan">{{ $t('nav.home') }}</span>
          </router-link>
        </div>

        <!-- 搜索 (桌面端) -->
        <div class="header-center">
          <el-input
            v-model="searchQuery"
            :placeholder="$t('search.placeholder')"
            class="search-input"
            size="default"
            @keyup.enter="goSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 右侧：导航 + 主题 + 语言 + 用户 -->
        <div class="header-right">
          <nav class="nav-links">
            <router-link to="/search" class="nav-link">
              <el-icon><Search /></el-icon>
              <span class="nav-text">{{ $t('nav.search') }}</span>
            </router-link>
            <template v-if="authStore.isAuthenticated">
              <router-link to="/user/favorites" class="nav-link">
                <el-icon><Star /></el-icon>
                <span class="nav-text">{{ $t('nav.favorites') }}</span>
              </router-link>
              <router-link to="/user/subscriptions" class="nav-link">
                <el-icon><Bell /></el-icon>
                <span class="nav-text">{{ $t('nav.subscriptions') }}</span>
              </router-link>
            </template>
          </nav>

          <el-divider direction="vertical" />

          <!-- 主题切换 -->
          <el-tooltip
            :content="settingsStore.theme === 'light' ? $t('user.darkMode') : $t('user.lightMode')"
            placement="bottom"
          >
            <el-button class="icon-btn" text circle @click="settingsStore.toggleTheme()">
              <el-icon :size="18">
                <Moon v-if="settingsStore.theme === 'light'" />
                <Sunny v-else />
              </el-icon>
            </el-button>
          </el-tooltip>

          <!-- 语言选择：下拉菜单列出所有支持的语言 -->
          <el-dropdown trigger="click" @command="switchLang">
            <el-button class="lang-btn" text size="small">
              {{ localeDisplayName(settingsStore.locale) }}
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="loc in SUPPORTED_LOCALES"
                  :key="loc"
                  :command="loc"
                  :class="{ 'is-active': settingsStore.locale === loc }"
                >
                  {{ localeDisplayName(loc) }}
                  <span v-if="settingsStore.locale === loc" class="check-mark">✓</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 用户菜单 -->
          <template v-if="authStore.isAuthenticated">
            <router-link to="/user/profile" class="nav-link user-link">
              <el-avatar :size="28" :src="authStore.user?.avatar" />
              <span class="nav-text">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            </router-link>
            <el-button class="logout-btn" text size="small" @click="authStore.logout(); $router.push('/')">
              {{ $t('nav.logout') }}
            </el-button>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">
              <el-button size="small" type="primary">{{ $t('nav.login') }}</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 主要内容区 -->
    <main class="app-main">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { Search, Moon, Sunny, Star, Bell, ArrowDown } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/authStore';
import { useSettingsStore } from '@/stores/settingsStore';
import { SUPPORTED_LOCALES, localeDisplayName } from '@/locales/config';

const router = useRouter();
const i18n = useI18n();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

// 双向同步 locale：settingsStore ↔ i18n
onMounted(() => {
  if (i18n.locale.value !== settingsStore.locale) {
    i18n.locale.value = settingsStore.locale;
  }
});
// i18n 变化 → store（仅接受受支持的语言）
watch(() => i18n.locale.value, (val) => {
  if (val !== settingsStore.locale && SUPPORTED_LOCALES.includes(val as typeof SUPPORTED_LOCALES[number])) {
    settingsStore.setLocale(val);
  }
});
// store 变化 → i18n
watch(() => settingsStore.locale, (val) => {
  if (i18n.locale.value !== val) {
    i18n.locale.value = val;
  }
});

const searchQuery = ref('');

function goSearch() {
  if (searchQuery.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(searchQuery.value.trim())}`);
  }
}

/** 切换语言（下拉菜单选择），通过 store 统一管理 */
function switchLang(loc: string) {
  settingsStore.setLocale(loc);
  i18n.locale.value = loc;
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* ---- Header ---- */
.app-header {
  background: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left {
  flex-shrink: 0;
}
.logo-link {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent-color);
  margin: 0;
}
.slogan {
  font-size: 12px;
  color: var(--text-secondary);
  display: none;
}

.header-center {
  flex: 1;
  max-width: 400px;
}
.search-input {
  width: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  font-size: 14px;
  color: var(--text-primary);
  border-radius: 6px;
  transition: background 0.2s;
  text-decoration: none;
}
.nav-link:hover {
  background: var(--hover-bg);
}
.nav-link.router-link-active {
  color: var(--accent-color);
}
.user-link {
  gap: 6px;
  padding: 4px 8px;
}

.icon-btn {
  font-size: 18px;
}
.lang-btn {
  font-weight: 600;
  font-size: 12px;
  min-width: 36px;
  padding: 4px 6px;
}
.logout-btn {
  font-size: 12px;
}

/* ---- Main ---- */
.app-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px 16px;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .app-header {
    height: 52px;
  }
  .header-inner {
    padding: 0 12px;
    gap: 8px;
  }
  .slogan {
    display: none;
  }
  .header-center {
    display: none; /* 移动端隐藏搜索框，改用搜索页 */
  }
  .nav-text {
    display: none;
  }
  .nav-link {
    padding: 6px 8px;
  }
  .user-link .nav-text {
    display: none;
  }
  .logout-btn {
    display: none;
  }
  .app-main {
    padding: 12px 10px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .header-center {
    max-width: 260px;
  }
}
</style>
