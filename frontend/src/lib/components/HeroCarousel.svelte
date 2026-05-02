<script lang="ts">
	let {
		images,
		height = 'calc(33.33vh - 80px)',
		imageClass = 'grayscale brightness-50'
	}: {
		images: { url: string; label?: string }[];
		height?: string;
		imageClass?: string;
	} = $props();

	let currentIndex = $state(0);
	let isPaused = $state(false);
	let intervalId: ReturnType<typeof setInterval> | null = null;

	function startAutoRotation() {
		if (intervalId) clearInterval(intervalId);
		intervalId = setInterval(() => {
			if (!isPaused) {
				currentIndex = (currentIndex + 1) % images.length;
			}
		}, 4500);
	}

	$effect(() => {
		if (images.length > 1) {
			startAutoRotation();
		}
		return () => {
			if (intervalId) clearInterval(intervalId);
		};
	});
</script>

<div
	class="relative overflow-hidden w-full"
	style="height: {height}"
	onmouseenter={() => (isPaused = true)}
	onmouseleave={() => (isPaused = false)}
	role="region"
	aria-label="Image carousel"
>
	{#each images as image, i}
		<div
			class="absolute inset-0 transition-opacity duration-[300ms]"
			class:opacity-100={i === currentIndex}
			class:opacity-0={i !== currentIndex}
		>
			<img
				src={image.url}
				alt={image.label ?? ''}
				class="w-full h-full object-cover {imageClass}"
			/>
			{#if image.label}
				<div
					class="absolute bottom-4 right-4 md:bottom-8 md:right-8"
				>
					<span class="font-headline font-light text-on-surface/60 text-lg md:text-2xl tracking-tight uppercase">
						{image.label}
					</span>
				</div>
			{/if}
		</div>
	{/each}

	<!-- Progress indicators -->
	{#if images.length > 1}
		<div class="absolute bottom-4 right-4 flex gap-1.5">
			{#each images as _, i}
				<div
					class="h-0.5 transition-all duration-300
						{i === currentIndex ? 'w-8 bg-primary-container' : 'w-4 bg-on-surface/20'}"
				></div>
			{/each}
		</div>
	{/if}
</div>
