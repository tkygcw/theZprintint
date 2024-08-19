package com.jby.thezprinting.object;

import java.io.Serializable;

public class DocumentObject implements Serializable {
    private String documentID, documentNo, documentDetailID;
    private String item, price, quantity, subTotal, date, customer, status, personInCharge, deposit, description;

    public DocumentObject() {
    }

    /*
     *child list view
     * */
    public DocumentObject(String customer, String personInCharge, String documentID, String documentNo, String status, String date, String deposit) {
        this.documentID = documentID;
        this.documentNo = documentNo;
        this.customer = customer;
        this.status = status;
        this.personInCharge = personInCharge;
        this.date = date;
        this.deposit = deposit;
    }

    /*
     * quotataion detail
     * */
    public DocumentObject(String documentDetailID, String item, String description, String price, String quantity, String subTotal) {
        this.documentDetailID = documentDetailID;
        this.item = item;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }

    public String getTarget() {
        return customer;
    }

    public String getDocumentID() {
        return documentID;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public String getDocumentDetailID() {
        return documentDetailID;
    }

    public String getItem() {
        return item;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getPersonInCharge() {
        return personInCharge;
    }

    public String getDeposit() {
        return deposit;
    }

    public String getDescription() {
        return description;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public void setDocumentNo(String documentNo) {
        this.documentNo = documentNo;
    }
}
