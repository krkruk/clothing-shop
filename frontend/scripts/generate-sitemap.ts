const BASE_URL = 'https://example.com';
const API_URL = 'http://localhost:8080/api/v1/products?limit=100';

interface ProductSummary {
	id: string;
}

async function generateSitemap() {
	const urls: string[] = [BASE_URL + '/'];

	try {
		const res = await fetch(API_URL);
		if (res.ok) {
			const data = await res.json();
			const products: ProductSummary[] = data.items ?? [];
			for (const p of products) {
				urls.push(`${BASE_URL}/products/${p.id}`);
			}
		} else {
			console.warn('Failed to fetch products, generating sitemap with landing page only');
		}
	} catch {
		console.warn('API unavailable, generating sitemap with landing page only');
	}

	const sitemap = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls.map((u) => `  <url><loc>${u}</loc></url>`).join('\n')}
</urlset>`;

	const fs = await import('fs');
	const path = await import('path');
	const outputPath = path.join(__dirname, '..', 'static', 'sitemap.xml');
	fs.writeFileSync(outputPath, sitemap);
	console.log(`Sitemap generated with ${urls.length} URLs`);
}

generateSitemap().catch(console.error);
