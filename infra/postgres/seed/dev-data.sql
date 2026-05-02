-- Seed data for Clothingshop product catalog
-- Run AFTER Liquibase migrations (categories already exist)
-- 5 products across 2 categories: tops (3), coats (2)
-- Each product has PLN and EUR prices in product_price table
-- Images are loaded from infra/minio/seed/images/ by seed.sh

-- ============================================================
-- TOPS (3 products)
-- ============================================================

INSERT INTO product (id, name, description, short_description, category_id, fabrication_content, fabrication_care, ethics_origin, ethics_impact, is_active, created_at, updated_at)
VALUES (
    'a1b2c3d4-0001-4000-8000-000000000001',
    'Ruins Scholar Dress',
    'A structured midi dress that channels the silence of empty libraries and the weight of unread pages. Tailored bodice with a mandarin collar flows into a gently flared skirt with concealed side pockets. The fabric holds its structure through long evenings of study and longer nights of wandering. Button placket runs from collar to waist, each button matte and unassuming. The hem falls just below the knee — modest enough for lecture halls, deliberate enough for darkened corridors.',
    'Structured midi dress with mandarin collar and flared skirt',
    (SELECT id FROM category WHERE slug = 'tops'),
    '80% wool, 20% linen, 280gsm',
    'Dry clean recommended. Store folded on a shelf, not on a hanger.',
    'Crafted by skilled artisans incorporated by their patron to serve great works at no cost',
    'All materials ethically collected by very young workers, outside school hours — every fiber traced, every hand respected',
    true, NOW() - interval '25 days', NOW()
);
INSERT INTO product_price (id, product_id, currency, price) VALUES
    ('b1c2d3e4-0001-4000-8000-000000000001', 'a1b2c3d4-0001-4000-8000-000000000001', 'PLN', 599.00),
    ('b1c2d3e4-0001-4000-8000-000000000002', 'a1b2c3d4-0001-4000-8000-000000000001', 'EUR', 150.00);

INSERT INTO product (id, name, description, short_description, category_id, fabrication_content, fabrication_care, ethics_origin, ethics_impact, is_active, created_at, updated_at)
VALUES (
    'a1b2c3d4-0002-4000-8000-000000000002',
    'Absinthe Mourning Vest',
    'A sharply cut waistcoat that remembers every funeral it never attended. Peak lapels frame a deep V that invites layering over crumpled linen or bare collarbones alike. Six matte buttons trace the front closure — each one a silent vow. The back is fitted with a half-belt and adjustable buckle, because even grief should have good posture. Wear it to ruin someone''s evening.',
    'Sharply cut waistcoat with peak lapels and half-belt back',
    (SELECT id FROM category WHERE slug = 'tops'),
    '80% wool, 20% linen, 280gsm',
    'Dry clean recommended. Store folded on a shelf, not on a hanger.',
    'Crafted by skilled artisans incorporated by their patron to serve great works at no cost',
    'All materials ethically collected by very young workers, outside school hours — every fiber traced, every hand respected',
    true, NOW() - interval '24 days', NOW()
);
INSERT INTO product_price (id, product_id, currency, price) VALUES
    ('b1c2d3e4-0002-4000-8000-000000000001', 'a1b2c3d4-0002-4000-8000-000000000002', 'PLN', 399.00),
    ('b1c2d3e4-0002-4000-8000-000000000002', 'a1b2c3d4-0002-4000-8000-000000000002', 'EUR', 89.00);

