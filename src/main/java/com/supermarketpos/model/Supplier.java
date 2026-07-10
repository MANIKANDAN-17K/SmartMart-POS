package com.supermarketpos.model;

import java.time.LocalDateTime;

public class Supplier {

    private int id;
    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String mobile;
    private String email;
    private String gstNumber;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Supplier() {
    }

    public Supplier(String supplierCode, String supplierName, String contactPerson, String mobile,
                    String email, String gstNumber, String address, String city,
                    String state, String pincode, boolean active) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.mobile = mobile;
        this.email = email;
        this.gstNumber = gstNumber;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatusLabel() {
        return active ? "ACTIVE" : "INACTIVE";
    }

    @Override
    public String toString() {
        return supplierName;
    }
}