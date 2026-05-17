package com.starry.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private Integer role;
    private String token;
    private Integer borrowQuantity;

    public User(Integer id,Integer role,String token){
        this.id = id;
        this.role = role;
        this.token = token;
    }
}
