package com.example.demo.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_tb")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 45, nullable = false)
    private String name;

    @Column(length = 11, nullable = false)
    private String phoneNumber;

    @Column(length = 30)
    @Convert(converter = StringArrayConverter.class)
    private List<String> roles = new ArrayList<>();

    @Column(length = 255)
    private String access_token;

    @Column(length = 255)
    private String refresh_token;

    @Column(length = 100)
    private String platform;

    @Builder
    public User(int id, String email, String password, String name, String phoneNumber, List<String> roles, String access_token, String refresh_token, String platform) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.platform = platform;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public void output(){
        System.out.println(       "\t\tid :" + "\t" + id);
        System.out.println(      "\temail :" + "\t" + email);
        System.out.println(   "\tpassword :" + "\t" + password);
        System.out.println(     "\t\tname :" + "\t" + name);
        System.out.println(  "phoneNumber :" + "\t" + phoneNumber);
        System.out.println(      "\troles :" + "\t" + roles);
        System.out.println( "access_token :" + "\t" + access_token);
        System.out.println("refresh_token :" + "\t" + refresh_token);
        System.out.println(   "\tplatform :" + "\t" + platform);
    }
}