INSERT INTO product (id, name, description, short_description, category_id, fabrication_content, fabrication_care, ethics_origin, ethics_impact, is_active, created_at, updated_at)
VALUES (
    'a1b2c3d4-0003-4000-8000-000000000003',
    'Tallow Flame Shirt',
    'A billowing shirt that belongs in a candlelit room with ink-stained fingers and unfinished letters. Generous body with a gathered yoke that falls into soft pleats. The collar is tall enough to frame the jaw, fastened with a single covered button at the throat. Sleeves are full and gathered into narrow cuffs — roll them or let them pool at the wrist. The cotton is light enough for summer vigils, heavy enough for winter layering.',
    'Billowing cotton shirt with gathered yoke and tall collar',
    (SELECT id FROM category WHERE slug = 'tops'),
    '100% cotton, 180gsm',
    'Machine wash cold on gentle cycle. Hang dry. Iron on medium.',
    'Crafted by skilled artisans incorporated by their patron to serve great works at no cost',
    'All materials ethically collected by very young workers, outside school hours — every fiber traced, every hand respected',
    true, NOW() - interval '23 days', NOW()
);
INSERT INTO product_price (id, product_id, currency, price) VALUES
    ('b1c2d3e4-0003-4000-8000-000000000001', 'a1b2c3d4-0003-4000-8000-000000000003', 'PLN', 199.00),
    ('b1c2d3e4-0003-4000-8000-000000000002', 'a1b2c3d4-0003-4000-8000-000000000003', 'EUR', 49.00);

-- ============================================================
-- COATS (2 products)
-- ============================================================

INSERT INTO product (id, name, description, short_description, category_id, fabrication_content, fabrication_care, ethics_origin, ethics_impact, is_active, created_at, updated_at)
VALUES (
    'a1b2c3d4-0004-4000-8000-000000000004',
    'Silverwood Long Coat',
    'A floor-grazing coat that fell out of a forest that exists only in half-remembered dreams. Wide sleeves drape past the wrist, gathered at the shoulder seams like folding wings. The collar stands tall when buttoned, collapses into lapels when left open. Hidden pockets sit at the hip, deep enough for books, hands, or secrets. The fabric has a faint sheen that shifts between silver and charcoal depending on the light — and the mood of the wearer.',
    'Floor-length coat with wide draped sleeves and standing collar',
    (SELECT id FROM category WHERE slug = 'coats'),
    '80% wool, 20% linen, 280gsm',
    'Dry clean recommended. Store folded on a shelf, not on a hanger.',
    'Crafted by skilled artisans incorporated by their patron to serve great works at no cost',
    'All materials ethically collected by very young workers, outside school hours — every fiber traced, every hand respected',
    true, NOW() - interval '22 days', NOW()
);
INSERT INTO product_price (id, product_id, currency, price) VALUES
    ('b1c2d3e4-0004-4000-8000-000000000001', 'a1b2c3d4-0004-4000-8000-000000000004', 'PLN', 1299.00),
    ('b1c2d3e4-0004-4000-8000-000000000002', 'a1b2c3d4-0004-4000-8000-000000000004', 'EUR', 279.00);

INSERT INTO product (id, name, description, short_description, category_id, fabrication_content, fabrication_care, ethics_origin, ethics_impact, is_active, created_at, updated_at)
VALUES (
    'a1b2c3d4-0005-4000-8000-000000000005',
    'Barrow Gate Frock Coat',
    'A frock coat that looks like it was dug from the foundations of a cathedral — and improved by the burial. Knee-length with a full skirt that swings with every step. Wide cuffs with decorative buttons that serve no purpose and demand no apology. The back vent allows movement, though standing perfectly still is also encouraged. Collar is notched just enough to suggest authority without insisting on it. Pockets are hidden in the side seams — the coat keeps its secrets.',
    'Knee-length frock coat with full skirt and wide decorative cuffs',
    (SELECT id FROM category WHERE slug = 'coats'),
    '80% wool, 20% linen, 280gsm',
    'Dry clean recommended. Store folded on a shelf, not on a hanger.',
    'Crafted by skilled artisans incorporated by their patron to serve great works at no cost',
    'All materials ethically collected by very young workers, outside school hours — every fiber traced, every hand respected',
    true, NOW() - interval '21 days', NOW()
);
INSERT INTO product_price (id, product_id, currency, price) VALUES
    ('b1c2d3e4-0005-4000-8000-000000000001', 'a1b2c3d4-0005-4000-8000-000000000005', 'PLN', 1299.00),
    ('b1c2d3e4-0005-4000-8000-000000000002', 'a1b2c3d4-0005-4000-8000-000000000005', 'EUR', 279.00);
