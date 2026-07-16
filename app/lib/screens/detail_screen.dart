import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/news_provider.dart';

class DetailScreen extends ConsumerWidget {
  final int? newsId;

  const DetailScreen({super.key, this.newsId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (newsId == null) {
      return Scaffold(
        appBar: AppBar(),
        body: const Center(child: Text('No article selected')),
      );
    }

    final detailAsync = ref.watch(newsDetailProvider(newsId!));

    return Scaffold(
      appBar: AppBar(
        actions: [
          IconButton(icon: const Icon(Icons.bookmark_border), onPressed: () {}),
          IconButton(icon: const Icon(Icons.share), onPressed: () {}),
        ],
      ),
      body: detailAsync.when(
        data: (data) => SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(data['title'] ?? '',
                  style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 12),
              Row(
                children: [
                  Text(data['sourceName'] ?? '',
                      style: Theme.of(context).textTheme.bodySmall),
                  const Spacer(),
                  Text(data['publishTime'] ?? '',
                      style: Theme.of(context).textTheme.bodySmall),
                ],
              ),
              const Divider(height: 24),
              if (data['summary'] != null) ...[
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(data['summary'],
                      style: Theme.of(context).textTheme.bodyLarge),
                ),
                const SizedBox(height: 16),
              ],
              Text(data['content'] ?? '',
                  style: Theme.of(context).textTheme.bodyLarge),
            ],
          ),
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }
}
