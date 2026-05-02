<script lang="ts">
	const links = [
		{ label: 'INVENTORY', href: '/', primary: false },
		{ label: 'TRANSACTIONS', href: '/', primary: false },
		{ label: 'LEGAL', href: '/', primary: false },
		{ label: 'MANIFESTO', href: '/', primary: true }
	];
</script>

<footer class="bg-surface-container-lowest py-12 px-6 lg:px-12">
	<div class="max-w-7xl mx-auto">
		<nav class="flex flex-wrap gap-8 mb-8" aria-label="Footer">
			{#each links as link}
				<a
					href={link.href}
					class="font-label text-[10px] uppercase tracking-[0.3em] transition-colors duration-700
						{link.primary
						? 'text-primary'
						: 'text-surface-container-highest hover:text-on-surface'}"
				>
					{link.label}
				</a>
			{/each}
		</nav>
		<p class="font-label text-[10px] uppercase tracking-[0.3em] text-surface-container-highest">
			&copy; MMXXIV CLOTHINGSHOP. ALL RIGHTS RESERVED.
		</p>
	</div>
</footer>
