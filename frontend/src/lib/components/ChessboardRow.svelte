<script lang="ts">
	import type { ProductSummary } from '../../api/generated';

	let {
		product,
		index
	}: {
		product: ProductSummary;
		index: number;
	} = $props();

	let isEven = $derived(index % 2 === 1);
</script>

<div class="grid grid-cols-1 md:grid-cols-3">
	<!-- Image column -->
	<div
		class="relative overflow-hidden bg-surface-container-high max-h-[33.33vh]
			{isEven ? 'md:order-2' : 'md:order-1'}"
	>
		<a href="/products/{product.id}">
			<img
				src={product.imageUrl ?? ''}
				alt={product.name ?? ''}
				class="w-full h-full object-contain opacity-80 mix-blend-luminosity hover:scale-105 transition-transform duration-700 min-h-[300px] md:min-h-full"
			/>
		</a>
	</div>

	<!-- Content column -->
	<div
		class="flex flex-col justify-center px-8 py-12 md:px-24
			{isEven ? 'md:order-1 md:text-right' : 'md:order-2'}"
		style="grid-column: span 2"
	>
		<h3 class="font-headline font-bold tracking-tighter uppercase text-on-surface text-2xl md:text-4xl mb-4">
			{product.name}
		</h3>
		<p class="font-body font-light tracking-wide text-on-secondary-container text-sm md:text-base mb-6 leading-relaxed
			{isEven ? 'md:ml-auto' : ''}"
			style="max-width: 480px"
		>
			{product.shortDescription}
		</p>
		<div class="flex items-center gap-6 {isEven ? 'md:justify-end' : ''}">
			<span class="font-headline font-bold text-on-surface text-lg">
				{product.currency ?? 'PLN'} {Number(product.price ?? 0).toFixed(2)}
			</span>
			<a
				href="/products/{product.id}"
				class="bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-8 py-3 hover:bg-on-primary-fixed-variant transition-colors inline-block"
			>
				ACQUIRE
			</a>
		</div>
	</div>
</div>
