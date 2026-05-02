<script lang="ts">
	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import { fetchProductDetail } from '$lib/api/products';
	import type { ProductDetailResponse } from '../../../api/generated';
	import HeroCarousel from '$lib/components/HeroCarousel.svelte';
	import ProductNarrative from '$lib/components/ProductNarrative.svelte';
	import AcquisitionForm from '$lib/components/AcquisitionForm.svelte';
	import DetailGrid from '$lib/components/DetailGrid.svelte';
	import { currency } from '$lib/stores/currency';

	let productId = $derived($page.params.id ?? '');

	let product = $state<ProductDetailResponse | null>(null);
	let loading = $state(true);
	let notFound = $state(false);

	let carouselImages = $derived(
		product
			? (product.images ?? []).map((img, i) => ({
					url: img.imageUrl ?? '',
					label: i === 0 ? product?.name : undefined
				}))
			: []
	);

	let pageTitle = $derived(product ? `${product.name} — CLOTHINGSHOP` : 'Artifact not found — CLOTHINGSHOP');

	let jsonLd = $derived(
		product
			? JSON.stringify({
					"@context": "https://schema.org",
					"@type": "Product",
					"name": product.name,
					"description": product.shortDescription ?? product.description,
					"image": product.images?.map(i => i.imageUrl).filter(Boolean) ?? [],
					"offers": {
						"@type": "Offer",
						"price": product.price,
						"priceCurrency": product.currency ?? 'PLN'
					}
				})
			: ''
	);

	onMount(async () => {
		try {
			product = await fetchProductDetail(productId);
		} catch (e) {
			notFound = true;
		} finally {
			loading = false;
		}

		// Re-fetch product when currency changes
		return currency.subscribe(() => {
			if (product) {
				fetchProductDetail(productId).then((p) => (product = p)).catch(() => {});
			}
		});
	});
</script>

<svelte:head>
	<title>{pageTitle}</title>
	{#if product?.shortDescription}
		<meta name="description" content={product.shortDescription} />
	{/if}
	{#if product}
		<meta property="og:title" content={pageTitle} />
		{#if product.shortDescription}
			<meta property="og:description" content={product.shortDescription} />
		{/if}
		<meta property="og:type" content="product" />
		<meta property="og:url" content="https://example.com/products/{productId}" />

		<script type="application/ld+json">{JSON.stringify({
			"@context": "https://schema.org",
			"@type": "Product",
			"name": product.name,
			"description": product.shortDescription ?? product.description,
			"image": product.images?.map(i => i.imageUrl).filter(Boolean) ?? [],
			"offers": {
				"@type": "Offer",
				"price": product.price,
				"priceCurrency": product.currency ?? 'PLN'
			}
		})}</script>
	{/if}
</svelte:head>

{#if loading}
	<div class="min-h-screen flex items-center justify-center bg-surface-container-lowest">
		<div class="w-8 h-8 border-2 border-on-surface/20 border-t-primary rounded-full animate-spin"></div>
	</div>
{:else if notFound || !product}
	<div class="min-h-screen flex items-center justify-center bg-surface-container-lowest">
		<p class="font-headline text-2xl uppercase text-on-surface">Artifact not found</p>
	</div>
{:else}
	<!-- Hero Carousel -->
	<div class="relative">
		<HeroCarousel
			images={carouselImages}
			height="66.66vh"
			imageClass="grayscale brightness-50 contrast-125"
		/>
		<!-- Bottom gradient overlay -->
		<div class="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-surface-container-lowest/80 to-transparent pointer-events-none"></div>
	</div>

	<!-- Content area (white bg) -->
	<div class="bg-[#f5f5f5] py-16 px-6 lg:px-12">
		<div class="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-12">
			<!-- Left column: Narrative (2/3) -->
			<div class="md:col-span-2">
				<ProductNarrative {product} />
			</div>

			<!-- Right column: Acquisition Form (1/3, sticky) -->
			<div class="md:col-span-1">
				<div class="md:sticky md:top-32">
					<AcquisitionForm {product} />
				</div>
			</div>
		</div>
	</div>

	<!-- Detail Grid (returns to dark) -->
	<DetailGrid images={product.images ?? []} />
{/if}
