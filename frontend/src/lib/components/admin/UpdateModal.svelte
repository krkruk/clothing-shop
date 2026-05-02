<script lang="ts">
	import { onMount } from 'svelte';
	import { auth } from '$lib/stores/auth';
	import { get } from 'svelte/store';
	import { updateProduct, uploadProductImage, deleteProductImage } from '$lib/api/admin';
	import { fetchCategories } from '$lib/api/categories';
	import type { ProductResponse } from '../../../api/generated/models/ProductResponse';
	import type { CategoryDto } from '../../../api/generated/models/CategoryDto';
	import type { ProductImageDto } from '../../../api/generated/models/ProductImageDto';
	import type { ProductPriceDto } from '../../../api/generated/models/ProductPriceDto';
	import TiptapEditor from './TiptapEditor.svelte';

	let {
		product,
		onClose,
		onUpdated
	}: {
		product: ProductResponse;
		onClose: () => void;
		onUpdated: () => void;
	} = $props();

	let categories = $state<CategoryDto[]>([]);

	// Extract prices from product
	function getPrice(prices: ProductPriceDto[] | undefined, currency: string): string {
		if (!prices) return '';
		const found = prices.find((p) => p.currency === currency);
		return found?.price?.toString() ?? '';
	}

	// Form fields
	let name = $state(String(product.name ?? ''));
	let shortDescription = $state(String(product.shortDescription ?? ''));
	let pricePln = $state(getPrice(product.prices, 'PLN'));
	let priceEur = $state(getPrice(product.prices, 'EUR'));
	let categoryId = $state(product.category?.id || '');
	let sku = $state(String(product.sku ?? ''));
	let isActive = $state(product.isActive ?? true);
	let description = $state(String(product.description ?? ''));
	let fabricationContent = $state(String(product.fabrication?.content ?? ''));
	let fabricationCare = $state(String(product.fabrication?.care ?? ''));
	let ethicsOrigin = $state(String(product.ethics?.origin ?? ''));
	let ethicsImpact = $state(String(product.ethics?.impact ?? ''));

	// Existing images
	let existingImages = $state<ProductImageDto[]>(product.images || []);
	let imagesToRemove = $state<string[]>([]);

	// New images
	let newFiles = $state<File[]>([]);
	let newPreviews = $state<string[]>([]);

	// State
	let loading = $state(false);
	let error = $state<string | null>(null);

	onMount(async () => {
		try {
			categories = await fetchCategories();
		} catch {
			error = 'Failed to load categories';
		}
	});

	function handleNewImageSelect(e: Event) {
		const input = e.target as HTMLInputElement;
		if (!input.files) return;
		const files = Array.from(input.files);
		newFiles = [...newFiles, ...files];
		for (const file of files) {
			const reader = new FileReader();
			reader.onload = (ev) => {
				if (ev.target?.result) {
					newPreviews = [...newPreviews, ev.target.result as string];
				}
			};
			reader.readAsDataURL(file);
		}
		input.value = '';
	}

	function removeExistingImage(imageId: string) {
		imagesToRemove = [...imagesToRemove, imageId];
		existingImages = existingImages.filter((img) => img.imageId !== imageId);
	}

	function removeNewImage(index: number) {
		newFiles = newFiles.filter((_, i) => i !== index);
		newPreviews = newPreviews.filter((_, i) => i !== index);
	}

	function handleDrop(e: DragEvent) {
		e.preventDefault();
		if (!e.dataTransfer?.files) return;
		const files = Array.from(e.dataTransfer.files).filter((f) => f.type.startsWith('image/'));
		newFiles = [...newFiles, ...files];
		for (const file of files) {
			const reader = new FileReader();
			reader.onload = (ev) => {
				if (ev.target?.result) {
					newPreviews = [...newPreviews, ev.target.result as string];
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
		loading = true;

		const credentials = getCredentials();
		if (!credentials) {
			error = 'Not authenticated';
			loading = false;
			return;
		}

		try {
			await updateProduct(credentials.username, credentials.password, product.id!, {
				name: name.trim(),
				shortDescription: shortDescription.trim(),
				prices: [
					{ currency: 'PLN', price: pricePln.trim() },
					{ currency: 'EUR', price: priceEur.trim() }
				],
				categoryId: categoryId,
				sku: sku.trim() || undefined,
				isActive,
				description: description.trim(),
				fabrication: {
					content: fabricationContent.trim(),
					care: fabricationCare.trim()
				},
				ethics: {
					origin: ethicsOrigin.trim(),
					impact: ethicsImpact.trim()
				}
			});

			// Delete removed images
			for (const imageId of imagesToRemove) {
				await deleteProductImage(credentials.username, credentials.password, product.id!, imageId);
			}

			// Upload new images
			for (const file of newFiles) {
				await uploadProductImage(credentials.username, credentials.password, product.id!, file);
			}

			onUpdated();
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to update product';
		} finally {
			loading = false;
		}
	}

	function getCredentials() {
		return get(auth);
	}
</script>

<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<div
	class="fixed inset-0 z-50 flex items-start justify-center bg-black/70 backdrop-blur-sm overflow-y-auto py-8"
	onclick={onClose}
	role="presentation"
>
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div
		class="bg-surface-container-lowest w-full max-w-4xl mx-4 border-l-2 border-primary-container"
		onclick={(e) => e.stopPropagation()}
	>
		<!-- Header -->
		<div class="flex items-center justify-between p-6 border-b border-outline/10">
			<div class="flex items-center gap-3">
				<div class="w-1 h-6 bg-primary-container"></div>
				<h2 class="font-headline text-on-surface text-xs uppercase tracking-[0.3em]">
					Update Artifact
				</h2>
			</div>
			<button
				type="button"
				onclick={onClose}
				class="p-1.5 text-on-secondary-container hover:text-primary transition-colors"
			>
				<span class="material-symbols-outlined text-[20px]">close</span>
			</button>
		</div>

		<!-- Form content -->
		<form onsubmit={handleSubmit} class="p-6">
			{#if error}
				<div class="bg-primary-container/20 border-l-2 border-primary-container px-4 py-3 mb-6">
					<p class="font-body text-primary text-sm">{error}</p>
				</div>
			{/if}

			<!-- Two-column grid -->
			<div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
				<!-- Left: Identification -->
				<div class="space-y-5">
					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-name">
							Product Identity
						</label>
						<input
							id="edit-name"
							type="text"
							bind:value={name}
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
						/>
					</div>

					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-short">
							Precise Abstract
						</label>
						<input
							id="edit-short"
							type="text"
							bind:value={shortDescription}
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
						/>
					</div>

					<!-- Valuation (PLN + EUR) -->
					<div>
						<div class="grid grid-cols-2 gap-4">
							<div>
								<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-price-pln">
									Valuation (PLN)
								</label>
								<input
									id="edit-price-pln"
									type="text"
									bind:value={pricePln}
									class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
								/>
							</div>
							<div>
								<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-price-eur">
									Valuation (EUR)
								</label>
								<input
									id="edit-price-eur"
									type="text"
									bind:value={priceEur}
									class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
								/>
							</div>
						</div>
					</div>

					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-sku">
							Stock Keeping Unit
						</label>
						<input
							id="edit-sku"
							type="text"
							bind:value={sku}
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
						/>
					</div>

					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-category">
							Taxonomy
						</label>
						<select
							id="edit-category"
							bind:value={categoryId}
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

				<!-- Right: Rich text -->
				<div class="space-y-5">
					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2">
							Narrative
						</label>
						<TiptapEditor
							bind:content={description}
							onChange={(html) => (description = html)}
							minHeight="160px"
						/>
					</div>

					<div class="grid grid-cols-2 gap-4">
						<div>
							<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2">
								Materiality
							</label>
							<TiptapEditor
								bind:content={fabricationContent}
								onChange={(html) => (fabricationContent = html)}
								minHeight="128px"
							/>
						</div>
						<div>
							<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2">
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

			<!-- Ethics row -->
			<div class="border-t border-outline/10 pt-5 mb-6">
				<div class="grid grid-cols-2 gap-6">
					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-origin">
							Provenance
						</label>
						<input
							id="edit-origin"
							type="text"
							bind:value={ethicsOrigin}
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
						/>
					</div>
					<div>
						<label class="block font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-2" for="edit-impact">
							Societal Resonance
						</label>
						<input
							id="edit-impact"
							type="text"
							bind:value={ethicsImpact}
							class="w-full bg-transparent border-b border-outline/30 text-on-surface font-body text-sm py-2 px-0 focus:border-primary-container focus:outline-none transition-colors"
						/>
					</div>
				</div>
			</div>

			<!-- Visual Documentation -->
			<div class="border-t border-outline/10 pt-5 mb-6">
				<h3 class="font-headline text-outline text-[10px] uppercase tracking-[0.3em] mb-4">
					Visual Documentation
				</h3>

				<!-- Existing images -->
				{#if existingImages.length > 0}
					<div class="grid grid-cols-4 gap-3 mb-4">
						{#each existingImages as img}
							<div class="relative group bg-surface-container aspect-square overflow-hidden">
								<img
									src={img.imageUrl}
									alt={img.alt || 'Product image'}
									class="w-full h-full object-cover grayscale group-hover:grayscale-0 transition-all"
								/>
								<button
									type="button"
									onclick={() => img.imageId && removeExistingImage(img.imageId)}
									class="absolute top-1 right-1 w-5 h-5 bg-primary-container/80 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
								>
									<span class="material-symbols-outlined text-on-surface text-[14px]">close</span>
								</button>
							</div>
						{/each}
					</div>
				{/if}

				<!-- New image drop zone -->
				<div
					class="border-2 border-dashed border-outline/20 bg-surface-container hover:border-outline/40 transition-colors flex flex-col items-center justify-center cursor-pointer"
					style="height: 10rem"
					ondrop={handleDrop}
					ondragover={handleDragOver}
					onclick={() => document.getElementById('edit-imageInput')?.click()}
				>
					<span class="material-symbols-outlined text-outline/40 text-[36px] mb-2">
						cloud_upload
					</span>
					<p class="font-headline text-outline text-[10px] uppercase tracking-[0.3em]">
						Add more visual assets
					</p>
				</div>
				<input
					id="edit-imageInput"
					type="file"
					accept="image/*"
					multiple
					class="hidden"
					onchange={handleNewImageSelect}
				/>

				{#if newPreviews.length > 0}
					<div class="grid grid-cols-4 gap-3 mt-3">
						{#each newPreviews as preview, i}
							<div class="relative group bg-surface-container aspect-square overflow-hidden">
								<img
									src={preview}
									alt="New preview {i + 1}"
									class="w-full h-full object-cover grayscale group-hover:grayscale-0 transition-all"
								/>
								<button
									type="button"
									onclick={() => removeNewImage(i)}
									class="absolute top-1 right-1 w-5 h-5 bg-primary-container/80 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
								>
									<span class="material-symbols-outlined text-on-surface text-[14px]">close</span>
								</button>
							</div>
						{/each}
					</div>
				{/if}
			</div>

			<!-- Actions -->
			<div class="flex gap-3">
				<button
					type="button"
					onclick={onClose}
					class="flex-1 bg-surface-container-high text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-6 py-3 hover:bg-surface-container-highest transition-colors"
				>
					Cancel
				</button>
				<button
					type="submit"
					disabled={loading}
					class="flex-1 bg-primary-container text-on-surface font-headline uppercase tracking-[0.2em] text-xs px-6 py-3 hover:bg-on-primary-fixed-variant transition-colors disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
				>
					{loading ? 'UPDATING...' : 'UPDATE ARTIFACT'}
				</button>
			</div>
		</form>
	</div>
</div>
