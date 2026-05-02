<script lang="ts">
	import { cartItems, cartTotal, cartCurrency } from '$lib/stores/cart';
	import CartItemComponent from './CartItem.svelte';
	import CartEmptyState from './CartEmptyState.svelte';

	let {
		isOpen = false,
		onClose
	}: {
		isOpen?: boolean;
		onClose: () => void;
	} = $props();

	let items = $derived($cartItems);
	let total = $derived($cartTotal);
	let currency = $derived($cartCurrency);
	let isEmpty = $derived(items.length === 0);
</script>

{#if isOpen}
	<!-- Backdrop -->
	<!-- svelte-ignore a11y_click_events_have_key_events -->
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div
		class="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm transition-opacity duration-500"
		onclick={onClose}
	></div>

	<!-- Drawer -->
	<aside
		class="fixed top-0 right-0 z-50 h-full w-full md:w-[450px] bg-surface-container-highest/90 backdrop-blur-[12px] transform transition-transform duration-500 flex flex-col"
	>
		<!-- Header -->
		<div class="flex items-center justify-between px-6 py-6">
			<h2 class="font-headline font-bold text-2xl tracking-tighter uppercase text-on-surface">
				CURRENT INVENTORY
			</h2>
			<button
				class="text-on-surface hover:text-primary transition-colors"
				onclick={onClose}
				aria-label="Close cart"
			>
				<span class="material-symbols-outlined">close</span>
			</button>
		</div>

		<!-- Items -->
		<div class="flex-1 overflow-y-auto px-6">
			{#if isEmpty}
				<CartEmptyState />
			{:else}
				{#each items as item (item.id)}
					<div class="py-6">
						<CartItemComponent {item} />
					</div>
				{/each}
			{/if}
		</div>

		<!-- Footer -->
		{#if !isEmpty}
			<div class="px-6 py-6 border-t border-outline/20">
				<div class="flex justify-between items-baseline mb-6">
					<span class="font-label text-xs tracking-widest uppercase text-on-secondary-container">
						TOTAL VALUE
					</span>
					<span class="font-headline font-bold text-xl uppercase text-on-surface">
						{currency} {total.toFixed(2)}
					</span>
				</div>
				<button
					class="w-full bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs py-5 hover:bg-on-primary-fixed-variant transition-colors"
				>
					PROCEED TO TRANSACTION
				</button>
			</div>
		{/if}
	</aside>
{/if}
