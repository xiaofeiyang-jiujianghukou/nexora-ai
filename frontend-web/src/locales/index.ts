import { createI18n } from 'vue-i18n';
import { DEFAULT_LOCALE, FALLBACK_LOCALE } from './config';
import zhCN from './zh-CN.json';
import enUS from './en-US.json';

// 所有 locale 消息在此注册（新增语言时添加对应 import + 条目）
const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
};

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('nexora-locale') || DEFAULT_LOCALE,
  fallbackLocale: FALLBACK_LOCALE,
  messages,
});

export default i18n;
