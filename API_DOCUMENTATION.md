# API Product Management Documentation

## Overview
This document describes the AJAX-based Product Management API implementation. The system provides RESTful endpoints for complete CRUD operations on products while maintaining the existing web interface.

## Architecture

### 1. Backend API
**Location**: `src/main/java/com/example/demo1/controller/ProductApiController.java`

The ProductApiController provides REST endpoints for product management:
- **Base URL**: `/api/products`
- **Content-Type**: `application/json`
- **CORS**: Enabled for all origins

### 2. Frontend JavaScript Modules

#### a) Product API Service (`/static/js/product-api.js`)
Low-level HTTP client for API communication.

**Main Methods**:
```javascript
ProductAPI.getAllProducts()        // GET /api/products
ProductAPI.getProductById(id)      // GET /api/products/{id}
ProductAPI.createProduct(data)     // POST /api/products
ProductAPI.updateProduct(id, data) // PUT /api/products/{id}
ProductAPI.deleteProduct(id)       // DELETE /api/products/{id}
ProductAPI.searchProducts(query)   // GET /api/products/search?q={query}
```

#### b) Product Manager (`/static/js/product-manager.js`)
High-level product management with UI interaction and AJAX functionality.

**Main Features**:
- Automatic product list loading and rendering
- AJAX form submission handling
- Real-time UI updates
- Error handling and user notifications
- Loading indicators

## API Endpoints

### 1. Get All Products
```
GET /api/products
```
**Response**:
```json
[
  {
    "id": 1,
    "name": "Sản phẩm 1",
    "price": 1000000,
    "description": "Mô tả sản phẩm",
    "imageUrl": "product1.jpg",
    "stock": 10,
    "category": { "id": 1, "name": "Điện thoại" },
    "brand": "Samsung",
    "rating": 4.5,
    "discount": 10,
    "discountedPrice": 900000,
    "promoStock": 5
  }
]
```

### 2. Get Product by ID
```
GET /api/products/{id}
```
**Response**: Single product object (same structure as above)

### 3. Create Product
```
POST /api/products
Content-Type: application/json

{
  "name": "Sản phẩm mới",
  "price": 1500000,
  "description": "Mô tả",
  "categoryId": 1,
  "brand": "Samsung",
  "stock": 20,
  "rating": null,
  "discount": 5,
  "discountedPrice": 1425000,
  "promoStock": 10
}
```
**Response**: 201 Created with created product object

### 4. Update Product
```
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Tên sản phẩm cập nhật",
  "price": 1600000,
  "description": "Mô tả cập nhật",
  "categoryId": 2,
  "brand": "Apple",
  "stock": 15,
  "discount": 8,
  "discountedPrice": 1472000
}
```
**Response**: 200 OK with updated product object

### 5. Delete Product
```
DELETE /api/products/{id}
```
**Response**: 204 No Content

## Frontend Implementation

### A. Product List View (`product-list.html`)
- Displays products in a responsive table
- Real-time AJAX updates (no page reload)
- Delete and edit buttons with AJAX functionality
- Auto-refreshing product list
- Live notifications for all actions

**Key Elements**:
- `#productTableBody` - Container for dynamically rendered product rows
- `#alertContainer` - Alert notifications
- `.btn-delete-ajax` - Delete button with AJAX handler
- `.btn-edit-ajax` - Edit button (redirects to edit form)

### B. Add Product Form (`add-product.html`)
- AJAX form submission
- Real-time form validation
- Image preview functionality
- Success/error notifications

**Features**:
- Form ID: `#addProductForm`
- Auto-redirect on success
- Comprehensive product field support

### C. Update Product Form (`update-product.html`)
- AJAX form submission for updates
- Pre-filled form with existing product data
- Real-time validation
- Image preview and replacement

**Features**:
- Form ID: `#updateProductForm`
- Data attribute: `data-product-id`
- Comprehensive field update support

## Usage Examples

### JavaScript API Usage

```javascript
// Get all products
ProductAPI.getAllProducts()
  .then(products => console.log(products))
  .catch(error => console.error(error));

// Create product
ProductAPI.createProduct({
  name: "New Product",
  price: 1000000,
  categoryId: 1,
  brand: "Samsung",
  stock: 10,
  discount: 5,
  discountedPrice: 950000
})
.then(product => console.log("Created:", product))
.catch(error => console.error("Error:", error));

// Update product
ProductAPI.updateProduct(1, {
  name: "Updated Name",
  price: 1200000
})
.then(product => console.log("Updated:", product))
.catch(error => console.error("Error:", error));

// Delete product
ProductAPI.deleteProduct(1)
.then(() => console.log("Deleted"))
.catch(error => console.error("Error:", error));

// Search products
ProductAPI.searchProducts("Samsung")
.then(results => console.log("Found:", results))
.catch(error => console.error("Error:", error));
```

