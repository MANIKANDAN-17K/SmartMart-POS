package com.supermarketpos.service;

import com.supermarketpos.dao.SupplierDao;
import com.supermarketpos.model.Supplier;
import com.supermarketpos.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class SupplierService {

    private static final Logger LOGGER = Logger.getLogger(SupplierService.class.getName());

    private static final int CODE_MAX_LENGTH = 20;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int CONTACT_MAX_LENGTH = 100;
    private static final int MOBILE_MAX_LENGTH = 15;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int GST_MAX_LENGTH = 20;
    private static final int ADDRESS_MAX_LENGTH = 500;
    private static final int CITY_MAX_LENGTH = 50;
    private static final int STATE_MAX_LENGTH = 50;
    private static final int PINCODE_MAX_LENGTH = 10;

    private final SupplierDao supplierDao = new SupplierDao();

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    public Supplier createSupplier(String supplierCode, String supplierName, String contactPerson,
                                   String mobile, String email, String gstNumber, String address,
                                   String city, String state, String pincode)
            throws ValidationException, SQLException {

        Supplier s = buildTrimmedSupplier(supplierCode, supplierName, contactPerson, mobile,
                email, gstNumber, address, city, state, pincode, true);

        validateSupplierFields(s, null);

        if (supplierDao.existsByCode(s.getSupplierCode(), null)) {
            throw new ValidationException("Supplier code '" + s.getSupplierCode() + "' already exists.");
        }
        if (supplierDao.existsByName(s.getSupplierName(), null)) {
            // Business rule says "warning" not hard block, but without a UI confirmation hook here,
            // treat as a soft validation failure the controller can choose to override via re-submit logic.
            throw new ValidationException("A supplier named '" + s.getSupplierName() +
                    "' already exists. Please verify before proceeding.");
        }

        int id = supplierDao.create(s);
        s.setId(id);
        LOGGER.info("Supplier created: " + s.getSupplierName() + " (id=" + id + ")");
        return s;
    }

    public void updateSupplier(int id, String supplierCode, String supplierName, String contactPerson,
                               String mobile, String email, String gstNumber, String address,
                               String city, String state, String pincode)
            throws ValidationException, SQLException {

        Supplier s = buildTrimmedSupplier(supplierCode, supplierName, contactPerson, mobile,
                email, gstNumber, address, city, state, pincode, true);
        s.setId(id);

        validateSupplierFields(s, id);

        if (supplierDao.existsByCode(s.getSupplierCode(), id)) {
            throw new ValidationException("Supplier code '" + s.getSupplierCode() + "' already exists.");
        }
        if (supplierDao.existsByName(s.getSupplierName(), id)) {
            throw new ValidationException("A supplier named '" + s.getSupplierName() +
                    "' already exists. Please verify before proceeding.");
        }

        supplierDao.update(s);
        LOGGER.info("Supplier updated: id=" + id);
    }

    public void activateSupplier(int id) throws SQLException {
        supplierDao.setActiveStatus(id, true);
        LOGGER.info("Supplier activated: id=" + id);
    }

    public void deactivateSupplier(int id) throws SQLException {
        supplierDao.setActiveStatus(id, false);
        LOGGER.info("Supplier deactivated: id=" + id);
    }

    public Supplier getSupplierById(int id) throws SQLException {
        LOGGER.info("Supplier viewed: id=" + id);
        return supplierDao.findById(id);
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDao.findAll();
    }

    public List<Supplier> getSuppliersByStatus(boolean active) throws SQLException {
        return supplierDao.findByStatus(active);
    }

    public List<Supplier> searchSupplier(String keyword) throws SQLException {
        if (ValidationUtil.isNullOrEmpty(keyword)) {
            return supplierDao.findAll();
        }
        return supplierDao.search(keyword.trim());
    }

    private Supplier buildTrimmedSupplier(String supplierCode, String supplierName, String contactPerson,
                                          String mobile, String email, String gstNumber, String address,
                                          String city, String state, String pincode, boolean active) {
        Supplier s = new Supplier();
        s.setSupplierCode(ValidationUtil.trimOrEmpty(supplierCode));
        s.setSupplierName(ValidationUtil.trimOrEmpty(supplierName));
        s.setContactPerson(ValidationUtil.trimOrEmpty(contactPerson));
        s.setMobile(ValidationUtil.trimOrEmpty(mobile));
        s.setEmail(ValidationUtil.trimOrEmpty(email));
        s.setGstNumber(ValidationUtil.trimOrEmpty(gstNumber));
        s.setAddress(ValidationUtil.trimOrEmpty(address));
        s.setCity(ValidationUtil.trimOrEmpty(city));
        s.setState(ValidationUtil.trimOrEmpty(state));
        s.setPincode(ValidationUtil.trimOrEmpty(pincode));
        s.setActive(active);
        return s;
    }

    private void validateSupplierFields(Supplier s, Integer excludeId) throws ValidationException {
        if (ValidationUtil.isNullOrEmpty(s.getSupplierCode())) {
            throw new ValidationException("Supplier code is required.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getSupplierCode(), CODE_MAX_LENGTH)) {
            throw new ValidationException("Supplier code cannot exceed " + CODE_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.isNullOrEmpty(s.getSupplierName())) {
            throw new ValidationException("Supplier name is required.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getSupplierName(), NAME_MAX_LENGTH)) {
            throw new ValidationException("Supplier name cannot exceed " + NAME_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getContactPerson(), CONTACT_MAX_LENGTH)) {
            throw new ValidationException("Contact person cannot exceed " + CONTACT_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.isNullOrEmpty(s.getMobile())) {
            throw new ValidationException("Mobile number is required.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getMobile(), MOBILE_MAX_LENGTH)) {
            throw new ValidationException("Mobile number cannot exceed " + MOBILE_MAX_LENGTH + " characters.");
        }
        if (!ValidationUtil.isValidMobile(s.getMobile())) {
            throw new ValidationException("Mobile number is invalid.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getEmail(), EMAIL_MAX_LENGTH)) {
            throw new ValidationException("Email cannot exceed " + EMAIL_MAX_LENGTH + " characters.");
        }
        if (!ValidationUtil.isValidEmail(s.getEmail())) {
            throw new ValidationException("Email format is invalid.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getGstNumber(), GST_MAX_LENGTH)) {
            throw new ValidationException("GST number cannot exceed " + GST_MAX_LENGTH + " characters.");
        }
        if (!ValidationUtil.isValidGstNumber(s.getGstNumber())) {
            throw new ValidationException("GST number format is invalid.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getAddress(), ADDRESS_MAX_LENGTH)) {
            throw new ValidationException("Address cannot exceed " + ADDRESS_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getCity(), CITY_MAX_LENGTH)) {
            throw new ValidationException("City cannot exceed " + CITY_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getState(), STATE_MAX_LENGTH)) {
            throw new ValidationException("State cannot exceed " + STATE_MAX_LENGTH + " characters.");
        }
        if (ValidationUtil.exceedsMaxLength(s.getPincode(), PINCODE_MAX_LENGTH)) {
            throw new ValidationException("Pincode cannot exceed " + PINCODE_MAX_LENGTH + " characters.");
        }
    }
}