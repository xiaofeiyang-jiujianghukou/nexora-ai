// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$NewsArticleImpl _$$NewsArticleImplFromJson(Map<String, dynamic> json) =>
    _$NewsArticleImpl(
      id: (json['id'] as num).toInt(),
      title: json['title'] as String,
      summary: json['summary'] as String?,
      content: json['content'] as String?,
      sourceUrl: json['sourceUrl'] as String?,
      sourceName: json['sourceName'] as String?,
      language: json['language'] as String,
      categoryName: json['categoryName'] as String?,
      categoryCode: json['categoryCode'] as String?,
      hotScore: (json['hotScore'] as num?)?.toDouble(),
      viewCount: (json['viewCount'] as num?)?.toInt(),
      likeCount: (json['likeCount'] as num?)?.toInt(),
      publishTime: json['publishTime'] == null
          ? null
          : DateTime.parse(json['publishTime'] as String),
      createdTime: json['createdTime'] == null
          ? null
          : DateTime.parse(json['createdTime'] as String),
      coverImageUrl: json['coverImageUrl'] as String?,
    );

Map<String, dynamic> _$$NewsArticleImplToJson(_$NewsArticleImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'summary': instance.summary,
      'content': instance.content,
      'sourceUrl': instance.sourceUrl,
      'sourceName': instance.sourceName,
      'language': instance.language,
      'categoryName': instance.categoryName,
      'categoryCode': instance.categoryCode,
      'hotScore': instance.hotScore,
      'viewCount': instance.viewCount,
      'likeCount': instance.likeCount,
      'publishTime': instance.publishTime?.toIso8601String(),
      'createdTime': instance.createdTime?.toIso8601String(),
      'coverImageUrl': instance.coverImageUrl,
    };

_$NewsCategoryImpl _$$NewsCategoryImplFromJson(Map<String, dynamic> json) =>
    _$NewsCategoryImpl(
      id: (json['id'] as num).toInt(),
      name: json['name'] as String,
      code: json['code'] as String,
      parentId: (json['parentId'] as num?)?.toInt(),
      sort: (json['sort'] as num?)?.toInt(),
    );

Map<String, dynamic> _$$NewsCategoryImplToJson(_$NewsCategoryImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'code': instance.code,
      'parentId': instance.parentId,
      'sort': instance.sort,
    };
