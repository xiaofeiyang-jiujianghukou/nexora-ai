import 'package:go_router/go_router.dart';
import '../screens/home_screen.dart';
import '../screens/detail_screen.dart';
import '../screens/login_screen.dart';
import '../screens/profile_screen.dart';

/// 应用路由 — GoRouter 实例
final router = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(
      path: '/',
      name: 'home',
      builder: (context, state) => const HomeScreen(),
    ),
    GoRoute(
      path: '/news/:id',
      name: 'detail',
      builder: (context, state) {
        final id = int.tryParse(state.pathParameters['id'] ?? '');
        return DetailScreen(newsId: id);
      },
    ),
    GoRoute(
      path: '/login',
      name: 'login',
      builder: (context, state) => const LoginScreen(),
    ),
    GoRoute(
      path: '/profile',
      name: 'profile',
      builder: (context, state) => const ProfileScreen(),
    ),
  ],
);
