import { render } from '@testing-library/svelte';
import { describe, it, expect } from 'vitest';
import Header from '$lib/components/Header.svelte';

describe('Header', () => {
	it('renders brand name', () => {
		const { getByText } = render(Header);
		expect(getByText('CLOTHINGSHOP')).toBeInTheDocument();
	});

	it('renders navigation links', () => {
		const { getByText } = render(Header);
		expect(getByText('TOPS')).toBeInTheDocument();
		expect(getByText('BOTTOMS')).toBeInTheDocument();
		expect(getByText('ACCESSORIES')).toBeInTheDocument();
	});

	it('renders cart badge with 0', () => {
		const { getByText } = render(Header);
		expect(getByText('0')).toBeInTheDocument();
	});

	it('brand name links to home', () => {
		const { getByText } = render(Header);
		const brandLink = getByText('CLOTHINGSHOP').closest('a');
		expect(brandLink).toHaveAttribute('href', '/');
	});

	it('renders person icon button', () => {
		const { getByLabelText } = render(Header);
		expect(getByLabelText('Account')).toBeInTheDocument();
	});

	it('renders cart icon button', () => {
		const { getByLabelText } = render(Header);
		expect(getByLabelText('Cart')).toBeInTheDocument();
	});
});
