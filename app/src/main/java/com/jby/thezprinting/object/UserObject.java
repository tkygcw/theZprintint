package com.jby.thezprinting.object;

import java.io.Serializable;

public class UserObject {
    private String username, password, company, logo;

    public UserObject(String username, String password, String company, String logo) {
        this.username = username;
        this.password = password;
        this.company = company;
        this.logo = logo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCompany() {
        return company;
    }

    public String getLogo() {
        return logo;
    }
}
