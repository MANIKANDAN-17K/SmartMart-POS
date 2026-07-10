package com.supermarketpos.service;

import com.supermarketpos.dao.CategoryDao;
import com.supermarketpos.dao.ProductDao;
import com.supermarketpos.model.Category;
import com.supermarketpos.model.Product;
import com.supermarketpos.util.BarcodeUtil;
import com.supermarketpos.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ProductService {

    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());
    private static final int NAME_MAX_LENGTH = 100;
    private static final int CODE_MAX_LENGTH = 50;

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public Product createProduct(String name, String barcode, String sku, int categoryId,
                                 BigDecimal costPrice, BigDecimal sellingPrice,
                                 BigDecimal gstPercentage, String imagePath)
            throws ValidationException, SQLException {

        String trimmedName = ValidationUtil.trimOrEmpty(name);
        String trimmedBarcode = ValidationUtil.trimOrEmpty(barcode);
        String trimmedSku = ValidationUtil.trimOrEmpty(sku);

        if (ValidationUtil.isNullOrEmpty(trimmedBarcode)) {
            trimmedBarcode = BarcodeUtil.generateBarcode();
        }

        validateProductFields(trimmedName, trimmedBarcode, trimmedSku, categoryId, costPrice, sellingPrice, gstPercentage);

        Category category = categoryDao.findById(categoryId);
        if (category == null) {
            throw new ValidationException("Selected category does not exist.");
        }
        if (!category.isActive()) {
            throw new ValidationException("Cannot add a product to an inactive category.");
        }

        if (productDao.existsByBarcode(trimmedBarcode, null)) {
            throw new ValidationException("Barcode '" + trimmedBarcode + "' is already in use.");
        }
        if (productDao.existsBySku(trimmedSku, null)) {
            throw new ValidationException("SKU '" + trimmedSku + "' is already in use.");
        }

        Product product = new Product(trimmedName, trimmedBarcode, trimmedSku, categoryId,
                costPrice, sellingPrice, gstPercentage, imagePath, true);
        int id = productDao.create(product);
        product.setId(id);

        LOGGER.info("Product created: " + trimmedName + " (id=" + id + ")");
        if (imagePath != null && !imagePath.isBlank()) {
            LOGGER.info("Image uploaded for product id=" + id);
        }
        return product;
    }

    public void updateProduct(int id, String name, String barcode, String sku, int categoryId,
                              BigDecimal costPrice, BigDecimal sellingPrice,
                              BigDecimal gstPercentage, String imagePath)
            throws ValidationException, SQLException {

        String trimmedName = ValidationUtil.trimOrEmpty(name);
        String trimmedBarcode = ValidationUtil.trimOrEmpty(barcode);
        String trimmedSku = ValidationUtil.trimOrEmpty(sku);

        validateProductFields(trimmedName, trimmedBarcode, trimmedSku, categoryId, costPrice, sellingPrice, gstPercentage);

        Category category = categoryDao.findById(categoryId);
        if (category == null) {
            throw new ValidationException("Selected category does not exist.");
        }

        if (productDao.existsByBarcode(trimmedBarcode, id)) {
            throw new ValidationException("Barcode '" + trimmedBarcode + "' is already in use.");
        }
        if (productDao.existsBySku(trimmedSku, id)) {
            throw new ValidationException("SKU '" + trimmedSku + "' is already in use.");
        }

        Product product = new Product();
        product.setId(id);
        product.setName(trimmedName);
        product.setBarcode(trimmedBarcode);
        product.setSku(trimmedSku);
        product.setCategoryId(categoryId);
        product.setCostPrice(costPrice);
        product.setSellingPrice(sellingPrice);
        product.setGstPercentage(gstPercentage);
        product.setImagePath(imagePath);

        productDao.update(product);
        LOGGER.info("Product updated: id=" + id);
    }

    public void activateProduct(int id) throws SQLException {
        productDao.setActiveStatus(id, true);
        LOGGER.info("Product activated: id=" + id);
    }

    public void deactivateProduct(int id) throws SQLException {
        productDao.setActiveStatus(id, false);
        LOGGER.info("Product deactivated: id=" + id);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productDao.findAll();
    }

    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        return productDao.findByCategory(categoryId);
    }

    public Product findByBarcode(String barcode) throws SQLException {
        return productDao.findByBarcode(barcode);
    }

    public Product findBySku(String sku) throws SQLException {
        return productDao.findBySku(sku);
    }

    public List<Product> searchProduct(String keyword) throws SQLException {
        if (ValidationUtil.isNullOrEmpty(keyword)) {
            return productDao.findAll();
        }
        return productDao.search(keyword.trim());
    }

    public Product getById(int id) throws SQLException {
        return productDao.findById(id);
    }

    private void validateProductFields(String name, String barcode, String sku, int categoryId,
                                       BigDecimal costPrice, BigDecimal sellingPrice, BigDecimal gst)
            throws ValidationException {

        if (ValidationUtil.isNullOrEmpty(name)) {
            throw new ValidationException("Product name is required.");
        }
        if (ValidationUtil.exceedsMaxLength(name, NAME_MAX_LENGTH)) {
            throw new ValidationException("Product name cannot exceed " + NAME_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.isNullOrEmpty(barcode)) {
            throw new ValidationException("Barcode is required.");
        }
        if (ValidationUtil.exceedsMaxLength(barcode, CODE_MAX_LENGTH)) {
            throw new ValidationException("Barcode cannot exceed " + CODE_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.isNullOrEmpty(sku)) {
            throw new ValidationException("SKU is required.");
        }
        if (ValidationUtil.exceedsMaxLength(sku, CODE_MAX_LENGTH)) {
            throw new ValidationException("SKU cannot exceed " + CODE_MAX_LENGTH + " characters.");
        }
        if (categoryId <= 0) {
            throw new ValidationException("A category must be selected.");
        }
        if (!ValidationUtil.isPositive(costPrice)) {
            throw new ValidationException("Cost price must be greater than zero.");
        }
        if (!ValidationUtil.isPositive(sellingPrice)) {
            throw new ValidationException("Selling price must be greater than zero.");
        }
        if (!ValidationUtil.isSellingPriceValid(costPrice, sellingPrice)) {
            throw new ValidationException("Selling price cannot be lower than cost price.");
        }
        if (!ValidationUtil.isValidGst(gst)) {
            throw new ValidationException("GST percentage must be between 0 and 100.");
        }
    }
}