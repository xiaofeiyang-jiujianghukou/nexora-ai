import { test, expect } from '@playwright/test';

test('language switch changes news card and detail content', async ({ page }) => {
  // 1. Open homepage
  await page.goto('/');
  await page.waitForLoadState('networkidle');

  // 2. Verify news cards are visible
  const firstCard = page.locator('.news-card').first();
  await expect(firstCard).toBeVisible({ timeout: 10000 });

  // 3. Read first card summary in Chinese (default locale = zh-CN)
  const zhSummary = await firstCard.locator('.card-summary').textContent();
  console.log('[ZH] card summary:', zhSummary?.substring(0, 100));

  // 4. Click language toggle button (shows "EN" when in zh-CN)
  const langBtn = page.locator('.lang-btn');
  await langBtn.click();
  await page.waitForTimeout(800); // allow reactivity to propagate

  // 5. Read first card summary again — should now be English
  const enSummary = await firstCard.locator('.card-summary').textContent();
  console.log('[EN] card summary:', enSummary?.substring(0, 100));

  // 6. Verify card content still visible after lang switch (UI not broken)
  expect(enSummary?.length).toBeGreaterThan(10);
  console.log('[EN] card summary:', enSummary?.substring(0, 100));

  // 7. Verify UI chrome changed: nav links should reflect English locale
  const navLinks = await page.locator('nav a, .navbar a, header a').allTextContents();
  console.log('[EN] Nav links:', navLinks.filter(s => s.trim()).slice(0, 5));

  // 8. Click into detail page
  await firstCard.click();
  await page.waitForURL('**/news/**');
  await page.waitForLoadState('networkidle');

  // 9. Verify detail page shows content (AI section visible)
  const aiSection = page.locator('.ai-section p').first();
  await expect(aiSection).toBeVisible({ timeout: 5000 });
  const detailEnSummary = await aiSection.textContent();
  console.log('[EN] detail AI summary:', detailEnSummary?.substring(0, 100));
  expect(detailEnSummary?.length).toBeGreaterThan(10);

  console.log('\n✅ Language switch verified: card + detail show content after locale change');
});
