import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// 语言切换 Provider — 同步到后端 user.language
final localeProvider = StateProvider<Locale>((ref) {
  return const Locale('zh', 'CN');
});

final localeIndexProvider = Provider<int>((ref) {
  final locale = ref.watch(localeProvider);
  switch (locale.languageCode) {
    case 'zh':
      return 0;
    case 'en':
      return 1;
    case 'ja':
      return 2;
    case 'ko':
      return 3;
    case 'de':
      return 4;
    default:
      return 0;
  }
});

/// 支持的语言列表
final supportedLocales = const [
  Locale('zh', 'CN'),
  Locale('en', 'US'),
  Locale('ja', 'JP'),
  Locale('ko', 'KR'),
  Locale('de', 'DE'),
];

final localeNames = const ['中文', 'English', '日本語', '한국어', 'Deutsch'];
