import { createI18n } from 'vue-i18n';
import { DEFAULT_LOCALE, FALLBACK_LOCALE } from './config';
import zhCN from './zh-CN.json';
import enUS from './en-US.json';
import jaJP from './ja-JP.json';
import koKR from './ko-KR.json';
import frFR from './fr-FR.json';
import deDE from './de-DE.json';
import ruRU from './ru-RU.json';

// 所有 locale 消息在此注册（新增语言时添加对应 import + 条目）
const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'ja-JP': jaJP,
  'ko-KR': koKR,
  'fr-FR': frFR,
  'de-DE': deDE,
  'ru-RU': ruRU,
};

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('nexora-locale') || DEFAULT_LOCALE,
  fallbackLocale: FALLBACK_LOCALE,
  messages,
});

export default i18n;
