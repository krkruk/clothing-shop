<script lang="ts">
	import type { ProductDetailResponse } from '../../api/generated';

	let { product }: { product: ProductDetailResponse } = $props();

	let fabricationItems = $derived(() => {
		const items: { label: string; value: string }[] = [];
		if (product.fabrication?.content) {
			items.push({ label: 'Content', value: product.fabrication.content });
		}
		if (product.fabrication?.care) {
			items.push({ label: 'Care', value: product.fabrication.care });
		}
		return items;
	});

	let ethicsItems = $derived(() => {
		const items: { label: string; value: string }[] = [];
		if (product.ethics?.origin) {
			items.push({ label: 'Origin', value: product.ethics.origin });
		}
		if (product.ethics?.impact) {
			items.push({ label: 'Impact', value: product.ethics.impact });
		}
		return items;
	});
</script>

<div>
	<!-- Series label -->
	<p class="font-headline text-[10px] uppercase tracking-[0.4em] text-neutral-500 mb-4">
		{product.category?.name ?? 'Series'} / {product.name}
	</p>

	<!-- Product title -->
	<h1 class="font-headline text-4xl md:text-6xl font-bold tracking-tighter uppercase text-neutral-900 leading-none mb-8">
		{product.name}
	</h1>

	<!-- Manifesto -->
	{#if product.description}
		<div class="border-l-2 border-[#5c0000] pl-6 mb-12">
			<p class="font-body text-lg font-light tracking-wide text-neutral-800 leading-relaxed">
				{product.description}
			</p>
		</div>
	{/if}

	<!-- Fabrication grid -->
	{#if fabricationItems().length > 0}
		<div class="mb-12">
			<h2 class="font-headline text-xs uppercase tracking-[0.2em] text-neutral-900 mb-4">Fabrication</h2>
			<div class="grid grid-cols-2 gap-x-8 gap-y-3">
				{#each fabricationItems() as item}
					<div>
						<p class="font-headline text-[10px] uppercase tracking-[0.2em] text-neutral-500">{item.label}</p>
						<p class="font-body text-sm font-light text-neutral-800">{item.value}</p>
					</div>
				{/each}
			</div>
		</div>
	{/if}

	<!-- Ethics -->
	{#if ethicsItems().length > 0}
		<div>
			<h2 class="font-headline text-xs uppercase tracking-[0.2em] text-neutral-900 mb-4">Ethics</h2>
			<div class="grid grid-cols-2 gap-x-8 gap-y-3">
				{#each ethicsItems() as item}
					<div>
						<p class="font-headline text-[10px] uppercase tracking-[0.2em] text-neutral-500">{item.label}</p>
						<p class="font-body text-sm font-light text-neutral-800">{item.value}</p>
					</div>
				{/each}
			</div>
		</div>
	{/if}
</div>
