import { Configuration } from '../../api/generated/runtime';
import { AdminProductsApi } from '../../api/generated/apis/AdminProductsApi';
import type { AdminProductListResponse } from '../../api/generated/models/AdminProductListResponse';
import type { CreateProductRequest } from '../../api/generated/models/CreateProductRequest';
import type { ProductResponse } from '../../api/generated/models/ProductResponse';
import type { UpdateProductRequest } from '../../api/generated/models/UpdateProductRequest';
import type { ImageUploadResponse } from '../../api/generated/models/ImageUploadResponse';

function createApi(username: string, password: string): AdminProductsApi {
	const config = new Configuration({
		basePath: '/api/v1',
		username,
		password
	});
	return new AdminProductsApi(config);
}

export async function createProduct(
	username: string,
	password: string,
	request: CreateProductRequest
): Promise<ProductResponse> {
	const api = createApi(username, password);
	return api.createProduct({ createProductRequest: request });
}

export async function updateProduct(
	username: string,
	password: string,
	id: string,
	request: UpdateProductRequest
): Promise<ProductResponse> {
	const api = createApi(username, password);
	return api.updateProduct({ id, updateProductRequest: request });
}

export async function deleteProduct(
	username: string,
	password: string,
	id: string
): Promise<void> {
	const api = createApi(username, password);
	return api.deleteProduct({ id });
}

export async function listAdminProducts(
	username: string,
	password: string,
	cursor?: string,
	limit: number = 20
): Promise<AdminProductListResponse> {
	const api = createApi(username, password);
	return api.listAdminProducts({ cursor, limit });
}

export async function deleteProductImage(
	username: string,
	password: string,
	productId: string,
	imageId: string
): Promise<void> {
	const api = createApi(username, password);
	return api.deleteProductImage({ id: productId, imageId });
}

export async function uploadProductImage(
	username: string,
	password: string,
	productId: string,
	file: Blob,
	alt?: string
): Promise<ImageUploadResponse> {
	const api = createApi(username, password);
	return api.uploadProductImage({ id: productId, file, alt });
}

export type {
	AdminProductListResponse,
	CreateProductRequest,
	ProductResponse,
	UpdateProductRequest,
	ImageUploadResponse
};
