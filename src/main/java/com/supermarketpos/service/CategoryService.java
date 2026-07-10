package com.supermarketpos.service;

import com.supermarketpos.dao.CategoryDao;
import com.supermarketpos.model.Category;
import com.supermarketpos.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryService {

    private static final Logger LOGGER = Logger.getLogger(CategoryService.class.getName());
    private static final int NAME_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    private final CategoryDao categoryDao = new CategoryDao();

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public Category createCategory(String name, String description) throws ValidationException, SQLException {
        String trimmedName = ValidationUtil.trimOrEmpty(name);
        String trimmedDescription = ValidationUtil.trimOrEmpty(description);

        validateCategoryFields(trimmedName, trimmedDescription);

        if (categoryDao.existsByName(trimmedName, null)) {
            throw new ValidationException("A category named '" + trimmedName + "' already exists.");
        }

        Category category = new Category(trimmedName, trimmedDescription, true);
        int id = categoryDao.create(category);
        category.setId(id);

        LOGGER.info("Category created: " + trimmedName + " (id=" + id + ")");
        return category;
    }

    public void updateCategory(int id, String name, String description) throws ValidationException, SQLException {
        String trimmedName = ValidationUtil.trimOrEmpty(name);
        String trimmedDescription = ValidationUtil.trimOrEmpty(description);

        validateCategoryFields(trimmedName, trimmedDescription);

        if (categoryDao.existsByName(trimmedName, id)) {
            throw new ValidationException("A category named '" + trimmedName + "' already exists.");
        }

        Category category = new Category();
        category.setId(id);
        category.setName(trimmedName);
        category.setDescription(trimmedDescription);
        categoryDao.update(category);

        LOGGER.info("Category updated: id=" + id);
    }

    public void activateCategory(int id) throws SQLException {
        categoryDao.setActiveStatus(id, true);
        LOGGER.info("Category activated: id=" + id);
    }

    public void deactivateCategory(int id) throws SQLException {
        categoryDao.setActiveStatus(id, false);
        LOGGER.info("Category deactivated: id=" + id);
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDao.findAll();
    }

    public List<Category> getAllActiveCategories() throws SQLException {
        return categoryDao.findAllActive();
    }

    public List<Category> searchCategory(String keyword) throws SQLException {
        if (ValidationUtil.isNullOrEmpty(keyword)) {
            return categoryDao.findAll();
        }
        return categoryDao.search(keyword.trim());
    }

    public Category getById(int id) throws SQLException {
        return categoryDao.findById(id);
    }

    private void validateCategoryFields(String name, String description) throws ValidationException {
        if (ValidationUtil.isNullOrEmpty(name)) {
            throw new ValidationException("Category name is required.");
        }
        if (ValidationUtil.exceedsMaxLength(name, NAME_MAX_LENGTH)) {
            throw new ValidationException("Category name cannot exceed " + NAME_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.exceedsMaxLength(description, DESCRIPTION_MAX_LENGTH)) {
            throw new ValidationException("Description cannot exceed " + DESCRIPTION_MAX_LENGTH + " characters.");
        }
    }
}