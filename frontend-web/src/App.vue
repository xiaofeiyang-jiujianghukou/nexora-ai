<template>
  <el-config-provider :locale="elLocale">
    <AppLayout v-if="!$route.meta.plain">
      <router-view />
    </AppLayout>
    <router-view v-else />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import en from 'element-plus/dist/locale/en.mjs';
import AppLayout from '@/components/layout/AppLayout.vue';
import { localeToLang } from '@/locales/config';

const i18n = useI18n();

/** Element Plus locale：按语言 key 匹配，未知语言回退到 en */
const elLocale = computed(() => {
  const lang = localeToLang(i18n.locale.value);
  if (lang === 'zh') return zhCn;
  return en; // 其余全部 fallback 到 en
});
</script>
