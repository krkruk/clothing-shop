<script lang="ts">
	import type { ProductResponse } from '../../../api/generated/models/ProductResponse';

	let {
		products,
		onEdit,
		onDelete
	}: {
		products: ProductResponse[];
		onEdit: (product: ProductResponse) => void;
		onDelete: (product: ProductResponse) => void;
	} = $props();
</script>

<div class="overflow-x-auto">
	<table class="w-full">
		<thead>
			<tr class="border-b border-outline/20">
				<th class="text-left font-headline text-outline text-[10px] uppercase tracking-[0.3em] py-3 px-4">
					Asset
				</th>
				<th class="text-left font-headline text-outline text-[10px] uppercase tracking-[0.3em] py-3 px-4">
					Identity
				</th>
				<th class="text-left font-headline text-outline text-[10px] uppercase tracking-[0.3em] py-3 px-4">
					Classification
				</th>
				<th class="text-left font-headline text-outline text-[10px] uppercase tracking-[0.3em] py-3 px-4">
					Modified
				</th>
				<th class="text-right font-headline text-outline text-[10px] uppercase tracking-[0.3em] py-3 px-4">
					Directives
				</th>
			</tr>
		</thead>
		<tbody>
			{#each products as product}
				<tr class="border-b border-outline/5 hover:bg-surface-container group transition-colors">
					<!-- Asset (thumbnail) -->
					<td class="py-3 px-4">
						<div class="w-12 h-12 bg-surface-container-high overflow-hidden">
							{#if product.images && product.images.length > 0}
								<img
									src={product.images[0].imageUrl}
									alt={product.images[0].alt || product.name || ''}
									class="w-full h-full object-cover grayscale group-hover:grayscale-0 transition-all"
								/>
							{:else}
								<div class="w-full h-full flex items-center justify-center">
									<span class="material-symbols-outlined text-outline/30 text-[20px]">
										image_not_supported
									</span>
								</div>
							{/if}
						</div>
					</td>

					<!-- Identity -->
					<td class="py-3 px-4">
						<p class="font-headline text-on-surface text-sm tracking-wide">
							{product.name || 'Untitled'}
						</p>
						<p class="font-body text-on-secondary-container text-xs mt-0.5">
							{product.sku || 'No SKU'}
						</p>
						{#if !product.isActive}
							<span class="font-headline text-[8px] uppercase tracking-[0.3em] bg-primary-container/30 text-primary px-1.5 py-0.5 mt-1 inline-block">
								Inactive
							</span>
						{/if}
					</td>

					<!-- Classification -->
					<td class="py-3 px-4">
						<p class="font-body text-on-secondary-container text-xs">
							{product.category?.name || 'Uncategorized'}
						</p>
						<p class="font-body text-outline text-xs mt-0.5">
							{#if product.prices && product.prices.length > 0}
									{product.prices.map((p) => `${p.price} ${p.currency}`).join(' / ')}
								{:else}
									No pricing
								{/if}
						</p>
					</td>

					<!-- Modified -->
					<td class="py-3 px-4">
						<p class="font-body text-on-secondary-container text-xs">
							{product.updatedAt
								? new Date(product.updatedAt).toLocaleDateString('en-GB', {
										day: '2-digit',
										month: 'short',
										year: 'numeric'
									})
								: '—'}
						</p>
					</td>

					<!-- Directives -->
					<td class="py-3 px-4 text-right">
						<div class="flex items-center justify-end gap-2">
							<button
								type="button"
								onclick={() => onEdit(product)}
								class="p-1.5 text-on-secondary-container hover:text-primary hover:bg-surface-container-high transition-colors"
								title="Edit product"
							>
								<span class="material-symbols-outlined text-[18px]">edit</span>
							</button>
							<button
								type="button"
								onclick={() => onDelete(product)}
								class="p-1.5 text-on-secondary-container hover:text-primary-container hover:bg-surface-container-high transition-colors"
								title="Delete product"
							>
								<span class="material-symbols-outlined text-[18px]">delete</span>
							</button>
						</div>
					</td>
				</tr>
			{/each}
		</tbody>
	</table>
</div>
