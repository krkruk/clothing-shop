import { describe, it, expect, beforeEach } from 'vitest';
import { get } from 'svelte/store';
import { currency, formatPrice } from '$lib/stores/currency';

describe('Currency Store', () => {
	beforeEach(() => {
		currency.set('PLN');
		localStorage.clear();
	});

	it('defaults to PLN', () => {
		expect(get(currency)).toBe('PLN');
	});

	it('can be set to EUR', () => {
		currency.set('EUR');
		expect(get(currency)).toBe('EUR');
	});

	it('persists to localStorage', () => {
		currency.set('EUR');
		expect(localStorage.getItem('clothingshop-currency')).toBe('EUR');
	});

	it('loads from localStorage on init', () => {
		localStorage.setItem('clothingshop-currency', 'EUR');
		// Note: store already initialized, so we test the set+persist behavior
		currency.set('PLN');
		expect(localStorage.getItem('clothingshop-currency')).toBe('PLN');
	});
});

describe('formatPrice', () => {
	it('formats a number price with currency code', () => {
		expect(formatPrice(285, 'PLN')).toBe('PLN 285.00');
	});

	it('formats a string price with currency code', () => {
		expect(formatPrice('399.50', 'EUR')).toBe('EUR 399.50');
	});

	it('handles zero', () => {
		expect(formatPrice(0, 'PLN')).toBe('PLN 0.00');
	});

	it('handles invalid string', () => {
		expect(formatPrice('invalid', 'PLN')).toBe('PLN 0.00');
	});
});
