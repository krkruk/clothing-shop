import { Configuration } from '../../api/generated/runtime';
import { ProductsApi } from '../../api/generated/apis/ProductsApi';
import type { ProductListResponse } from '../../api/generated/models/ProductListResponse';
import type { ProductDetailResponse } from '../../api/generated/models/ProductDetailResponse';
import { get } from 'svelte/store';
import { currency } from '$lib/stores/currency';

const config = new Configuration({
	basePath: '/api/v1',
	middleware: [
		{
			pre: async (context) => {
				const currentCurrency = get(currency);
				context.init.headers = {
					...context.init.headers,
					'x-currency-code': currentCurrency
				};
				return { url: context.url, init: context.init };
			}
		}
	]
});

const api = new ProductsApi(config);

export async function fetchProducts(
	cursor?: string,
	limit: number = 7
): Promise<ProductListResponse> {
	return api.listProducts({ cursor, limit });
}

export async function fetchProductDetail(id: string): Promise<ProductDetailResponse> {
	return api.getProductDetail({ id });
}

export type { ProductListResponse, ProductSummary, ProductDetailResponse } from '../../api/generated';
