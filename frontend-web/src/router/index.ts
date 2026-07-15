import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/pages/home/index.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/news/:id',
    name: 'news-detail',
    component: () => import('@/pages/news/detail.vue'),
    meta: { title: '新闻详情' },
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/pages/search/index.vue'),
    meta: { title: '搜索' },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/pages/auth/login.vue'),
    meta: { title: '登录', guest: true, plain: true },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/pages/auth/register.vue'),
    meta: { title: '注册', guest: true, plain: true },
  },
  {
    path: '/user/profile',
    name: 'user-profile',
    component: () => import('@/pages/user/profile.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: '/user/favorites',
    name: 'user-favorites',
    component: () => import('@/pages/user/favorites.vue'),
    meta: { title: '我的收藏', requiresAuth: true },
  },
  {
    path: '/user/subscriptions',
    name: 'user-subscriptions',
    component: () => import('@/pages/user/subscriptions.vue'),
    meta: { title: '我的订阅', requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
});

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('nexora-token');

  if (to.meta.requiresAuth && !token) {
    next({ name: 'login', query: { redirect: to.fullPath } });
  } else if (to.meta.guest && token) {
    next({ name: 'home' });
  } else {
    next();
  }
});

export default router;
