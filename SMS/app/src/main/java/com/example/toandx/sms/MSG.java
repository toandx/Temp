package com.example.toandx.sms;

public class MSG {
    public String tel;
    public String info;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public MSG() {
    }

    public MSG(String tel, String info) {
        this.tel = tel;
        this.info = info;
    }
}
