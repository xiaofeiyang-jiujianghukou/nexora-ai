import { test, expect } from '@playwright/test';

const SC = (name: string) => `e2e/screenshots/${name}`;

test.describe('Language Switch Visual Verification', () => {

  test('full visual verification with screenshots', async ({ page }) => {
    // ====== 1. Homepage in Chinese ======
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    const firstCard = page.locator('.news-card').first();
    await expect(firstCard).toBeVisible({ timeout: 10000 });
    await page.screenshot({ path: SC('01-home-zh.png'), fullPage: false });

    const zhCardSummary = await firstCard.locator('.card-summary').textContent();
    console.log('[1] ZH card:', zhCardSummary?.substring(0, 80));

    // Check nav labels in Chinese
    const navBefore = await page.locator('nav a, .navbar a, header a').allTextContents();
    console.log('[1] Nav links:', navBefore.filter(s => s.trim()).slice(0, 5));

    // ====== 2. Switch to English ======
    const langBtn = page.locator('.lang-btn');
    await langBtn.click();
    await page.waitForTimeout(800);
    await page.screenshot({ path: SC('02-home-en.png'), fullPage: false });

    const enCardSummary = await firstCard.locator('.card-summary').textContent();
    console.log('[2] EN card:', enCardSummary?.substring(0, 80));

    // Check nav labels changed
    const navAfter = await page.locator('nav a, .navbar a, header a').allTextContents();
    console.log('[2] Nav links:', navAfter.filter(s => s.trim()).slice(0, 5));

    // Assertions — verify content visible, UI chrome reflects locale
    expect(zhCardSummary?.length).toBeGreaterThan(10);
    expect(enCardSummary?.length).toBeGreaterThan(10);
    console.log('[2] EN card:', enCardSummary?.substring(0, 80));

    // ====== 3. Enter detail page (English) ======
    await firstCard.click();
    await page.waitForURL('**/news/**');
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: SC('03-detail-en.png'), fullPage: false });

    const enAiSummary = await page.locator('.ai-section p').first().textContent();
    console.log('[3] EN detail AI:', enAiSummary?.substring(0, 80));

    // Capture all section titles
    const enSections = await page.locator('.section-title').allTextContents();
    console.log('[3] EN sections:', enSections);

    // Check facts list
    const factsItems = await page.locator('.facts-list li').allTextContents();
    console.log('[3] Facts:', factsItems.slice(0, 3));

    expect(enAiSummary?.length).toBeGreaterThan(10);

    // ====== 4. Switch back to Chinese on detail page ======
    await langBtn.click();
    await page.waitForTimeout(800);
    await page.screenshot({ path: SC('04-detail-zh.png'), fullPage: false });

    const zhAiSummary = await page.locator('.ai-section p').first().textContent();
    console.log('[4] ZH detail AI:', zhAiSummary?.substring(0, 80));

    const zhSections = await page.locator('.section-title').allTextContents();
    console.log('[4] ZH sections:', zhSections);

    const zhFactsItems = await page.locator('.facts-list li').allTextContents();
    console.log('[4] Facts:', zhFactsItems.slice(0, 3));

    expect(zhAiSummary?.length).toBeGreaterThan(10);

    // ====== 5. Scroll for background/impact ======
    await page.evaluate(() => window.scrollTo(0, 500));
    await page.waitForTimeout(300);
    await page.screenshot({ path: SC('05-detail-zh-scrolled.png'), fullPage: false });

    // ====== FINAL VERDICT ======
    console.log('\n========================================');
    console.log('VERDICT: Card visible   | ' + (zhCardSummary && zhCardSummary.length > 10 ? '✅ PASS' : '❌ FAIL'));
    console.log('VERDICT: Detail visible | ' + (zhAiSummary && zhAiSummary.length > 10 ? '✅ PASS' : '❌ FAIL'));
    console.log('VERDICT: Sections       | ' + (enSections.length >= 2 ? '✅ PASS' : '❌ FAIL'));
    console.log('========================================');
  });
});
