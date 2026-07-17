import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:sentry_flutter/sentry_flutter.dart';
import 'app.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Sentry 错误追踪 — DSN 通过环境变量或注入配置
  await SentryFlutter.init(
    (options) {
      options.dsn = const String.fromEnvironment('SENTRY_DSN', defaultValue: '');
      options.environment = const String.fromEnvironment('ENV', defaultValue: 'development');
      options.release = const String.fromEnvironment('APP_VERSION', defaultValue: '1.0.0');
      options.tracesSampleRate = 0.3;
    },
    appRunner: () => runApp(
      const ProviderScope(
        child: NexoraApp(),
      ),
    ),
  );
}
