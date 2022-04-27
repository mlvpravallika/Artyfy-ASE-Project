package com.gallery.art.models

import java.io.Serializable

class User(
    var email: String,
    var phone: String,
    var name: String,
    var userType: String,
    var password: String
) : Serializable {
    override fun toString(): String {
        return "User{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", Name='" + name + '\'' +
                ", userType='" + userType + '\'' +
                '}'
    }
}