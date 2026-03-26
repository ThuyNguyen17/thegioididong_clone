/**
 * Product API Service
 * Provides methods to interact with the product REST API
 */

const ProductAPI = {
    BASE_URL: '/api/products',
    
    /**
     * Get all products
     * @returns {Promise<Array>} List of products
     */
    getAllProducts() {
        return fetch(this.BASE_URL)
            .then(response => {
                if (!response.ok) throw new Error('Failed to fetch products');
                return response.json();
            });
    },

    /**
     * Get product by ID
     * @param {number} id - Product ID
     * @returns {Promise<Object>} Product object
     */
    getProductById(id) {
        return fetch(`${this.BASE_URL}/${id}`)
            .then(response => {
                if (!response.ok) throw new Error(`Product not found: ${id}`);
                return response.json();
            });
    },

    /**
     * Create a new product
     * @param {Object} productData - Product data
     * @returns {Promise<Object>} Created product
     */
    createProduct(productData) {
        return fetch(this.BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(productData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.message || 'Failed to create product');
                });
            }
            return response.json();
        });
    },

    /**
     * Update an existing product
     * @param {number} id - Product ID
     * @param {Object} productData - Updated product data
     * @returns {Promise<Object>} Updated product
     */
    updateProduct(id, productData) {
        return fetch(`${this.BASE_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(productData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(error => {
                    throw new Error(error.message || 'Failed to update product');
                });
            }
            return response.json();
        });
    },

    /**
     * Delete a product
     * @param {number} id - Product ID
     * @returns {Promise<void>}
     */
    deleteProduct(id) {
        return fetch(`${this.BASE_URL}/${id}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete product');
            }
        });
    },

    /**
     * Search products by query
     * @param {string} query - Search query
     * @returns {Promise<Array>} List of matching products
     */
    searchProducts(query) {
        return fetch(`${this.BASE_URL}/search?q=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) throw new Error('Search failed');
                return response.json();
            });
    }
};

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ProductAPI;
}
