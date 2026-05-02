export const heroImages = [
	{
		url: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1920&h=600&fit=crop',
		label: 'Collection I'
	},
	{
		url: 'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1920&h=600&fit=crop',
		label: 'Collection II'
	},
	{
		url: 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=1920&h=600&fit=crop',
		label: 'Collection III'
	},
	{
		url: 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=1920&h=600&fit=crop',
		label: 'Collection IV'
	}
];

export const lookbookImages = [
	{
		url: 'https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&h=800&fit=crop',
		span: 'col-span-5 row-span-2'
	},
	{
		url: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&h=400&fit=crop',
		span: 'col-span-4'
	},
	{
		url: 'https://images.unsplash.com/photo-1529139574466-a303027c1d8b?w=600&h=400&fit=crop',
		span: 'col-span-3'
	},
	{
		url: 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600&h=400&fit=crop',
		span: 'col-span-7'
	}
];

export function getProductThumbnail(productId: string): string {
	return `https://images.unsplash.com/photo-1558171813-4c088753af8f?w=96&h=128&fit=crop`;
}
