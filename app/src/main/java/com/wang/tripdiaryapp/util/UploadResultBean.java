package com.wang.tripdiaryapp.util;

import java.io.Serializable;

/**
 * Created by WANG on 2018/5/4.
 */

public class UploadResultBean implements Serializable {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UploadResult{" +
                "message='" + message + '\'' +
                '}';
    }
}
