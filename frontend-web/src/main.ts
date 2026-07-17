import { createApp } from 'vue';
import { createPinia } from 'pinia';
import * as Sentry from '@sentry/vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';

import App from './App.vue';
import router from './router';
import i18n from './locales';
import './assets/styles/global.css';

const app = createApp(App);

// Sentry 错误追踪 — DSN 通过环境变量 VITE_SENTRY_DSN 传入
if (import.meta.env.VITE_SENTRY_DSN) {
  Sentry.init({
    app,
    dsn: import.meta.env.VITE_SENTRY_DSN,
    environment: import.meta.env.MODE,
    release: import.meta.env.VITE_APP_VERSION || '1.0.0',
    tracesSampleRate: import.meta.env.PROD ? 0.3 : 1.0,
    replaysSessionSampleRate: 0.1,
    replaysOnErrorSampleRate: 1.0,
    integrations: [
      Sentry.browserTracingIntegration({ router }),
      Sentry.replayIntegration(),
    ],
  });
}

app.use(createPinia());
app.use(router);
app.use(ElementPlus);
app.use(i18n);

app.mount('#app');
