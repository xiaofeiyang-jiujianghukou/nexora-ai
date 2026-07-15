import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import http from '@/utils/http';

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  nickname: string;
  avatar: string;
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('nexora-token') || '');
  const refreshToken = ref<string>(localStorage.getItem('nexora-refresh-token') || '');
  const user = ref<UserInfo | null>(null);

  const isAuthenticated = computed(() => !!token.value);

  async function login(email: string, password: string) {
    const res = await http.post('/api/v1/auth/login', { email, password });
    token.value = res.data.token;
    refreshToken.value = res.data.refreshToken;
    user.value = res.data.user;
    localStorage.setItem('nexora-token', token.value);
    localStorage.setItem('nexora-refresh-token', refreshToken.value);
    return res.data;
  }

  async function register(form: { email: string; password: string; nickname: string }) {
    const res = await http.post('/api/v1/auth/register', form);
    return res.data;
  }

  async function fetchProfile() {
    if (!token.value) return;
    const res = await http.get('/api/v1/user/profile');
    user.value = res.data;
  }

  function logout() {
    token.value = '';
    refreshToken.value = '';
    user.value = null;
    localStorage.removeItem('nexora-token');
    localStorage.removeItem('nexora-refresh-token');
  }

  return { token, refreshToken, user, isAuthenticated, login, register, fetchProfile, logout };
});
