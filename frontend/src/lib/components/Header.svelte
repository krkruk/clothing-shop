<script lang="ts">
	import { cartCount } from '$lib/stores/cart';
	import { currency, type CurrencyCode } from '$lib/stores/currency';
	import { auth, isAuthenticated } from '$lib/stores/auth';
	import { categories } from '$lib/mock';
	import { onMount } from 'svelte';

	let { onCartClick = () => {} }: { onCartClick?: () => void } = $props();

	let mobileMenuOpen = $state(false);
	let userDropdownOpen = $state(false);
	let currencyDropdownOpen = $state(false);

	let userDropdownEl: HTMLElement | undefined = $state();
	let currencyDropdownEl: HTMLElement | undefined = $state();

	function toggleMobileMenu() {
		mobileMenuOpen = !mobileMenuOpen;
	}

	function toggleUserDropdown() {
		currencyDropdownOpen = false;
		userDropdownOpen = !userDropdownOpen;
	}

	function toggleCurrencyDropdown() {
		userDropdownOpen = false;
		currencyDropdownOpen = !currencyDropdownOpen;
	}

	function selectCurrency(code: CurrencyCode) {
		currency.set(code);
		currencyDropdownOpen = false;
	}

	function handleLogout() {
		auth.logout();
		userDropdownOpen = false;
	}

	function handleClickOutside(e: MouseEvent) {
		const target = e.target as HTMLElement;
		if (userDropdownEl && !userDropdownEl.contains(target)) {
			userDropdownOpen = false;
		}
		if (currencyDropdownEl && !currencyDropdownEl.contains(target)) {
			currencyDropdownOpen = false;
		}
	}

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Escape') {
			userDropdownOpen = false;
			currencyDropdownOpen = false;
		}
	}

	onMount(() => {
		document.addEventListener('click', handleClickOutside);
		document.addEventListener('keydown', handleKeydown);
		return () => {
			document.removeEventListener('click', handleClickOutside);
			document.removeEventListener('keydown', handleKeydown);
		};
	});
</script>

<header
	class="fixed top-0 left-0 right-0 z-50 h-20 bg-surface-container-lowest flex items-center justify-between px-6 lg:px-12"
