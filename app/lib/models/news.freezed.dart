// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'news.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

NewsArticle _$NewsArticleFromJson(Map<String, dynamic> json) {
  return _NewsArticle.fromJson(json);
}

/// @nodoc
mixin _$NewsArticle {
  int get id => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String? get summary => throw _privateConstructorUsedError;
  String? get content => throw _privateConstructorUsedError;
  String? get sourceUrl => throw _privateConstructorUsedError;
  String? get sourceName => throw _privateConstructorUsedError;
  String get language => throw _privateConstructorUsedError;
  String? get categoryName => throw _privateConstructorUsedError;
  String? get categoryCode => throw _privateConstructorUsedError;
  double? get hotScore => throw _privateConstructorUsedError;
  int? get viewCount => throw _privateConstructorUsedError;
  int? get likeCount => throw _privateConstructorUsedError;
  DateTime? get publishTime => throw _privateConstructorUsedError;
  DateTime? get createdTime => throw _privateConstructorUsedError;
  String? get coverImageUrl => throw _privateConstructorUsedError;

  /// Serializes this NewsArticle to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of NewsArticle
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $NewsArticleCopyWith<NewsArticle> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $NewsArticleCopyWith<$Res> {
  factory $NewsArticleCopyWith(
          NewsArticle value, $Res Function(NewsArticle) then) =
      _$NewsArticleCopyWithImpl<$Res, NewsArticle>;
  @useResult
  $Res call(
      {int id,
      String title,
      String? summary,
      String? content,
      String? sourceUrl,
      String? sourceName,
      String language,
      String? categoryName,
      String? categoryCode,
      double? hotScore,
      int? viewCount,
      int? likeCount,
      DateTime? publishTime,
      DateTime? createdTime,
      String? coverImageUrl});
}

/// @nodoc
class _$NewsArticleCopyWithImpl<$Res, $Val extends NewsArticle>
    implements $NewsArticleCopyWith<$Res> {
  _$NewsArticleCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of NewsArticle
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? summary = freezed,
    Object? content = freezed,
    Object? sourceUrl = freezed,
    Object? sourceName = freezed,
    Object? language = null,
    Object? categoryName = freezed,
    Object? categoryCode = freezed,
    Object? hotScore = freezed,
    Object? viewCount = freezed,
    Object? likeCount = freezed,
    Object? publishTime = freezed,
    Object? createdTime = freezed,
    Object? coverImageUrl = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      summary: freezed == summary
          ? _value.summary
          : summary // ignore: cast_nullable_to_non_nullable
              as String?,
      content: freezed == content
          ? _value.content
          : content // ignore: cast_nullable_to_non_nullable
              as String?,
      sourceUrl: freezed == sourceUrl
          ? _value.sourceUrl
          : sourceUrl // ignore: cast_nullable_to_non_nullable
              as String?,
      sourceName: freezed == sourceName
          ? _value.sourceName
          : sourceName // ignore: cast_nullable_to_non_nullable
              as String?,
      language: null == language
          ? _value.language
          : language // ignore: cast_nullable_to_non_nullable
              as String,
      categoryName: freezed == categoryName
          ? _value.categoryName
          : categoryName // ignore: cast_nullable_to_non_nullable
              as String?,
      categoryCode: freezed == categoryCode
          ? _value.categoryCode
          : categoryCode // ignore: cast_nullable_to_non_nullable
              as String?,
      hotScore: freezed == hotScore
          ? _value.hotScore
          : hotScore // ignore: cast_nullable_to_non_nullable
              as double?,
      viewCount: freezed == viewCount
          ? _value.viewCount
          : viewCount // ignore: cast_nullable_to_non_nullable
              as int?,
      likeCount: freezed == likeCount
          ? _value.likeCount
          : likeCount // ignore: cast_nullable_to_non_nullable
              as int?,
      publishTime: freezed == publishTime
          ? _value.publishTime
          : publishTime // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      createdTime: freezed == createdTime
          ? _value.createdTime
          : createdTime // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      coverImageUrl: freezed == coverImageUrl
          ? _value.coverImageUrl
          : coverImageUrl // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$NewsArticleImplCopyWith<$Res>
    implements $NewsArticleCopyWith<$Res> {
  factory _$$NewsArticleImplCopyWith(
          _$NewsArticleImpl value, $Res Function(_$NewsArticleImpl) then) =
      __$$NewsArticleImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int id,
      String title,
      String? summary,
      String? content,
      String? sourceUrl,
      String? sourceName,
      String language,
      String? categoryName,
      String? categoryCode,
      double? hotScore,
      int? viewCount,
      int? likeCount,
      DateTime? publishTime,
      DateTime? createdTime,
      String? coverImageUrl});
}

