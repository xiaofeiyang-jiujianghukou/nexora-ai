import 'package:freezed_annotation/freezed_annotation.dart';

part 'news.freezed.dart';
part 'news.g.dart';

@freezed
class NewsArticle with _$NewsArticle {
  const factory NewsArticle({
    required int id,
    required String title,
    String? summary,
    String? content,
    String? sourceUrl,
    String? sourceName,
    required String language,
    String? categoryName,
    String? categoryCode,
    double? hotScore,
    int? viewCount,
    int? likeCount,
    DateTime? publishTime,
    DateTime? createdTime,
    String? coverImageUrl,
  }) = _NewsArticle;

  factory NewsArticle.fromJson(Map<String, dynamic> json) =>
      _$NewsArticleFromJson(json);
}

@freezed
class NewsCategory with _$NewsCategory {
  const factory NewsCategory({
    required int id,
    required String name,
    required String code,
    int? parentId,
    int? sort,
  }) = _NewsCategory;

  factory NewsCategory.fromJson(Map<String, dynamic> json) =>
      _$NewsCategoryFromJson(json);
}
