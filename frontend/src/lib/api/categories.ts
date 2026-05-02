import { Configuration } from '../../api/generated/runtime';
import { CategoriesApi } from '../../api/generated/apis/CategoriesApi';
import type { CategoryDto } from '../../api/generated/models/CategoryDto';

const config = new Configuration({ basePath: '/api/v1' });
const api = new CategoriesApi(config);

export async function fetchCategories(): Promise<CategoryDto[]> {
	return api.listCategories();
}

export type { CategoryDto };
