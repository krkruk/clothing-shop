import { render } from '@testing-library/svelte';
import { describe, it, expect, beforeEach } from 'vitest';
import { get } from 'svelte/store';
import CartDrawer from '$lib/components/CartDrawer.svelte';
import { cartItems } from '$lib/stores/cart';
import { Silhouette } from '$lib/mock/types';
import type { Product, Personalization } from '$lib/mock/types';

const mockProduct: Product = {
	id: 'test-1',
	name: 'Test Artifact',
	description: 'Test',
	shortDescription: 'Test',
	price: 100,
	categoryId: 'cat-tops',
	fabrication: [],
	ethics: [],
	images: ['https://example.com/img.jpg']
};

const defaultPersonalization: Personalization = {
	silhouette: Silhouette.BOXY,
	waistCm: 80,
	hipsCm: 95,
	heightCm: 175
};

describe('CartDrawer', () => {
	beforeEach(() => {
		cartItems.set([]);
		localStorage.clear();
	});

	it('renders empty state when cart is empty', () => {
		const { getByText } = render(CartDrawer, { props: { isOpen: true, onClose: () => {} } });
		expect(getByText('INVENTORY IS CURRENTLY EMPTY')).toBeInTheDocument();
	});

	it('renders header with CURRENT INVENTORY', () => {
		const { getByText } = render(CartDrawer, { props: { isOpen: true, onClose: () => {} } });
		expect(getByText('CURRENT INVENTORY')).toBeInTheDocument();
	});

	it('renders items after adding to cart', () => {
		cartItems.addItem(mockProduct, defaultPersonalization);
		const { getByText, getAllByText } = render(CartDrawer, { props: { isOpen: true, onClose: () => {} } });
		expect(getByText('Test Artifact')).toBeInTheDocument();
		expect(getAllByText(/PLN 100\.00/).length).toBeGreaterThanOrEqual(1);
	});

	it('renders total value footer when items present', () => {
		cartItems.addItem(mockProduct, defaultPersonalization);
		const { getByText } = render(CartDrawer, { props: { isOpen: true, onClose: () => {} } });
		expect(getByText('TOTAL VALUE')).toBeInTheDocument();
		expect(getByText('PROCEED TO TRANSACTION')).toBeInTheDocument();
	});

	it('renders quantity controls', () => {
		cartItems.addItem(mockProduct, defaultPersonalization);
		const { getByText } = render(CartDrawer, { props: { isOpen: true, onClose: () => {} } });
		expect(getByText('1')).toBeInTheDocument();
	});
});
