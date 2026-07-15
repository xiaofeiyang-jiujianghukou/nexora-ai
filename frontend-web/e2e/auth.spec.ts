import { test, expect } from '@playwright/test';

test.describe('用户认证流程', () => {

  const testEmail = `e2e_${Date.now()}@nexora.ai`;
  const testPassword = 'e2e123456';

  test('注册 → 登录 → 跳转首页 → 退出', async ({ page }) => {
    // 1. 访问注册页
    await page.goto('/register');
    await expect(page.locator('h2')).toContainText('注册');

    // 2. 填写注册表单
    await page.fill('input[type="email"]', testEmail);
    await page.fill('input[placeholder="昵称"]', 'E2E测试');
    await page.getByPlaceholder('密码').first().fill(testPassword);
    await page.getByPlaceholder('再次输入密码').fill(testPassword);
    await page.click('button:has-text("注册")');

    // 3. 注册成功 → 跳转登录页
    await expect(page).toHaveURL('/login');

    // 4. 登录
    await page.fill('input[type="email"]', testEmail);
    await page.fill('input[type="password"]', testPassword);
    await page.click('button:has-text("登录")');

    // 5. 跳转首页
    await expect(page).toHaveURL('/');
    await expect(page.locator('.logo')).toContainText('Nexora AI');
  });
});
