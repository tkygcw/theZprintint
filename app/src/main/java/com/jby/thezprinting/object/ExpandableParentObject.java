package com.jby.thezprinting.object;

import java.util.ArrayList;

public class ExpandableParentObject {
    private String date;
    private ArrayList<DocumentObject> documentObjectArrayList = new ArrayList<>();


    public ExpandableParentObject(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDocumentObjectArrayList(DocumentObject documentObject) {
        this.documentObjectArrayList.add(documentObject);
    }

    public ArrayList<DocumentObject> getDocumentObjectArrayList() {
        return documentObjectArrayList;
    }

}
