/**
 * Product Manager
 * Handles product CRUD operations with AJAX and UI updates
 */

class ProductManager {
    constructor() {
        this.products = [];
        this.init();
    }

    /**
     * Initialize the product manager
     */
    init() {
        this.setupEventListeners();
        this.loadProducts();
    }

    /**
     * Setup event listeners for product operations
     */
    setupEventListeners() {
        // Add Product Form
        const addForm = document.getElementById('addProductForm');
        if (addForm) {
            addForm.addEventListener('submit', (e) => this.handleAddProduct(e));
        }

        // Update Product Form
        const updateForm = document.getElementById('updateProductForm');
        if (updateForm) {
            updateForm.addEventListener('submit', (e) => this.handleUpdateProduct(e));
        }

        // Delete buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.btn-delete-ajax')) {
                const productId = e.target.closest('.btn-delete-ajax').dataset.productId;
                this.handleDeleteProduct(productId);
            }
        });

        // Edit buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.btn-edit-ajax')) {
                const productId = e.target.closest('.btn-edit-ajax').dataset.productId;
                this.handleEditProduct(productId);
            }
        });

        // Refresh list button
        const refreshBtn = document.getElementById('refreshProductList');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', () => this.loadProducts());
        }
    }

    /**
     * Load all products
     */
    loadProducts() {
        ProductAPI.getAllProducts()
            .then(products => {
                this.products = products;
                this.renderProductList(products);
            })
            .catch(error => {
                console.error('Error loading products:', error);
                this.showAlert('Lỗi khi tải danh sách sản phẩm: ' + error.message, 'danger');
            });
    }

    /**
     * Render product list in table
     */
    renderProductList(products) {
        const tbody = document.getElementById('productTableBody');
        if (!tbody) return;

        if (products.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Không có sản phẩm nào</td></tr>';
            return;
        }

        tbody.innerHTML = products.map(product => `
            <tr>
                <td>
                    <div class="d-flex align-items-center">
                        ${product.imageUrl ? `<img src="/images/${product.imageUrl}" class="product-img me-3">` : 
                          `<div class="product-img me-3 bg-light d-flex align-items-center justify-content-center"><i class="fas fa-image text-muted"></i></div>`}
                        <div>
                            <div class="product-name">${this.escapeHtml(product.name)}</div>
                            <div class="product-category">${product.brand || 'Hãng chưa cập nhật'}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="price-text">${this.formatPrice(product.price)}</div>
                    ${product.isDiscounted ? `<span class="discount-badge">-${product.discount}%</span>` : ''}
                </td>
                <td>${product.category ? product.category.name : '—'}</td>
                <td>
                    <div class="mb-1">
                        ${product.stock > 0 ? 
                          `<span class="stock-tag stock-in">Còn hàng: ${product.stock}</span>` : 
                          `<span class="stock-tag stock-out">Hết hàng</span>`}
                    </div>
                    ${product.isPromoAvailable ? `<div><span class="stock-tag stock-special"><i class="fas fa-bolt me-1"></i>Suất KM: ${product.promoStock}</span></div>` : ''}
                </td>
                <td>
                    <div class="action-btns">
                        <button class="btn-action btn-edit-ajax" data-product-id="${product.id}" title="Sửa">
                            <i class="fas fa-pen-to-square"></i>
                        </button>
                        <button class="btn-action btn-delete-ajax" data-product-id="${product.id}" title="Xóa">
                            <i class="fas fa-trash-can"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        // Update product count
        const countBadge = document.querySelector('.stats-badge');
        if (countBadge) {
            countBadge.querySelector('span:last-child').textContent = products.length;
        }
    }

    /**
     * Handle add product form submission
     */
    handleAddProduct(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const productData = {
            name: formData.get('name'),
            price: parseFloat(formData.get('price')),
            description: formData.get('description'),
            imageUrl: formData.get('imageUrl') || null,
            stock: parseInt(formData.get('stock')) || 0,
            categoryId: parseInt(formData.get('category.id')),
            brand: formData.get('brand') || null,
            rating: formData.get('rating') ? parseFloat(formData.get('rating')) : null,
            discount: formData.get('discount') ? parseInt(formData.get('discount')) : null,
            discountedPrice: formData.get('discountedPrice') ? parseFloat(formData.get('discountedPrice')) : null,
            specs: formData.get('specs') || null,
            code: formData.get('code') || null,
            upcoming: formData.get('upcoming') === 'on',
            installmentPrice: formData.get('installmentPrice') ? parseFloat(formData.get('installmentPrice')) : null,
            promoStock: formData.get('promoStock') ? parseInt(formData.get('promoStock')) : null
        };

        // Validate required fields
        if (!productData.name || !productData.price || !productData.categoryId) {
            this.showAlert('Vui lòng điền đúng thông tin bắt buộc', 'warning');
            return;
        }

        this.showLoading();

        ProductAPI.createProduct(productData)
            .then(product => {
                this.hideLoading();
                this.showAlert('Thêm sản phẩm thành công!', 'success');
                form.reset();
                this.loadProducts();
                
                // Close modal if exists
                const modal = document.getElementById('addProductModal');
                if (modal) {
                    const bootstrapModal = bootstrap.Modal.getInstance(modal);
                    if (bootstrapModal) bootstrapModal.hide();
                }
            })
            .catch(error => {
                this.hideLoading();
                this.showAlert('Lỗi: ' + error.message, 'danger');
            });
    }

    /**
     * Handle update product form submission
     */
    handleUpdateProduct(e) {
        e.preventDefault();

        const form = e.target;
        const productId = form.dataset.productId;
        const formData = new FormData(form);

        const productData = {
            name: formData.get('name'),
            price: parseFloat(formData.get('price')),
            description: formData.get('description'),
            imageUrl: formData.get('imageUrl') || null,
            stock: parseInt(formData.get('stock')) || 0,
            categoryId: parseInt(formData.get('category.id')),
            brand: formData.get('brand') || null,
            rating: formData.get('rating') ? parseFloat(formData.get('rating')) : null,
            discount: formData.get('discount') ? parseInt(formData.get('discount')) : null,
            discountedPrice: formData.get('discountedPrice') ? parseFloat(formData.get('discountedPrice')) : null,
            specs: formData.get('specs') || null,
            code: formData.get('code') || null,
            upcoming: formData.get('upcoming') === 'on',
            installmentPrice: formData.get('installmentPrice') ? parseFloat(formData.get('installmentPrice')) : null,
            promoStock: formData.get('promoStock') ? parseInt(formData.get('promoStock')) : null
        };

        // Validate required fields
        if (!productData.name || !productData.price) {
            this.showAlert('Vui lòng điền đúng thông tin bắt buộc', 'warning');
            return;
        }

        this.showLoading();

        ProductAPI.updateProduct(productId, productData)
            .then(product => {
                this.hideLoading();
                this.showAlert('Cập nhật sản phẩm thành công!', 'success');
                this.loadProducts();
                
                // Redirect to list after update
                setTimeout(() => {
                    window.location.href = '/product';
                }, 1000);
            })
            .catch(error => {
                this.hideLoading();
                this.showAlert('Lỗi: ' + error.message, 'danger');
            });
    }

    /**
     * Handle delete product
     */
    handleDeleteProduct(productId) {
        if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này không?')) {
            return;
        }

        this.showLoading();

        ProductAPI.deleteProduct(productId)
            .then(() => {
                this.hideLoading();
                this.showAlert('Xóa sản phẩm thành công!', 'success');
                this.loadProducts();
            })
            .catch(error => {
                this.hideLoading();
                this.showAlert('Lỗi khi xóa: ' + error.message, 'danger');
            });
    }

    /**
     * Handle edit product - load for editing
     */
    handleEditProduct(productId) {
        ProductAPI.getProductById(productId)
            .then(product => {
                // Redirect to edit page
                window.location.href = `/product/edit/${productId}`;
            })
            .catch(error => {
                this.showAlert('Lỗi khi tải sản phẩm: ' + error.message, 'danger');
            });
    }

    /**
     * Show alert message
     */
    showAlert(message, type = 'info') {
        const alertContainer = document.getElementById('alertContainer') || this.createAlertContainer();
        
        const alertId = 'alert-' + Date.now();
        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show rounded-4 border-0 shadow-sm" role="alert">
                <i class="fas fa-${this.getIconForType(type)} me-2"></i>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        
        alertContainer.insertAdjacentHTML('beforeend', alertHtml);
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            const alert = document.getElementById(alertId);
            if (alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    }

    /**
     * Create alert container if not exists
     */
    createAlertContainer() {
        let container = document.getElementById('alertContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'alertContainer';
            container.style.position = 'fixed';
            container.style.top = '80px';
            container.style.right = '20px';
            container.style.zIndex = '9999';
            container.style.maxWidth = '400px';
            document.body.appendChild(container);
        }
        return container;
    }

    /**
     * Show loading indicator
     */
    showLoading() {
        let loader = document.getElementById('loadingIndicator');
        if (!loader) {
            loader = document.createElement('div');
            loader.id = 'loadingIndicator';
            loader.className = 'spinner-border text-primary';
            loader.style.position = 'fixed';
            loader.style.top = '50%';
            loader.style.left = '50%';
            loader.style.transform = 'translate(-50%, -50%)';
            loader.style.zIndex = '9998';
            document.body.appendChild(loader);
        }
        loader.style.display = 'block';
    }

    /**
     * Hide loading indicator
     */
    hideLoading() {
        const loader = document.getElementById('loadingIndicator');
        if (loader) {
            loader.style.display = 'none';
        }
    }

    /**
     * Format price with Vietnamese locale
     */
    formatPrice(price) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            maximumFractionDigits: 0
        }).format(price);
    }

    /**
     * Get icon for alert type
     */
    getIconForType(type) {
        const icons = {
            success: 'check-circle',
            danger: 'exclamation-circle',
            warning: 'triangle-exclamation',
            info: 'circle-info'
        };
        return icons[type] || 'info-circle';
    }

    /**
     * Escape HTML to prevent XSS
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.productManager = new ProductManager();
});
