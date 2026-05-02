<script lang="ts">
	import { onMount } from 'svelte';
	import HeroCarousel from '$lib/components/HeroCarousel.svelte';
	import ChessboardRow from '$lib/components/ChessboardRow.svelte';
	import LookbookFragment from '$lib/components/LookbookFragment.svelte';
	import { fetchProducts } from '$lib/api/products';
	import type { ProductSummary } from '$lib/api/products';
	import { currency } from '$lib/stores/currency';

	let products = $state<ProductSummary[]>([]);
	let nextCursor = $state<string | null>(null);
	let hasMore = $state(true);
	let loading = $state(false);
	let error = $state<string | null>(null);

	let sentinelEl: HTMLElement | undefined = $state();

	let heroSlides = $derived(
		products.slice(0, 4).map((p) => ({ url: p.imageUrl ?? '', label: p.name }))
	);

	let lookbookUrls = $derived(products.slice(0, 4).map((p) => p.imageUrl ?? ''));

	async function loadPage(cursor?: string) {
		if (loading) return;
		loading = true;
		error = null;
		try {
			const res = await fetchProducts(cursor ?? undefined, 7);
			if (cursor) {
				products = [...products, ...res.items];
			} else {
				products = res.items;
			}
			nextCursor = res.nextCursor ?? null;
			hasMore = res.hasMore;
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to load products';
		} finally {
			loading = false;
		}
	}

	onMount(() => {
		loadPage();

		// Re-fetch when currency changes
		return currency.subscribe(() => {
			if (products.length > 0) {
				products = [];
				nextCursor = null;
				hasMore = true;
				loadPage();
			}
		});
	});

	// Manage IntersectionObserver reactively as sentinel appears/disappears
	$effect(() => {
		if (!sentinelEl) return;
		const observer = new IntersectionObserver(
			(entries) => {
				if (entries[0]?.isIntersecting && hasMore && !loading) {
					loadPage(nextCursor ?? undefined);
				}
			},
			{ rootMargin: '200px' }
		);
		observer.observe(sentinelEl);
		return () => observer.disconnect();
	});
</script>

<svelte:head>
	<title>CLOTHINGSHOP — Acquire the Void</title>
	<meta name="description" content="Clothingshop — Alternative clothing. Acquire artifacts that embody the void." />
	<meta property="og:title" content="CLOTHINGSHOP — Acquire the Void" />
	<meta property="og:description" content="Alternative clothing. Acquire artifacts that embody the void." />
	<meta property="og:type" content="website" />
	<meta property="og:url" content="https://example.com/" />
</svelte:head>

<!-- Hero Carousel -->
{#if heroSlides.length > 0}
	<HeroCarousel images={heroSlides} />
{/if}

<!-- Scroll hint -->
<div class="flex justify-center bg-surface-container-lowest pb-4">
	<div
		class="w-px h-12 bg-gradient-to-b from-primary to-transparent animate-pulse"
	></div>
</div>

<!-- Chessboard Product Grid -->
<section class="bg-surface-container-lowest">
	{#each products as product, i}
		<ChessboardRow {product} index={i} />
	{/each}
</section>

<!-- Empty state -->
{#if !loading && products.length === 0 && !error}
	<div class="flex items-center justify-center py-24 bg-surface-container-lowest">
		<p class="font-headline uppercase tracking-widest text-on-secondary-container text-sm">
			No artifacts found
		</p>
	</div>
{/if}

<!-- Error state with retry -->
{#if error}
	<div class="flex flex-col items-center justify-center py-12 bg-surface-container-lowest">
		<p class="font-body text-on-secondary-container text-sm mb-4">{error}</p>
		<button
			class="bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-8 py-3 hover:bg-on-primary-fixed-variant transition-colors"
			onclick={() => loadPage(nextCursor ?? undefined)}
		>
			RETRY
		</button>
	</div>
{/if}

<!-- Lookbook Fragment -->
{#if lookbookUrls.length > 0}
	<LookbookFragment images={lookbookUrls} />
{/if}

<!-- Loading spinner -->
{#if loading}
	<div class="flex justify-center py-12 bg-surface-container-lowest">
		<div
			class="w-6 h-6 border-2 border-on-surface/20 border-t-primary rounded-full animate-spin"
		></div>
	</div>
{/if}

<!-- Intersection observer sentinel — always present when more pages exist -->
{#if hasMore}
	<div bind:this={sentinelEl} class="h-1"></div>
{/if}
