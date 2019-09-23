package com.jby.thezprinting.object;

import java.util.ArrayList;

public class ProductObject {
    private String product_id, name, price, unit, category;
    private ArrayList<ProductObject> childProductObjectArrayList = new ArrayList<>();

    public ProductObject(String product_id, String name, String price, String unit) {
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.unit = unit;
    }

    public ProductObject(String category) {
        this.category = category;
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

    public String getUnit() {
        return unit;
    }

    public String getCategory() {
        return category;
    }
}
