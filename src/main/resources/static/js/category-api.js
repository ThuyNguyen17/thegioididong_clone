/**
 * Category API Service
 * Provides methods to interact with the category REST API
 */

const CategoryAPI = {
    BASE_URL: '/api/categories',
    
    /**
     * Get all categories
     * @returns {Promise<Array>} List of categories
     */
    getAllCategories() {
        return fetch(this.BASE_URL)
            .then(response => {
                if (!response.ok) throw new Error('Failed to fetch categories');
                return response.json();
            });
    },

    /**
     * Get category by ID
     * @param {number} id - Category ID
     * @returns {Promise<Object>} Category object
     */
    getCategoryById(id) {
        return fetch(`${this.BASE_URL}/${id}`)
            .then(response => {
                if (!response.ok) throw new Error(`Category not found: ${id}`);
                return response.json();
            });
    },

    /**
     * Create a new category
     * @param {Object} categoryData - Category data
     * @returns {Promise<Object>} Created category
     */
    createCategory(categoryData) {
        return fetch(this.BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(categoryData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.message || 'Failed to create category');
                });
            }
            return response.json();
        });
    },

    /**
     * Update an existing category
     * @param {number} id - Category ID
     * @param {Object} categoryData - Updated category data
     * @returns {Promise<Object>} Updated category
     */
    updateCategory(id, categoryData) {
        return fetch(`${this.BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(categoryData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.message || 'Failed to update category');
                });
            }
            return response.json();
        });
    },

    /**
     * Delete a category
     * @param {number} id - Category ID
     * @returns {Promise<void>}
     */
    deleteCategory(id) {
        return fetch(`${this.BASE_URL}/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete category');
            }
        });
    }
};

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CategoryAPI;
}
