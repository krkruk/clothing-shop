import { writable, derived, get } from 'svelte/store';
import type { Personalization, Product } from '$lib/mock/types';
import type { ProductDetailResponse } from '../../api/generated';
import { fetchProductDetail } from '$lib/api/products';
import { currency } from './currency';

const STORAGE_KEY = 'clothingshop-cart';

export interface CartItem {
	id: string;
	productId: string;
	productName: string;
	price: number;
	currency: string;
	quantity: number;
	thumbnail: string;
	personalization: Personalization;
}

function loadCart(): CartItem[] {
	if (typeof window === 'undefined') return [];
	try {
		const stored = localStorage.getItem(STORAGE_KEY);
		return stored ? JSON.parse(stored) : [];
	} catch {
		return [];
	}
}

function persist(items: CartItem[]): void {
	if (typeof window === 'undefined') return;
	try {
		localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
	} catch {
		// localStorage may be full or unavailable
	}
}

function createCartStore() {
	const { subscribe, set, update } = writable<CartItem[]>(loadCart());

	let converting = false;

	function addItemFromApi(product: ProductDetailResponse, personalization: Personalization): void {
		const currentCurrency = get(currency);
		const item: CartItem = {
			id: crypto.randomUUID(),
			productId: product.id ?? '',
			productName: product.name ?? '',
			price: parseFloat(product.price ?? '0'),
			currency: product.currency ?? currentCurrency,
			quantity: 1,
			thumbnail: product.imageUrl ?? '',
			personalization
		};
		update((items) => {
			const updated = [...items, item];
			persist(updated);
			return updated;
		});
	}

	function addItem(product: Product, personalization: Personalization): void {
		const currentCurrency = get(currency);
		const item: CartItem = {
			id: crypto.randomUUID(),
			productId: product.id,
			productName: product.name,
			price: product.price,
			currency: currentCurrency,
			quantity: 1,
			thumbnail: product.images[0] ?? '',
			personalization
		};
		update((items) => {
			const updated = [...items, item];
			persist(updated);
			return updated;
		});
	}

	function removeItem(cartItemId: string): void {
		update((items) => {
			const updated = items.filter((i) => i.id !== cartItemId);
			persist(updated);
			return updated;
		});
	}

	function updateQuantity(cartItemId: string, quantity: number): void {
		if (quantity <= 0) {
			removeItem(cartItemId);
			return;
		}
		update((items) => {
			const updated = items.map((i) => (i.id === cartItemId ? { ...i, quantity } : i));
			persist(updated);
			return updated;
		});
	}

	async function convertCurrency(newCurrency: string): Promise<void> {
		if (converting) return;
		converting = true;

		let currentItems: CartItem[] = [];
		const unsub = subscribe((items) => (currentItems = items));
		unsub();

		if (currentItems.length === 0) {
			converting = false;
			return;
		}

		try {
			const updatedItems = await Promise.all(
				currentItems.map(async (item) => {
					try {
						const detail = await fetchProductDetail(item.productId);
						return {
							...item,
							price: parseFloat(detail.price ?? '0'),
							currency: detail.currency ?? newCurrency
						};
					} catch {
						return item;
					}
				})
			);
			update(() => {
				persist(updatedItems);
				return updatedItems;
			});
		} catch {
			// retain old prices on failure
		} finally {
			converting = false;
		}
	}

	return {
		subscribe,
		set,
		addItem,
		addItemFromApi,
		removeItem,
		updateQuantity,
		convertCurrency
	};
}

export const cartItems = createCartStore();

export const cartTotal = derived(cartItems, ($items) =>
	$items.reduce((sum, item) => sum + item.price * item.quantity, 0)
);

export const cartCount = derived(cartItems, ($items) =>
	$items.reduce((sum, item) => sum + item.quantity, 0)
);

export const cartCurrency = derived(cartItems, ($items) =>
	$items.length > 0 ? $items[0].currency : 'PLN'
);

// Subscribe to currency changes to trigger cart conversion
if (typeof window !== 'undefined') {
	currency.subscribe(($currency) => {
		let currentItems: CartItem[] = [];
		const unsub = cartItems.subscribe((items) => (currentItems = items));
		unsub();

		if (currentItems.length > 0 && currentItems[0].currency !== $currency) {
			cartItems.convertCurrency($currency);
		}
	});
}
