import { defineStore } from 'pinia';
import { ref } from 'vue';
import { DEFAULT_LOCALE, nextLocale } from '@/locales/config';

export const useSettingsStore = defineStore('settings', () => {
  // ---- 主题 ----
  const theme = ref<'light' | 'dark'>(
    (localStorage.getItem('nexora-theme') as 'light' | 'dark') || 'light'
  );

  function applyTheme() {
    document.documentElement.setAttribute('data-theme', theme.value);
    document.documentElement.classList.toggle('dark', theme.value === 'dark');
    localStorage.setItem('nexora-theme', theme.value);
  }

  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light';
    applyTheme();
  }

  // ---- 语言（持久化到 localStorage，由 AppLayout 双向同步到 i18n） ----
  const locale = ref<string>(
    localStorage.getItem('nexora-locale') || DEFAULT_LOCALE
  );

  function setLocale(lang: string) {
    locale.value = lang;
    localStorage.setItem('nexora-locale', lang);
  }

  /** 切换到下一个支持的语言 */
  function toggleLocale() {
    setLocale(nextLocale(locale.value));
  }

  // 启动时立即应用
  applyTheme();

  return { theme, toggleTheme, applyTheme, locale, setLocale, toggleLocale };
});
