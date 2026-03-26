# Product Management AJAX Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        BROWSER / CLIENT SIDE                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────┐  ┌────────────┐  │
│  │  HTML Templates  │    │ JavaScript Files │  │  Bootstrap │  │
│  ├──────────────────┤    ├──────────────────┤  │  CSS/Icons │  │
│  │ product-list.html│    │ product-api.js   │  │            │  │
│  │ add-product.html │    │ product-manager  │  │            │  │
│  │ update-product   │    │ .js              │  │            │  │
│  └──────────────────┘    └──────────────────┘  └────────────┘  │
│         │                       │                     │          │
│         └───────────────────────┼─────────────────────┘          │
│                                 │                                 │
│         ┌───────────────────────▼────────────────────────┐       │
│         │    AJAX Interaction & DOM Manipulation        │       │
│         │  - Load/Render product list                   │       │
│         │  - Handle form submissions                    │       │
│         │  - Show/hide alerts & loading indicators      │       │
│         │  - Update UI in real-time                     │       │
│         └───────────────────────┬────────────────────────┘       │
│                                 │                                 │
└─────────────────────────────────┼─────────────────────────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │    HTTP/FETCH API CALLS    │
                    │  (JSON over HTTP)          │
                    └─────────────┬──────────────┘
                                  │
┌─────────────────────────────────▼──────────────────────────────┐
│                   NETWORK / API LAYER                          │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  Endpoints:                                                   │
│  • GET    /api/products          (list)                      │
│  • GET    /api/products/{id}     (detail)                    │
│  • POST   /api/products          (create)                    │
│  • PUT    /api/products/{id}     (update)                    │
│  • DELETE /api/products/{id}     (delete)                    │
│                                                                │
└─────────────────────────────────┬──────────────────────────────┘
                                  │
┌─────────────────────────────────▼──────────────────────────────┐
│                    SERVER SIDE / BACKEND                      │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  ProductApiController                                         │
│  ├── getAllProducts()                                        │
│  ├── getProductById(id)                                      │
│  ├── createProduct(data)                                     │
│  ├── updateProduct(id, data)                                 │
│  └── deleteProduct(id)                                       │
│       │           │                                           │
│       └───────────▼────────────────┐                          │
│                                    │                          │
│                          ProductService                       │
│                          │                                    │
│                          ├── getAllProducts()                │
│                          ├── getProductById()                │
│                          ├── addProduct()                    │
│                          ├── updateProduct()                 │
│                          ├── deleteProductById()             │
│                          └── Other utilities                 │
│                                    │                          │
│                                    ▼                          │
│                           ProductRepository                   │
│                          (JPA Interface)                       │
│                                                                │
│                                    │                          │
└────────────────────────────────────┼──────────────────────────┘
                                     │
┌────────────────────────────────────▼──────────────────────────┐
│                        DATABASE                               │
├──────────────────────────────────────────────────────────────┤
│                                                                │
│  products table                                              │
│  ├── id (PK)                                                │
│  ├── name                                                   │
│  ├── price                                                  │
│  ├── description                                            │
│  ├── imageUrl                                               │
│  ├── stock                                                  │
│  ├── brand                                                  │
│  ├── rating                                                 │
│  ├── discount                                               │
│  ├── discountedPrice                                        │
│  ├── promoStock                                             │
│  ├── category_id (FK)                                       │
│  └── ...other fields                                        │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

## Request/Response Flow

### 1. Get All Products (Product List Page Load)

```
Browser Page Load
    │
    ▼
ProductManager.init()
    │
    ▼
loadProducts()
    │
    ▼
ProductAPI.getAllProducts()
    │
    ▼
fetch('/api/products')
    │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
Success          Error
    │                 │
    ▼                 ▼
parseJSON()      showAlert(error)
    │
    ▼
renderProductList()
    │
    ▼
Update DOM with table rows
    │
    ▼
Display to User
```

### 2. Create Product (Add Form Submission)

```
User submits form
    │
    ▼
Form submit event
    │
    ▼
collectFormData()
    │
    ▼
validateData()
    │
    ┌────────┴──────────┐
    │                   │
Invalid              Valid
    │                   │
    ▼                   ▼
showAlert()    ProductAPI.createProduct()
                   │
                   ▼
             fetch(POST, JSON)
                   │
                   ▼
             ProductApiController.create()
                   │
                   ▼
             ProductService.addProduct()
                   │
                   ▼
             Database INSERT
                   │
                   ┌────────┴────────┐
                   │                 │
                   ▼                 ▼
               Success          Exception
                   │                 │
                   ▼                 ▼
             return JSON         Error response
                   │                 │
                   ▼                 ▼
           showAlert(success)  showAlert(error)
                   │
                   ▼
        setTimeout(redirect, 1500ms)
                   │
                   ▼
           Redirect to /product
```

### 3. Delete Product

