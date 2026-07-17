import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/news_provider.dart';

class CategoryBar extends ConsumerWidget {
  final String? selectedCategory;
  final ValueChanged<String?>? onCategoryChanged;

  const CategoryBar({super.key, this.selectedCategory, this.onCategoryChanged});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final categoriesAsync = ref.watch(categoriesProvider);

    return categoriesAsync.when(
      data: (categories) => SizedBox(
        height: 44,
        child: ListView.separated(
          scrollDirection: Axis.horizontal,
          padding: const EdgeInsets.symmetric(horizontal: 12),
          itemCount: categories.length + 1,
          separatorBuilder: (_, __) => const SizedBox(width: 8),
          itemBuilder: (context, index) {
            if (index == 0) {
              final isSelected = selectedCategory == null;
              return FilterChip(
                label: const Text('All'),
                selected: isSelected,
                onSelected: (_) => onCategoryChanged?.call(null),
              );
            }
            final cat = categories[index - 1];
            final isSelected = cat.code == selectedCategory;
            return FilterChip(
              label: Text(cat.name),
              selected: isSelected,
              onSelected: (_) => onCategoryChanged?.call(cat.code),
            );
          },
        ),
      ),
      loading: () => const SizedBox(height: 44),
      error: (_, __) => const SizedBox.shrink(),
    );
  }
}
