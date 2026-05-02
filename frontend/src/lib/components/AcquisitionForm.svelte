<script lang="ts">
	import { cartItems } from '$lib/stores/cart';
	import type { ProductDetailResponse } from '../../api/generated';
	import { Silhouette } from '$lib/mock/types';

	let { product }: { product: ProductDetailResponse } = $props();

	let silhouette = $state(Silhouette.BOXY);
	let waistCm = $state('');
	let hipsCm = $state('');
	let heightCm = $state('');

	const silhouetteOptions = [
		{ value: Silhouette.BOXY, label: 'Boxy/Architectural' },
		{ value: Silhouette.CURVY, label: 'Tailored/Clinical' },
		{ value: Silhouette.OTHER, label: 'Oversized/Monastic' }
	];

	function handleSubmit(e: Event) {
		e.preventDefault();
		cartItems.addItemFromApi(product, {
			silhouette,
			waistCm: Number(waistCm) || 0,
			hipsCm: Number(hipsCm) || 0,
			heightCm: Number(heightCm) || 0
		});
	}
</script>

<form onsubmit={handleSubmit} class="space-y-6">
	<!-- Silhouette dropdown -->
	<div>
		<label for="silhouette" class="font-headline text-[10px] uppercase tracking-[0.3em] font-bold text-neutral-900 block mb-2">
			Silhouette
		</label>
		<div class="relative">
			<select
				id="silhouette"
				bind:value={silhouette}
				class="w-full bg-[#f5f5f5] text-neutral-900 font-body text-sm py-3 px-0 border-b border-neutral-400 focus:border-[#5c0000] focus:ring-0 focus:outline-none transition-colors appearance-none"
			>
				{#each silhouetteOptions as opt}
					<option value={opt.value}>{opt.label}</option>
				{/each}
			</select>
			<span class="material-symbols-outlined absolute right-0 top-1/2 -translate-y-1/2 text-neutral-400 text-sm pointer-events-none">
				expand_more
			</span>
		</div>
	</div>

	<!-- Waist + Hips row -->
	<div class="grid grid-cols-2 gap-4">
		<div>
			<label for="waist" class="font-headline text-[10px] uppercase tracking-[0.3em] font-bold text-neutral-900 block mb-2">
				Waist (cm)
			</label>
			<input
				id="waist"
				type="text"
				inputmode="numeric"
				bind:value={waistCm}
				placeholder="--"
				class="w-full bg-[#f5f5f5] text-neutral-900 font-body text-sm py-3 px-0 border-b border-neutral-400 focus:border-[#5c0000] focus:ring-0 focus:outline-none transition-colors"
			/>
		</div>
		<div>
			<label for="hips" class="font-headline text-[10px] uppercase tracking-[0.3em] font-bold text-neutral-900 block mb-2">
				Hips (cm)
			</label>
			<input
				id="hips"
				type="text"
				inputmode="numeric"
				bind:value={hipsCm}
				placeholder="--"
				class="w-full bg-[#f5f5f5] text-neutral-900 font-body text-sm py-3 px-0 border-b border-neutral-400 focus:border-[#5c0000] focus:ring-0 focus:outline-none transition-colors"
			/>
		</div>
	</div>

	<!-- Height -->
	<div>
		<label for="height" class="font-headline text-[10px] uppercase tracking-[0.3em] font-bold text-neutral-900 block mb-2">
			Height (cm)
		</label>
		<input
			id="height"
			type="text"
			inputmode="numeric"
			bind:value={heightCm}
			placeholder="--"
			class="w-full bg-[#f5f5f5] text-neutral-900 font-body text-sm py-3 px-0 border-b border-neutral-400 focus:border-[#5c0000] focus:ring-0 focus:outline-none transition-colors"
		/>
	</div>

	<!-- Price -->
	<div class="pt-6 border-t border-neutral-300">
		<p class="font-label text-xs tracking-widest uppercase text-neutral-500 mb-1">
			Transaction Value
		</p>
		<p class="font-headline text-2xl font-bold text-neutral-900">
			{product.currency ?? 'PLN'} {Number(product.price ?? 0).toFixed(2)}
		</p>
	</div>

	<!-- ACQUIRE ARTIFACT button -->
	<button
		type="submit"
		class="w-full bg-[#5c0000] text-white font-headline uppercase tracking-[0.2em] text-sm py-6 hover:bg-[#920703] transition-colors mt-6"
	>
		ACQUIRE ARTIFACT
	</button>

	<!-- Shipping note -->
	<p class="font-label text-[10px] uppercase tracking-widest text-neutral-400 text-center mt-4">
		Complimentary secure transit for all global acquisitions.
	</p>
</form>
