<script lang="ts">
	import { onMount } from 'svelte';
	import { auth } from '$lib/stores/auth';
	import { get } from 'svelte/store';
	import { listAdminProducts, deleteProduct } from '$lib/api/admin';
	import type { ProductResponse } from '$lib/api/admin';
	import InventoryTable from '$lib/components/admin/InventoryTable.svelte';
	import PaginationFooter from '$lib/components/admin/PaginationFooter.svelte';
	import AdminFooter from '$lib/components/admin/AdminFooter.svelte';
	import DeleteModal from '$lib/components/admin/DeleteModal.svelte';
	import UpdateModal from '$lib/components/admin/UpdateModal.svelte';

	let products = $state<ProductResponse[]>([]);
	let loading = $state(false);
	let error = $state<string | null>(null);

	// Pagination
	let currentPage = $state(1);
	let pageSize = 20;
	let hasMore = $state(false);
	let nextCursor = $state<string | undefined>(undefined);
	let totalItems = $state(0);

	// Modals
	let productToDelete = $state<ProductResponse | null>(null);
	let productToEdit = $state<ProductResponse | null>(null);

	onMount(() => {
		loadProducts();
	});

	async function loadProducts(page: number = 1) {
		loading = true;
		error = null;

		const credentials = getCredentials();
		if (!credentials) {
			error = 'Not authenticated';
			loading = false;
			return;
		}

		try {
			// For pages beyond 1, use the nextCursor; for page 1, no cursor
			const cursor = page > 1 ? nextCursor : undefined;
			const response = await listAdminProducts(
				credentials.username,
				credentials.password,
				cursor,
				pageSize
			);

			if (page === 1) {
				products = response.items;
			} else {
				products = [...products, ...response.items];
			}
			nextCursor = response.nextCursor;
			hasMore = response.hasMore;
			totalItems = hasMore ? page * pageSize + response.items.length : (page - 1) * pageSize + response.items.length;
			currentPage = page;
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to load products';
		} finally {
			loading = false;
		}
	}

	function handlePageChange(page: number) {
		// Reset and load from scratch for simplicity (cursor-based is forward-only,
		// so we reload from beginning for page navigation)
		if (page < currentPage) {
			products = [];
			nextCursor = undefined;
			loadFromStart(page);
		} else {
			loadProducts(page);
		}
	}

	async function loadFromStart(targetPage: number) {
		loading = true;
		error = null;

		const credentials = getCredentials();
		if (!credentials) {
			error = 'Not authenticated';
			loading = false;
			return;
		}

		try {
			let allProducts: ProductResponse[] = [];
			let cursor: string | undefined = undefined;
			let more = true;

			for (let p = 1; p <= targetPage && more; p++) {
				const response = await listAdminProducts(
					credentials.username,
					credentials.password,
					cursor,
					pageSize
				);
				allProducts = [...allProducts, ...response.items];
				cursor = response.nextCursor;
				more = response.hasMore;

				if (p === targetPage) {
					products = allProducts;
					nextCursor = response.nextCursor;
					hasMore = response.hasMore;
					totalItems = hasMore ? targetPage * pageSize + response.items.length : allProducts.length;
					currentPage = targetPage;
				}
			}
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to load products';
		} finally {
			loading = false;
		}
	}

	function handleEdit(product: ProductResponse) {
		productToEdit = product;
	}

	function handleDelete(product: ProductResponse) {
		productToDelete = product;
	}

	async function confirmDelete() {
		if (!productToDelete?.id) return;

		const credentials = getCredentials();
		if (!credentials) return;

		try {
			await deleteProduct(credentials.username, credentials.password, productToDelete.id);
			productToDelete = null;
			// Reload current page
			products = [];
			nextCursor = undefined;
			loadFromStart(currentPage);
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to delete product';
		}
	}

	function handleUpdateComplete() {
		productToEdit = null;
		products = [];
		nextCursor = undefined;
		loadFromStart(currentPage);
	}

	function getCredentials() {
		return get(auth);
	}
</script>

<svelte:head>
	<title>CLOTHINGSHOP — Inventory Management</title>
</svelte:head>

<!-- Page header -->
<div class="mb-6">
	<div class="flex items-center gap-3 mb-1">
		<div class="w-1 h-6 bg-primary-container"></div>
		<h1 class="font-headline text-on-surface text-xs uppercase tracking-[0.3em]">
			Inventory Management
		</h1>
	</div>
	<p class="font-body text-on-secondary-container text-xs pl-4">
		Modify or remove registered artifacts
	</p>
</div>

<!-- Stats -->
<div class="grid grid-cols-3 gap-4 mb-6">
	<div class="bg-surface-container p-4 border-l-2 border-primary-container/30">
		<p class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-1">Total Artifacts</p>
		<p class="font-headline text-on-surface text-2xl">{products.length}</p>
	</div>
	<div class="bg-surface-container p-4 border-l-2 border-outline/20">
		<p class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-1">Active</p>
		<p class="font-headline text-on-surface text-2xl">{products.filter((p) => p.isActive).length}</p>
	</div>
	<div class="bg-surface-container p-4 border-l-2 border-outline/20">
		<p class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-1">Inactive</p>
		<p class="font-headline text-on-surface text-2xl">{products.filter((p) => !p.isActive).length}</p>
	</div>
</div>

{#if error}
	<div class="bg-primary-container/20 border-l-2 border-primary-container px-4 py-3 mb-6">
		<p class="font-body text-primary text-sm">{error}</p>
	</div>
{/if}

<!-- Loading -->
{#if loading}
	<div class="flex justify-center py-12">
		<div class="w-6 h-6 border-2 border-on-surface/20 border-t-primary animate-spin"></div>
	</div>
{/if}

<!-- Table -->
{#if !loading}
	<div class="bg-surface-container-low">
		<InventoryTable {products} onEdit={handleEdit} onDelete={handleDelete} />

		{#if products.length === 0 && !error}
			<div class="flex flex-col items-center justify-center py-16">
				<span class="material-symbols-outlined text-outline/30 text-[48px] mb-4">
					inventory_2
				</span>
				<p class="font-headline text-on-secondary-container text-xs uppercase tracking-[0.3em]">
					No artifacts registered
				</p>
			</div>
		{/if}

		{#if products.length > 0}
			<div class="px-4">
				<PaginationFooter
					{totalItems}
					currentPage={currentPage}
					pageSize={pageSize}
					{hasMore}
					onPageChange={handlePageChange}
				/>
			</div>
		{/if}
	</div>
{/if}

<!-- Admin footer -->
<AdminFooter />

<!-- Modals -->
{#if productToDelete}
	<DeleteModal product={productToDelete} onConfirm={confirmDelete} onCancel={() => (productToDelete = null)} />
{/if}

{#if productToEdit}
	<UpdateModal product={productToEdit} onClose={() => (productToEdit = null)} onUpdated={handleUpdateComplete} />
{/if}
