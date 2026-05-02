# Product Requirements Document — Simple Shop

## 1. Overview

**Product name:** Clothingshop
**Description:** A lightweight e-commerce application for alternative clothing allowing users to browse products, manage a shopping cart, and place orders.
**Target users:** Small business owners and individual sellers looking for a minimal online storefront.

## 2. Goals & Non-Goals

### Goals
- SHALL provide a clean, intuitive product browsing experience
- SHALL enable users to add/remove items from a cart
- SHALL support a straightforward checkout and order placement flow
- SHALL allow an admin to manage products (add, edit, remove)
- SHALL provide infinite scrolling mechanism

### Non-Goals
- Payment gateway integration (v1)
- User accounts / authentication (v1)
- Inventory tracking
- Multi-language or multi-currency support

## 3. User Stories

| ID  | Role  | Story                                                                                  |
| --- | ----- | -------------------------------------------------------------------------------------- |
| US1 | User  | As a user, I want to view a list of products so I can decide what to buy.             |
| US2 | User  | As a user, I want to see product details (name, description, price, image) on a dedicated page. |
| US3 | User  | As a user, I want to add a product to my cart.                                         |
| US4 | User  | As a user, I want to view my cart and see the total price.                             |
| US5 | User  | As a user, I want to remove items from my cart or change quantities.                   |
| US6 | Admin | As an admin, I want to add, edit, and remove products.                                 |
| US7 | Admin | As an admin, I want to view a list of placed orders.                                   |

## 4. Functional Requirements


### 4.1 Product Catalog - Landing page
- Display all products in an infinite scrolling list
- Each product shows: name, price, short description
- Clicking a product opens its detail page
- A single view shall be split to a chessboard-like layout: 
	- 1/3 screen width: product photo
	- 2/3 screen width: short description, name, button that redirect to *Product Detail Page*

### 4.2 Product Detail Page
- Shows full product info: name, description, price, image
- "Add to Cart" button with the following configuration:
	- Silhouette (dropdown): boxy, curvy, other
	- Waist measurement (line edit): in centimeters
	- Hips measurement (line edit): in centimeters
	- Height (line edit): in centimeters (how tall is the person)	

### 4.3 Shopping Cart
- Persistent across page navigation (session-based)
- Shows item name, unit price, personalization details
- Displays cart total
- Allow item removal

### 4.4 Checkout
- Simple form: full name, email, shipping address
- Order confirmation screen after submission
- No payment processing in v1

### 4.5 Admin — Product Management
- CRUD operations for products
- Fields: name, description, price, image URL

### 4.6 Admin — Orders
- List of all placed orders with customer info and ordered items
- Read-only view (no status management in v1)

## 5. Non-Functional Requirements

| Category      | Requirement                                                      |
| ------------- | ---------------------------------------------------------------- |
| Performance   | Page load under 2 s on a standard connection                     |
| Responsiveness| Usable on mobile (320 px) and desktop viewports                  |
| Accessibility | Semantic HTML, keyboard-navigable                                |
| Browser support | Latest versions of Chrome, Firefox, Safari, Edge              |

## 6. Technical Considerations

| Area        | Decision / Note                                 |
| ----------- | ----------------------------------------------- |
| Frontend    | Svelte + TypeScript
| Backend     | Java (SpringBoot) |
| Database    | Postgres (Product details), MiniIO S3 buckets for multimedia |
| Hosting     | Self-hosted + Podman: Compose and Swarm         |
| State mgmt  | Cart state stored client-side (localStorage)    |
