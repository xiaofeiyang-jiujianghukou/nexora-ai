import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/locale_provider.dart';

class LangSwitcher extends ConsumerWidget {
  const LangSwitcher({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locale = ref.watch(localeProvider);

    return PopupMenuButton<Locale>(
      icon: Text(_flagEmoji(locale.languageCode),
          style: const TextStyle(fontSize: 20)),
      tooltip: 'Language',
      onSelected: (loc) {
        ref.read(localeProvider.notifier).state = loc;
      },
      itemBuilder: (context) => supportedLocales.map((loc) {
        return PopupMenuItem<Locale>(
          value: loc,
          child: Text('${_flagEmoji(loc.languageCode)}  ${localeNames[supportedLocales.indexOf(loc)]}'),
        );
      }).toList(),
    );
  }

  String _flagEmoji(String code) {
    switch (code) {
      case 'zh':
        return '🇨🇳';
      case 'en':
        return '🇺🇸';
      case 'ja':
        return '🇯🇵';
      case 'ko':
        return '🇰🇷';
      case 'de':
        return '🇩🇪';
      default:
        return '🌐';
    }
  }
}