### Product Manager Usage

The ProductManager class is automatically initialized on DOM ready:

```javascript
// Access the manager instance (once initialized)
window.productManager.loadProducts();      // Reload product list
window.productManager.showAlert(msg, type); // Show custom alert
```

## Data Validation

### Required Fields (API)
- `name` - Product name (string, non-empty)
- `price` - Base price (number, > 0)
- `categoryId` - Category ID (number, required)

### Optional Fields
- `description` - Product description (string)
- `imageUrl` - Image filename (string)
- `stock` - Inventory count (number)
- `brand` - Brand name (string)
- `rating` - Star rating (number, 0-5)
- `discount` - Discount percentage (number, 0-100)
- `discountedPrice` - Promotional price (number, < price)
- `promoStock` - Promotional quantity available (number)
- `specs` - Technical specifications (string)
- `code` - Product code (string)
- `installmentPrice` - Installment payment amount (number)

### Validation Rules
- `price` must be greater than 0
- `discountedPrice` must be less than `price` if provided
- `discount` must be between 0 and 100 if provided
- `rating` must be between 0 and 5 if provided
- `stock` and `promoStock` must be >= 0 if provided

## Error Handling

### HTTP Status Codes
- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST request
- `204 No Content` - Successful DELETE request
- `400 Bad Request` - Invalid data format or validation error
- `404 Not Found` - Product or resource not found
- `500 Internal Server Error` - Server-side error

### Error Response Format
```json
{
  "message": "Product not found: 999",
  "status": 404
}
```

## UI Features

### Notifications
- **Success** (Green): Operation completed successfully
- **Error** (Red): Operation failed with details
- **Warning** (Yellow): Validation or input issues
- **Info** (Blue): General information

Auto-dismiss after 5 seconds.

### Loading Indicator
Shows centered spinner during API operations.

### Product Table Rendering
- Automatic formatting of:
  - Vietnamese currency formatting
  - Product images
  - Stock status badges
  - Discount information
  - Promotional offers

## Integration with Existing UI

### MVC Controllers (Still Active)
- `ProductController` - Serves HTML pages
  - GET `/product` - Product list page
  - GET `/product/add` - Add form page
  - GET `/product/edit/{id}` - Update form page

### API Routes (New)
- `ProductApiController` - REST API endpoints
  - GET/POST/PUT/DELETE `/api/products`
  - GET `/api/products/{id}`

## Browser Compatibility
- Chrome 60+
- Firefox 55+
- Safari 11+
- Edge 79+

Requires:
- ES6 JavaScript support
- Fetch API
- File API (for image preview)
- Bootstrap 5.3 (for styling)

## Performance Considerations

1. **Caching**: Products are loaded once on page load
2. **Lazy Loading**: Consider implementing for large product lists
3. **Pagination**: Recommended for 100+ products
4. **Debouncing**: Search is not debounced (implement if needed)

## Security Notes

1. **CORS**: Enabled for development - restrict in production
2. **CSRF**: Ensure CSRF tokens are handled by Spring Security
3. **Input Validation**: All data validated on server
4. **SQL Injection**: Uses JPA parameterized queries
5. **XSS Prevention**: HTML content escaped in JavaScript

## Future Enhancements

1. Implement pagination for product lists
2. Add search filtering to product listing
3. Bulk operations (bulk delete, bulk update)
4. Image upload via API
5. Product export/import functionality
6. Advanced filtering and sorting
7. Caching strategy for performance
8. Real-time updates via WebSocket

## Testing

### Manual Testing via Browser Console
```javascript
// Test API connectivity
fetch('/api/products')
  .then(r => r.json())
  .then(d => console.log(d))
  .catch(e => console.error(e));

// Test product creation
fetch('/api/products', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'Test',
    price: 100000,
    categoryId: 1
  })
})
.then(r => r.json())
.then(d => console.log(d))
.catch(e => console.error(e));
```

## File Structure

```
src/main/resources/
├── static/
│   └── js/
│       ├── product-api.js       (API client)
│       └── product-manager.js   (UI manager)
└── templates/
    └── products/
        ├── product-list.html    (List with AJAX)
        ├── add-product.html     (Add form with AJAX)
        └── update-product.html  (Update form with AJAX)

src/main/java/com/example/demo1/
└── controller/
    └── ProductApiController.java (REST API)
```

## Contact & Support
For issues or questions, check:
1. Browser console for JavaScript errors
2. Network tab for API request/response details
3. Server logs for backend errors
4. Validation messages in UI alerts
