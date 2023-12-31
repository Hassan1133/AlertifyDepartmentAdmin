package com.example.alertify_department_admin.model;

import java.io.Serializable;

public class Users_Model implements Serializable {
    private String id;
    private String name;
    private String phoneNo;
    private String cnicNo;
    private String email;
    private String imgUrl;
    private String userStatus;
    private String type;

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCnicNo() {
        return cnicNo;
    }

    public void setCnicNo(String cnicNo) {
        this.cnicNo = cnicNo;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
