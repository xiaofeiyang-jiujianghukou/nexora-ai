// ============================================================
// Nexora AI — k6 Smoke Test（冒烟测试）
// 用法: k6 run tests/k6/nexora-smoke.js
// 快速验证所有端点可用，不上强度
// ============================================================
import http from 'k6/http';
import { check, group } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.3'],
  },
};

export default function () {
  group('Health', () => {
    const res = http.get(`${BASE_URL}/actuator/health`);
    check(res, { 'health up': (r) => r.status === 200 });
  });

  group('News List', () => {
    const res = http.get(`${BASE_URL}/api/v1/news/list?page=1&size=5`);
    check(res, {
      'status 200': (r) => r.status === 200,
      'has list': (r) => r.json('data.list') !== undefined,
    });
  });

  group('Search', () => {
    const res = http.get(`${BASE_URL}/api/v1/search/news?q=AI&page=1&size=5`);
    check(res, { 'status 200': (r) => r.status === 200 });
  });

  group('Categories', () => {
    const res = http.get(`${BASE_URL}/api/v1/news/categories`);
    check(res, { 'status 200': (r) => r.status === 200 });
  });

  group('Recommendations', () => {
    const res = http.get(`${BASE_URL}/api/v1/news/recommendations?limit=5`);
    check(res, { 'status 200': (r) => r.status === 200 });
  });
}
