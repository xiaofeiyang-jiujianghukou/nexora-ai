// ============================================================
// Nexora AI — k6 性能测试脚本
// 用法:
//   k6 run tests/k6/nexora-api-test.js
//   k6 run --env BASE_URL=http://prod:8080 tests/k6/nexora-api-test.js
//   k6 run --vus 50 --duration 60s tests/k6/nexora-api-test.js
// ============================================================
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// ---- 自定义指标 ----
const newsListDuration = new Trend('news_list_duration', true);
const searchDuration = new Trend('search_duration', true);
const detailDuration = new Trend('detail_duration', true);
const loginDuration = new Trend('login_duration', true);
const errorRate = new Rate('errors');
const successCount = new Counter('successful_requests');

// ---- 配置 ----
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  // 分阶段压测
  stages: [
    { duration: '30s', target: 10 },   // 预热：10 VU
    { duration: '1m',  target: 50 },   // 爬升：50 VU
    { duration: '1m',  target: 50 },   // 稳定：50 VU
    { duration: '30s', target: 100 },  // 加压：100 VU
    { duration: '1m',  target: 100 },  // 峰值：100 VU
    { duration: '30s', target: 0 },    // 冷却
  ],

  // 性能阈值
  thresholds: {
    'http_req_duration':     ['p(95)<1000', 'p(99)<3000'], // 95% 请求 < 1s
    'http_req_failed':       ['rate<0.02'],                 // 错误率 < 2%
    'news_list_duration':    ['p(95)<800'],
    'search_duration':       ['p(95)<1500'],
    'detail_duration':       ['p(95)<500'],
    'login_duration':        ['p(95)<1000'],
    'errors':                ['rate<0.02'],
  },
};

// ---- Setup: 获取测试数据 ----
export function setup() {
  // 注册并登录以获取 token
  const testEmail = `perf_${Date.now()}@test.com`;
  const regRes = http.post(`${BASE_URL}/api/v1/auth/register`, JSON.stringify({
    email: testEmail,
    password: 'test123456',
    nickname: 'PerfTest',
  }), { headers: { 'Content-Type': 'application/json' } });

  let token = '';
  if (regRes.status === 200) {
    token = regRes.json('data.token') || '';
  }

  // 如果注册失败（用户已存在），尝试登录
  if (!token) {
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
      email: testEmail,
      password: 'test123456',
    }), { headers: { 'Content-Type': 'application/json' } });
    token = loginRes.json('data.token') || '';
  }

  return { token, testEmail };
}

// ---- 主测试流程 ----
export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
  };
  if (data.token) {
    headers['Authorization'] = `Bearer ${data.token}`;
  }

  // 1. 新闻列表（高频端点）
  group('News List', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/news/list?page=1&size=20`, { headers });
    newsListDuration.add(Date.now() - start);

    const ok = check(res, {
      'news/list status 200': (r) => r.status === 200,
      'news/list has data':  (r) => r.json('data.list') !== undefined,
    });
    if (ok) successCount.add(1);
    errorRate.add(!ok);
    sleep(1);
  });

  // 2. 新闻详情
  group('News Detail', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/news/1`, { headers });
    detailDuration.add(Date.now() - start);

    const ok = check(res, {
      'news/detail status OK': (r) => r.status === 200 || r.status === 404,
    });
    errorRate.add(!ok);
    sleep(0.5);
  });

  // 3. 搜索
  group('Search', () => {
    const queries = ['AI', 'technology', '中国', 'Japan', 'Wirtschaft'];
    const q = queries[Math.floor(Math.random() * queries.length)];

    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/search/news?q=${q}&page=1&size=10`, { headers });
    searchDuration.add(Date.now() - start);

    const ok = check(res, {
      'search status 200': (r) => r.status === 200,
    });
    errorRate.add(!ok);
    sleep(1.5);
  });

  // 4. 新闻推荐
  group('Recommendations', () => {
    const res = http.get(`${BASE_URL}/api/v1/news/recommendations?limit=10`, { headers });
    const ok = check(res, {
      'recommendations status 200': (r) => r.status === 200,
    });
    errorRate.add(!ok);
    sleep(1);
  });

  // 5. 分类列表
  group('Categories', () => {
    const res = http.get(`${BASE_URL}/api/v1/news/categories`, { headers });
    const ok = check(res, {
      'categories status 200': (r) => r.status === 200,
    });
    errorRate.add(!ok);
    sleep(0.5);
  });

  // 6. 认证（低频 — 仅 10% VU 执行）
  if (Math.random() < 0.1) {
    group('Login', () => {
      const start = Date.now();
      const res = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email: data.testEmail || 'test@nexora.ai',
        password: 'test123456',
      }), { headers: { 'Content-Type': 'application/json' } });
      loginDuration.add(Date.now() - start);

      const ok = check(res, {
        'login status OK': (r) => r.status === 200 || r.status === 401,
      });
      errorRate.add(!ok);
    });
  }
}

// ---- Teardown ----
export function teardown(data) {
  // 清理测试数据（可选）
}

// ---- 摘要报告 ----
export function handleSummary(data) {
  return {
    'tests/k6/results/summary.json': JSON.stringify(data),
    stdout: `
╔═══════════════════════════════════════════════════════════╗
║         Nexora AI — k6 Performance Report               ║
╠═══════════════════════════════════════════════════════════╣
║  Total Requests:    ${data.metrics.http_reqs.values.count}
║  Failed:            ${data.metrics.http_req_failed.values.passes || 0}
║  Avg Duration:      ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
║  P95 Duration:      ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms
║  P99 Duration:      ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms
║  Peak VUs:          ${data.metrics.vus_max.values.max}
╠═══════════════════════════════════════════════════════════╣
║  News List   AVG:   ${data.metrics.news_list_duration?.values.avg.toFixed(2) || 'N/A'}ms
║  Search      AVG:   ${data.metrics.search_duration?.values.avg.toFixed(2) || 'N/A'}ms
║  Detail      AVG:   ${data.metrics.detail_duration?.values.avg.toFixed(2) || 'N/A'}ms
║  Login       AVG:   ${data.metrics.login_duration?.values.avg.toFixed(2) || 'N/A'}ms
╚═══════════════════════════════════════════════════════════╝
`,
  };
}
