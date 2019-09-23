package com.jby.thezprinting.object;

import java.io.Serializable;

public class SupplierObject implements Serializable {
    private String supplier_id, name, address, email, contact, website;
    private String supplier_price_list_id, price, unit;
    private boolean isNewAdded = false;
    private boolean isUpdated = false;

    public SupplierObject() {
    }

    /*
     * supplier dialog purpose
     * */
    public SupplierObject(String supplier_id, String name, String address, String email, String contact, String website) {
        this.supplier_id = supplier_id;
        this.name = name;
        this.address = address;
        this.email = email;
        this.contact = contact;
        this.website = website;
    }

    public SupplierObject(String supplier_price_list_id, String supplier_id, String name, String address, String price, String unit, boolean isNewAdded, boolean isUpdated) {
        this.supplier_price_list_id = supplier_price_list_id;
        this.supplier_id = supplier_id;
        this.name = name;
        this.address = address;
        this.price = price;
        this.unit = unit;
        this.isNewAdded = isNewAdded;
        this.isUpdated = isUpdated;
    }

    public String getSupplier_id() {
        return supplier_id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getContact() {
        return contact;
    }

    public String getWebsite() {
        return website;
    }

    public String getPrice() {
        return price;
    }

    public String getSupplier_price_list_id() {
        return supplier_price_list_id;
    }

    public boolean isNewAdded() {
        return isNewAdded;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public String getUnit() {
        return unit;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setNewAdded(boolean newAdded) {
        isNewAdded = newAdded;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }
}
