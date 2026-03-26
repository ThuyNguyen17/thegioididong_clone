/**
 * Category Manager
 * Handles category CRUD operations with AJAX and UI updates
 */

class CategoryManager {
    constructor() {
        this.categories = [];
        this.init();
    }

    /**
     * Initialize the category manager
     */
    init() {
        this.setupEventListeners();
        this.loadCategories();
    }

    /**
     * Setup event listeners for category operations
     */
    setupEventListeners() {
        // Add Category Form
        const addForm = document.getElementById('addCategoryForm');
        if (addForm) {
            addForm.addEventListener('submit', (e) => this.handleAddCategory(e));
        }

        // Update Category Form
        const updateForm = document.getElementById('updateCategoryForm');
        if (updateForm) {
            updateForm.addEventListener('submit', (e) => this.handleUpdateCategory(e));
        }

        // Delete buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.btn-delete-category')) {
                const categoryId = e.target.closest('.btn-delete-category').dataset.categoryId;
                this.handleDeleteCategory(categoryId);
            }
        });

        // Edit buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.btn-edit-category')) {
                const categoryId = e.target.closest('.btn-edit-category').dataset.categoryId;
                this.handleEditCategory(categoryId);
            }
        });
    }

    /**
     * Load all categories
     */
    loadCategories() {
        CategoryAPI.getAllCategories()
            .then(categories => {
                this.categories = categories;
                this.renderCategoryList(categories);
            })
            .catch(error => {
                console.error('Error loading categories:', error);
                this.showAlert('Lỗi khi tải danh sách danh mục: ' + error.message, 'danger');
            });
    }

    /**
     * Render category list in table
     */
    renderCategoryList(categories) {
        const tbody = document.getElementById('categoryTableBody');
        if (!tbody) return;

        if (categories.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4">Không có danh mục nào</td></tr>';
            return;
        }

        tbody.innerHTML = categories.map(category => `
            <tr>
                <td>
                    ${category.imageUrl ? `<img src="/images/${category.imageUrl}" class="category-img me-2" style="width:40px;height:40px;object-fit:cover;border-radius:4px;">` : 
                      `<div style="width:40px;height:40px;display:inline-flex;align-items:center;justify-content:center;background:#f0f0f0;border-radius:4px;"><i class="fas fa-folder text-muted"></i></div>`}
                    <span>${this.escapeHtml(category.name)}</span>
                </td>
                <td>${category.groupName || '—'}</td>
                <td>${this.escapeHtml(category.imageUrl || 'Chưa có ảnh')}</td>
                <td>
                    <div style="display:flex;gap:8px;">
                        <button class="btn-edit-category" data-category-id="${category.id}" title="Sửa" style="width:36px;height:36px;display:flex;align-items:center;justify-content:center;border-radius:8px;color:#64748b;background:#f8fafc;border:1px solid #e2e8f0;cursor:pointer;transition:all 0.2s;">
                            <i class="fas fa-pen-to-square"></i>
                        </button>
                        <button class="btn-delete-category" data-category-id="${category.id}" title="Xóa" style="width:36px;height:36px;display:flex;align-items:center;justify-content:center;border-radius:8px;color:#64748b;background:#f8fafc;border:1px solid #e2e8f0;cursor:pointer;transition:all 0.2s;">
                            <i class="fas fa-trash-can"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        // Update count badge
        const countBadge = document.querySelector('.category-count-badge');
        if (countBadge) {
            countBadge.textContent = categories.length;
        }
    }

    /**
     * Handle add category form submission
     */
    handleAddCategory(e) {
        e.preventDefault();
        
        const form = e.target;
        const formData = new FormData(form);
        
        const categoryData = {
            name: formData.get('name'),
            groupName: formData.get('groupName') || null,
            imageUrl: formData.get('imageUrl') || null
        };

        // Validate required fields
        if (!categoryData.name) {
            this.showAlert('Vui lòng nhập tên danh mục', 'warning');
            return;
        }

        this.showLoading();

        CategoryAPI.createCategory(categoryData)
            .then(category => {
                this.hideLoading();
                this.showAlert('Thêm danh mục thành công!', 'success');
                form.reset();
                this.loadCategories();
            })
            .catch(error => {
                this.hideLoading();
                this.showAlert('Lỗi: ' + error.message, 'danger');
            });
    }

    /**
     * Handle update category form submission
     */
    handleUpdateCategory(e) {
        e.preventDefault();

        const form = e.target;
        const categoryId = form.dataset.categoryId;
        const formData = new FormData(form);

        const categoryData = {
            name: formData.get('name'),
            groupName: formData.get('groupName') || null,
            imageUrl: formData.get('imageUrl') || null
        };

        // Validate required fields
        if (!categoryData.name) {
            this.showAlert('Vui lòng nhập tên danh mục', 'warning');
            return;
        }

        this.showLoading();

        CategoryAPI.updateCategory(categoryId, categoryData)
            .then(category => {
                this.hideLoading();
                this.showAlert('Cập nhật danh mục thành công!', 'success');
                this.loadCategories();
                
                // Close modal if exists
                const modal = document.getElementById('updateCategoryModal');
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
     * Handle delete category
     */
    handleDeleteCategory(categoryId) {
        if (!confirm('Bạn có chắc chắn muốn xóa danh mục này không?')) {
            return;
        }

        this.showLoading();

        CategoryAPI.deleteCategory(categoryId)
            .then(() => {
                this.hideLoading();
                this.showAlert('Xóa danh mục thành công!', 'success');
                this.loadCategories();
            })
            .catch(error => {
                this.hideLoading();
                this.showAlert('Lỗi khi xóa: ' + error.message, 'danger');
            });
    }

    /**
     * Handle edit category - load for editing
     */
    handleEditCategory(categoryId) {
        CategoryAPI.getCategoryById(categoryId)
            .then(category => {
                // Populate edit form
                document.getElementById('updateCategoryForm').dataset.categoryId = categoryId;
                document.querySelector('[name="name"]', document.getElementById('updateCategoryForm')).value = category.name;
                document.querySelector('[name="groupName"]', document.getElementById('updateCategoryForm')).value = category.groupName || '';
                document.querySelector('[name="imageUrl"]', document.getElementById('updateCategoryForm')).value = category.imageUrl || '';
                
                // Open modal
                const modal = new bootstrap.Modal(document.getElementById('updateCategoryModal'));
                modal.show();
            })
            .catch(error => {
                this.showAlert('Lỗi khi tải danh mục: ' + error.message, 'danger');
            });
    }

    /**
     * Show alert message
     */
    showAlert(message, type = 'info') {
        const alertContainer = document.getElementById('alertContainer') || this.createAlertContainer();
        
        const alertId = 'alert-' + Date.now();
        
        const icons = {
            success: 'check-circle',
            danger: 'exclamation-circle',
            warning: 'triangle-exclamation',
            info: 'circle-info'
        };

        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show rounded-4 border-0 shadow-sm" role="alert">
                <i class="fas fa-${icons[type]} me-2"></i>${message}
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
    window.categoryManager = new CategoryManager();
});
