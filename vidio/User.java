package com.example.vidio;

import java.util.List;

public class User {
    private String name,email,pass;


    public User(){
    }

    public User(String email, String pass, String name) {
        this.email=email;
        this.pass=pass;
        this.name=name;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
