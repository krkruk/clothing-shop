<script lang="ts">
	import { goto } from '$app/navigation';
	import { auth } from '$lib/stores/auth';

	let username = $state('');
	let password = $state('');
	let error = $state<string | null>(null);
	let loading = $state(false);

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = null;
		loading = true;

		if (!username.trim() || !password.trim()) {
			error = 'All fields are required';
			loading = false;
			return;
		}

		try {
			// Verify credentials by making a test API call
			const response = await fetch('/api/v1/admin/products?limit=1', {
				headers: {
					Authorization: 'Basic ' + btoa(username + ':' + password)
				}
			});

			if (response.status === 401) {
				error = 'Invalid credentials';
				loading = false;
				return;
			}

			if (!response.ok && response.status !== 200) {
				error = 'Authentication failed';
				loading = false;
				return;
			}

			auth.login(username, password);
			goto('/admin/add-product');
		} catch {
			error = 'Connection failed';
		} finally {
			loading = false;
		}
	}
</script>

<svelte:head>
	<title>CLOTHINGSHOP — Admin Authentication</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center bg-surface-container-lowest px-4">
	<div class="w-full max-w-sm">
		<!-- Header -->
		<div class="mb-12">
			<h1 class="font-headline text-on-surface text-2xl tracking-[0.2em] uppercase mb-2">
				CLOTHINGSHOP
			</h1>
			<p class="font-headline text-outline text-[10px] uppercase tracking-[0.3em]">
				Corporate Access Terminal
			</p>
			<div class="w-12 h-0.5 bg-primary-container mt-4"></div>
		</div>

		<!-- Login form -->
		<form onsubmit={handleSubmit} class="space-y-8">
			{#if error}
				<div class="bg-primary-container/20 border-l-2 border-primary-container px-4 py-3">
					<p class="font-body text-primary text-sm">{error}</p>
				</div>
			{/if}

			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="username"
				>
					Operator ID
				</label>
				<input
					id="username"
					type="text"
					bind:value={username}
					autocomplete="username"
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="Enter credentials"
				/>
			</div>

			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="password"
				>
					Access Key
				</label>
				<input
					id="password"
					type="password"
					bind:value={password}
					autocomplete="current-password"
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="Enter access key"
				/>
			</div>

			<button
				type="submit"
				disabled={loading}
				class="w-full bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-8 py-3 hover:bg-on-primary-fixed-variant transition-colors disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
			>
				{loading ? 'AUTHENTICATING...' : 'AUTHENTICATE'}
			</button>
		</form>
	</div>
</div>
