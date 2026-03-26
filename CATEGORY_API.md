# Category API Documentation

## Overview
Complete REST API for category management with CRUD operations (Create, Read, Update, Delete).

## API Endpoints

### Base URL
```
/api/categories
```

### 1. Get All Categories
```http
GET /api/categories
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Điện thoại",
    "groupName": "Thiết bị điện tử",
    "imageUrl": "category1.jpg"
  },
  {
    "id": 2,
    "name": "Máy tính",
    "groupName": "Thiết bị điện tử",
    "imageUrl": "category2.jpg"
  }
]
```

### 2. Get Category by ID
```http
GET /api/categories/{id}
```

**Example:**
```
GET /api/categories/1
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Điện thoại",
  "groupName": "Thiết bị điện tử",
  "imageUrl": "category1.jpg"
}
```

**Error Response** (404 Not Found):
```json
{
  "message": "Category not found: 999",
  "status": 404
}
```

### 3. Create Category
```http
POST /api/categories
Content-Type: application/json

{
  "name": "Laptop",
  "groupName": "Thiết bị điện tử",
  "imageUrl": "laptop.jpg"
}
```

**Required Fields:**
- `name` (string): Category name (required)

**Optional Fields:**
- `groupName` (string): Category group
- `imageUrl` (string): Path to category image

**Response** (201 Created):
```json
{
  "id": 3,
  "name": "Laptop",
  "groupName": "Thiết bị điện tử",
  "imageUrl": "laptop.jpg"
}
```

**Error Response** (400 Bad Request):
```json
{
  "message": "Category name is required",
  "status": 400
}
```

### 4. Update Category
```http
PUT /api/categories/{id}
Content-Type: application/json

{
  "name": "Laptop Gaming",
  "groupName": "Thiết bị điện tử",
  "imageUrl": "laptop-gaming.jpg"
}
```

**Example:**
```
PUT /api/categories/1
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "Laptop Gaming",
  "groupName": "Thiết bị điện tử",
  "imageUrl": "laptop-gaming.jpg"
}
```

**Error Response** (404 Not Found):
```json
{
  "message": "Category not found: 999",
  "status": 404
}
```

### 5. Delete Category
```http
DELETE /api/categories/{id}
```

**Example:**
```
DELETE /api/categories/1
```

**Response** (204 No Content):
```
[empty body]
```

**Error Response** (404 Not Found):
```json
{
  "message": "Category not found: 999",
  "status": 404
}
```

## JavaScript API Client

### Using CategoryAPI in Browser Console

```javascript
// Get all categories
CategoryAPI.getAllCategories()
  .then(categories => console.log(categories));

// Get single category
CategoryAPI.getCategoryById(1)
  .then(category => console.log(category));

// Create category
CategoryAPI.createCategory({
  name: 'New Category',
  groupName: 'Electronics',
  imageUrl: 'new-cat.jpg'
})
.then(category => console.log('Created:', category));

// Update category
CategoryAPI.updateCategory(1, {
  name: 'Updated Name',
  groupName: 'Electronics'
})
.then(category => console.log('Updated:', category));

// Delete category
CategoryAPI.deleteCategory(1)
.then(() => console.log('Deleted'));
```

## cURL Examples

### Get All Categories
```bash
curl http://localhost:8080/api/categories
```

### Get Single Category
```bash
curl http://localhost:8080/api/categories/1
```

### Create Category
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Category",
    "groupName": "Electronics",
    "imageUrl": "new-cat.jpg"
  }'
```

### Update Category
```bash
curl -X PUT http://localhost:8080/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "groupName": "Electronics"
  }'
```

### Delete Category
```bash
curl -X DELETE http://localhost:8080/api/categories/1
```

## Data Model

```
Category
├── id (Long) - Primary Key
├── name (String) - Category name [REQUIRED]
├── groupName (String) - Group name [OPTIONAL]
└── imageUrl (String) - Image path [OPTIONAL]
```

## Field Validation

### name Field
- **Required**: Yes
- **Type**: String
- **Max Length**: Not specified
- **Min Length**: 1

### groupName Field
- **Required**: No
- **Type**: String
- **Max Length**: 100

### imageUrl Field
- **Required**: No
- **Type**: String
- **Max Length**: 500

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 200 | OK | Successful GET/PUT request |
| 201 | Created | Successful POST request |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Invalid data or validation error |
| 404 | Not Found | Category not found |
| 500 | Internal Server Error | Server error |

## Integration Example

### HTML Form with AJAX

```html
<form id="addCategoryForm">
  <input type="text" name="name" placeholder="Category Name" required>
  <input type="text" name="groupName" placeholder="Group Name">
  <input type="text" name="imageUrl" placeholder="Image URL">
  <button type="submit">Create Category</button>
</form>

<script>
  document.getElementById('addCategoryForm').addEventListener('submit', (e) => {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const categoryData = {
      name: formData.get('name'),
      groupName: formData.get('groupName') || null,
      imageUrl: formData.get('imageUrl') || null
    };

    CategoryAPI.createCategory(categoryData)
      .then(category => {
        console.log('Created:', category);
        e.target.reset();
      })
      .catch(error => {
        console.error('Error:', error.message);
      });
  });
</script>
```

## CORS Configuration
- **Enabled**: Yes
- **Origins**: * (All origins allowed)
- This is suitable for development. For production, restrict to specific domains.

## Performance Notes

- **GET all**: Fast (optimized for small-medium category lists)
- **GET by ID**: Very fast (direct lookup)
- **POST**: Fast (simple insert)
- **PUT**: Fast (simple update)
- **DELETE**: Fast (simple delete)

For very large category lists (1000+), consider:
- Adding pagination
- Adding category search
- Implementing caching

## Security Considerations

- ✅ Server-side validation (name is required)
- ✅ Input sanitization
- ✅ Error message doesn't expose sensitive info
- ⚠️ CORS is open (production: restrict domains)
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ XSS protection (HTML escaping)

## Category Manager (UI Integration)

The `CategoryManager` class provides automatic:
- List rendering in tables
- Form handling (add/edit/delete)
- Real-time alerts
- Loading indicators
- Automatic list refresh

### Using Category Manager

```javascript
// Access the manager instance
window.categoryManager.loadCategories();              // Reload
window.categoryManager.showAlert(msg, type);         // Show alert
```

## Testing the API

### Browser Console Test
```javascript
// Test all operations
async function testCategoryAPI() {
  try {
    // 1. Get all
    const all = await CategoryAPI.getAllCategories();
    console.log('✓ Get all:', all.length, 'categories');

    // 2. Create
    const created = await CategoryAPI.createCategory({
      name: 'Test Category'
    });
    console.log('✓ Create:', created.id);

    // 3. Get one
    const single = await CategoryAPI.getCategoryById(created.id);
    console.log('✓ Get one:', single.name);

    // 4. Update
    const updated = await CategoryAPI.updateCategory(created.id, {
      name: 'Updated Category'
    });
    console.log('✓ Update:', updated.name);

    // 5. Delete
    await CategoryAPI.deleteCategory(created.id);
    console.log('✓ Delete: Success');

    console.log('\n✅ All tests passed!');
  } catch (e) {
    console.error('❌ Test failed:', e.message);
  }
}

testCategoryAPI();
```

## Summary

Complete REST API for category management with:
- ✅ 5 endpoints (CRUD + List)
- ✅ JSON request/response
- ✅ Comprehensive error handling
- ✅ Client-side API wrapper
- ✅ Server-side validation
- ✅ CORS enabled

Ready for immediate use in your application!
