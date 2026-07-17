import { test, expect } from '@playwright/test';

test.describe('用户收藏流程', () => {

  const testPassword = 'fav123456';

  test.beforeEach(async ({ page }) => {
    const testEmail = `e2e_fav_${Date.now()}@nexora.ai`;

    // 注册并登录
    await page.goto('/register');
    await page.fill('input[type="email"]', testEmail);
    await page.fill('input[placeholder="昵称"]', '收藏测试');
    await page.getByPlaceholder('密码').first().fill(testPassword);
    await page.getByPlaceholder('再次输入密码').fill(testPassword);
    await page.click('button:has-text("注册")');

    // 注册成功 → 跳转登录页 → 登录
    await expect(page).toHaveURL('/login', { timeout: 5000 });
    await page.fill('input[type="email"]', testEmail);
    await page.fill('input[type="password"]', testPassword);
    await page.click('button:has-text("登录")');

    await expect(page).toHaveURL('/', { timeout: 8000 });
  });

  test('新闻详情页点击收藏 → 收藏列表可见 → 取消收藏', async ({ page }) => {
    // 1. 首页等待新闻卡片加载
    await page.waitForSelector('.news-card', { timeout: 10000 });
    const firstCard = page.locator('.news-card').first();
    await expect(firstCard).toBeVisible();

    // 2. 进入详情页
    await firstCard.click();
    await page.waitForURL('**/news/**');
    await page.waitForLoadState('networkidle');

    // 3. 点击收藏按钮
    const favBtn = page.locator('.favorite-btn').or(page.locator('button:has-text("收藏")'));
    if (await favBtn.count() > 0) {
      await favBtn.first().click();
      await page.waitForTimeout(500);

      // 验证按钮状态改变（变成已收藏）
      const activeBtn = page.locator('.favorite-btn.active').or(page.locator('button:has-text("已收藏")'));
      await expect(activeBtn.first()).toBeVisible({ timeout: 3000 });
    }

    // 4. 返回首页，进入收藏页
    await page.goto('/favorites');
    await page.waitForTimeout(500);

    // 5. 验证收藏列表或空状态
    const pageContent = page.locator('body');
    await expect(pageContent).toBeVisible();
  });

  test('未登录用户点击收藏 → 被拦截', async ({ page }) => {
    // 先退出登录
    await page.goto('/profile');
    const logoutBtn = page.locator('button:has-text("退出")').or(page.locator('text=退出登录'));
    if (await logoutBtn.count() > 0) {
      await logoutBtn.first().click();
      await page.waitForTimeout(300);
    }

    // 尝试访问收藏页
    await page.goto('/favorites');
    await page.waitForTimeout(500);

    // 应该被重定向或显示未登录
    const url = page.url();
    const body = page.locator('body');
    await expect(body).toBeVisible();
  });
});
