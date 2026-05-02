# Design System Document

## 1. Overview & Creative North Star: "The Obsidian Monolith"
The Creative North Star for this design system is **"The Obsidian Monolith."** It represents a convergence of academic austerity and high-fashion corporate gothicism. Unlike traditional e-commerce platforms that rely on busy grids and bright calls-to-action, this system embraces the void. 

We break the "template" look by utilizing extreme negative space, brutalist geometric shapes, and intentional asymmetry. Layouts should feel like a high-end editorial spread—cold, serious, and deeply intentional. We do not "shop" here; we "acquire." The interface acts as a silent curator, allowing the moody, dark-environment photography to breathe within a rigid, sharp-edged framework.

---

## 2. Colors: The Tonal Void
The palette is built on "watered-down" blacks and layered greys, punctuated by a singular, visceral blood-crimson.

*   **Primary (#ffb4a8) & Primary Container (#5c0000):** These tokens represent the "Blood-Crimson" core. Use the `primary_container` for the "Acquire" buttons to maintain a deep, saturated mood, while `primary` serves as the high-contrast interactive state.
*   **Surface Hierarchy:** We utilize `surface_container_lowest` (#0e0e0e) for the primary backdrop to ground the experience in total darkness.
*   **The "No-Line" Rule:** 1px solid borders are strictly prohibited for sectioning. Boundaries are defined solely through background shifts. A product description in `surface_container` sits against a `surface` background to create a "block" without a stroke.
*   **Signature Textures:** Use a subtle linear gradient on main CTAs, transitioning from `primary_container` (#5c0000) to `on_primary_fixed_variant` (#920703). This subtle shift provides a "velvet" depth that flat hex codes cannot achieve.
*   **Glass & Gradient:** For the modal cart, use `surface_container_high` with a 12px backdrop blur and 85% opacity. This creates a "smoked glass" effect that feels premium and integrated.

---

## 3. Typography: Geometric Authority
The typography leverages two geometric powerhouses: **Space Grotesk** for structural authority and **Manrope** for clinical readability.

*   **Display & Headline (Space Grotesk):** Used for product titles and high-level editorial headers. The tight kerning and sharp geometric apertures of Space Grotesk mirror the "Corpo Goth" aesthetic.
*   **Body & Label (Manrope):** This is our "corporate" voice—neutral, highly legible, and modern. 
*   **Hierarchy as Identity:** Use `display-lg` (3.5rem) sparingly to create a sense of scale. Product prices should be set in `headline-sm` to give them weight. All "Acquire" labels must be `title-sm` in all-caps with a 0.1em letter spacing for an architectural feel.

---

## 4. Elevation & Depth: Tonal Layering
In a world of total darkness, shadows are invisible. Therefore, we convey depth through **Tonal Layering** and **Geometric Stacking**.

*   **The Layering Principle:** Depth is achieved by "stacking" surface tiers. Place a `surface_container_highest` (#353534) card on a `surface_dim` (#131313) background. The contrast in grey values provides the "lift."
*   **Ambient Shadows:** For floating elements like the modal cart, use a shadow with a 40px blur at 10% opacity, using the color `#000000`. This mimics the way light dies in a dark room rather than casting a fake "drop shadow."
*   **The "Ghost Border" Fallback:** If a separation is required for accessibility, use the `outline_variant` (#57423e) at 15% opacity. It should be felt, not seen.
*   **Geometric Shapes:** Incorporate large, decorative rectangles or triangles using `surface_container_low` behind product photography to create asymmetrical depth.

---

## 5. Components

### Buttons: The "Acquire" Action
*   **Primary:** Background: `primary_container`; Text: `on_surface`; Radius: `0px`. No rounded corners. The shape must be a perfect, sharp-edged rectangle.
*   **Secondary:** Background: `transparent`; Border: `outline` at 20% opacity; Text: `on_surface`.
*   **Interaction:** On hover, the primary button shifts from `primary_container` to `primary_fixed_variant`.

### Modal Cart: The Smoked Glass Overlay
*   **Styling:** Slides in from the right. Background: `surface_container_highest` at 90% opacity with a heavy backdrop blur.
*   **Dividers:** Strictly forbidden. Separate items using 24px of vertical space (padding).

### Input Fields: Clinical Precision
*   **Style:** Minimalist underline only. Background: `surface_container_low`. 
*   **States:** On focus, the underline transitions from `outline` to `primary`.

### Cards: The Shadowless Frame
*   **Construction:** No borders, no shadows. Use a `surface_container_lowest` background for the image container and `surface` for the text area below. The transition in grey tones is the only divider.

### Additional Component: "The Lookbook Fragment"
*   A non-standard component: An asymmetrical grid of three images of varying heights. One image should overlap a `surface_container_high` geometric block to break the traditional vertical scroll rhythm.

---

## 6. Do’s and Don'ts

*   **DO:** Use all-caps for `label-md` and `label-sm` to lean into the corporate/industrial aesthetic.
*   **DO:** Lean into extreme asymmetry. If an image is on the left, let the text sit significantly lower on the right.
*   **DON'T:** Use a 0px border-radius and then use rounded icons. All icons must be sharp-angled or "light" weight (200-300).
*   **DON'T:** Use "Buy Now" or "Add to Cart." The language must remain serious: "Acquire," "Inventory," "Proceed to Transaction."
*   **DO:** Ensure `on_surface` text (#e5e2e1) is used against dark backgrounds to maintain AA accessibility standards while preserving the moody contrast.