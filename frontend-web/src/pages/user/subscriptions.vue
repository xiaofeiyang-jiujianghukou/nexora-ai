<template>
  <div class="sub-page">
    <div class="page-topbar">
      <router-link to="/" class="back-link">← {{ $t('common.back') }}</router-link>
      <h2 class="page-title">{{ $t('subscribe.title') }}</h2>
    </div>

    <div class="page-main">
      <!-- 添加订阅 -->
      <div class="add-section">
        <el-select v-model="subType" class="type-select" size="default">
          <el-option :label="$t('subscribe.tag')" value="TAG" />
          <el-option :label="$t('subscribe.entity')" value="ENTITY" />
          <el-option :label="$t('subscribe.category')" value="CATEGORY" />
          <el-option :label="$t('subscribe.company')" value="COMPANY" />
        </el-select>
        <el-input v-model="subTarget" :placeholder="$t('subscribe.target')" class="target-input" @keyup.enter="addSub" />
        <el-button type="primary" @click="addSub">{{ $t('subscribe.add') }}</el-button>
      </div>

      <!-- 订阅列表 -->
      <div v-loading="loading" class="sub-list">
        <template v-if="subs.length">
          <div v-for="sub in subs" :key="sub.id" class="sub-item">
            <el-tag size="small">{{ sub.type }}</el-tag>
            <span class="sub-target">{{ sub.target }}</span>
            <el-button text type="danger" size="small" @click="removeSub(sub.id)">{{ $t('common.delete') }}</el-button>
          </div>
        </template>
        <el-empty v-else :description="$t('common.empty')" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import http from '@/utils/http';

interface Subscription {
  id: number;
  type: string;
  target: string;
}

const subs = ref<Subscription[]>([]);
const loading = ref(false);
const subType = ref('TAG');
const subTarget = ref('');

async function fetch() {
  loading.value = true;
  try {
    const res = await http.get('/api/v1/subscribe/list');
    subs.value = res.data || [];
  } finally { loading.value = false; }
}

async function addSub() {
  if (!subTarget.value.trim()) return;
  try {
    await http.post('/api/v1/subscribe', { type: subType.value, target: subTarget.value.trim() });
    subTarget.value = '';
    ElMessage.success('订阅成功');
    fetch();
  } catch (e: any) { ElMessage.error(e.message); }
}

async function removeSub(id: number) {
  try {
    await http.delete(`/api/v1/subscribe/${id}`);
    ElMessage.success('已取消');
    fetch();
  } catch (e: any) { ElMessage.error(e.message); }
}

onMounted(fetch);
</script>

<style scoped>
.sub-page {
  max-width: 640px;
  margin: 0 auto;
  width: 100%;
}
.page-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0 16px;
}
.back-link { font-size: 15px; color: var(--accent-color); }
.page-title { font-size: 20px; font-weight: 600; margin: 0; color: var(--text-primary); }
.page-main { min-height: 300px; }
.add-section { display: flex; gap: 10px; margin-bottom: 24px; }
.type-select { width: 100px; }
.target-input { flex: 1; }
.sub-list { min-height: 200px; }
.sub-item { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border-color); }
.sub-target { flex: 1; font-size: 15px; color: var(--text-primary); }

@media (max-width: 768px) {
  .sub-page { max-width: 100%; }
  .add-section { flex-direction: column; }
  .type-select { width: 100%; }
}
</style>
