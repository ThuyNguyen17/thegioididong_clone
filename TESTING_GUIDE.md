# API Testing Guide

## Quick Start Testing

### Test 1: Verify API is Running

**Browser Console:**
```javascript
fetch('/api/products')
  .then(r => r.json())
  .then(d => console.log('API Running. Products:', d))
  .catch(e => console.error('API Error:', e));
```

**Expected Output:**
```
API Running. Products: [
  { id: 1, name: "Product 1", price: 1000000, ... },
  { id: 2, name: "Product 2", price: 1500000, ... },
  ...
]
```

---

## Manual API Testing with cURL

### 1. Get All Products
```bash
curl http://localhost:8080/api/products
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "Điện thoại Samsung",
    "price": 12000000,
    "description": "Galaxy S21 Ultra",
    "imageUrl": null,
    "stock": 10,
    "category": {
      "id": 1,
      "name": "Điện thoại"
    },
    "brand": "Samsung",
    "rating": 4.8,
    "discount": 5,
    "discountedPrice": 11400000,
    "promoStock": 5
  }
]
```

### 2. Get Single Product
```bash
curl http://localhost:8080/api/products/1
```

**Expected Response:**
```json
{
  "id": 1,
  "name": "Điện thoại Samsung",
  "price": 12000000,
  ...
}
```

### 3. Create New Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "price": 500000,
    "categoryId": 1,
    "brand": "Test Brand",
    "stock": 20,
    "description": "A test product"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": 3,
  "name": "Test Product",
  "price": 500000,
  "categoryId": 1,
  "brand": "Test Brand",
  "stock": 20,
  "description": "A test product",
  ...
}
```

### 4. Update Product
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product Name",
    "price": 13000000,
    "discount": 10,
    "discountedPrice": 11700000
  }'
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Updated Product Name",
  "price": 13000000,
  "discount": 10,
  "discountedPrice": 11700000,
  ...
}
```

### 5. Delete Product
```bash
curl -X DELETE http://localhost:8080/api/products/3
```

**Expected Response (204 No Content):**
```
[empty response body]
```

---

## Browser Developer Tools Testing

### Step 1: Open Product List
1. Navigate to `http://localhost:8080/product`
2. Open DevTools (F12)
3. Go to **Console** tab

### Step 2: Test API in Console

```javascript
// Test 1: Get all products
ProductAPI.getAllProducts()
  .then(products => {
    console.log('✓ Got products:', products.length);
    console.log(products);
  })
  .catch(e => console.error('✗ Error:', e));
```

**Expected:**
```
✓ Got products: 2
[
  {...product 1...},
  {...product 2...}
]
```

```javascript
// Test 2: Get single product
ProductAPI.getProductById(1)
  .then(product => console.log('✓ Got product:', product))
  .catch(e => console.error('✗ Error:', e));
```

```javascript
// Test 3: Create product
ProductAPI.createProduct({
  name: 'New Test Product',
  price: 999999,
  categoryId: 1,
  brand: 'Test'
})
.then(p => console.log('✓ Created:', p))
.catch(e => console.error('✗ Error:', e));
```

```javascript
// Test 4: Update product
ProductAPI.updateProduct(1, {
  name: 'Updated via API',
  price: 999999
})
.then(p => console.log('✓ Updated:', p))
.catch(e => console.error('✗ Error:', e));
```

```javascript
// Test 5: Delete product
ProductAPI.deleteProduct(3)
.then(() => console.log('✓ Deleted'))
.catch(e => console.error('✗ Error:', e));
```

---

## Network Tab Testing

### Monitor AJAX Requests

1. Open DevTools (F12)
2. Go to **Network** tab
3. Perform action on page (add/edit/delete product)
4. Watch the network request/response

### Example Network Request
```
Request URL: http://localhost:8080/api/products
Request Method: GET
Status: 200
Headers:
  Content-Type: application/json
Response:
  [{"id":1,"name":"Product 1",...}]
```

---

## UI Testing Checklist

