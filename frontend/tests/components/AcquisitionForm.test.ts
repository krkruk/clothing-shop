import { render, fireEvent } from '@testing-library/svelte';
import { describe, it, expect, beforeEach } from 'vitest';
import { get } from 'svelte/store';
import AcquisitionForm from '$lib/components/AcquisitionForm.svelte';
import { cartItems } from '$lib/stores/cart';
import type { ProductDetailResponse } from '../../src/api/generated';

const mockProduct: ProductDetailResponse = {
	id: 'test-1',
	name: 'Test Artifact',
	description: 'Test',
	shortDescription: 'Test',
	price: '285',
	currency: 'PLN',
	sku: 'SKU-001',
	imageUrl: 'https://example.com/img.jpg',
	images: [],
	fabrication: { content: 'Cotton', care: 'Machine wash' },
	ethics: { origin: 'Poland', impact: 'Low' }
};

describe('AcquisitionForm', () => {
	beforeEach(() => {
		cartItems.set([]);
		localStorage.clear();
	});

	it('renders all four input fields', () => {
		const { getByLabelText } = render(AcquisitionForm, { props: { product: mockProduct } });
		expect(getByLabelText('Silhouette')).toBeInTheDocument();
		expect(getByLabelText('Waist (cm)')).toBeInTheDocument();
		expect(getByLabelText('Hips (cm)')).toBeInTheDocument();
		expect(getByLabelText('Height (cm)')).toBeInTheDocument();
	});

	it('renders ACQUIRE ARTIFACT button', () => {
		const { getByText } = render(AcquisitionForm, { props: { product: mockProduct } });
		expect(getByText('ACQUIRE ARTIFACT')).toBeInTheDocument();
	});

	it('renders price display', () => {
		const { getByText } = render(AcquisitionForm, { props: { product: mockProduct } });
		expect(getByText(/PLN 285\.00/)).toBeInTheDocument();
	});

	it('renders shipping note', () => {
		const { getByText } = render(AcquisitionForm, { props: { product: mockProduct } });
		expect(getByText(/Complimentary secure transit/)).toBeInTheDocument();
	});

	it('adds item to cart on form submission', async () => {
		const { getByText } = render(AcquisitionForm, { props: { product: mockProduct } });
		await fireEvent.click(getByText('ACQUIRE ARTIFACT'));
		const items = get(cartItems);
		expect(items).toHaveLength(1);
		expect(items[0].productId).toBe('test-1');
		expect(items[0].productName).toBe('Test Artifact');
	});

	it('renders silhouette options', () => {
		const { getByText } = render(AcquisitionForm, { props: { product: mockProduct } });
		expect(getByText('Boxy/Architectural')).toBeInTheDocument();
		expect(getByText('Tailored/Clinical')).toBeInTheDocument();
		expect(getByText('Oversized/Monastic')).toBeInTheDocument();
	});
});
