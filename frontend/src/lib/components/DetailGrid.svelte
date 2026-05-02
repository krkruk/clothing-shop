<script lang="ts">
	import type { ProductImageDto } from '../../api/generated';

	let { images }: { images: ProductImageDto[] } = $props();

	let primaryImage = $derived(images[0]?.imageUrl ?? '');
	let secondaryImage = $derived(images[1]?.imageUrl ?? '');
</script>

<section class="bg-surface-container-lowest py-16 px-6 lg:px-12">
	<div class="max-w-7xl mx-auto grid grid-cols-12 gap-4">
		<!-- Large image (7 cols) -->
		<div class="col-span-12 md:col-span-7 overflow-hidden">
			{#if primaryImage}
				<img
					src={primaryImage}
					alt="Product detail"
					class="w-full h-full object-cover grayscale opacity-60"
				/>
			{/if}
		</div>
		<!-- Secondary image (5 cols, offset) -->
		<div class="col-span-12 md:col-span-5 md:mt-24 bg-surface-container-high overflow-hidden">
			{#if secondaryImage}
				<img
					src={secondaryImage}
					alt="Product detail alternate"
					class="w-full h-full object-cover grayscale opacity-60"
				/>
			{/if}
		</div>
	</div>
</section>
