import { defineStore } from 'pinia';
import { ref } from 'vue';

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

  // 启动时立即应用
  applyTheme();

  return { theme, toggleTheme, applyTheme };
});