/// @nodoc
class __$$NewsArticleImplCopyWithImpl<$Res>
    extends _$NewsArticleCopyWithImpl<$Res, _$NewsArticleImpl>
    implements _$$NewsArticleImplCopyWith<$Res> {
  __$$NewsArticleImplCopyWithImpl(
      _$NewsArticleImpl _value, $Res Function(_$NewsArticleImpl) _then)
      : super(_value, _then);

  /// Create a copy of NewsArticle
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? summary = freezed,
    Object? content = freezed,
    Object? sourceUrl = freezed,
    Object? sourceName = freezed,
    Object? language = null,
    Object? categoryName = freezed,
    Object? categoryCode = freezed,
    Object? hotScore = freezed,
    Object? viewCount = freezed,
    Object? likeCount = freezed,
    Object? publishTime = freezed,
    Object? createdTime = freezed,
    Object? coverImageUrl = freezed,
  }) {
    return _then(_$NewsArticleImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      summary: freezed == summary
          ? _value.summary
          : summary // ignore: cast_nullable_to_non_nullable
              as String?,
      content: freezed == content
          ? _value.content
          : content // ignore: cast_nullable_to_non_nullable
              as String?,
      sourceUrl: freezed == sourceUrl
          ? _value.sourceUrl
          : sourceUrl // ignore: cast_nullable_to_non_nullable
              as String?,
      sourceName: freezed == sourceName
          ? _value.sourceName
          : sourceName // ignore: cast_nullable_to_non_nullable
              as String?,
      language: null == language
          ? _value.language
          : language // ignore: cast_nullable_to_non_nullable
              as String,
      categoryName: freezed == categoryName
          ? _value.categoryName
          : categoryName // ignore: cast_nullable_to_non_nullable
              as String?,
      categoryCode: freezed == categoryCode
          ? _value.categoryCode
          : categoryCode // ignore: cast_nullable_to_non_nullable
              as String?,
      hotScore: freezed == hotScore
          ? _value.hotScore
          : hotScore // ignore: cast_nullable_to_non_nullable
              as double?,
      viewCount: freezed == viewCount
          ? _value.viewCount
          : viewCount // ignore: cast_nullable_to_non_nullable
              as int?,
      likeCount: freezed == likeCount
          ? _value.likeCount
          : likeCount // ignore: cast_nullable_to_non_nullable
              as int?,
      publishTime: freezed == publishTime
          ? _value.publishTime
          : publishTime // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      createdTime: freezed == createdTime
          ? _value.createdTime
          : createdTime // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      coverImageUrl: freezed == coverImageUrl
          ? _value.coverImageUrl
          : coverImageUrl // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$NewsArticleImpl implements _NewsArticle {
  const _$NewsArticleImpl(
      {required this.id,
      required this.title,
      this.summary,
      this.content,
      this.sourceUrl,
      this.sourceName,
      required this.language,
      this.categoryName,
      this.categoryCode,
      this.hotScore,
      this.viewCount,
      this.likeCount,
      this.publishTime,
      this.createdTime,
      this.coverImageUrl});

  factory _$NewsArticleImpl.fromJson(Map<String, dynamic> json) =>
      _$$NewsArticleImplFromJson(json);

  @override
  final int id;
  @override
  final String title;
  @override
  final String? summary;
  @override
  final String? content;
  @override
  final String? sourceUrl;
  @override
  final String? sourceName;
  @override
  final String language;
  @override
  final String? categoryName;
  @override
  final String? categoryCode;
  @override
  final double? hotScore;
  @override
  final int? viewCount;
  @override
  final int? likeCount;
  @override
  final DateTime? publishTime;
  @override
  final DateTime? createdTime;
  @override
  final String? coverImageUrl;

  @override
  String toString() {
    return 'NewsArticle(id: $id, title: $title, summary: $summary, content: $content, sourceUrl: $sourceUrl, sourceName: $sourceName, language: $language, categoryName: $categoryName, categoryCode: $categoryCode, hotScore: $hotScore, viewCount: $viewCount, likeCount: $likeCount, publishTime: $publishTime, createdTime: $createdTime, coverImageUrl: $coverImageUrl)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$NewsArticleImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.summary, summary) || other.summary == summary) &&
            (identical(other.content, content) || other.content == content) &&
            (identical(other.sourceUrl, sourceUrl) ||
                other.sourceUrl == sourceUrl) &&
            (identical(other.sourceName, sourceName) ||
                other.sourceName == sourceName) &&
            (identical(other.language, language) ||
                other.language == language) &&
            (identical(other.categoryName, categoryName) ||
                other.categoryName == categoryName) &&
            (identical(other.categoryCode, categoryCode) ||
                other.categoryCode == categoryCode) &&
            (identical(other.hotScore, hotScore) ||
                other.hotScore == hotScore) &&
            (identical(other.viewCount, viewCount) ||
                other.viewCount == viewCount) &&
            (identical(other.likeCount, likeCount) ||
                other.likeCount == likeCount) &&
            (identical(other.publishTime, publishTime) ||
                other.publishTime == publishTime) &&
            (identical(other.createdTime, createdTime) ||
                other.createdTime == createdTime) &&
            (identical(other.coverImageUrl, coverImageUrl) ||
                other.coverImageUrl == coverImageUrl));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      title,
      summary,
      content,
      sourceUrl,
      sourceName,
      language,
      categoryName,
      categoryCode,
      hotScore,
      viewCount,
      likeCount,
      publishTime,
      createdTime,
      coverImageUrl);

  /// Create a copy of NewsArticle
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$NewsArticleImplCopyWith<_$NewsArticleImpl> get copyWith =>
      __$$NewsArticleImplCopyWithImpl<_$NewsArticleImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$NewsArticleImplToJson(
      this,
    );
  }
}

