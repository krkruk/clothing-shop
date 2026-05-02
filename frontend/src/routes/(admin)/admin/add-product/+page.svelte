<script lang="ts">
	import { onMount } from 'svelte';
	import { auth } from '$lib/stores/auth';
	import { get } from 'svelte/store';
	import { createProduct, uploadProductImage } from '$lib/api/admin';
	import { fetchCategories } from '$lib/api/categories';
	import type { CategoryDto } from '$lib/api/categories';
	import TiptapEditor from '$lib/components/admin/TiptapEditor.svelte';

	let categories = $state<CategoryDto[]>([]);

	// Form fields
	let name = $state('');
	let shortDescription = $state('');
	let pricePln = $state('');
	let priceEur = $state('');
	let categoryId = $state('');
	let sku = $state('');
	let isActive = $state(true);
	let description = $state('');
	let fabricationContent = $state('');
	let fabricationCare = $state('');
	let ethicsOrigin = $state('');
	let ethicsImpact = $state('');

	// Images
	let selectedFiles: File[] = $state([]);
	let imagePreviews: string[] = $state([]);

	// State
	let loading = $state(false);
	let success = $state<string | null>(null);
	let error = $state<string | null>(null);

	onMount(async () => {
		try {
			categories = await fetchCategories();
		} catch {
			error = 'Failed to load categories';
		}
	});

	function handleImageSelect(e: Event) {
		const input = e.target as HTMLInputElement;
		if (!input.files) return;

		const newFiles = Array.from(input.files);
		selectedFiles = [...selectedFiles, ...newFiles];

		for (const file of newFiles) {
			const reader = new FileReader();
			reader.onload = (ev) => {
				if (ev.target?.result) {
					imagePreviews = [...imagePreviews, ev.target.result as string];
				}
			};
			reader.readAsDataURL(file);
		}

		input.value = '';
	}

	function removeImage(index: number) {
		selectedFiles = selectedFiles.filter((_, i) => i !== index);
		imagePreviews = imagePreviews.filter((_, i) => i !== index);
	}

	function handleDrop(e: DragEvent) {
		e.preventDefault();
		if (!e.dataTransfer?.files) return;

		const newFiles = Array.from(e.dataTransfer.files).filter((f) =>
			f.type.startsWith('image/')
		);
		selectedFiles = [...selectedFiles, ...newFiles];

		for (const file of newFiles) {
			const reader = new FileReader();
			reader.onload = (ev) => {
				if (ev.target?.result) {
					imagePreviews = [...imagePreviews, ev.target?.result as string];
				}
			};
			reader.readAsDataURL(file);
		}
	}

	function handleDragOver(e: DragEvent) {
		e.preventDefault();
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		error = null;
		success = null;

		// Validate
		if (!name.trim()) {
			error = 'Product Identity is required';
			return;
		}
		if (!shortDescription.trim()) {
			error = 'Precise Abstract is required';
			return;
		}
		if (!pricePln.trim()) {
			error = 'Valuation (PLN) is required';
			return;
		}
		if (!priceEur.trim()) {
			error = 'Valuation (EUR) is required';
			return;
		}
		if (!categoryId) {
			error = 'Taxonomy selection is required';
			return;
		}

		const credentials = getCredentials();
		if (!credentials) {
			error = 'Not authenticated';
			return;
		}

		loading = true;

		try {
			const product = await createProduct(credentials.username, credentials.password, {
				name: name.trim(),
				shortDescription: shortDescription.trim(),
				description: description.trim() || '<p></p>',
				prices: [
					{ currency: 'PLN', price: pricePln.trim() },
					{ currency: 'EUR', price: priceEur.trim() }
				],
				categoryId,
				sku: sku.trim() || undefined,
				isActive,
				fabrication: {
					content: fabricationContent.trim() || undefined,
					care: fabricationCare.trim() || undefined
				},
				ethics: {
					origin: ethicsOrigin.trim() || undefined,
					impact: ethicsImpact.trim() || undefined
				}
			});

			// Upload images sequentially
			if (selectedFiles.length > 0 && product.id) {
				for (const file of selectedFiles) {
					await uploadProductImage(credentials.username, credentials.password, product.id, file);
				}
			}

			success = `Product "${product.name}" registered successfully`;
			resetForm();
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to register product';
		} finally {
			loading = false;
		}
	}

	function getCredentials() {
		return get(auth);
	}

	function resetForm() {
		name = '';
		shortDescription = '';
		pricePln = '';
		priceEur = '';
		categoryId = '';
		sku = '';
		isActive = true;
		description = '';
		fabricationContent = '';
		fabricationCare = '';
		ethicsOrigin = '';
		ethicsImpact = '';
		selectedFiles = [];
		imagePreviews = [];
	}