```
User clicks delete button
    │
    ▼
Confirmation dialog
    │
    ┌────────┴──────────┐
    │                   │
   No                 Yes
    │                   │
Return              Continue
                        │
                        ▼
                showLoading()
                        │
                        ▼
         ProductAPI.deleteProduct(id)
                        │
                        ▼
            fetch(DELETE, /api/products/{id})
                        │
                        ▼
        ProductApiController.delete()
                        │
                        ▼
        ProductService.deleteProductById()
                        │
                        ▼
            Database DELETE
                        │
                        ┌────────┴────────┐
                        │                 │
                        ▼                 ▼
                    Success          Exception
                        │                 │
                        ▼                 ▼
                  hideLoading()     hideLoading()
                        │                 │
                        ▼                 ▼
           showAlert(success)    showAlert(error)
                        │
                        ▼
               loadProducts() - refresh list
```

## Data Model

```
Product Entity
├── id: Long (Primary Key)
├── name: String (Required)
├── price: Double (Required, > 0)
├── description: String (Optional)
├── imageUrl: String (Optional)
├── stock: Integer (Optional)
├── brand: String (Optional)
├── rating: Double (Optional, 0-5)
├── discount: Integer (Optional, 0-100)
├── discountedPrice: Double (Optional, < price)
├── promoStock: Integer (Optional)
├── specs: String (Optional)
├── code: String (Optional)
├── upcoming: Boolean (Default: false)
├── installmentPrice: Double (Optional)
├── stockTotal: Integer (Optional)
├── promoTotal: Integer (Optional)
└── category: Category (Required)
    ├── id: Long
    ├── name: String
    └── ...
```

## State Management

```
ProductManager State:
├── this.products[] - Cached product list
├── this.setupEventListeners() - Event delegation
│   ├── Form submission handlers
│   ├── Delete button handlers
│   ├── Edit button handlers
│   └── Refresh button handlers
└── Methods:
    ├── loadProducts() - Fetch from API
    ├── renderProductList() - Update DOM
    ├── handleAddProduct() - AJAX submit
    ├── handleUpdateProduct() - AJAX submit
    ├── handleDeleteProduct() - AJAX delete
    └── UI helpers
        ├── showAlert()
        ├── showLoading()
        └── formatPrice()
```

## Message Flow (WebSequence Diagram Style)

```
User          Browser              API            Database
  │              │                  │                │
  ├─ Click add ─>│                  │                │
  │              ├─ Show form ─────>│                │
  │              │                  │                │
  ├─ Fill form ─>│                  │                │
  │              │                  │                │
  ├─ Submit ────>│                  │                │
  │              ├─ Validate ──────>│                │
  │              │                  ├─ Validate    │
  │              │<─ OK ────────────┤                │
  │              ├─ POST JSON ─────>│                │
  │              │                  ├─ Save to DB ─>│
  │              │                  │<─ Success ──┤
  │              │<─ 201 + JSON ────┤                │
  │<─ Success ───┤                  │                │
  │              ├─ Refresh list ──>│                │
  │              │                  ├─ Query ─────>│
  │              │<─ Products JSON ─┤<─ Results ──┤
  │              ├─ Update table ──>│                │
  │<─ Done ──────┤                  │                │
```

## Component Interaction

```
┌────────────────────────────────────┐
│    HTML Page (product-list.html)   │
├────────────────────────────────────┤
│  <div id="productTableBody">       │
│    <!-- Will be populated by JS --> │
│  </div>                            │
└────────────────────────────────────┘
           △ │
           │ │ render
           │ │
           │ ▼
     ┌──────────────┐
     │ ProductAPI   │ <-- fetch /api/products
     │ + Manager    │
     └──────────────┘
           △ │
           │ │ calls
           │ │
           │ ▼
     ┌──────────────────────┐
     │ ProductApiController │
     │                      │
     │ @GetMapping("/")     │──> ProductService
     │ @PostMapping("/")    │       │
     │ @PutMapping("/{id}") │───────┼──> ProductRepository
     │ @DeleteMapping("/{id}")      │
     └──────────────────────┘       │
                                    │
                                    ▼
                              PostgreSQL/MySQL
```

## Error Handling Flow

```
API Request
    │
    ▼
Network Error?
    ├─ Yes → Catch error → showAlert(error)
    │
    └─ No ▼
      Response OK?
        ├─ No → Parse error → showAlert(error message)
        │
        └─ Yes ▼
          Parse JSON
            │
            ▼
          Process Data
            │
            ├─ Success → showAlert(success)
            │
            └─ Exception → showAlert(error)
```

## Security & Validation

```
User Input
    │
    ▼
Client-side validation (JavaScript)
    ├─ Required fields
    ├─ Data type
    └─ Length/Format
    │
    ▼
API Request
    │
    ▼
Server-side validation (Spring)
    ├─ Required fields
    ├─ Business rules
    ├─ Authorization
    └─ Data constraints
    │
    ▼
Database Constraints
    ├─ PK/FK
    ├─ Unique constraints
    ├─ NOT NULL
    └─ Domain checks
    │
    ▼
Response (Success or Error)
```

---

## Summary

The AJAX Product Management system follows a clean **MVC + REST** architecture:
- **View Layer**: HTML + CSS + Bootstrap (product-list.html, etc.)
- **Controller Layer**: JavaScript (product-api.js + product-manager.js)
- **API Layer**: REST endpoints (ProductApiController)
- **Service Layer**: Business logic (ProductService)
- **Data Layer**: Database (JPA Repository + PostgreSQL/MySQL)

All operations are asynchronous via AJAX, providing a smooth, responsive user experience without page reloads.