abstract class _NewsArticle implements NewsArticle {
  const factory _NewsArticle(
      {required final int id,
      required final String title,
      final String? summary,
      final String? content,
      final String? sourceUrl,
      final String? sourceName,
      required final String language,
      final String? categoryName,
      final String? categoryCode,
      final double? hotScore,
      final int? viewCount,
      final int? likeCount,
      final DateTime? publishTime,
      final DateTime? createdTime,
      final String? coverImageUrl}) = _$NewsArticleImpl;

  factory _NewsArticle.fromJson(Map<String, dynamic> json) =
      _$NewsArticleImpl.fromJson;

  @override
  int get id;
  @override
  String get title;
  @override
  String? get summary;
  @override
  String? get content;
  @override
  String? get sourceUrl;
  @override
  String? get sourceName;
  @override
  String get language;
  @override
  String? get categoryName;
  @override
  String? get categoryCode;
  @override
  double? get hotScore;
  @override
  int? get viewCount;
  @override
  int? get likeCount;
  @override
  DateTime? get publishTime;
  @override
  DateTime? get createdTime;
  @override
  String? get coverImageUrl;

  /// Create a copy of NewsArticle
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$NewsArticleImplCopyWith<_$NewsArticleImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

NewsCategory _$NewsCategoryFromJson(Map<String, dynamic> json) {
  return _NewsCategory.fromJson(json);
}

/// @nodoc
mixin _$NewsCategory {
  int get id => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String get code => throw _privateConstructorUsedError;
  int? get parentId => throw _privateConstructorUsedError;
  int? get sort => throw _privateConstructorUsedError;

  /// Serializes this NewsCategory to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of NewsCategory
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $NewsCategoryCopyWith<NewsCategory> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $NewsCategoryCopyWith<$Res> {
  factory $NewsCategoryCopyWith(
          NewsCategory value, $Res Function(NewsCategory) then) =
      _$NewsCategoryCopyWithImpl<$Res, NewsCategory>;
  @useResult
  $Res call({int id, String name, String code, int? parentId, int? sort});
}

/// @nodoc
class _$NewsCategoryCopyWithImpl<$Res, $Val extends NewsCategory>
    implements $NewsCategoryCopyWith<$Res> {
  _$NewsCategoryCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of NewsCategory
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? code = null,
    Object? parentId = freezed,
    Object? sort = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      parentId: freezed == parentId
          ? _value.parentId
          : parentId // ignore: cast_nullable_to_non_nullable
              as int?,
      sort: freezed == sort
          ? _value.sort
          : sort // ignore: cast_nullable_to_non_nullable
              as int?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$NewsCategoryImplCopyWith<$Res>
    implements $NewsCategoryCopyWith<$Res> {
  factory _$$NewsCategoryImplCopyWith(
          _$NewsCategoryImpl value, $Res Function(_$NewsCategoryImpl) then) =
      __$$NewsCategoryImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int id, String name, String code, int? parentId, int? sort});
}

/// @nodoc
class __$$NewsCategoryImplCopyWithImpl<$Res>
    extends _$NewsCategoryCopyWithImpl<$Res, _$NewsCategoryImpl>
    implements _$$NewsCategoryImplCopyWith<$Res> {
  __$$NewsCategoryImplCopyWithImpl(
      _$NewsCategoryImpl _value, $Res Function(_$NewsCategoryImpl) _then)
      : super(_value, _then);

  /// Create a copy of NewsCategory
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? name = null,
    Object? code = null,
    Object? parentId = freezed,
    Object? sort = freezed,
  }) {
    return _then(_$NewsCategoryImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as int,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      parentId: freezed == parentId
          ? _value.parentId
          : parentId // ignore: cast_nullable_to_non_nullable
              as int?,
      sort: freezed == sort
          ? _value.sort
          : sort // ignore: cast_nullable_to_non_nullable
              as int?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$NewsCategoryImpl implements _NewsCategory {
  const _$NewsCategoryImpl(
      {required this.id,
      required this.name,
      required this.code,
      this.parentId,
      this.sort});

  factory _$NewsCategoryImpl.fromJson(Map<String, dynamic> json) =>
      _$$NewsCategoryImplFromJson(json);

  @override
  final int id;
  @override
  final String name;
  @override
  final String code;
  @override
  final int? parentId;
  @override
  final int? sort;

  @override
  String toString() {
    return 'NewsCategory(id: $id, name: $name, code: $code, parentId: $parentId, sort: $sort)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$NewsCategoryImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.parentId, parentId) ||
                other.parentId == parentId) &&
            (identical(other.sort, sort) || other.sort == sort));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, name, code, parentId, sort);

  /// Create a copy of NewsCategory
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$NewsCategoryImplCopyWith<_$NewsCategoryImpl> get copyWith =>
      __$$NewsCategoryImplCopyWithImpl<_$NewsCategoryImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$NewsCategoryImplToJson(
      this,
    );
  }
}

abstract class _NewsCategory implements NewsCategory {
  const factory _NewsCategory(
      {required final int id,
      required final String name,
      required final String code,
      final int? parentId,
      final int? sort}) = _$NewsCategoryImpl;

  factory _NewsCategory.fromJson(Map<String, dynamic> json) =
      _$NewsCategoryImpl.fromJson;

  @override
  int get id;
  @override
  String get name;
  @override
  String get code;
  @override
  int? get parentId;
  @override
  int? get sort;

  /// Create a copy of NewsCategory
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$NewsCategoryImplCopyWith<_$NewsCategoryImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
