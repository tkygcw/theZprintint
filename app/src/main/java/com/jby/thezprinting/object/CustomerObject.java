package com.jby.thezprinting.object;

import java.io.Serializable;

public class CustomerObject implements Serializable {
    private String customerID, name, address, contact;


    public CustomerObject() {
    }

    public CustomerObject(String customerID, String name, String address, String contact) {
        this.customerID = customerID;
        this.name = name;
        this.address = address;
        this.contact = contact;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }
}