>
	<!-- Brand -->
	<a href="/" class="font-headline font-bold tracking-[0.2em] uppercase text-on-surface text-lg">
		CLOTHINGSHOP
	</a>

	<!-- Desktop Nav -->
	<nav class="hidden md:flex items-center gap-8">
		{#each categories as category}
			<div class="relative group">
				<a
					href="/"
					class="font-headline tracking-tighter uppercase text-sm text-on-surface hover:text-primary transition-colors duration-300 py-2"
				>
					{category.name}
				</a>
				<!-- Dropdown -->
				<div
					class="absolute left-0 top-full opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 bg-surface-container-high border-t-2 border-primary-container min-w-[200px] pt-2 pb-4 px-4"
				>
					{#each category.subCategories as sub}
						<a
							href="/"
							class="block font-label text-xs tracking-widest uppercase text-on-surface hover:text-primary transition-colors py-1"
						>
							{sub}
						</a>
					{/each}
				</div>
			</div>
		{/each}
	</nav>

	<!-- Right icons -->
	<div class="flex items-center gap-4">
		<!-- Currency Picker -->
		<div class="relative" bind:this={currencyDropdownEl}>
			<button
				class="text-on-surface hover:text-primary transition-colors font-headline text-[10px] uppercase tracking-widest"
				onclick={toggleCurrencyDropdown}
				aria-label="Select currency"
			>
				{$currency}
			</button>
			{#if currencyDropdownOpen}
				<div
					class="absolute right-0 top-full mt-2 bg-surface-container-high border-t-2 border-primary-container min-w-[80px] py-2 animate-[fadeIn_300ms_ease]"
				>
					<button
						class="w-full text-left px-4 py-2 font-headline text-[10px] uppercase tracking-widest text-on-surface hover:text-primary transition-colors {($currency === 'PLN') ? 'text-primary' : ''}"
						onclick={() => selectCurrency('PLN')}
					>
						PLN
					</button>
					<button
						class="w-full text-left px-4 py-2 font-headline text-[10px] uppercase tracking-widest text-on-surface hover:text-primary transition-colors {($currency === 'EUR') ? 'text-primary' : ''}"
						onclick={() => selectCurrency('EUR')}
					>
						EUR
					</button>
				</div>
			{/if}
		</div>

		<!-- User Icon -->
		<div class="relative" bind:this={userDropdownEl}>
			<button
				class="text-on-surface hover:text-primary transition-colors"
				aria-label="Account"
				onclick={toggleUserDropdown}
			>
				<span class="material-symbols-outlined text-[24px]">person</span>
			</button>
			{#if userDropdownOpen}
				<div
					class="absolute right-0 top-full mt-2 bg-surface-container-high border-t-2 border-primary-container min-w-[180px] py-2 animate-[fadeIn_300ms_ease]"
				>
					{#if $isAuthenticated}
						<a
							href="/admin/add-product"
							class="block px-4 py-2 font-headline text-[10px] uppercase tracking-widest text-on-surface hover:text-primary transition-colors"
							onclick={() => (userDropdownOpen = false)}
						>
							Admin Panel
						</a>
						<button
							class="w-full text-left px-4 py-2 font-headline text-[10px] uppercase tracking-widest text-on-surface hover:text-primary transition-colors"
							onclick={handleLogout}
						>
							Log Off
						</button>
					{:else}
						<a
							href="/admin/login"
							class="block px-4 py-2 font-headline text-[10px] uppercase tracking-widest text-on-surface hover:text-primary transition-colors"
							onclick={() => (userDropdownOpen = false)}
						>
							Login
						</a>
					{/if}
				</div>
			{/if}
		</div>

		<!-- Cart -->
		<button
			class="relative text-on-surface hover:text-primary transition-colors"
			aria-label="Cart"
			onclick={onCartClick}
		>
			<span class="material-symbols-outlined text-[24px]">shopping_bag</span>
			<span
				class="absolute -top-1 -right-2 bg-primary-container text-on-surface text-[8px] font-headline font-bold min-w-[16px] h-4 flex items-center justify-center"
			>
				{$cartCount}
			</span>
		</button>

		<!-- Mobile hamburger -->
		<button
			class="md:hidden text-on-surface hover:text-primary transition-colors"
			aria-label="Menu"
			onclick={toggleMobileMenu}
		>
			<span class="material-symbols-outlined text-[24px]">
				{mobileMenuOpen ? 'close' : 'menu'}
			</span>
		</button>
	</div>
</header>

<!-- Mobile menu -->
{#if mobileMenuOpen}
	<!-- svelte-ignore a11y_click_events_have_key_events -->
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div class="fixed inset-0 top-20 z-40 bg-black/60 backdrop-blur-sm md:hidden" onclick={toggleMobileMenu} role="presentation">
		<nav class="bg-surface-container-lowest p-6 flex flex-col gap-4">
			{#each categories as category}
				<a
					href="/"
					class="font-headline tracking-tighter uppercase text-sm text-on-surface hover:text-primary transition-colors"
					onclick={toggleMobileMenu}
				>
					{category.name}
				</a>
				{#each category.subCategories as sub}
					<a
						href="/"
						class="font-label text-xs tracking-widest uppercase text-on-secondary-container pl-4 hover:text-primary transition-colors"
						onclick={toggleMobileMenu}
					>
						{sub}
					</a>
				{/each}
			{/each}
		</nav>
	</div>
{/if}

<style>
	@keyframes fadeIn {
		from { opacity: 0; }
		to { opacity: 1; }
	}
</style>
