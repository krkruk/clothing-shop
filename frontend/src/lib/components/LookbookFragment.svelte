<script lang="ts">
	let {
		images
	}: {
		images: string[];
	} = $props();

	const spans = ['col-span-5 row-span-2', 'col-span-4', 'col-span-3', 'col-span-7'];
</script>

<section class="bg-surface-dim py-16 px-6 lg:px-12">
	<div class="max-w-7xl mx-auto grid grid-cols-12 gap-4">
		{#each images.slice(0, 4) as url, i}
			<div class="{spans[i] ?? 'col-span-6'} overflow-hidden min-h-[200px]">
				<img
					src={url}
					alt="Lookbook image {i + 1}"
					class="w-full h-full object-cover grayscale opacity-70 hover:opacity-90 transition-opacity duration-700"
				/>
			</div>
		{/each}
	</div>
</section>
