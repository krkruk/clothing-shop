<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { auth, isAuthenticated } from '$lib/stores/auth';
	import AdminSidebar from '$lib/components/admin/AdminSidebar.svelte';
	import AdminTopNav from '$lib/components/admin/AdminTopNav.svelte';

	let { children } = $props();

	onMount(() => {
		if (!$isAuthenticated) {
			goto('/admin/login');
		}
	});

	function handleLogout() {
		auth.logout();
		goto('/admin/login');
	}

	let sectionTitle = $derived.by(() => {
		const path = $page.url.pathname;
		if (path.includes('add-product')) return 'PRODUCT REGISTRATION';
		if (path.includes('inventory')) return 'INVENTORY MANAGEMENT';
		if (path.includes('login')) return 'AUTHENTICATION';
		return 'ADMIN CONSOLE';
	});
</script>

{#if $isAuthenticated}
	<div class="flex h-screen bg-surface-container-lowest overflow-hidden">
		<!-- Sidebar -->
		<AdminSidebar onLogout={handleLogout} />

		<!-- Main content area -->
		<div class="flex-1 flex flex-col overflow-hidden">
			<AdminTopNav {sectionTitle} />

			<main class="flex-1 overflow-y-auto p-6">
				{@render children()}
			</main>
		</div>

		<!-- Right edge gradient line -->
		<div class="w-1 h-screen bg-gradient-to-b from-primary-container via-transparent to-transparent flex-shrink-0"></div>
	</div>
{:else}
	<div class="min-h-screen bg-surface-container-lowest">
		{@render children()}
	</div>
{/if}
