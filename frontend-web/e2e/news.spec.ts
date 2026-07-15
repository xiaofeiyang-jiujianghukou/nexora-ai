import { test, expect } from '@playwright/test';

test.describe('新闻浏览流程', () => {

  test('首页加载 → 新闻卡片显示 → 点击进入详情', async ({ page }) => {
    await page.goto('/');

    // 等待新闻卡片加载
    await page.waitForSelector('.news-card', { timeout: 10000 });

    // 至少有新闻卡片
    const cards = page.locator('.news-card');
    const count = await cards.count();
    expect(count).toBeGreaterThan(0);

    // 验证卡片内容
    const firstCard = cards.first();
    await expect(firstCard.locator('.card-title')).toBeVisible();
    await expect(firstCard.locator('.card-meta')).toBeVisible();

    // 点击进入详情
    const title = await firstCard.locator('.card-title').textContent();
    await firstCard.click();

    // 等待详情页加载（检查 URL 变化和内容渲染）
    await page.waitForSelector('.detail-title', { timeout: 10000 });
    await expect(page.locator('.detail-title')).toBeVisible();
    await expect(page.locator('.detail-meta')).toBeVisible();
  });

  test('分类筛选', async ({ page }) => {
    await page.goto('/');

    // 分类按钮存在
    const catBar = page.locator('.category-bar');
    await expect(catBar).toBeVisible();

    // 点击 AI 分类
    const aiBtn = catBar.locator('button:has-text("AI")');
    if (await aiBtn.count() > 0) {
      await aiBtn.click();
      await page.waitForTimeout(500);
    }
  });
});
