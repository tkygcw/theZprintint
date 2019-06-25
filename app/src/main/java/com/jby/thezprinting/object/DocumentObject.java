package com.jby.thezprinting.object;

import java.io.Serializable;

public class DocumentObject implements Serializable {
    private String documentID, documentDetailID;
    private String item, price, quantity, subTotal, date, target, status, personInCharge;

    public DocumentObject() {
    }

    /*
     *child list view
     * */
    public DocumentObject(String documentID, String date, String target, String status, String personInCharge) {
        this.documentID = documentID;
        this.date = date;
        this.target = target;
        this.status = status;
        this.personInCharge = personInCharge;
    }

    /*
     * quotataion detail
     * */
    public DocumentObject(String documentDetailID, String item, String price, String quantity, String subTotal, String date) {
        this.documentDetailID = documentDetailID;
        this.item = item;
        this.price = price;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }

    public String getTarget() {
        return target;
    }

    public String getDocumentID() {
        return documentID;
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

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }
}
