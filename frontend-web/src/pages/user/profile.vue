<template>
  <div class="profile-page">
    <!-- 错误状态 -->
    <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon closable @close="errorMsg = ''" />

    <!-- 加载状态 -->
    <el-skeleton v-if="!authStore.user" :rows="6" animated />

    <template v-else>
      <!-- 用户信息卡片 -->
      <el-card class="profile-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">{{ $t('user.profile') }}</span>
            <el-button v-if="!editing" size="small" @click="startEdit">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-button>
          </div>
        </template>

        <!-- 查看模式 -->
        <div v-if="!editing" class="profile-view">
          <div class="avatar-section">
            <el-avatar :size="80" :src="authStore.user.avatar" class="profile-avatar">
              {{ (authStore.user.nickname || authStore.user.username || '?')[0] }}
            </el-avatar>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">{{ $t('auth.nickname') }}</span>
              <span class="info-value">{{ authStore.user.nickname || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('auth.email') }}</span>
              <span class="info-value">{{ authStore.user.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">{{ $t('user.language') }}</span>
              <span class="info-value">{{ i18n.locale.value === 'zh-CN' ? '中文' : 'English' }}</span>
            </div>
          </div>
        </div>

        <!-- 编辑模式 -->
        <el-form v-else ref="formRef" :model="editForm" label-width="80px" class="profile-form">
          <el-form-item :label="$t('auth.nickname')" prop="nickname"
            :rules="[{ max: 64, message: '最多 64 个字符', trigger: 'blur' }]">
            <el-input v-model="editForm.nickname" maxlength="64" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="saveProfile">
              {{ $t('common.save') }}
            </el-button>
            <el-button @click="cancelEdit">
              {{ $t('common.cancel') }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 设置卡片 -->
      <el-card class="settings-card" shadow="hover">
        <template #header>
          <span class="card-title">{{ $t('user.settings') }}</span>
        </template>

        <div class="settings-list">
          <!-- 主题 -->
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">{{ $t('user.theme') }}</span>
              <span class="setting-value">
                {{ settingsStore.theme === 'light' ? $t('user.lightMode') : $t('user.darkMode') }}
              </span>
            </div>
            <el-switch
              :model-value="settingsStore.theme === 'dark'"
              inline-prompt
              :active-icon="Moon"
              :inactive-icon="Sunny"
              @change="settingsStore.toggleTheme()"
            />
          </div>

          <!-- 语言 -->
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">{{ $t('user.language') }}</span>
              <span class="setting-value">
                {{ i18n.locale.value === 'zh-CN' ? '中文 (zh-CN)' : 'English (en-US)' }}
              </span>
            </div>
            <el-button size="small" @click="toggleLang">
              {{ i18n.locale.value === 'zh-CN' ? 'Switch to English' : '切换到中文' }}
            </el-button>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Moon, Sunny, Edit } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/authStore';
import { useSettingsStore } from '@/stores/settingsStore';
import http from '@/utils/http';
import type { FormInstance } from 'element-plus';

const i18n = useI18n();
const authStore = useAuthStore();
const settingsStore = useSettingsStore();

function toggleLang() {
  const newLocale = i18n.locale.value === 'zh-CN' ? 'en-US' : 'zh-CN';
  i18n.locale.value = newLocale;
  localStorage.setItem('nexora-locale', newLocale);
}

const editing = ref(false);
const saving = ref(false);
const errorMsg = ref('');
const formRef = ref<FormInstance>();

const editForm = reactive({
  nickname: '',
});

onMounted(async () => {
  await authStore.fetchProfile();
  if (authStore.user) {
    editForm.nickname = authStore.user.nickname || '';
  }
});

function startEdit() {
  if (authStore.user) {
    editForm.nickname = authStore.user.nickname || '';
  }
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
  if (authStore.user) {
    editForm.nickname = authStore.user.nickname || '';
  }
}

async function saveProfile() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    saving.value = true;
    await http.put('/api/v1/user/profile', { nickname: editForm.nickname });
    await authStore.fetchProfile();
    editing.value = false;
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || e?.message || '保存失败';
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

/* ---- 查看模式 ---- */
.profile-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 12px 0;
}
.profile-avatar {
  font-size: 28px;
}
.info-grid {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
}
.info-label {
  font-size: 14px;
  color: var(--text-secondary);
}
.info-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

/* ---- 编辑表单 ---- */
.profile-form {
  padding-top: 4px;
}

/* ---- 设置 ---- */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}
.setting-row:last-child {
  border-bottom: none;
}
.setting-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.setting-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}
.setting-value {
  font-size: 13px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .profile-page {
    max-width: 100%;
    gap: 14px;
  }
}
</style>
