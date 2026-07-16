import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user.dart';
import '../services/auth_service.dart';

final isLoggedInProvider = FutureProvider<bool>((ref) async {
  final service = ref.watch(authServiceProvider);
  return service.isLoggedIn();
});

final currentUserProvider = FutureProvider<User?>((ref) async {
  final isLoggedIn = ref.watch(isLoggedInProvider).value ?? false;
  if (!isLoggedIn) return null;
  final service = ref.watch(authServiceProvider);
  return service.getProfile();
});

final authStateProvider = StateNotifierProvider<AuthNotifier, AsyncValue<User?>>(
  (ref) => AuthNotifier(ref),
);

class AuthNotifier extends StateNotifier<AsyncValue<User?>> {
  final Ref _ref;

  AuthNotifier(this._ref) : super(const AsyncValue.loading()) {
    _init();
  }

  Future<void> _init() async {
    state = const AsyncValue.loading();
    try {
      final service = _ref.read(authServiceProvider);
      final loggedIn = await service.isLoggedIn();
      if (loggedIn) {
        final user = await service.getProfile();
        state = AsyncValue.data(user);
      } else {
        state = const AsyncValue.data(null);
      }
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> login(String email, String password) async {
    state = const AsyncValue.loading();
    try {
      final service = _ref.read(authServiceProvider);
      final response = await service.login(email, password);
      state = AsyncValue.data(response.user);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> logout() async {
    final service = _ref.read(authServiceProvider);
    await service.logout();
    state = const AsyncValue.data(null);
  }
}
