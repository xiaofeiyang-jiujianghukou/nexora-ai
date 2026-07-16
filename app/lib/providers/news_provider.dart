import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/news.dart';
import '../services/news_service.dart';

final newsListProvider =
    FutureProvider.family<List<NewsArticle>, String?>((ref, categoryCode) async {
  final service = ref.watch(newsServiceProvider);
  final data = await service.getNewsList(categoryCode: categoryCode);
  final list = data['list'] as List;
  return list.map((j) => NewsArticle.fromJson(j)).toList();
});

final recommendationsProvider =
    FutureProvider<List<NewsArticle>>((ref) async {
  final service = ref.watch(newsServiceProvider);
  return service.getRecommendations();
});

final categoriesProvider = FutureProvider<List<NewsCategory>>((ref) async {
  final service = ref.watch(newsServiceProvider);
  return service.getCategories();
});

final newsDetailProvider =
    FutureProvider.family<Map<String, dynamic>, int>((ref, id) async {
  final service = ref.watch(newsServiceProvider);
  return service.getDetail(id);
});

final searchProvider =
    FutureProvider.family<List<NewsArticle>, String>((ref, query) async {
  final service = ref.watch(newsServiceProvider);
  return service.search(query);
});
