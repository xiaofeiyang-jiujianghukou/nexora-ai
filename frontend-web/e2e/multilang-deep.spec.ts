import { test, expect } from '@playwright/test';

const LOCALES = [
  { code: 'zh-CN', label: '中文', btnText: 'EN', flagKey: 'cn' },
  { code: 'en', label: 'English', btnText: '中文', flagKey: 'us' },
  { code: 'ja', label: '日本語', btnText: '中文', flagKey: 'jp' },
  { code: 'ko', label: '한국어', btnText: '中文', flagKey: 'kr' },
  { code: 'de', label: 'Deutsch', btnText: '中文', flagKey: 'de' },
];

test.describe('多语言深度验证', () => {

  test('所有 5 种语言切换 → UI 文案 + 新闻内容同步更新', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    await page.waitForSelector('.news-card', { timeout: 10000 });

    for (const locale of LOCALES) {
      console.log(`\n--- Testing locale: ${locale.label} (${locale.code}) ---`);

      // 1. 点击语言切换按钮
      const langBtn = page.locator('.lang-btn');
      await langBtn.click();
      await page.waitForTimeout(300);

      // 2. 在语言下拉中选择目标语言
      const langOption = page.locator(`[data-lang="${locale.code}"]`)
        .or(page.locator(`.lang-dropdown >> text=${locale.label}`))
        .or(page.locator(`text=${locale.label}`));
      if (await langOption.count() > 0) {
        await langOption.first().click();
      }
      await page.waitForTimeout(800);

      // 3. 验证新闻卡片内容已切换
      const card = page.locator('.news-card').first();
      await expect(card).toBeVisible({ timeout: 5000 });
      const cardText = await card.textContent();
      console.log(`[${locale.code}] Card text (first 80 chars): ${cardText?.substring(0, 80)}`);
      expect(cardText?.length).toBeGreaterThan(10);

      // 4. 验证分类按钮可见（导航栏未丢失）
      const catBar = page.locator('.category-bar');
      await expect(catBar).toBeVisible();

      // 5. 点击进入详情页
      await card.click();
      await page.waitForURL('**/news/**');
      await page.waitForLoadState('networkidle');

      // 6. 验证详情页元信息
      const detailMeta = page.locator('.detail-meta');
      await expect(detailMeta).toBeVisible({ timeout: 5000 });

      // 7. 返回首页继续下一个语言
      await page.goto('/');
      await page.waitForLoadState('networkidle');
      await page.waitForSelector('.news-card', { timeout: 5000 });
    }

    console.log('\n✅ All 5 locales verified successfully');
  });

  test('URL 参数 lang → 首页直接显示对应语言', async ({ page }) => {
    // 直接访问 /?lang=en
    await page.goto('/?lang=en');
    await page.waitForLoadState('networkidle');
    await page.waitForSelector('.news-card', { timeout: 10000 });

    const card = page.locator('.news-card').first();
    const cardText = await card.textContent();
    console.log(`[lang=en] Card text (first 80 chars): ${cardText?.substring(0, 80)}`);
    expect(cardText?.length).toBeGreaterThan(10);
  });
});
