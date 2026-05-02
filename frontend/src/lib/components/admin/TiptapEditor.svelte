<script lang="ts">
	import { onMount } from 'svelte';
	import { Editor } from '@tiptap/core';
	import StarterKit from '@tiptap/starter-kit';
	import Bold from '@tiptap/extension-bold';
	import Italic from '@tiptap/extension-italic';
	import BulletList from '@tiptap/extension-bullet-list';

	let { content = $bindable(''), onChange, minHeight = '160px' }: { content?: string; onChange?: (html: string) => void; minHeight?: string } = $props();

	let editorEl: HTMLElement | undefined = $state();
	let editor: Editor | undefined = $state();

	onMount(() => {
		if (!editorEl) return;

		editor = new Editor({
			element: editorEl,
			extensions: [StarterKit, Bold, Italic, BulletList],
			content: content || '<p></p>',
			onUpdate: ({ editor: e }) => {
				onChange?.(e.getHTML());
			}
		});

		return () => {
			editor?.destroy();
		};
	});

	// Sync external content changes (e.g. form reset) into the editor
	$effect(() => {
		const currentContent = content;
		if (editor && !editor.isDestroyed) {
			const editorHTML = editor.getHTML();
			// Only update if the external value differs from what the editor holds,
			// to avoid disrupting cursor position during typing.
			if (currentContent !== editorHTML) {
				editor.commands.setContent(currentContent || '<p></p>');
			}
		}
	});

	function setBold() {
		editor?.chain().focus().toggleBold().run();
	}

	function setItalic() {
		editor?.chain().focus().toggleItalic().run();
	}

	function setBulletList() {
		editor?.chain().focus().toggleBulletList().run();
	}
</script>

<div class="bg-surface-container border-l-2 border-primary-container/30">
	<!-- Toolbar -->
	<div class="flex items-center gap-1 px-3 py-2 border-b border-outline/10">
		<button
			type="button"
			onclick={setBold}
			class="p-1 text-on-secondary-container hover:text-primary hover:bg-surface-container-high transition-colors"
			title="Bold"
		>
			<span class="material-symbols-outlined text-[16px]">format_bold</span>
		</button>
		<button
			type="button"
			onclick={setItalic}
			class="p-1 text-on-secondary-container hover:text-primary hover:bg-surface-container-high transition-colors"
			title="Italic"
		>
			<span class="material-symbols-outlined text-[16px]">format_italic</span>
		</button>
		<button
			type="button"
			onclick={setBulletList}
			class="p-1 text-on-secondary-container hover:text-primary hover:bg-surface-container-high transition-colors"
			title="Bullet list"
		>
			<span class="material-symbols-outlined text-[16px]">format_list_bulleted</span>
		</button>
	</div>

	<!-- Editor area -->
	<div
		bind:this={editorEl}
		class="px-4 py-3 text-on-surface font-body text-sm overflow-y-auto prose prose-invert prose-sm max-w-none"
		style="min-height: {minHeight}"
	></div>
</div>

<style>
	:global(.tiptap) {
		outline: none;
		min-height: inherit;
	}

	:global(.tiptap p) {
		margin: 0.25em 0;
	}

	:global(.tiptap ul) {
		list-style: disc;
		padding-left: 1.5em;
		margin: 0.25em 0;
	}

	:global(.tiptap strong) {
		color: var(--color-primary);
	}
</style>
