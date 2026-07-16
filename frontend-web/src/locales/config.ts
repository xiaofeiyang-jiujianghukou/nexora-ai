/**
 * 多语种配置中心
 *
 * 新增语言步骤：
 * 1. 在 SUPPORTED_LOCALES 中添加 locale code（如 'ja-JP'）
 * 2. 在本地创建对应的 JSON 消息文件（如 ja-JP.json）
 * 3. 后端 aiResult 中增加对应语言 key 的输出（如 "ja"）
 */

/** 所有支持的语言（顺序 = 下拉菜单顺序） */
export const SUPPORTED_LOCALES = [
  'zh-CN', 'en-US',
] as const;

/** 默认语言 */
export const DEFAULT_LOCALE = 'zh-CN';

/** i18n 回退语言 */
export const FALLBACK_LOCALE = 'en-US';

/** 各语言显示名称（用于切换按钮和下拉菜单） */
const DISPLAY_NAMES: Record<string, string> = {
  'zh-CN': '中文',
  'en-US': 'EN',
};

/**
 * 从 locale 提取语言 key（用于 aiResult 字段）
 * 'zh-CN' → 'zh', 'en-US' → 'en', 'ja-JP' → 'ja'
 */
export function localeToLang(locale: string): string {
  const hyphenIdx = locale.indexOf('-');
  return hyphenIdx > 0 ? locale.substring(0, hyphenIdx) : locale;
}

/** 获取 locale 的显示名称 */
export function localeDisplayName(locale: string): string {
  return DISPLAY_NAMES[locale] || locale;
}

/** 获取循环切换的下一个 locale */
export function nextLocale(current: string): string {
  const idx = SUPPORTED_LOCALES.indexOf(current as typeof SUPPORTED_LOCALES[number]);
  if (idx < 0) return SUPPORTED_LOCALES[0];
  return SUPPORTED_LOCALES[(idx + 1) % SUPPORTED_LOCALES.length];
}

/** 下一个 locale 的显示名称（用于切换按钮文字） */
export function nextLocaleLabel(current: string): string {
  return localeDisplayName(nextLocale(current));
}

/** 判断列表是否缺失某个语言 key 的章节 */
export function isLangMissing(aiResult: Record<string, unknown> | null | undefined, lang: string): boolean {
  if (!aiResult) return true;
  return !aiResult[lang] || aiResult[lang] == null;
}

/**
 * 从 aiResult 获取当前语言的最佳内容
 * 回退链: 当前语言 → en（国际化通用回退） → null（显示原文标题）
 */
export function getLocalizedContent<T = Record<string, unknown>>(
  aiResult: Record<string, unknown> | null | undefined,
  lang: string,
): T | null {
  if (!aiResult) return null;
  // 1. 精确匹配
  if (aiResult[lang] && typeof aiResult[lang] === 'object') {
    return aiResult[lang] as T;
  }
  // 2. 回退到英文（国际化通用回退，不做其他语言兜底）
  if (lang !== 'en' && aiResult['en'] && typeof aiResult['en'] === 'object') {
    return aiResult['en'] as T;
  }
  // 3. 无可用内容 → 由调用方使用原文 title
  return null;
}
