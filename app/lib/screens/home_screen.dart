import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/news_provider.dart';
import '../providers/locale_provider.dart';
import '../widgets/news_card.dart';
import '../widgets/category_bar.dart';
import '../widgets/lang_switcher.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  String? _selectedCategory;

  @override
  Widget build(BuildContext context) {
    final newsAsync = ref.watch(newsListProvider(_selectedCategory));
    final recommendationsAsync = ref.watch(recommendationsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Nexora News'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () => _showSearch(context),
          ),
          const LangSwitcher(),
          IconButton(
            icon: const Icon(Icons.person_outline),
            onPressed: () => Navigator.pushNamed(context, '/profile'),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async => ref.invalidate(newsListProvider(_selectedCategory)),
        child: CustomScrollView(
          slivers: [
            // Recommendations row
            SliverToBoxAdapter(
              child: recommendationsAsync.when(
                data: (articles) => articles.isEmpty
                    ? const SizedBox.shrink()
                    : _RecommendationRow(articles: articles),
                loading: () => const SizedBox(height: 100),
                error: (_, __) => const SizedBox.shrink(),
              ),
            ),
            // Category bar
            const SliverToBoxAdapter(child: CategoryBar()),
            // News list
            newsAsync.when(
              data: (articles) => SliverList(
                delegate: SliverChildBuilderDelegate(
                  (context, index) => NewsCard(article: articles[index]),
                  childCount: articles.length,
                ),
              ),
              loading: () => const SliverFillRemaining(
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (e, _) => SliverFillRemaining(
                child: Center(child: Text('Error: $e')),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showSearch(BuildContext context) {
    showSearch(
      context: context,
      delegate: _NewsSearchDelegate(),
    );
  }
}

class _RecommendationRow extends StatelessWidget {
  final List articles;
  const _RecommendationRow({required this.articles});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.fromLTRB(16, 12, 16, 8),
          child: Text('🔥 For You',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        ),
        SizedBox(
          height: 180,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 12),
            itemCount: articles.length,
            itemBuilder: (context, index) =>
                SizedBox(width: 220, child: NewsCard(article: articles[index])),
          ),
        ),
      ],
    );
  }
}

class _NewsSearchDelegate extends SearchDelegate<String> {
  @override
  List<Widget> buildActions(BuildContext context) => [
        IconButton(
          icon: const Icon(Icons.clear),
          onPressed: () => query = '',
        ),
      ];

  @override
  Widget buildLeading(BuildContext context) => IconButton(
        icon: const Icon(Icons.arrow_back),
        onPressed: () => close(context, ''),
      );

  @override
  Widget buildResults(BuildContext context) => _buildSearchResults(context);

  @override
  Widget buildSuggestions(BuildContext context) =>
      _buildSearchResults(context);

  Widget _buildSearchResults(BuildContext context) {
    // TODO: wire to searchProvider
    return const Center(child: Text('Search results'));
  }
}
