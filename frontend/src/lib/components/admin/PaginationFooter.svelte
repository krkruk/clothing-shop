<script lang="ts">
	let {
		totalItems = 0,
		currentPage = 1,
		pageSize = 20,
		hasMore = false,
		onPageChange
	}: {
		totalItems: number;
		currentPage: number;
		pageSize: number;
		hasMore: boolean;
		onPageChange: (page: number) => void;
	} = $props();

	let totalPages = $derived(Math.max(1, Math.ceil(totalItems / pageSize)));
	let startItem = $derived((currentPage - 1) * pageSize + 1);
	let endItem = $derived(Math.min(currentPage * pageSize, totalItems));

	let pages = $derived.by(() => {
		const result: number[] = [];
		const maxVisible = 5;
		let start = Math.max(1, currentPage - Math.floor(maxVisible / 2));
		let end = Math.min(totalPages, start + maxVisible - 1);
		if (end - start < maxVisible - 1) {
			start = Math.max(1, end - maxVisible + 1);
		}
		for (let i = start; i <= end; i++) {
			result.push(i);
		}
		return result;
	});
</script>

<div class="flex items-center justify-between py-4 border-t border-outline/10">
	<!-- Range display -->
	<p class="font-headline text-on-secondary-container text-[10px] uppercase tracking-[0.3em]">
		{#if totalItems > 0}
			Displaying {startItem}-{endItem} of {totalItems} units
		{:else}
			No units found
		{/if}
	</p>

	<!-- Page navigation -->
	<div class="flex items-center gap-1">
		<!-- Previous -->
		<button
			type="button"
			disabled={currentPage <= 1}
			onclick={() => onPageChange(currentPage - 1)}
			class="p-1.5 text-on-secondary-container hover:text-primary disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
		>
			<span class="material-symbols-outlined text-[18px]">chevron_left</span>
		</button>

		<!-- Page numbers -->
		{#each pages as page}
			<button
				type="button"
				onclick={() => onPageChange(page)}
				class="w-7 h-7 flex items-center justify-center font-headline text-xs transition-colors {page ===
				currentPage
					? 'bg-primary-container text-on-surface'
					: 'text-on-secondary-container hover:bg-surface-container-high'}"
			>
				{page}
			</button>
		{/each}

		<!-- Next -->
		<button
			type="button"
			disabled={!hasMore}
			onclick={() => onPageChange(currentPage + 1)}
			class="p-1.5 text-on-secondary-container hover:text-primary disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
		>
			<span class="material-symbols-outlined text-[18px]">chevron_right</span>
		</button>
	</div>
</div>
