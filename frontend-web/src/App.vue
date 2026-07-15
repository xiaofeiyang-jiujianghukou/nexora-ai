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

const i18n = useI18n();

const elLocale = computed(() => {
  return i18n.locale.value === 'zh-CN' ? zhCn : en;
});
</script>
