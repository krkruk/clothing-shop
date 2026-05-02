import { writable, derived } from 'svelte/store';

export type CurrencyCode = 'PLN' | 'EUR';

const STORAGE_KEY = 'clothingshop-currency';
const DEFAULT_CURRENCY: CurrencyCode = 'PLN';

function loadCurrency(): CurrencyCode {
	if (typeof window === 'undefined') return DEFAULT_CURRENCY;
	try {
		const stored = localStorage.getItem(STORAGE_KEY);
		if (stored === 'PLN' || stored === 'EUR') return stored;
		return DEFAULT_CURRENCY;
	} catch {
		return DEFAULT_CURRENCY;
	}
}

function persist(value: CurrencyCode): void {
	if (typeof window === 'undefined') return;
	try {
		localStorage.setItem(STORAGE_KEY, value);
	} catch {
		// localStorage may be unavailable
	}
}

function createCurrencyStore() {
	const { subscribe, set, update } = writable<CurrencyCode>(loadCurrency());

	function setCurrency(value: CurrencyCode): void {
		persist(value);
		set(value);
	}

	return {
		subscribe,
		set: setCurrency,
		update
	};
}

export const currency = createCurrencyStore();

export function formatPrice(price: string | number, currencyCode: string): string {
	const numPrice = typeof price === 'string' ? parseFloat(price) : price;
	if (isNaN(numPrice)) return `${currencyCode} 0.00`;
	return `${currencyCode} ${numPrice.toFixed(2)}`;
}
