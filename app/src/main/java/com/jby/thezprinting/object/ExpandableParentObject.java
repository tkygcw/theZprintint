package com.jby.thezprinting.object;

import java.util.ArrayList;

public class ExpandableParentObject {
    private String date = "unknown";
    private ArrayList<DocumentObject> documentObjectArrayList = new ArrayList<>();


    public ExpandableParentObject(String date) {
        this.date = date;
    }

    public ExpandableParentObject() {
    }

    public String getDate() {
        return date;
    }

    public void setDocumentObjectArrayList(ArrayList<DocumentObject> documentObjectArrayList) {
        this.documentObjectArrayList = documentObjectArrayList;
    }

    public ArrayList<DocumentObject> getDocumentObjectArrayList() {
        return documentObjectArrayList;
    }

}
