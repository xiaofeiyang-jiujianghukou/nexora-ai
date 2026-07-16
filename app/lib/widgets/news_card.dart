import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../models/news.dart';

class NewsCard extends StatelessWidget {
  final NewsArticle? article;

  const NewsCard({super.key, this.article});

  @override
  Widget build(BuildContext context) {
    if (article == null) return const SizedBox.shrink();

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () => context.push('/news/${article!.id}'),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                article!.title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(context).textTheme.titleSmall,
              ),
              const Spacer(),
              Row(
                children: [
                  if (article!.categoryName != null)
                    Chip(
                      label: Text(article!.categoryName!,
                          style: const TextStyle(fontSize: 10)),
                      padding: EdgeInsets.zero,
                      materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                    ),
                  const Spacer(),
                  Text(article!.sourceName ?? '',
                      style: Theme.of(context).textTheme.bodySmall),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
