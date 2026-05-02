export enum Silhouette {
	BOXY = 'BOXY',
	CURVY = 'CURVY',
	OTHER = 'OTHER'
}

export interface Personalization {
	silhouette: Silhouette;
	waistCm: number;
	hipsCm: number;
	heightCm: number;
}

export interface Product {
	id: string;
	name: string;
	description: string;
	shortDescription: string;
	price: number;
	categoryId: string;
	fabrication: { label: string; value: string }[];
	ethics: { label: string; value: string }[];
	images: string[];
}

export interface Category {
	id: string;
	name: string;
	slug: string;
	description: string;
	displayOrder: number;
	subCategories: string[];
}

export interface CartItem {
	id: string;
	productId: string;
	productName: string;
	price: number;
	quantity: number;
	thumbnail: string;
	personalization: Personalization;
}
