import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../models/user.dart';
import 'api_client.dart';

final authServiceProvider = Provider<AuthService>((ref) {
  return AuthService(ref.watch(dioProvider), ref.watch(secureStorageProvider));
});

class AuthService {
  final Dio _dio;
  final FlutterSecureStorage _storage;

  AuthService(this._dio, this._storage);

  Future<LoginResponse> login(String email, String password) async {
    final response = await _dio.post('/api/v1/auth/login', data: {
      'email': email,
      'password': password,
    });
    final loginResp = LoginResponse.fromJson(response.data['data']);
    await _storage.write(key: 'access_token', value: loginResp.token);
    await _storage.write(key: 'refresh_token', value: loginResp.refreshToken);
    return loginResp;
  }

  Future<void> register(String email, String password, String nickname) async {
    await _dio.post('/api/v1/auth/register', data: {
      'email': email,
      'password': password,
      'nickname': nickname,
    });
  }

  Future<User> getProfile() async {
    final response = await _dio.get('/api/v1/user/profile');
    return User.fromJson(response.data['data']);
  }

  Future<void> logout() async {
    await _storage.deleteAll();
  }

  Future<bool> isLoggedIn() async {
    final token = await _storage.read(key: 'access_token');
    return token != null;
  }
}
