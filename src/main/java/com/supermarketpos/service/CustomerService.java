package com.supermarketpos.service;

import com.supermarketpos.dao.BillDao;
import com.supermarketpos.dao.BillDao.PurchaseHistoryRow;
import com.supermarketpos.dao.CustomerDao;
import com.supermarketpos.model.Customer;
import com.supermarketpos.model.Customer.Status;
import com.supermarketpos.util.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerService {

    private static final Logger log = Logger.getLogger(CustomerService.class.getName());

    private final CustomerDao customerDao = new CustomerDao();
    private final BillDao     billDao     = new BillDao();

    // ── create ────────────────────────────────────────────────────────────────

    public Customer createCustomer(Connection conn, String name, String phone,
                                   String email, String address) throws SQLException {
        name    = ValidationUtil.sanitize(name);
        phone   = ValidationUtil.sanitize(phone);
        email   = ValidationUtil.sanitize(email);
        address = ValidationUtil.sanitize(address);

        ValidationUtil.requireNonBlank(name,  "Customer name");
        ValidationUtil.requireNonBlank(phone, "Mobile number");
        validatePhone(phone);
        if (!email.isEmpty()) validateEmail(email);

        if (customerDao.phoneExists(conn, phone)) {
            throw new IllegalArgumentException("Mobile number already registered: " + phone);
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email.isEmpty() ? null : email);
        customer.setAddress(address.isEmpty() ? null : address);
        customer.setStatus(Status.ACTIVE);

        customerDao.insert(conn, customer);
        log.info("Customer created: " + customer.getPhone());
        return customer;
    }

    // ── update ────────────────────────────────────────────────────────────────

    public Customer updateCustomer(Connection conn, int id, String name, String phone,
                                   String email, String address) throws SQLException {
        name    = ValidationUtil.sanitize(name);
        phone   = ValidationUtil.sanitize(phone);
        email   = ValidationUtil.sanitize(email);
        address = ValidationUtil.sanitize(address);

        ValidationUtil.requireNonBlank(name,  "Customer name");
        ValidationUtil.requireNonBlank(phone, "Mobile number");
        validatePhone(phone);
        if (!email.isEmpty()) validateEmail(email);

        if (customerDao.phoneExistsForOther(conn, phone, id)) {
            throw new IllegalArgumentException("Mobile number already registered to another customer.");
        }

        Customer customer = customerDao.findById(conn, id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: id=" + id));

        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email.isEmpty() ? null : email);
        customer.setAddress(address.isEmpty() ? null : address);

        customerDao.update(conn, customer);
        log.info("Customer updated: id=" + id);
        return customer;
    }

    // ── status ────────────────────────────────────────────────────────────────

    public void activateCustomer(Connection conn, int customerId) throws SQLException {
        customerDao.setStatus(conn, customerId, Status.ACTIVE);
        log.info("Customer activated: id=" + customerId);
    }

    public void deactivateCustomer(Connection conn, int customerId) throws SQLException {
        customerDao.setStatus(conn, customerId, Status.INACTIVE);
        log.info("Customer deactivated: id=" + customerId);
    }

    // ── queries ───────────────────────────────────────────────────────────────

    public Optional<Customer> getCustomerById(Connection conn, int id) throws SQLException {
        return customerDao.findById(conn, id);
    }

    public Optional<Customer> getCustomerByPhone(Connection conn, String phone) throws SQLException {
        return customerDao.findByPhone(conn, phone.trim());
    }

    public List<Customer> searchCustomer(Connection conn, String keyword) throws SQLException {
        keyword = ValidationUtil.sanitize(keyword);
        if (keyword.isEmpty()) return getAllCustomers(conn);
        // Try phone-style search if all digits, otherwise name search
        if (keyword.matches("\\d+")) {
            return customerDao.searchByPhone(conn, keyword);
        }
        return customerDao.searchByName(conn, keyword);
    }

    public List<Customer> getAllCustomers(Connection conn) throws SQLException {
        return customerDao.findAll(conn);
    }

    public List<PurchaseHistoryRow> getPurchaseHistory(Connection conn, int customerId)
            throws SQLException {
        log.info("Purchase history viewed: customerId=" + customerId);
        return billDao.getPurchaseHistory(conn, customerId);
    }

    // ── validation ────────────────────────────────────────────────────────────

    private void validatePhone(String phone) {
        if (!phone.matches("\\d{7,15}")) {
            throw new IllegalArgumentException(
                    "Invalid mobile number. Must be 7–15 digits: " + phone);
        }
    }

    private void validateEmail(String email) {
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
    }
}