</script>

<svelte:head>
	<title>CLOTHINGSHOP — Product Registration</title>
</svelte:head>

<!-- Page header -->
<div class="mb-6">
	<div class="flex items-center gap-3 mb-1">
		<div class="w-1 h-6 bg-primary-container"></div>
		<h1 class="font-headline text-on-surface text-xs uppercase tracking-[0.3em]">
			Product Registration
		</h1>
	</div>
	<p class="font-body text-on-secondary-container text-xs pl-4">
		Register a new artifact into the system
	</p>
</div>

{#if error}
	<div class="bg-primary-container/20 border-l-2 border-primary-container px-4 py-3 mb-6">
		<p class="font-body text-primary text-sm">{error}</p>
	</div>
{/if}

{#if success}
	<div class="bg-green-900/20 border-l-2 border-green-700 px-4 py-3 mb-6">
		<p class="font-body text-green-400 text-sm">{success}</p>
	</div>
{/if}

<form onsubmit={handleSubmit}>
	<!-- Two-column grid -->
	<div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
		<!-- Left column: Identification Cluster -->
		<div class="space-y-6">
			<div>
				<h2 class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-4">
					Identification Cluster
				</h2>
			</div>

			<!-- Product Identity (name) -->
			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="name"
				>
					Product Identity
				</label>
				<input
					id="name"
					type="text"
					bind:value={name}
					required
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="Enter product name"
				/>
			</div>

			<!-- Precise Abstract (shortDescription) -->
			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="shortDescription"
				>
					Precise Abstract
				</label>
				<input
					id="shortDescription"
					type="text"
					bind:value={shortDescription}
					required
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="Brief product description"
				/>
			</div>

			<!-- Valuation (PLN + EUR) -->
			<div>
				<div class="grid grid-cols-2 gap-4">
					<div>
						<label
							class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
							for="pricePln"
						>
							Valuation (PLN)
						</label>
						<input
							id="pricePln"
							type="text"
							bind:value={pricePln}
							required
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
							placeholder="0.00"
						/>
					</div>
					<div>
						<label
							class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
							for="priceEur"
						>
							Valuation (EUR)
						</label>
						<input
							id="priceEur"
							type="text"
							bind:value={priceEur}
							required
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
							placeholder="0.00"
						/>
					</div>
				</div>
			</div>

			<!-- SKU -->
			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="sku"
				>
					Stock Keeping Unit
				</label>
				<input
					id="sku"
					type="text"
					bind:value={sku}
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="SKU identifier"
				/>
			</div>

			<!-- Taxonomy (category) -->
			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="categoryId"
				>
					Taxonomy
				</label>
				<select
					id="categoryId"
					bind:value={categoryId}
					required
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors appearance-none cursor-pointer"
				>
					<option value="" disabled class="bg-surface-container-lowest text-on-surface">
						Select category
					</option>
					{#each categories as cat}
						<option value={cat.id} class="bg-surface-container-lowest text-on-surface">
							{cat.name}
						</option>
					{/each}
				</select>
			</div>

			<!-- Status (isActive) -->
			<div class="flex items-center gap-3 pt-2">
				<button
					type="button"
					onclick={() => (isActive = !isActive)}
					class="relative w-10 h-5 flex-shrink-0 transition-colors {isActive
						? 'bg-primary-container'
						: 'bg-surface-container-highest'}"
				>
					<span
						class="absolute top-0.5 left-0.5 w-4 h-4 bg-on-surface transition-transform {isActive
							? 'translate-x-5'
							: 'translate-x-0'}"
					></span>
				</button>
				<label class="font-headline text-outline text-[10px] uppercase tracking-[0.3em]">
					Active Status
				</label>
			</div>
		</div>

		<!-- Right column: Rich Text Cluster -->
		<div class="space-y-6">
			<div>
				<h2 class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-4">
					Rich Text Cluster
				</h2>
			</div>

			<!-- Narrative (description) -->
			<div>
				<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2">
					Narrative
				</label>
				<TiptapEditor bind:content={description} onChange={(html) => (description = html)} minHeight="160px" />
			</div>

			<!-- Fabrication sub-grid -->
			<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
				<!-- Materiality -->
				<div>
					<label
						class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					>
						Materiality
					</label>
					<TiptapEditor
						bind:content={fabricationContent}
						onChange={(html) => (fabricationContent = html)}
						minHeight="128px"
					/>
				</div>

				<!-- Preservation -->
				<div>
					<label
						class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					>
						Preservation
					</label>
					<TiptapEditor
						bind:content={fabricationCare}
						onChange={(html) => (fabricationCare = html)}
						minHeight="128px"
					/>
				</div>
			</div>
		</div>
	</div>

	<!-- Ethics row (full-width) -->
	<div class="border-t border-outline/10 pt-6 mb-6">
		<h2 class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-4">
			Ethics Documentation
		</h2>
		<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="ethicsOrigin"
				>
					Provenance
				</label>
				<input
					id="ethicsOrigin"
					type="text"
					bind:value={ethicsOrigin}
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="Origin / sourcing details"
				/>
			</div>
			<div>
				<label
					class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2"
					for="ethicsImpact"
				>
					Societal Resonance
				</label>
				<input
					id="ethicsImpact"
					type="text"
					bind:value={ethicsImpact}
					class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
					placeholder="Social impact statement"
				/>
			</div>
		</div>
	</div>

	<!-- Visual Documentation -->
	<div class="border-t border-outline/10 pt-6 mb-6">
		<h2 class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-4">
			Visual Documentation
		</h2>

		<!-- Drop zone -->
		<div
			class="border-2 border-dashed border-outline/20 bg-surface-container hover:border-outline/40 transition-colors flex flex-col items-center justify-center cursor-pointer"
			style="height: 16rem"
			ondrop={handleDrop}
			ondragover={handleDragOver}
			onclick={() => document.getElementById('imageInput')?.click()}
		>
			<span class="material-symbols-outlined text-outline/40 text-[48px] mb-3">
				cloud_upload
			</span>
			<p class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-1">
				Drop visual assets here
			</p>
			<p class="font-body text-outline/50 text-xs">
				or click to browse files
			</p>
		</div>
		<input
			id="imageInput"
			type="file"
			accept="image/*"
			multiple
			class="hidden"
			onchange={handleImageSelect}
		/>

		<!-- Image preview grid -->
		{#if imagePreviews.length > 0}
			<div class="grid grid-cols-4 gap-3 mt-4">
				{#each imagePreviews as preview, i}
					<div class="relative group bg-surface-container aspect-square overflow-hidden">
						<img
							src={preview}
							alt="Preview {i + 1}"
							class="w-full h-full object-cover grayscale group-hover:grayscale-0 transition-all"
						/>
						<button
							type="button"
							onclick={() => removeImage(i)}
							class="absolute top-1 right-1 w-5 h-5 bg-primary-container/80 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
						>
							<span class="material-symbols-outlined text-on-surface text-[14px]">close</span>
						</button>
					</div>
				{/each}
			</div>
		{/if}
	</div>

	<!-- Submit button -->
	<button
		type="submit"
		disabled={loading}
		class="w-full bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-8 py-4 hover:bg-on-primary-fixed-variant transition-all disabled:opacity-50 disabled:cursor-not-allowed hover:-translate-y-0.5 active:scale-95"
	>
		{loading ? 'REGISTERING ARTIFACT...' : 'ADD PRODUCT'}
	</button>
</form>
