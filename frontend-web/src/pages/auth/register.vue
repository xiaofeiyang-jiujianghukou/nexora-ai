<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">{{ $t('auth.register') }}</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item :label="$t('auth.nickname')" prop="nickname">
          <el-input
            v-model="form.nickname"
            :placeholder="$t('auth.nickname')"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item :label="$t('auth.email')" prop="email">
          <el-input
            v-model="form.email"
            type="email"
            :placeholder="$t('auth.email')"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item :label="$t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="$t('auth.password')"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item :label="'确认密码'" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="auth-btn" @click="handleRegister">
            {{ $t('auth.register') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-footer">
        {{ $t('auth.hasAccount') }}
        <router-link to="/login">{{ $t('auth.login') }}</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();
const { t } = useI18n();

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
});

const validateConfirm = (_rule: unknown, value: string, callback: (e?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'));
  }
  callback();
};

const rules: FormRules = {
  nickname: [
    { required: true, message: () => t('auth.nicknameMax'), trigger: 'blur' },
    { max: 64, message: () => t('auth.nicknameMax'), trigger: 'blur' },
  ],
  email: [
    { required: true, message: () => t('auth.emailRequired'), trigger: 'blur' },
    { type: 'email', message: () => t('auth.emailInvalid'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { max: 32, message: () => t('auth.passwordRequired'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
};

async function handleRegister() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    await authStore.register({
      email: form.email,
      password: form.password,
      nickname: form.nickname,
    });
    ElMessage.success(t('auth.registerSuccess'));
    router.push('/login');
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : t('common.operationFailed');
    ElMessage.error(msg);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-secondary);
}

.auth-card {
  width: 420px;
  padding: 40px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: 0 2px 12px var(--shadow-color);
}

.auth-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 24px;
  color: var(--text-primary);
}

.auth-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.auth-footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
