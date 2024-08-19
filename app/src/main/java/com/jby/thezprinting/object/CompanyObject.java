package com.jby.thezprinting.object;

import java.io.Serializable;

public class CompanyObject implements Serializable {
    private String name, logo, registration_no, address, mobile, email, bank, bank_co_name, bank_no, invoice_note, do_note, sign_by, do_footer_message, invoice_footer_message;

    public CompanyObject(String name, String logo, String registration_no, String address, String mobile, String email, String bank, String bank_co_name, String bank_no, String invoice_note, String do_note, String sign_by, String do_footer_message, String invoice_footer_message) {
        this.name = name;
        this.logo = logo;
        this.registration_no = registration_no;
        this.address = address;
        this.mobile = mobile;
        this.email = email;
        this.bank = bank;
        this.bank_co_name = bank_co_name;
        this.bank_no = bank_no;
        this.invoice_note = invoice_note;
        this.do_note = do_note;
        this.sign_by = sign_by;
        this.do_footer_message = do_footer_message;
        this.invoice_footer_message = invoice_footer_message;
    }

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public String getRegistration_no() {
        return registration_no;
    }

    public String getAddress() {
        return address;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getBank() {
        return bank;
    }

    public String getBank_co_name() {
        return bank_co_name;
    }

    public String getBank_no() {
        return bank_no;
    }

    public String getInvoice_note() {
        return invoice_note;
    }

    public String getDo_note() {
        return do_note;
    }

    public String getSign_by() {
        return sign_by;
    }

    public String getDo_footer_message() {
        return do_footer_message;
    }

    public String getInvoice_footer_message() {
        return invoice_footer_message;
    }
}
