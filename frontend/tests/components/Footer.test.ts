import { render } from '@testing-library/svelte';
import { describe, it, expect } from 'vitest';
import Footer from '$lib/components/Footer.svelte';

describe('Footer', () => {
	it('renders all four links', () => {
		const { getByText } = render(Footer);
		expect(getByText('INVENTORY')).toBeInTheDocument();
		expect(getByText('TRANSACTIONS')).toBeInTheDocument();
		expect(getByText('LEGAL')).toBeInTheDocument();
		expect(getByText('MANIFESTO')).toBeInTheDocument();
	});

	it('MANIFESTO link has primary color class', () => {
		const { getByText } = render(Footer);
		const manifestoLink = getByText('MANIFESTO');
		expect(manifestoLink.className).toContain('text-primary');
	});

	it('renders copyright text', () => {
		const { getByText } = render(Footer);
		expect(getByText(/MMXXIV CLOTHINGSHOP/)).toBeInTheDocument();
	});
});
