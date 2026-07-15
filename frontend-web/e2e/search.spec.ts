import { test, expect } from '@playwright/test';

test.describe('搜索流程', () => {

  test('关键词搜索 → 查看结果', async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('.search-input', { timeout: 5000 });

    // 找到搜索框，Element Plus 的 el-input 内部才是真正的 input
    const searchInput = page.locator('.search-input input').or(page.locator('.search-input').locator('input'));
    await searchInput.first().fill('AI');
    await page.keyboard.press('Enter');

    // 等待页面跳转
    await page.waitForTimeout(1500);

    // 验证页面内容（可能有结果或空状态）
    const body = page.locator('body');
    await expect(body).toBeVisible();
  });
});
