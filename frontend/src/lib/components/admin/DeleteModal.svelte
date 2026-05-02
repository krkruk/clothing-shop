<script lang="ts">
	import type { ProductResponse } from '../../../api/generated/models/ProductResponse';

	let {
		product,
		onConfirm,
		onCancel
	}: {
		product: ProductResponse;
		onConfirm: () => void;
		onCancel: () => void;
	} = $props();
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	class="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm"
	onclick={onCancel}
	role="presentation"
>
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div
		class="bg-surface-container-lowest w-full max-w-md mx-4 border-l-2 border-primary-container"
		onclick={(e) => e.stopPropagation()}
	>
		<!-- Header -->
		<div class="p-6 border-b border-outline/10">
			<h2 class="font-headline text-on-surface text-xs uppercase tracking-[0.3em]">
				Confirm Removal of Asset?
			</h2>
		</div>

		<!-- Body -->
		<div class="p-6">
			<p class="font-body text-on-secondary-container text-sm mb-2">
				This action will deactivate the following artifact:
			</p>
			<div class="bg-surface-container p-3 border-l-2 border-outline/30">
				<p class="font-headline text-on-surface text-sm">{product.name || 'Untitled'}</p>
				<p class="font-body text-on-secondary-container text-xs mt-1">
					{product.sku || 'No SKU'} &middot; {product.category?.name || 'Uncategorized'}
				</p>
			</div>
			<p class="font-body text-outline text-xs mt-3">
				This is a soft deletion. The artifact will be marked as inactive but retained in the system.
			</p>
		</div>

		<!-- Actions -->
		<div class="flex gap-3 p-6 pt-0">
			<button
				type="button"
				onclick={onCancel}
				class="flex-1 bg-surface-container-high text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-6 py-3 hover:bg-surface-container-highest transition-colors"
			>
				Abort
			</button>
			<button
				type="button"
				onclick={onConfirm}
				class="flex-1 bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-6 py-3 hover:bg-on-primary-fixed-variant transition-colors"
			>
				Confirm Removal
			</button>
		</div>
	</div>
</div>
