package com.redis.example;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {
    private String userId;
    private String name;
    private String phone;
    private String email;

}
