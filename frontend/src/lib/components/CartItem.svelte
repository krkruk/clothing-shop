<script lang="ts">
	import { cartItems } from '$lib/stores/cart';
	import type { CartItem as CartItemType } from '$lib/stores/cart';

	let { item }: { item: CartItemType } = $props();

	function increment() {
		const store = cartItems;
		store.updateQuantity(item.id, item.quantity + 1);
	}

	function decrement() {
		if (item.quantity <= 1) {
			cartItems.removeItem(item.id);
		} else {
			cartItems.updateQuantity(item.id, item.quantity - 1);
		}
	}
</script>

<div class="flex gap-4">
	<!-- Thumbnail -->
	<div class="w-24 h-32 flex-shrink-0 bg-surface-container overflow-hidden">
		<img
			src={item.thumbnail}
			alt={item.productName}
			class="w-full h-full object-cover grayscale brightness-50"
		/>
	</div>

	<!-- Details -->
	<div class="flex-1 min-w-0">
		<p class="font-headline text-[10px] uppercase tracking-widest text-primary-container mb-1">
			{item.productId.replace('prod-', '').toUpperCase()}
		</p>
		<p class="font-headline text-sm font-bold uppercase text-on-surface mb-1 truncate">
			{item.productName}
		</p>
		<p class="font-label text-[10px] uppercase tracking-widest text-on-surface/60 mb-3">
			{item.personalization.silhouette} / {item.personalization.heightCm}cm
		</p>

		<!-- Quantity controls -->
		<div class="flex items-center gap-3 mb-2">
			<button
				class="w-7 h-7 flex items-center justify-center border border-on-surface/20 text-on-surface hover:text-primary transition-colors"
				onclick={decrement}
				aria-label="Decrease quantity"
			>
				<span class="text-xs">&minus;</span>
			</button>
			<span class="font-headline text-sm text-on-surface min-w-[20px] text-center">
				{item.quantity}
			</span>
			<button
				class="w-7 h-7 flex items-center justify-center border border-on-surface/20 text-on-surface hover:text-primary transition-colors"
				onclick={increment}
				aria-label="Increase quantity"
			>
				<span class="text-xs">+</span>
			</button>
		</div>

		<p class="font-headline font-bold text-sm text-on-surface">
			{item.currency} {(item.price * item.quantity).toFixed(2)}
		</p>
	</div>
</div>
