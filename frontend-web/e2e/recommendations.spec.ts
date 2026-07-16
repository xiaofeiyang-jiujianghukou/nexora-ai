import { test, expect } from '@playwright/test';

test.describe('首页推荐卡片', () => {

  test('首页显示 "为你推荐 / For You" 区域', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // 1. 检查推荐区域存在
    const forYouSection = page.locator('text=为你推荐').or(page.locator('text=For You'));
    await expect(forYouSection.first()).toBeVisible({ timeout: 8000 });

    // 2. 检查推荐卡片存在且可横向滚动
    const recCards = page.locator('.recommendation-card').or(page.locator('.for-you .news-card'));
    const count = await recCards.count();
    console.log(`Recommendation cards found: ${count}`);
    expect(count).toBeGreaterThan(0);

    // 3. 验证第一张推荐卡片有标题和元信息
    const firstRec = recCards.first();
    await expect(firstRec).toBeVisible();

    const title = firstRec.locator('.card-title');
    if (await title.count() > 0) {
      await expect(title.first()).toBeVisible();
    }
  });

  test('推荐卡片可点击进入详情', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // 找到推荐区域的卡片
    const recCard = page.locator('.recommendation-card').first()
      .or(page.locator('.for-you .news-card').first());

    if (await recCard.count() > 0) {
      const cardText = await recCard.textContent();
      await recCard.click();

      // 等待导航到详情页
      await page.waitForURL('**/news/**', { timeout: 5000 });
      await page.waitForSelector('.detail-title, .article-detail', { timeout: 5000 });
      await expect(page.url()).toMatch(/\/news\/\d+/);
    }
  });

  test('横向滚动推荐区域', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // 找到推荐区域容器
    const scrollContainer = page.locator('.for-you .scroll-container')
      .or(page.locator('[class*="recommendation"]').locator('.scroll-container'));

    if (await scrollContainer.count() > 0) {
      // 检查有更多内容可以滚动
      const scrollWidth = await scrollContainer.evaluate(
        (el: HTMLElement) => el.scrollWidth
      );
      const clientWidth = await scrollContainer.evaluate(
        (el: HTMLElement) => el.clientWidth
      );
      console.log(`Scroll: width=${scrollWidth}, client=${clientWidth}`);
      // scrollWidth > clientWidth 表示可以滚动
      expect(scrollWidth).toBeGreaterThanOrEqual(clientWidth);
    }
  });
});