### ✓ Product List Page
- [ ] Page loads without errors
- [ ] Products display in table
- [ ] Product count badge shows correct number
- [ ] Delete buttons are visible
- [ ] Edit buttons are visible
- [ ] Alerts appear at top-right
- [ ] Clicking delete shows confirmation
- [ ] Alert auto-dismisses after 5 seconds

### ✓ Add Product Page
- [ ] Form loads with all fields
- [ ] Can fill out form
- [ ] Submit button works
- [ ] Loading spinner appears
- [ ] Success alert appears
- [ ] Redirects to product list after success
- [ ] Validation shows errors for missing required fields

### ✓ Edit Product Page
- [ ] Form pre-fills with product data
- [ ] Can modify fields
- [ ] Submit button works
- [ ] Loading spinner appears
- [ ] Success alert appears
- [ ] Redirects to product list after success

### ✓ Delete Operation
- [ ] Confirmation dialog appears
- [ ] Can cancel deletion
- [ ] Deletion executes via AJAX
- [ ] List updates without page reload
- [ ] Success alert appears

---

## Performance Monitoring

### Check Load Time
```javascript
// In browser console
performance.measure('API_Call_Start');
ProductAPI.getAllProducts();
performance.measure('API_Call_End');
performance.getEntriesByType('measure').forEach(m => console.log(m));
```

### Network Throttling (Testing Slow Connections)
1. DevTools → Network tab
2. Click "Slow 3G" dropdown at top
3. Test AJAX operations with throttled connection
4. Verify spinner and alerts still work

---

## Error Testing

### Test 404 Error
```javascript
ProductAPI.getProductById(99999)
  .then(p => console.log(p))
  .catch(e => console.error('Error caught:', e.message));
```

**Expected:**
```
Error caught: Product not found: 99999
```

### Test Invalid Data
```javascript
ProductAPI.createProduct({
  name: 'Test',
  // price is missing - required field!
  categoryId: 1
})
.catch(e => console.error('Validation error:', e.message));
```

### Test Network Error (Offline Mode)
1. Open DevTools → Network tab
2. Click "Offline" dropdown
3. Try to load products
4. See error handling in alerts

---

## Data Validation Testing

### Test Required Fields
```javascript
// Should fail - missing name
ProductAPI.createProduct({
  price: 100000,
  categoryId: 1
})
.catch(e => console.log('Expected error:', e.message));
```

### Test Price Validation
```javascript
// Should fail - price <= 0
ProductAPI.createProduct({
  name: 'Test',
  price: 0,  // Invalid!
  categoryId: 1
})
.catch(e => console.log('Price validation error:', e.message));
```

### Test Discount Validation
```javascript
// Should work - valid discount
ProductAPI.createProduct({
  name: 'Test',
  price: 100000,
  categoryId: 1,
  discountedPrice: 90000,  // Less than price - OK
  discount: 10  // 0-100 - OK
})
.then(p => console.log('Valid discount:', p.discount));

// Should fail or adjust - invalid discount
ProductAPI.createProduct({
  name: 'Test',
  price: 100000,
  categoryId: 1,
  discountedPrice: 110000  // Greater than price - Invalid!
})
.then(p => console.log('Discounted price:', p.discountedPrice)); // Should be null
```

---

## Load Testing (Multiple Products)

```javascript
// Create 10 test products
async function createTestProducts() {
  for (let i = 0; i < 10; i++) {
    const product = {
      name: `Test Product ${i+1}`,
      price: 100000 + (i * 10000),
      categoryId: 1,
      brand: `Brand ${i}`,
      stock: 10 + i
    };
    
    try {
      const result = await ProductAPI.createProduct(product);
      console.log(`✓ Created product ${i+1}:`, result.id);
    } catch (e) {
      console.error(`✗ Failed to create product ${i+1}:`, e.message);
    }
  }
  console.log('Done creating test products');
}

// Run the test
createTestProducts();
```

---

## Browser Compatibility Testing

Test in different browsers:
- [ ] Chrome/Chromium
- [ ] Firefox
- [ ] Safari
- [ ] Edge

**Required Features:**
- ES6 JavaScript
- Fetch API
- Template literals
- Arrow functions

---

## Mobile Testing

