<script lang="ts">
	import { page } from '$app/stores';

	let { onLogout }: { onLogout: () => void } = $props();

	let currentPath = $derived($page.url.pathname);

	let isAddProduct = $derived(currentPath.includes('add-product'));
	let isInventory = $derived(currentPath.includes('inventory'));
</script>

<aside class="w-64 h-screen bg-surface-dim flex flex-col flex-shrink-0 border-r border-outline/10">
	<!-- Header -->
	<div class="p-6 border-b border-outline/10">
		<h1 class="font-headline text-on-surface text-lg tracking-[0.15em] uppercase">Inventory</h1>
		<p class="font-body text-outline text-[10px] uppercase tracking-[0.3em] mt-1">Corpo Goth Admin</p>
		<div class="w-8 h-0.5 bg-primary-container mt-3"></div>
	</div>

	<!-- Navigation -->
	<nav class="flex-1 py-4">
		<ul class="space-y-1 px-3">
			<li>
				<a
					href="/admin/add-product"
					class="flex items-center gap-3 px-3 py-2.5 font-headline text-xs uppercase tracking-[0.15em] transition-colors {isAddProduct
						? 'bg-primary-container text-on-surface'
						: 'text-on-secondary-container hover:bg-surface-container-high hover:text-on-surface'}"
				>
					<span class="material-symbols-outlined text-[18px]">add_circle</span>
					Add new product
				</a>
			</li>
			<li>
				<a
					href="/admin/inventory"
					class="flex items-center gap-3 px-3 py-2.5 font-headline text-xs uppercase tracking-[0.15em] transition-colors {isInventory
						? 'bg-primary-container text-on-surface'
						: 'text-on-secondary-container hover:bg-surface-container-high hover:text-on-surface'}"
				>
					<span class="material-symbols-outlined text-[18px]">grid_view</span>
					Modify/delete product
				</a>
			</li>
		</ul>
	</nav>

	<!-- Operator footer -->
	<div class="p-4 border-t border-outline/10">
		<div class="flex items-center gap-3">
			<span
				class="font-headline text-[8px] uppercase tracking-[0.3em] bg-primary-container text-on-surface px-2 py-1"
			>
				SYS
			</span>
			<div>
				<p class="font-body text-on-secondary-container text-xs">Operator</p>
				<button
					onclick={onLogout}
					class="font-headline text-outline text-[10px] uppercase tracking-[0.2em] hover:text-primary transition-colors"
				>
					Disconnect
				</button>
			</div>
		</div>
	</div>
</aside>
