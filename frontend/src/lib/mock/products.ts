import type { Product } from './types';

export const products: Product[] = [
	{
		id: 'prod-void-shirt',
		name: 'The Void Shirt',
		description:
			'A deconstructed oxford that questions the very nature of formal dress. Asymmetric button placement and raw-edge detailing create a garment that exists between order and entropy. Each piece bears unique tonal variations from our proprietary mineral wash process.',
		shortDescription:
			'Deconstructed oxford in mineral-washed cotton. Asymmetric closure, raw edges.',
		price: 285,
		categoryId: 'cat-tops',
		fabrication: [
			{ label: 'Shell', value: '100% organic cotton, 160gsm' },
			{ label: 'Process', value: 'Mineral wash, enzyme soften' },
			{ label: 'Origin', value: 'Woven in Portugal' }
		],
		ethics: [
			{ label: 'Certification', value: 'GOTS organic' },
			{ label: 'Labor', value: 'Fair-trade certified factory' },
			{ label: 'Carbon', value: 'Carbon-neutral shipping' }
		],
		images: [
			'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1598033129183-c4f50c736c10?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-monolith-coat',
		name: 'Monolith Overcoat',
		description:
			'An architectural overcoat rendered in heavy-waxed canvas. The silhouette is severe — straight-shouldered, floor-length — yet the interior reveals a quilted viscose lining that speaks to hidden warmth. Dual-breasted with horn buttons sourced from a family atelier in Tuscany.',
		shortDescription:
			'Architectural overcoat in waxed canvas. Straight-shouldered, floor-length.',
		price: 640,
		categoryId: 'cat-tops',
		fabrication: [
			{ label: 'Shell', value: 'Waxed organic cotton canvas, 380gsm' },
			{ label: 'Lining', value: 'Quilted viscose' },
			{ label: 'Buttons', value: 'Natural horn, Tuscan origin' }
		],
		ethics: [
			{ label: 'Certification', value: 'OEKO-TEX Standard 100' },
			{ label: 'Labor', value: 'Small-batch, family workshop' },
			{ label: 'Longevity', value: 'Repair-for-life program' }
		],
		images: [
			'https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-silence-knit',
		name: 'Silence Knit',
		description:
			'A chunky merino turtleneck that absorbs sound as readily as it absorbs attention. The gauge is deliberately irregular — some rows loose, some tight — producing a textile that reads as both artifact and garment. Oversized fit drapes without structure.',
		shortDescription:
			'Chunky merino turtleneck with irregular gauge. Oversized, unstructured.',
		price: 320,
		categoryId: 'cat-tops',
		fabrication: [
			{ label: 'Yarn', value: 'Extra-fine merino wool, 2/28 Nm' },
			{ label: 'Gauge', value: '5gg irregular' },
			{ label: 'Origin', value: 'Handloomed in Scotland' }
		],
		ethics: [
			{ label: 'Certification', value: 'RWS certified wool' },
			{ label: 'Labor', value: 'Heritage handloom workshop' },
			{ label: 'Waste', value: 'Zero-waste pattern cutting' }
		],
		images: [
			'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1434389677669-e08b4cda3a06?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-void-trousers',
		name: 'Void Trousers',
		description:
			'Wide-leg trousers in double-weave wool that maintain their architectural drape from first wearing to last. A concealed elastic at the rear waist allows movement without compromising the clean front profile. Side-seam pockets disappear into the construction.',
		shortDescription:
			'Wide-leg trousers in double-weave wool. Architectural drape, concealed elastic.',
		price: 310,
		categoryId: 'cat-bottoms',
		fabrication: [
			{ label: 'Shell', value: 'Double-weave virgin wool, 280gsm' },
			{ label: 'Finish', value: 'Sponge-clean only' },
			{ label: 'Origin', value: 'Milled in Japan' }
		],
		ethics: [
			{ label: 'Certification', value: 'Responsible Wool Standard' },
			{ label: 'Labor', value: 'Artisan factory, Japan' },
			{ label: 'Longevity', value: 'Timeless cut, non-trend design' }
		],
		images: [
			'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-ritual-skirt',
		name: 'Ritual Skirt',
		description:
			'A midi skirt in bias-cut cupro that moves like liquid shadow. The cut follows the body without clinging — an engineering feat achieved through precise grain alignment. A single vent at the rear hem allows for the ritual of walking.',
		shortDescription:
			'Bias-cut cupro midi skirt. Liquid drape, rear vent for movement.',
		price: 265,
		categoryId: 'cat-bottoms',
		fabrication: [
			{ label: 'Shell', value: 'Cupro (Bemberg), 130gsm' },
			{ label: 'Process', value: 'Bias cut, French seamed' },
			{ label: 'Origin', value: 'Fabric from Japan, assembled in Romania' }
		],
		ethics: [
			{ label: 'Material', value: 'Cupro is a regenerated cellulose fiber' },
			{ label: 'Labor', value: 'Living wage factory' },
			{ label: 'End-of-life', value: 'Biodegradable fiber' }
		],
		images: [
			'https://images.unsplash.com/photo-1583496661160-fb5886a0uj00?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1592301933927-35b597393c0a?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-column-pants',
		name: 'Column Pants',
		description:
			'Straight-leg work pants in a rigid organic twill that softens predictably over months of wear. The rise is high, the thigh is generous, and the ankle crops clean. Internal waist adjustment tabs eliminate the need for a belt — unless desired as ornament.',
		shortDescription:
			'Straight-leg organic twill pants. High rise, generous thigh, clean crop.',
		price: 240,
		categoryId: 'cat-bottoms',
		fabrication: [
			{ label: 'Shell', value: 'Organic cotton twill, 300gsm' },
			{ label: 'Hardware', value: 'Matte black YKK zip' },
			{ label: 'Origin', value: 'Sewn in Turkey' }
		],
		ethics: [
			{ label: 'Certification', value: 'GOTS organic' },
			{ label: 'Labor', value: 'Fair-wage factory' },
			{ label: 'Water', value: 'Low-water dye process' }
		],
		images: [
			'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-binding-belt',
		name: 'The Binding Belt',
		description:
			'A substantial waist binding in full-grain vegetable-tanned leather. The buckle is a custom sand-cast bronze piece — heavy, tactile, and deliberately unpolished. The leather will develop a deep patina unique to its owner over years of use.',
		shortDescription:
			'Full-grain vegetable-tanned leather belt. Sand-cast bronze buckle.',
		price: 180,
		categoryId: 'cat-accessories',
		fabrication: [
			{ label: 'Leather', value: 'Full-grain veg-tan, 3.5mm' },
			{ label: 'Buckle', value: 'Sand-cast bronze' },
			{ label: 'Origin', value: 'Handmade in England' }
		],
		ethics: [
			{ label: 'Sourcing', value: 'By-product leather, EU tannery' },
			{ label: 'Labor', value: 'Single artisan workshop' },
			{ label: 'Longevity', value: 'Lifetime guarantee' }
		],
		images: [
			'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-relic-bag',
		name: 'Relic Bag',
		description:
			'A crossbody bag in waxed canvas with bridle leather trim. The closure is a magnetic snap hidden beneath a fold — the exterior is seamless. A single internal pocket divides the space into two chambers. Compact, intentional, complete.',
		shortDescription:
			'Crossbody waxed canvas bag with bridle leather trim. Hidden magnetic closure.',
		price: 220,
		categoryId: 'cat-accessories',
		fabrication: [
			{ label: 'Body', value: 'Waxed organic cotton canvas' },
			{ label: 'Trim', value: 'Bridle leather, veg-tan' },
			{ label: 'Hardware', value: 'Solid brass, antiqued' }
		],
		ethics: [
			{ label: 'Certification', value: 'FSC-certified canvas' },
			{ label: 'Labor', value: 'Small studio production' },
			{ label: 'Packaging', value: 'Recycled cardboard only' }
		],
		images: [
			'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=1067&fit=crop'
		]
	},
	{
		id: 'prod-sigil-ring',
		name: 'Sigil Ring',
		description:
			'A signet ring in recycled sterling silver with a matte-black rhodium plating. The face bears no engraving — it is a blank sigil waiting for meaning. The band is weighted toward the front, creating a satisfying presence on the finger.',
		shortDescription:
			'Sterling silver signet ring with black rhodium plating. Blank face, weighted band.',
		price: 145,
		categoryId: 'cat-accessories',
		fabrication: [
			{ label: 'Metal', value: 'Recycled 925 sterling silver' },
			{ label: 'Plating', value: 'Black rhodium, matte finish' },
			{ label: 'Process', value: 'Lost-wax casting' }
		],
		ethics: [
			{ label: 'Material', value: '100% recycled silver' },
			{ label: 'Labor', value: 'Single jeweler, Berlin' },
			{ label: 'Packaging', value: 'Linen pouch, recycled box' }
		],
		images: [
			'https://images.unsplash.com/photo-1605100804763-247f67b3557e?w=800&h=1067&fit=crop',
			'https://images.unsplash.com/photo-1603561591411-07134e71a2a9?w=800&h=1067&fit=crop'
		]
	}
];
