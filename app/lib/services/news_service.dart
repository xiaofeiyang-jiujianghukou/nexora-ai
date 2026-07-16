import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/news.dart';
import 'api_client.dart';

final newsServiceProvider = Provider<NewsService>((ref) {
  return NewsService(ref.watch(dioProvider));
});

class NewsService {
  final Dio _dio;

  NewsService(this._dio);

  Future<Map<String, dynamic>> getNewsList({
    int page = 1,
    int size = 10,
    String? categoryCode,
  }) async {
    final response = await _dio.get('/api/v1/news/list', queryParameters: {
      'page': page,
      'size': size,
      if (categoryCode != null) 'categoryCode': categoryCode,
    });
    return response.data['data'];
  }

  Future<Map<String, dynamic>> getDetail(int id) async {
    final response = await _dio.get('/api/v1/news/$id');
    return response.data['data'];
  }

  Future<List<NewsCategory>> getCategories() async {
    final response = await _dio.get('/api/v1/news/categories');
    final list = response.data['data'] as List;
    return list.map((j) => NewsCategory.fromJson(j)).toList();
  }

  Future<List<NewsArticle>> getRecommendations({int limit = 20}) async {
    final response = await _dio.get('/api/v1/news/recommendations',
        queryParameters: {'limit': limit});
    final list = response.data['data'] as List;
    return list.map((j) => NewsArticle.fromJson(j)).toList();
  }

  Future<List<NewsArticle>> search(String query, {int page = 1}) async {
    final response = await _dio.get('/api/v1/search/news',
        queryParameters: {'q': query, 'page': page, 'size': 10});
    final data = response.data['data'];
    final list = data['list'] as List;
    return list.map((j) => NewsArticle.fromJson(j)).toList();
  }
}