1. Open product list on phone
2. Check responsive design
3. Test touch interactions
4. Test on slow 3G network
5. Verify alerts are readable
6. Test form input on mobile

---

## Regression Testing

After any changes, verify:
- [ ] Page loads without errors
- [ ] All CRUD operations work
- [ ] Alerts appear correctly
- [ ] Forms validate input
- [ ] No console errors
- [ ] No broken links
- [ ] Images load (if any)
- [ ] Responsive design still works

---

## Common Issues & Solutions

### Issue: API returns 404
**Solution:** 
- Check endpoint URL in product-api.js
- Verify ProductApiController is mapped to /api/products
- Check server is running

### Issue: CORS Error
**Solution:**
- Verify @CrossOrigin annotation is on ProductApiController
- Check if accessing from different domain

### Issue: Form doesn't submit
**Solution:**
- Check browser console for JavaScript errors
- Verify form has id="addProductForm" or id="updateProductForm"
- Check network tab for failed request

### Issue: Products not loading
**Solution:**
- Check database has products
- Verify ProductService.getAllProducts() works
- Check server logs

### Issue: Delete doesn't work
**Solution:**
- Check product count before/after
- Verify database actually deleted
- Check server logs for exceptions
- Try manual cURL delete command

---

## Test Report Template

```
Date: ____________________
Tester: __________________
Browser: _________________
URL: http://localhost:8080

TESTS CONDUCTED:
─────────────────────────────────────────────

✓ API Connectivity
  Status: PASS/FAIL
  Note: ____________________

✓ Get All Products
  Status: PASS/FAIL
  Count: ____
  Note: ____________________

✓ Create Product
  Status: PASS/FAIL
  ID: ____
  Note: ____________________

✓ Edit Product
  Status: PASS/FAIL
  ID: ____
  Note: ____________________

✓ Delete Product
  Status: PASS/FAIL
  ID: ____
  Note: ____________________

✓ UI Responsiveness
  Status: PASS/FAIL
  Note: ____________________

✓ Error Handling
  Status: PASS/FAIL
  Note: ____________________

OVERALL: PASS / FAIL / PARTIAL

Issues Found:
─────────────────────────────────────────────
1. _________________________________
2. _________________________________
3. _________________________________

Notes:
─────────────────────────────────────────────
_________________________________________
_________________________________________
```

---

## Automated Testing (Bonus)

### Using Fetch in Node.js
```bash
npm install node-fetch
```

```javascript
// test-api.js
const fetch = require('node-fetch');

const BASE_URL = 'http://localhost:8080/api/products';

async function runTests() {
  try {
    // Test 1: Get all
    const allProducts = await fetch(BASE_URL).then(r => r.json());
    console.log('✓ Get all:', allProducts.length, 'products');

    // Test 2: Get one
    const product = await fetch(`${BASE_URL}/1`).then(r => r.json());
    console.log('✓ Get one:', product.name);

    // Test 3: Create
    const newProduct = await fetch(BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: 'Test Product',
        price: 100000,
        categoryId: 1
      })
    }).then(r => r.json());
    console.log('✓ Create:', newProduct.id);

    // Test 4: Update
    const updated = await fetch(`${BASE_URL}/${newProduct.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: 'Updated Test'
      })
    }).then(r => r.json());
    console.log('✓ Update:', updated.name);

    // Test 5: Delete
    await fetch(`${BASE_URL}/${newProduct.id}`, { method: 'DELETE' });
    console.log('✓ Delete: Success');

    console.log('\nAll tests passed!');
  } catch (e) {
    console.error('✗ Test failed:', e.message);
  }
}

runTests();
```

Run with:
```bash
node test-api.js
```

---

## Summary

This testing guide covers:
- ✓ API endpoint verification
- ✓ JavaScript console testing
- ✓ Network monitoring
- ✓ UI functionality testing
- ✓ Error handling
- ✓ Data validation
- ✓ Performance monitoring
- ✓ Cross-browser testing
- ✓ Mobile testing
- ✓ Automated testing

Use this guide to thoroughly test the AJAX Product Management API before deploying to production.
