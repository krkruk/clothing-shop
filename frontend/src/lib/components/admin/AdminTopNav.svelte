<script lang="ts">
	let { sectionTitle }: { sectionTitle: string } = $props();
</script>

<header
	class="h-16 bg-surface-container-lowest flex items-center justify-between px-6 border-b border-outline/10 flex-shrink-0"
>
	<div class="flex items-center gap-4">
		<div class="w-1 h-6 bg-primary-container"></div>
		<h2 class="font-headline text-on-surface text-xs uppercase tracking-[0.3em]">
			{sectionTitle}
		</h2>
	</div>

	<div class="flex items-center gap-4">
		<!-- Search input (placeholder) -->
		<div class="hidden md:flex items-center gap-2 bg-surface-container px-3 py-1.5">
			<span class="material-symbols-outlined text-outline text-[16px]">search</span>
			<input
				type="text"
				placeholder="Search inventory..."
				class="bg-transparent text-on-surface font-body text-xs w-40 focus:outline-none placeholder:text-outline/50"
			/>
		</div>

		<span class="material-symbols-outlined text-on-secondary-container text-[20px] hover:text-primary transition-colors cursor-pointer">
			admin_panel_settings
		</span>
		<span class="material-symbols-outlined text-on-secondary-container text-[20px] hover:text-primary transition-colors cursor-pointer">
			account_circle
		</span>
	</div>
</header>
