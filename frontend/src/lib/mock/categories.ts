import type { Category } from './types';

export const categories: Category[] = [
	{
		id: 'cat-tops',
		name: 'TOPS',
		slug: 'tops',
		description: 'Upper-body constructions',
		displayOrder: 1,
		subCategories: ['Shirts', 'Outerwear', 'Knitwear']
	},
	{
		id: 'cat-bottoms',
		name: 'BOTTOMS',
		slug: 'bottoms',
		description: 'Lower-body architectures',
		displayOrder: 2,
		subCategories: ['Pants', 'Skirts', 'Trousers']
	},
	{
		id: 'cat-accessories',
		name: 'ACCESSORIES',
		slug: 'accessories',
		description: 'Peripheral artifacts',
		displayOrder: 3,
		subCategories: ['Belts', 'Bags', 'Jewelry']
	}
];
