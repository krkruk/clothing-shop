import { describe, it, expect, beforeEach } from 'vitest';
import { get } from 'svelte/store';
import { cartItems, cartTotal, cartCount } from '$lib/stores/cart';
import { Silhouette } from '$lib/mock/types';
import type { Product, Personalization } from '$lib/mock/types';

const mockProduct: Product = {
	id: 'test-1',
	name: 'Test Artifact',
	description: 'A test product',
	shortDescription: 'Test',
	price: 100,
	categoryId: 'cat-tops',
	fabrication: [],
	ethics: [],
	images: ['https://example.com/img.jpg']
};

const mockProduct2: Product = {
	id: 'test-2',
	name: 'Test Artifact 2',
	description: 'Another test',
	shortDescription: 'Test 2',
	price: 50,
	categoryId: 'cat-bottoms',
	fabrication: [],
	ethics: [],
	images: ['https://example.com/img2.jpg']
};

const defaultPersonalization: Personalization = {
	silhouette: Silhouette.BOXY,
	waistCm: 80,
	hipsCm: 95,
	heightCm: 175
};

describe('Cart Store', () => {
	beforeEach(() => {
		cartItems.set([]);
		localStorage.clear();
	});

	describe('addItem', () => {
		it('adds an item to the cart', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			expect(items).toHaveLength(1);
			expect(items[0].productId).toBe('test-1');
			expect(items[0].productName).toBe('Test Artifact');
			expect(items[0].price).toBe(100);
			expect(items[0].quantity).toBe(1);
			expect(items[0].personalization.silhouette).toBe(Silhouette.BOXY);
		});

		it('creates unique IDs for each item', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			expect(items).toHaveLength(2);
			expect(items[0].id).not.toBe(items[1].id);
		});

		it('adds items with different personalization as separate entries', () => {
			const otherPersonalization: Personalization = {
				silhouette: Silhouette.CURVY,
				waistCm: 70,
				hipsCm: 90,
				heightCm: 180
			};
			cartItems.addItem(mockProduct, defaultPersonalization);
			cartItems.addItem(mockProduct, otherPersonalization);
			const items = get(cartItems);
			expect(items).toHaveLength(2);
		});
	});

	describe('removeItem', () => {
		it('removes an existing item', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			cartItems.removeItem(items[0].id);
			expect(get(cartItems)).toHaveLength(0);
		});

		it('is a no-op for non-existent item', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			cartItems.removeItem('non-existent-id');
			expect(get(cartItems)).toHaveLength(1);
		});
	});

	describe('updateQuantity', () => {
		it('updates quantity to a positive value', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			cartItems.updateQuantity(items[0].id, 3);
			expect(get(cartItems)[0].quantity).toBe(3);
		});

		it('removes item when quantity set to 0', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			cartItems.updateQuantity(items[0].id, 0);
			expect(get(cartItems)).toHaveLength(0);
		});

		it('removes item when quantity set to negative', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			cartItems.updateQuantity(items[0].id, -1);
			expect(get(cartItems)).toHaveLength(0);
		});
	});

	describe('cartTotal', () => {
		it('computes total for single item', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			expect(get(cartTotal)).toBe(100);
		});

		it('computes total for multiple items with quantities', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			cartItems.addItem(mockProduct2, defaultPersonalization);
			const items = get(cartItems);
			cartItems.updateQuantity(items[0].id, 2);
			expect(get(cartTotal)).toBe(250);
		});

		it('returns 0 for empty cart', () => {
			expect(get(cartTotal)).toBe(0);
		});

		it('updates on item removal', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			cartItems.addItem(mockProduct2, defaultPersonalization);
			const items = get(cartItems);
			cartItems.removeItem(items[0].id);
			expect(get(cartTotal)).toBe(50);
		});
	});

	describe('cartCount', () => {
		it('counts items with quantities', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			cartItems.addItem(mockProduct2, defaultPersonalization);
			const items = get(cartItems);
			cartItems.updateQuantity(items[0].id, 3);
			expect(get(cartCount)).toBe(4);
		});

		it('returns 0 for empty cart', () => {
			expect(get(cartCount)).toBe(0);
		});
	});

	describe('localStorage persistence', () => {
		it('persists to localStorage on add', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const stored = JSON.parse(localStorage.getItem('clothingshop-cart') ?? '[]');
			expect(stored).toHaveLength(1);
			expect(stored[0].productName).toBe('Test Artifact');
		});

		it('persists to localStorage on remove', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			cartItems.removeItem(items[0].id);
			const stored = JSON.parse(localStorage.getItem('clothingshop-cart') ?? '[]');
			expect(stored).toHaveLength(0);
		});

		it('persists to localStorage on quantity update', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const items = get(cartItems);
			cartItems.updateQuantity(items[0].id, 5);
			const stored = JSON.parse(localStorage.getItem('clothingshop-cart') ?? '[]');
			expect(stored[0].quantity).toBe(5);
		});

		it('round-trips personalization data', () => {
			cartItems.addItem(mockProduct, defaultPersonalization);
			const stored = JSON.parse(localStorage.getItem('clothingshop-cart') ?? '[]');
			expect(stored[0].personalization.waistCm).toBe(80);
			expect(stored[0].personalization.hipsCm).toBe(95);
			expect(stored[0].personalization.heightCm).toBe(175);
			expect(stored[0].personalization.silhouette).toBe('BOXY');
		});
	});
});
