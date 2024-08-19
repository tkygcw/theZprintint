package com.jby.thezprinting.object;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductObject implements Serializable {
    private String product_id, name, price, description, quantity = "1";
    private ArrayList<ProductObject> childProductObjectArrayList = new ArrayList<>();

    public ProductObject(String product_id, String name, String price, String description) {
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public ProductObject(String product_id, String name, String price, String description, String quantity) {
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
    }

    public ArrayList<ProductObject> getChildProductObjectArrayList() {
        return childProductObjectArrayList;
    }

//    public void setChildProductObjectArrayList(ProductObject productObject) {
//        this.childProductObjectArrayList.add(productObject);
//    }


    public void setChildProductObjectArrayList(ArrayList<ProductObject> childProductObjectArrayList) {
        this.childProductObjectArrayList = childProductObjectArrayList;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductObject{" +
                "product_id='" + product_id + '\'' +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", description='" + description + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }
}
