<script lang="ts">
	import '../app.css';
	import Header from '$lib/components/Header.svelte';
	import Footer from '$lib/components/Footer.svelte';
	import CartDrawer from '$lib/components/CartDrawer.svelte';

	let cartOpen = $state(false);

	function openCart() {
		cartOpen = true;
	}

	function closeCart() {
		cartOpen = false;
	}

	let { children } = $props();
</script>

<Header onCartClick={openCart} />

<main class="pt-20">
	{@render children()}
</main>

<Footer />

<CartDrawer isOpen={cartOpen} onClose={closeCart} />
